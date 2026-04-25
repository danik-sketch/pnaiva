package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.CounterResponseDto;
import dev.notebook.notebook.util.AtomicCounter;
import dev.notebook.notebook.util.NonAtomicCounter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CounterService {

  private static final long EXECUTION_TIMEOUT_SECONDS = 30L;

  public CounterResponseDto runCounter(int threads, int incrementsPerThread) {
    long expectedCount = (long) threads * incrementsPerThread;

    long nonAtomicCount = runNonAtomic(threads, incrementsPerThread);
    long atomicCount = runAtomic(threads, incrementsPerThread);

    return new CounterResponseDto(threads, incrementsPerThread, nonAtomicCount, atomicCount,
        expectedCount - nonAtomicCount);
  }

  private long runNonAtomic(int threads, int incrementsPerThread) {
    NonAtomicCounter counter = new NonAtomicCounter();
    executeConcurrent(threads, incrementsPerThread, counter::increment);
    return counter.get();
  }

  private long runAtomic(int threads, int incrementsPerThread) {
    AtomicCounter counter = new AtomicCounter();
    executeConcurrent(threads, incrementsPerThread, counter::incrementAndGet);
    return counter.get();
  }

  private void executeConcurrent(int threads, int incrementsPerThread, Runnable task) {
    try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
      CountDownLatch startLatch = new CountDownLatch(1);
      CountDownLatch doneLatch = new CountDownLatch(threads);
      AtomicReference<Throwable> error = new AtomicReference<>();

      for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
          try {
            startLatch.await();
            for (int j = 0; j < incrementsPerThread; j++) {
              task.run();
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } catch (Throwable t) {
            error.compareAndSet(null, t);
          } finally {
            doneLatch.countDown();
          }
        });
      }
      startLatch.countDown();
      if (!doneLatch.await(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        log.error("Race condition test timed out");
      }
      Throwable contextError = error.get();
      if (contextError != null) {
        throw new RuntimeException("Test execution failed", contextError);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Execution interrupted", e);
    }
  }
}