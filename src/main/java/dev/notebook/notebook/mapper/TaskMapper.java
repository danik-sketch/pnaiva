package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.ReminderResponseDto;
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

    Optional.ofNullable(task.getProject())
        .ifPresent(p -> dto.setProjectName(p.getName()));

    dto.setCategories(Optional.ofNullable(task.getCategories())
        .map(cats -> cats.stream()
            .map(Category::getTitle)
            .toList())
        .orElse(Collections.emptyList()));

    dto.setReminders(Optional.ofNullable(task.getReminders())
        .map(rems -> rems.stream()
            .map(reminder -> {
              ReminderResponseDto rDto = new ReminderResponseDto();
              rDto.setId(reminder.getId());
              rDto.setReminderTime(reminder.getTime());
              rDto.setMessage(reminder.getMessage());
              Optional.ofNullable(reminder.getTask())
                  .ifPresent(t -> rDto.setTaskId(t.getId()));
              return rDto;
            })
            .toList())
        .orElse(Collections.emptyList()));

    return dto;
  }

  public static Task toEntity(TaskRequestDto dto, Project project) {
    Task task = new Task();
    task.setTitle(dto.title());
    task.setDescription(dto.description());
    task.setDueDate(dto.dueDate());
    task.setCompleted(dto.completed());
    task.setProject(project);

    Optional.ofNullable(dto.reminders())
        .stream()
        .flatMap(List::stream)
        .map(remDto -> ReminderMapper.toEntity(remDto, task))
        .forEach(reminder -> task.getReminders().add(reminder));

    return task;
  }
}
