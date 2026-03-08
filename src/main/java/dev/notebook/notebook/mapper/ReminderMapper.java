package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.ReminderResponseDto;
import dev.notebook.notebook.entity.Reminder;

public class ReminderMapper {

  public static ReminderResponseDto toDto(Reminder reminder) {
    ReminderResponseDto dto = new ReminderResponseDto();
    dto.setId(reminder.getId());
    dto.setReminderTime(reminder.getReminderTime());
    dto.setMessage(reminder.getMessage());
    dto.setType(reminder.getType());
    if (reminder.getTask() != null) {
      dto.setTaskId(reminder.getTask().getId());
    }
    return dto;
  }
}

