package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.CounterResponseDto;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

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
