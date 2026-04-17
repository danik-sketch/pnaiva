package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.ReminderRequestDto;
import dev.notebook.notebook.dto.ReminderResponseDto;
import dev.notebook.notebook.entity.Reminder;
import dev.notebook.notebook.entity.Task;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReminderMapper {

  public static ReminderResponseDto toDto(Reminder reminder) {
    ReminderResponseDto dto = new ReminderResponseDto();
    dto.setId(reminder.getId());
    dto.setReminderTime(reminder.getTime());
    dto.setMessage(reminder.getMessage());
    if (reminder.getTask() != null) {
      dto.setTaskId(reminder.getTask().getId());
    }
    return dto;
  }

  public static Reminder toEntity(ReminderRequestDto dto, Task task) {
    Reminder reminder = new Reminder();
    reminder.setTime(dto.reminderTime());
    reminder.setMessage(dto.message());
    reminder.setTask(task);
    return reminder;
  }
}

