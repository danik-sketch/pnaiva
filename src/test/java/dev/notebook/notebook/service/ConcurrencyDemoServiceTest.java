package dev.notebook.notebook.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.notebook.notebook.dto.CounterResponseDto;
import org.junit.jupiter.api.Test;

class ConcurrencyDemoServiceTest {

  private final CounterService service = new CounterService();

  @Test
  void runRaceConditionDemoShouldKeepSafeCountersAccurate() {
    CounterResponseDto result = service.runCounter(50, 10000);
    long expectedCount = 500000;

    assertThat(result.getAtomicCount()).isEqualTo(expectedCount);
    assertThat(result.getNonAtomicCount()).isLessThanOrEqualTo(expectedCount);
  }
}
