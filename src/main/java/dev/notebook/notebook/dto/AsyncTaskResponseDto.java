package dev.notebook.notebook.dto;

import dev.notebook.notebook.entity.AsyncTaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Status information for an asynchronous task")
public class AsyncTaskResponseDto {

  @Schema(description = "Task identifier")
  private String taskId;

  @Schema(description = "Current task status")
  private AsyncTaskStatus status;

  @Schema(description = "Task creation timestamp")
  private LocalDateTime createdAt;

  @Schema(description = "Task update timestamp")
  private LocalDateTime updatedAt;
}
