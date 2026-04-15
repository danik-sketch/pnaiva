package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.ReminderRequestDto;
import dev.notebook.notebook.dto.ReminderResponseDto;
import dev.notebook.notebook.entity.Reminder;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.mapper.ReminderMapper;
import dev.notebook.notebook.repository.ReminderRepository;
import dev.notebook.notebook.repository.TaskRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
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
    if (dto.taskId() == null) {
      throw new IllegalArgumentException("Task id is required");
    }

    Task task = taskRepository.findById(dto.taskId()).orElseThrow(()
        -> new NotFoundException("Task not found"));

    try {
      Reminder reminder = new Reminder();
      reminder.setTime(dto.reminderTime());
      reminder.setMessage(dto.message());
      reminder.setTask(task);

      Reminder saved = reminderRepository.save(reminder);
      return ReminderMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to create reminder", exception);
    }
  }

  @Transactional
  public ReminderResponseDto update(Long id, ReminderRequestDto dto) {
    if (dto.taskId() == null) {
      throw new IllegalArgumentException("Task id is required");
    }

    Reminder reminder = reminderRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Reminder not found"));

    Task task = taskRepository.findById(dto.taskId()).orElseThrow(()
        -> new NotFoundException("Task not found"));

    try {
      reminder.setTime(dto.reminderTime());
      reminder.setMessage(dto.message());
      reminder.setTask(task);

      Reminder saved = reminderRepository.save(reminder);
      return ReminderMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to update reminder", exception);
    }
  }

  @Transactional
  public void delete(Long id) {
    try {
      reminderRepository.deleteById(id);
    } catch (EmptyResultDataAccessException ignored) {
      throw new NotFoundException("Reminder not found");
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to delete reminder", exception);
    }
  }

  public ReminderResponseDto getById(Long id) {
    Reminder reminder = reminderRepository.findById(id).orElseThrow(()
        -> new NotFoundException("Reminder not found"));
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
