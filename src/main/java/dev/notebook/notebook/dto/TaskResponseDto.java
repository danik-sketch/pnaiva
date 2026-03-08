package dev.notebook.notebook.dto;

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
public class TaskResponseDto {
  private Long id;
  private String title;
  private String description;
  private LocalDateTime dueDate;
  private boolean completed;
  private String projectName;
  private List<String> categories;
  private List<String> reminders;
}