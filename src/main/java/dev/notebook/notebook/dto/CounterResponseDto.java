package dev.notebook.notebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of race condition demonstration")
public class CounterResponseDto {

  @Schema(description = "Number of threads used in the experiment")
  private int threads;

  @Schema(description = "Number of increments done by each thread")
  private int incrementsPerThread;

  @Schema(description = "Final value from non-atomic counter")
  private long nonAtomicCount;

  @Schema(description = "Final value from atomic counter")
  private long atomicCount;

  @Schema(description = "Lost updates for non-atomic variant")
  private long nonAtomicLostUpdates;
}
