package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.CounterResponseDto;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CounterServiceTest {

  private final CounterService counterService = new CounterService();

  @Test
  void runCounterShouldReturnCorrectCounts() {
    int threads = 10;
    int incrementsPerThread = 100;
    long expected = (long) threads * incrementsPerThread;

    CounterResponseDto result = counterService.runCounter(threads, incrementsPerThread);

    assertThat(result.getThreads()).isEqualTo(threads);
    assertThat(result.getIncrementsPerThread()).isEqualTo(incrementsPerThread);
    assertThat(result.getAtomicCount()).isEqualTo(expected);
    assertThat(result.getNonAtomicCount()).isLessThanOrEqualTo(expected);
    assertThat(result.getNonAtomicLostUpdates()).isEqualTo(expected - result.getNonAtomicCount());
  }

  @Test
  void runCounterWithSingleThreadShouldHaveNoRaceCondition() {
    int threads = 1;
    int incrementsPerThread = 1000;
    long expected = (long) threads * incrementsPerThread;

    CounterResponseDto result = counterService.runCounter(threads, incrementsPerThread);

    assertThat(result.getAtomicCount()).isEqualTo(expected);
    assertThat(result.getNonAtomicCount()).isEqualTo(expected);
    assertThat(result.getNonAtomicLostUpdates()).isEqualTo(0);
  }

  @Test
  void runCounterWithHighThreadCountShouldShowRaceCondition() {
    int threads = 50;
    int incrementsPerThread = 100;
    long expected = (long) threads * incrementsPerThread;

    CounterResponseDto result = counterService.runCounter(threads, incrementsPerThread);

    assertThat(result.getAtomicCount()).isEqualTo(expected);

    assertThat(result.getNonAtomicCount()).isLessThan(expected);
    assertThat(result.getNonAtomicLostUpdates()).isGreaterThan(0);
  }

  @Test
  void runCounterShouldCountLostUpdatesCorrectly() {
    int threads = 20;
    int incrementsPerThread = 50;
    long expected = (long) threads * incrementsPerThread;

    CounterResponseDto result = counterService.runCounter(threads, incrementsPerThread);

    long lostUpdates = expected - result.getNonAtomicCount();
    assertThat(result.getNonAtomicLostUpdates()).isEqualTo(lostUpdates);
  }
}
