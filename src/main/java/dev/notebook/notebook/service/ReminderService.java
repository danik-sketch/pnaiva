package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.ReminderRequestDto;
import dev.notebook.notebook.dto.ReminderResponseDto;
import dev.notebook.notebook.entity.Reminder;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.mapper.ReminderMapper;
import dev.notebook.notebook.repository.ReminderRepository;
import dev.notebook.notebook.repository.TaskRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReminderService {

  private final ReminderRepository reminderRepository;
  private final TaskRepository taskRepository;

  @Transactional
  public ReminderResponseDto create(ReminderRequestDto dto) {
    Task task = taskRepository.findById(dto.taskId())
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    Reminder reminder = new Reminder();
    reminder.setReminderTime(dto.reminderTime());
    reminder.setMessage(dto.message());
    reminder.setType(dto.type());
    reminder.setTask(task);

    Reminder saved = reminderRepository.save(reminder);
    return ReminderMapper.toDto(saved);
  }

  @Transactional
  public ReminderResponseDto update(Long id, ReminderRequestDto dto) {
    Reminder reminder = reminderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));

    Task task = taskRepository.findById(dto.taskId())
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    reminder.setReminderTime(dto.reminderTime());
    reminder.setMessage(dto.message());
    reminder.setType(dto.type());
    reminder.setTask(task);

    Reminder saved = reminderRepository.save(reminder);
    return ReminderMapper.toDto(saved);
  }

  @Transactional
  public void delete(Long id) {
    reminderRepository.deleteById(id);
  }

  public ReminderResponseDto getById(Long id) {
    Reminder reminder = reminderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
    return ReminderMapper.toDto(reminder);
  }

  public List<ReminderResponseDto> getAll() {
    List<Reminder> reminders = reminderRepository.findAll();
    List<ReminderResponseDto> result = new ArrayList<>();
    for (Reminder reminder : reminders) {
      result.add(ReminderMapper.toDto(reminder));
    }
    return result;
  }

}
