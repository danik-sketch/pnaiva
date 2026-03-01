package dev.notebook.notebook.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskResponseDto {

  private Long id;
  private String title;
  private String description;
  private LocalDate dueDate;
  private boolean completed;

  public TaskResponseDto() {
  }

  public TaskResponseDto(Long id,
      String title,
      String description,
      LocalDate dueDate,
      boolean completed) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.dueDate = dueDate;
    this.completed = completed;
  }

}