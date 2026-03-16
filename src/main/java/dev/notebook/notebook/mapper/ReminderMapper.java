package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.ReminderResponseDto;
import dev.notebook.notebook.entity.Reminder;
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
}

