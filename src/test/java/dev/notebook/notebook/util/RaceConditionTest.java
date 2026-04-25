package dev.notebook.notebook.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RaceConditionTest {

  private static final int THREADS = 64;
  private static final int INCREMENTS_PER_THREAD = 20_000;

  @Test
  void shouldDemonstrateRaceConditionForNonAtomicCounter() throws InterruptedException {
    long expected = (long) THREADS * INCREMENTS_PER_THREAD;
    boolean raceDetected = false;

    for (int attempt = 0; attempt < 5; attempt++) {
      NonAtomicCounter nonAtomicCounter = new NonAtomicCounter();
      runConcurrentIncrements(nonAtomicCounter::increment);
      if (nonAtomicCounter.get() < expected) {
        raceDetected = true;
        break;
      }
    }

    assertThat(raceDetected).isTrue();
  }

  @Test
  void shouldSolveRaceConditionWithAtomicCounter() throws InterruptedException {
    AtomicCounter atomicCounter = new AtomicCounter();
    long expected = (long) THREADS * INCREMENTS_PER_THREAD;

    runConcurrentIncrements(atomicCounter::increment);

    assertThat(atomicCounter.get()).isEqualTo(expected);
  }

  private void runConcurrentIncrements(Runnable incrementAction) throws InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(THREADS);

    for (int i = 0; i < THREADS; i++) {
      executorService.submit(() -> {
        try {
          startLatch.await();
          for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
            incrementAction.run();
          }
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
        } finally {
          doneLatch.countDown();
        }
      });
    }

    startLatch.countDown();
    doneLatch.await(30, TimeUnit.SECONDS);
    executorService.shutdown();
    executorService.awaitTermination(30, TimeUnit.SECONDS);
  }
}
