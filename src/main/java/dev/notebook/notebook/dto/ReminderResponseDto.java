package dev.notebook.notebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
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
@Schema(description = "Reminder data returned by the API")
public class ReminderResponseDto {

  @Schema(description = "Reminder identifier")
  private Long id;
  @Schema(description = "Reminder date and time")
  private LocalDateTime reminderTime;
  @Schema(description = "Reminder message")
  private String message;
  @Schema(description = "Related task identifier")
  private Long taskId;
}
