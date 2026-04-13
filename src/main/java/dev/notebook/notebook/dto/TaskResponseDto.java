package dev.notebook.notebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Task data returned by the API")
public class TaskResponseDto {

  @Schema(description = "Task identifier")
  private Long id;
  @Schema(description = "Task title")
  private String title;
  @Schema(description = "Task description")
  private String description;
  @Schema(description = "Task due date and time")
  private LocalDateTime dueDate;
  @Schema(description = "Task completion date and time")
  private LocalDateTime completed;
  @Schema(description = "Related project name")
  private String projectName;
  @Schema(description = "Task categories")
  private List<String> categories;
  @Schema(description = "Task reminders")
  private List<ReminderResponseDto> reminders;
}
