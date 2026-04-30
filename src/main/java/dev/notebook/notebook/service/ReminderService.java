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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    Task task = taskRepository.findById(dto.taskId())
        .orElseThrow(() -> new NotFoundException("Task not found"));

    // Check if current user owns the task
    Long currentUserId = getCurrentUserId();
    if (currentUserId != null && !task.getProject().getUser().getId().equals(currentUserId)) {
      throw new OperationFailedException("Access denied: Task does not belong to current user",
          null);
    }

    try {
      Reminder reminder = ReminderMapper.toEntity(dto, task);

      Reminder saved = reminderRepository.save(reminder);
      log.info("ReminderService.create completed");
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

    Reminder reminder = reminderRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Reminder not found"));

    // Check if current user owns the reminder's task
    Long currentUserId = getCurrentUserId();
    if (currentUserId != null
        && !reminder.getTask().getProject().getUser().getId().equals(currentUserId)) {
      throw new OperationFailedException("Access denied: Reminder does not belong to current user",
          null);
    }

    Task task = taskRepository.findById(dto.taskId())
        .orElseThrow(() -> new NotFoundException("Task not found"));

    try {
      reminder.setTime(dto.reminderTime());
      reminder.setMessage(dto.message());
      reminder.setTask(task);

      Reminder saved = reminderRepository.save(reminder);
      log.info("ReminderService.update completed");
      return ReminderMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to update reminder", exception);
    }
  }

  @Transactional
  public void delete(Long id) {
    Reminder reminder = reminderRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Reminder not found"));

    // Check if current user owns the reminder's task
    Long currentUserId = getCurrentUserId();
    if (currentUserId != null
        && !reminder.getTask().getProject().getUser().getId().equals(currentUserId)) {
      throw new OperationFailedException("Access denied: Reminder does not belong to current user",
          null);
    }

    try {
      reminderRepository.deleteById(id);
      log.info("ReminderService.delete completed");
    } catch (EmptyResultDataAccessException _) {
      throw new NotFoundException("Reminder not found");
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to delete reminder", exception);
    }
  }

  public ReminderResponseDto getById(Long id) {
    Reminder reminder = reminderRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Reminder not found"));

    // Check if current user owns the reminder's task
    Long currentUserId = getCurrentUserId();
    if (currentUserId != null
        && !reminder.getTask().getProject().getUser().getId().equals(currentUserId)) {
      throw new NotFoundException("Reminder not found");
    }

    log.info("ReminderService.getById completed");
    return ReminderMapper.toDto(reminder);
  }

  public List<ReminderResponseDto> getAll() {
    Long currentUserId = getCurrentUserId();
    List<Reminder> reminders;
    if (currentUserId != null) {
      reminders = reminderRepository.findRemindersByUserId(currentUserId);
      log.info("ReminderService.getAll - returning reminders for userId: {}", currentUserId);
    } else {
      reminders = reminderRepository.findAll();
      log.info("ReminderService.getAll - returning all reminders (no auth)");
    }
    List<ReminderResponseDto> result = new ArrayList<>();
    for (Reminder reminder : reminders) {
      result.add(ReminderMapper.toDto(reminder));
    }
    log.info("ReminderService.getAll completed");
    return result;
  }

  private Long getCurrentUserId() {
    try {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (principal instanceof Long id) {
        return id;
      }
      return Long.valueOf(principal.toString());
    } catch (Exception e) {
      log.warn("Could not get currentUserId from SecurityContext: {}", e.getMessage());
      return null;
    }
  }
}
