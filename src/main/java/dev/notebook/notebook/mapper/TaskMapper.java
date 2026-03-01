package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Task;

public final class TaskMapper {

  private TaskMapper() {
  }

  public static TaskResponseDto toDto(Task task) {
    return new TaskResponseDto(
        task.getId(),
        task.getTitle(),
        task.getDescription(),
        task.getDueDate(),
        task.isCompleted()
    );
  }
}