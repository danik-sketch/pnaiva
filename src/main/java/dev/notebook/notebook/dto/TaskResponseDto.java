package dev.notebook.notebook.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {

  private Long id;
  private String title;
  private String description;
  private LocalDate dueDate;
  private boolean completed;

}