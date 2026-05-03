package dev.notebook.notebook.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static dev.notebook.notebook.service.TestFixtures.FIXED_TIME;
import static dev.notebook.notebook.service.TestFixtures.project;
import static dev.notebook.notebook.service.TestFixtures.reminder;
import static dev.notebook.notebook.service.TestFixtures.task;
import static dev.notebook.notebook.service.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

  @Mock
  private ReminderRepository reminderRepository;
  @Mock
  private TaskRepository taskRepository;

  @InjectMocks
  private ReminderService reminderService;

  @BeforeEach
  void auth() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(1L, null)
    );
  }

  private Task validTask(Long ownerId) {
    Task task = task(5L);
    task.setProject(project(1L));
    task.getProject().setUser(user(ownerId));
    return task;
  }

  private Reminder validReminder(Long ownerId) {
    return reminder(1L, FIXED_TIME, "Ping", validTask(ownerId));
  }

  @Test
  void create_shouldFail_whenTaskIdNull() {
    ReminderRequestDto dto = new ReminderRequestDto(FIXED_TIME, "Ping", null);
    assertThatThrownBy(() -> reminderService.create(dto))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void create_shouldFail_whenTaskNotFound() {
    when(taskRepository.findById(99L)).thenReturn(Optional.empty());
    ReminderRequestDto dto = new ReminderRequestDto(FIXED_TIME, "Ping", 99L);

    assertThatThrownBy(() -> reminderService.create(dto))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void create_success() {
    Task task = validTask(1L);

    when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
    when(reminderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ReminderResponseDto result =
        reminderService.create(new ReminderRequestDto(FIXED_TIME, "Ping", 5L));

    assertThat(result.getTaskId()).isEqualTo(5L);
  }

  @Test
  void create_shouldThrow_whenDbFails() {
    Task task = validTask(1L);

    when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
    when(reminderRepository.save(any()))
        .thenThrow(new DataAccessResourceFailureException("db"));

    ReminderRequestDto dto = new ReminderRequestDto(FIXED_TIME, "Ping", 5L);

    assertThatThrownBy(() -> reminderService.create(dto))
        .isInstanceOf(OperationFailedException.class);
  }

  @Test
  void create_shouldDenyAccess_whenWrongUser() {
    Task task = validTask(2L);

    when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
    ReminderRequestDto dto = new ReminderRequestDto(FIXED_TIME, "Ping", 5L);

    assertThatThrownBy(() -> reminderService.create(dto))
        .isInstanceOf(OperationFailedException.class);
  }

  @Test
  void update_shouldFail_whenReminderNotFound() {
    when(reminderRepository.findById(1L)).thenReturn(Optional.empty());
    ReminderRequestDto dto = new ReminderRequestDto(FIXED_TIME, "Ping", 5L);

    assertThatThrownBy(() -> reminderService.update(1L, dto))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void update_success() {
    Reminder existing = validReminder(1L);
    Task task = validTask(1L);

    when(reminderRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
    when(reminderRepository.save(existing)).thenReturn(existing);

    ReminderResponseDto result =
        reminderService.update(1L, new ReminderRequestDto(FIXED_TIME, "Updated", 5L));

    assertThat(result).isNotNull();
  }

  @Test
  void update_shouldThrow_whenDbFails() {
    Reminder existing = validReminder(1L);
    Task task = validTask(1L);

    when(reminderRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
    when(reminderRepository.save(existing))
        .thenThrow(new DataAccessResourceFailureException("db"));

    ReminderRequestDto dto = new ReminderRequestDto(FIXED_TIME, "Updated", 5L);

    assertThatThrownBy(() -> reminderService.update(1L, dto))
        .isInstanceOf(OperationFailedException.class);
  }

  @Test
  void update_shouldDenyAccess() {
    Reminder existing = validReminder(2L);

    when(reminderRepository.findById(1L)).thenReturn(Optional.of(existing));
    ReminderRequestDto dto = new ReminderRequestDto(FIXED_TIME, "Updated", 5L);

    assertThatThrownBy(() -> reminderService.update(1L, dto))
        .isInstanceOf(OperationFailedException.class);
  }

  @Test
  void delete_success() {
    Reminder reminder = validReminder(1L);

    when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));

    reminderService.delete(1L);

    verify(reminderRepository).deleteById(1L);
  }

  @Test
  void delete_shouldThrow_whenNotFound() {
    when(reminderRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reminderService.delete(1L))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void delete_shouldThrow_whenDbFails() {
    Reminder reminder = validReminder(1L);

    when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));
    doThrow(new DataAccessResourceFailureException("db"))
        .when(reminderRepository).deleteById(1L);

    assertThatThrownBy(() -> reminderService.delete(1L))
        .isInstanceOf(OperationFailedException.class);
  }

  @Test
  void delete_shouldDenyAccess() {
    Reminder reminder = validReminder(2L);

    when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));

    assertThatThrownBy(() -> reminderService.delete(1L))
        .isInstanceOf(OperationFailedException.class);
  }

  @Test
  void getById_success() {
    Reminder reminder = validReminder(1L);

    when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));

    assertThat(reminderService.getById(1L)).isNotNull();
  }

  @Test
  void getById_shouldThrow_whenNotFound() {
    when(reminderRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> reminderService.getById(1L))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void getAll_shouldReturnList() {
    Task task = validTask(1L);

    when(reminderRepository.findRemindersByUserId(1L))
        .thenReturn(List.of(
            reminder(1L, FIXED_TIME, "A", task),
            reminder(2L, FIXED_TIME, "B", task)
        ));

    assertThat(reminderService.getAll()).hasSize(2);
  }
}