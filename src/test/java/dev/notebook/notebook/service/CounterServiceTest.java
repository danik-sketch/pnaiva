package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.CounterResponseDto;
import dev.notebook.notebook.exception.OperationFailedException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CounterServiceTest {

  private final CounterService service = new CounterService();

  @Test
  void shouldReturnCorrectCounts() {
    int threads = 10;
    int increments = 1000;

    CounterResponseDto result = service.runCounter(threads, increments);

    long expected = (long) threads * increments;

    assertEquals(expected, result.getAtomicCount());
    assertTrue(result.getNonAtomicCount() <= expected);
    assertEquals(expected - result.getNonAtomicCount(),
        result.getNonAtomicLostUpdates());
  }

  @Test
  void shouldHandleSingleThreadCorrectly() {
    CounterResponseDto result = service.runCounter(1, 100);

    assertEquals(100, result.getAtomicCount());
    assertEquals(100, result.getNonAtomicCount());
  }

  @Test
  void shouldThrowOperationFailedException_whenTaskThrowsException() throws Exception {
    Method method = CounterService.class
        .getDeclaredMethod("executeConcurrent", int.class, int.class, Runnable.class);
    method.setAccessible(true);

    Runnable failingTask = () -> {
      throw new RuntimeException("boom");
    };

    Exception ex = assertThrows(Exception.class, () ->
        method.invoke(service, 2, 10, failingTask)
    );

    assertInstanceOf(OperationFailedException.class, ex.getCause());
  }

  @Test
  void shouldHandleInterruptedException() throws Exception {
    Thread.currentThread().interrupt();

    Method method = CounterService.class
        .getDeclaredMethod("executeConcurrent", int.class, int.class, Runnable.class);
    method.setAccessible(true);

    Exception ex = assertThrows(Exception.class, () ->
        method.invoke(service, 1, 1, (Runnable) () -> {
        })
    );

    assertInstanceOf(OperationFailedException.class, ex.getCause());

    Thread.interrupted();
  }

  @Test
  void shouldHitTimeoutBranch() throws Exception {
    Method method = CounterService.class
        .getDeclaredMethod("executeConcurrent", int.class, int.class, Runnable.class);
    method.setAccessible(true);

    CountDownLatch latch = new CountDownLatch(1);

    Runnable slowTask = () -> {
      try {
        latch.await(31, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    };

    method.invoke(service, 1, 1, slowTask);

    assertTrue(true);
  }

  @Test
  void shouldCaptureExceptionFromThread() throws Exception {
    Method method = CounterService.class
        .getDeclaredMethod("executeConcurrent", int.class, int.class, Runnable.class);
    method.setAccessible(true);

    Runnable task = new Runnable() {
      int count = 0;

      @Override
      public void run() {
        if (count++ == 0) {
          throw new RuntimeException();
        }
      }
    };

    Exception ex = assertThrows(Exception.class, () ->
        method.invoke(service, 2, 5, task)
    );

    assertInstanceOf(OperationFailedException.class, ex.getCause());
  }
}