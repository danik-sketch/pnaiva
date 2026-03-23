package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Category;
import dev.notebook.notebook.entity.Reminder;
import dev.notebook.notebook.entity.Task;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskMapper {

  public static TaskResponseDto toDto(Task task) {
    TaskResponseDto dto = new TaskResponseDto();
    dto.setId(task.getId());
    dto.setTitle(task.getTitle());
    dto.setDescription(task.getDescription());
    dto.setDueDate(task.getDueDate());
    dto.setCompleted(task.getCompleted());

    if (task.getProject() != null) {
      dto.setProjectName(task.getProject().getName());
    }

    if (task.getCategories() != null) {
      dto.setCategories(task.getCategories().stream()
          .map(Category::getTitle)
          .toList());
    }

    if (task.getReminders() != null) {
      dto.setReminders(task.getReminders().stream()
          .map(Reminder::getMessage)
          .toList());
    }

    return dto;
  }
}
