package dev.notebook.notebook.dto;

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
public class ReminderResponseDto {

  private Long id;
  private LocalDateTime reminderTime;
  private String message;
  private String type;
  private Long taskId;
}

