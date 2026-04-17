package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.TaskRequestDto;
import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Category;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.Task;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    } else {
      dto.setCategories(Collections.emptyList());
    }
    if (task.getReminders() != null) {
      dto.setReminders(task.getReminders().stream()
          .map(ReminderMapper::toDto)
          .toList());
    } else {
      dto.setReminders(Collections.emptyList());
    }
    return dto;
  }

  public static Task toEntity(TaskRequestDto dto, Project project) {
    Task task = new Task();
    task.setTitle(dto.title());
    task.setDescription(dto.description());
    task.setDueDate(dto.dueDate());
    task.setCompleted(dto.completed());
    task.setProject(project);

    if (dto.reminders() != null) {
      dto.reminders().stream()
          .map(remDto -> ReminderMapper.toEntity(remDto, task))
          .forEach(reminder -> task.getReminders().add(reminder));
    }

    return task;
  }
}
