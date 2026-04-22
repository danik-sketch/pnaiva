package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.TaskRequestDto;
import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.TaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import static dev.notebook.notebook.service.TestFixtures.FIXED_TIME;
import static dev.notebook.notebook.service.TestFixtures.OTHER_TIME;
import static dev.notebook.notebook.service.TestFixtures.project;
import static dev.notebook.notebook.service.TestFixtures.task;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private ProjectRepository projectRepository;

  @InjectMocks
  private TaskService taskService;

  @Test
  void createShouldRequireProjectId() {
    TaskRequestDto requestDto = new TaskRequestDto(
        "Task", "Desc", FIXED_TIME, null, null, List.of());

    assertThatThrownBy(() -> taskService.create(requestDto))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Project id is required");
  }

  @Test
  void createShouldThrowWhenProjectNotFound() {
    TaskRequestDto requestDto = new TaskRequestDto(
        "Task", "Desc", FIXED_TIME, null, 9L, List.of());

    when(projectRepository.findById(9L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.create(requestDto))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Project not found");
  }

  @Test
  void createShouldReturnSavedTask() {
    Project project = project(1L, "Main project");
    Task saved = task(10L, "Task", "Desc", FIXED_TIME, null, project);

    TaskRequestDto requestDto = new TaskRequestDto(
        "Task", "Desc", FIXED_TIME, null, 1L, List.of());

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    when(taskRepository.save(any(Task.class))).thenReturn(saved);

    TaskResponseDto result = taskService.create(requestDto);

    assertThat(result.getId()).isEqualTo(10L);
    assertThat(result.getProjectName()).isEqualTo("Main project");
  }

  @Test
  void createShouldWrapRepositoryFailure() {
    Project project = project(1L, "Main project");
    TaskRequestDto requestDto = new TaskRequestDto(
        "Task", "Desc", FIXED_TIME, null, 1L, List.of());

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    when(taskRepository.save(any(Task.class)))
        .thenThrow(new DataAccessResourceFailureException("db down"));

    assertThatThrownBy(() -> taskService.create(requestDto))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to create task");
  }

  @Test
  void updateShouldThrowWhenTaskNotFound() {
    when(taskRepository.findById(100L)).thenReturn(Optional.empty());
    TaskRequestDto requestDto = new TaskRequestDto("Task", "Desc", FIXED_TIME, null, 1L,
        List.of());

    assertThatThrownBy(() -> taskService.update(100L, requestDto))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Task not found");
  }

  @Test
  void updateShouldThrowWhenProjectChangedAndNewProjectNotFound() {
    Project oldProject = project(1L, "Old");
    Task existing = task(4L, "Task", "Desc", FIXED_TIME, null, oldProject);

    when(taskRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(projectRepository.findById(2L)).thenReturn(Optional.empty());

    TaskRequestDto requestDto = new TaskRequestDto(
        "Task", "Desc", FIXED_TIME, null, 2L, List.of());

    assertThatThrownBy(() -> taskService.update(4L, requestDto))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to update task");
  }

  @Test
  void updateShouldNotLoadProjectWhenProjectIdUnchanged() {
    Project project = project(1L, "Main project");
    Task existing = task(4L, "Task", "Desc", FIXED_TIME, null, project);

    when(taskRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(taskRepository.save(existing)).thenReturn(existing);

    TaskRequestDto requestDto = new TaskRequestDto(
        "Updated", "Updated desc", FIXED_TIME, null, 1L, List.of());

    TaskResponseDto result = taskService.update(4L, requestDto);

    assertThat(result.getTitle()).isEqualTo("Updated");
    verify(projectRepository, never()).findById(any());
  }

  @Test
  void updateShouldChangeProjectWhenProjectIdChanged() {
    Project oldProject = project(1L, "Old project");
    Project newProject = project(2L, "New project");
    Task existing = task(4L, "Task", "Desc", FIXED_TIME, null, oldProject);

    when(taskRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(projectRepository.findById(2L)).thenReturn(Optional.of(newProject));
    when(taskRepository.save(existing)).thenReturn(existing);

    TaskResponseDto result = taskService.update(
        4L, new TaskRequestDto("Updated", "Updated desc", FIXED_TIME, null, 2L, List.of()));

    assertThat(result.getProjectName()).isEqualTo("New project");
  }

  @Test
  void updateShouldLoadProjectWhenTaskProjectIsNull() {
    Project newProject = project(2L, "New project");
    Task existing = task(4L, "Task", "Desc", FIXED_TIME, null, null);

    when(taskRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(projectRepository.findById(2L)).thenReturn(Optional.of(newProject));
    when(taskRepository.save(existing)).thenReturn(existing);

    TaskResponseDto result = taskService.update(
        4L, new TaskRequestDto("Updated", "Updated desc", FIXED_TIME, null, 2L, List.of()));

    assertThat(result.getProjectName()).isEqualTo("New project");
  }

  @Test
  void updateShouldSkipProjectLookupWhenProjectIdIsNull() {
    Project project = project(1L, "Main project");
    Task existing = task(4L, "Task", "Desc", FIXED_TIME, null, project);

    when(taskRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(taskRepository.save(existing)).thenReturn(existing);

    TaskResponseDto result = taskService.update(
        4L, new TaskRequestDto("Updated", "Updated desc", FIXED_TIME, null, null, List.of()));

    assertThat(result.getTitle()).isEqualTo("Updated");
    verify(projectRepository, never()).findById(any());
  }

  @Test
  void updateShouldWrapRepositoryFailure() {
    Project project = project(1L, "Main project");
    Task existing = task(4L, "Task", "Desc", FIXED_TIME, null, project);
    TaskRequestDto requestDto = new TaskRequestDto(
        "Updated", "Updated desc", FIXED_TIME, null, 1L, List.of());

    when(taskRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(taskRepository.save(existing)).thenThrow(
        new DataAccessResourceFailureException("db down"));

    assertThatThrownBy(() -> taskService.update(4L, requestDto))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to update task");
  }

  @Test
  void deleteShouldMapEmptyResultToNotFound() {
    doThrow(new EmptyResultDataAccessException(1)).when(taskRepository).deleteById(6L);

    assertThatThrownBy(() -> taskService.delete(6L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Task not found");
  }

  @Test
  void deleteShouldWrapRepositoryFailure() {
    doThrow(new DataAccessResourceFailureException("db down")).when(taskRepository).deleteById(6L);

    assertThatThrownBy(() -> taskService.delete(6L))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to delete task");
  }

  @Test
  void deleteShouldCallRepository() {
    taskService.delete(6L);
    verify(taskRepository).deleteById(6L);
  }

  @Test
  void getAllShouldMapTasks() {
    Project project = project(1L, "Main project");
    when(taskRepository.findAll()).thenReturn(List.of(
        task(1L, "T1", "D1", FIXED_TIME, null, project),
        task(2L, "T2", "D2", OTHER_TIME, null, project)));

    List<TaskResponseDto> result = taskService.getAll();

    assertThat(result).hasSize(2);
    assertThat(result).extracting(TaskResponseDto::getTitle).containsExactly("T1", "T2");
  }

  @Test
  void getByIdShouldReturnTask() {
    Project project = project(1L, "Main project");
    when(taskRepository.findById(11L))
        .thenReturn(Optional.of(task(11L, "Task", "Desc", FIXED_TIME, null, project)));

    TaskResponseDto result = taskService.getById(11L);

    assertThat(result.getId()).isEqualTo(11L);
  }

  @Test
  void getByIdShouldThrowWhenTaskNotFound() {
    when(taskRepository.findById(11L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.getById(11L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Task not found");
  }

  @Test
  void getByTitleContainingShouldMapTasks() {
    when(taskRepository.findByTitleContaining("Ta"))
        .thenReturn(List.of(task(1L, "Task", "Desc", FIXED_TIME, null, null)));

    List<TaskResponseDto> result = taskService.getByTitleContaining("Ta");
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getTitle()).isEqualTo("Task");
  }

  @Test
  void getByDescriptionShouldMapTasks() {
    when(taskRepository.findByDescription("Desc"))
        .thenReturn(List.of(task(1L, "Task", "Desc", FIXED_TIME, null, null)));

    List<TaskResponseDto> result = taskService.getByDescription("Desc");
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getDescription()).isEqualTo("Desc");
  }

  @Test
  void getByCompletedShouldUseCorrectRepositoryMethod() {
    when(taskRepository.findByCompletedIsNotNull())
        .thenReturn(List.of(task(1L, "Done", "", FIXED_TIME, OTHER_TIME, null)));
    when(taskRepository.findByCompletedIsNull())
        .thenReturn(List.of(task(2L, "Todo", "", FIXED_TIME, null, null)));

    List<TaskResponseDto> completed = taskService.getByCompleted(true);
    List<TaskResponseDto> uncompleted = taskService.getByCompleted(false);

    assertThat(completed).hasSize(1);
    assertThat(uncompleted).hasSize(1);
    verify(taskRepository).findByCompletedIsNotNull();
    verify(taskRepository).findByCompletedIsNull();
  }

  @Test
  void getByDueDateShouldQueryFullDayRange() {
    LocalDate dueDate = LocalDate.of(2026, 4, 16);
    when(taskRepository.findByDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(List.of());

    taskService.getByDueDate(dueDate);

    ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(taskRepository).findByDueDateBetween(startCaptor.capture(), endCaptor.capture());

    assertThat(startCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 4, 16, 0, 0));
    assertThat(endCaptor.getValue()).isEqualTo(
        LocalDateTime.of(2026, 4, 16, 23, 59, 59, 999999999));
  }

  @Test
  void getByDueDateShouldMapTasks() {
    LocalDate dueDate = LocalDate.of(2026, 4, 16);
    when(taskRepository.findByDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenReturn(List.of(task(3L, "ByDate", "", dueDate.atStartOfDay(), null, null)));

    List<TaskResponseDto> result = taskService.getByDueDate(dueDate);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getTitle()).isEqualTo("ByDate");
  }

}
