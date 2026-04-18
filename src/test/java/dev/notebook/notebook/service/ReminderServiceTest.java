package dev.notebook.notebook.service;

import static dev.notebook.notebook.service.TestFixtures.FIXED_TIME;
import static dev.notebook.notebook.service.TestFixtures.OTHER_TIME;
import static dev.notebook.notebook.service.TestFixtures.reminder;
import static dev.notebook.notebook.service.TestFixtures.task;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.notebook.notebook.dto.ReminderRequestDto;
import dev.notebook.notebook.dto.ReminderResponseDto;
import dev.notebook.notebook.entity.Reminder;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.repository.ReminderRepository;
import dev.notebook.notebook.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

  @Mock
  private ReminderRepository reminderRepository;

  @Mock
  private TaskRepository taskRepository;

  @InjectMocks
  private ReminderService reminderService;

  @Test
  void createShouldValidateTaskAndWrapFailure() {
    assertThatThrownBy(() -> reminderService.create(new ReminderRequestDto(FIXED_TIME, "Ping", null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Task id is required");

    when(taskRepository.findById(99L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> reminderService.create(new ReminderRequestDto(FIXED_TIME, "Ping", 99L)))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Task not found");

    Task task = task(5L);
    when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
    when(reminderRepository.save(any(Reminder.class))).thenReturn(reminder(1L, FIXED_TIME, "Ping", task));
    assertThat(reminderService.create(new ReminderRequestDto(FIXED_TIME, "Ping", 5L)).getTaskId())
        .isEqualTo(5L);

    when(reminderRepository.save(any(Reminder.class)))
        .thenThrow(new DataAccessResourceFailureException("db down"));
    assertThatThrownBy(() -> reminderService.create(new ReminderRequestDto(FIXED_TIME, "Ping", 5L)))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to create reminder");
  }

  @Test
  void updateShouldValidateLookupAndWrapFailure() {
    assertThatThrownBy(() -> reminderService.update(1L, new ReminderRequestDto(FIXED_TIME, "Ping", null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Task id is required");

    when(reminderRepository.findById(1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> reminderService.update(1L, new ReminderRequestDto(FIXED_TIME, "Ping", 5L)))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Reminder not found");

    Reminder existing = reminder(1L, FIXED_TIME, "Ping", task(5L));
    when(reminderRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(taskRepository.findById(8L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> reminderService.update(1L, new ReminderRequestDto(FIXED_TIME, "Updated", 8L)))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Task not found");

    Task linkedTask = task(5L);
    when(taskRepository.findById(5L)).thenReturn(Optional.of(linkedTask));
    when(reminderRepository.save(existing)).thenReturn(existing);
    ReminderResponseDto ok = reminderService.update(1L,
        new ReminderRequestDto(OTHER_TIME, "Updated", 5L));
    assertThat(ok.getReminderTime()).isEqualTo(OTHER_TIME);

    when(reminderRepository.save(existing)).thenThrow(new DataAccessResourceFailureException("db down"));
    assertThatThrownBy(() -> reminderService.update(1L,
        new ReminderRequestDto(OTHER_TIME, "Updated", 5L)))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to update reminder");
  }

  @Test
  void deleteShouldMapExceptions() {
    doThrow(new EmptyResultDataAccessException(1)).when(reminderRepository).deleteById(3L);
    assertThatThrownBy(() -> reminderService.delete(3L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Reminder not found");

    doThrow(new DataAccessResourceFailureException("db down")).when(reminderRepository).deleteById(4L);
    assertThatThrownBy(() -> reminderService.delete(4L))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to delete reminder");
  }

  @Test
  void deleteShouldCallRepository() {
    reminderService.delete(5L);
    verify(reminderRepository).deleteById(5L);
  }

  @Test
  void getByIdShouldMapFoundAndNotFound() {
    when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder(1L, FIXED_TIME, "A", task(2L))));
    assertThat(reminderService.getById(1L).getTaskId()).isEqualTo(2L);

    when(reminderRepository.findById(2L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> reminderService.getById(2L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Reminder not found");
  }

  @Test
  void getAllShouldMapReminders() {
    when(reminderRepository.findAll()).thenReturn(List.of(
        reminder(1L, FIXED_TIME, "A", task(1L)),
        reminder(2L, OTHER_TIME, "B", task(1L))));

    assertThat(reminderService.getAll()).hasSize(2);
  }
}
