package dev.notebook.notebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static dev.notebook.notebook.service.TestFixtures.FIXED_TIME;
import static dev.notebook.notebook.service.TestFixtures.OTHER_TIME;
import static dev.notebook.notebook.service.TestFixtures.project;
import static dev.notebook.notebook.service.TestFixtures.task;
import static dev.notebook.notebook.service.TestFixtures.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.dto.ReminderRequestDto;
import dev.notebook.notebook.dto.TaskRequestDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ProjectService projectService;

  @Test
  void createShouldThrowWhenUserNotFound() {
    ProjectRequestDto requestDto = new ProjectRequestDto("P", "D", 77L, List.of());
    when(userRepository.findById(77L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> projectService.create(requestDto))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void createShouldSaveProjectWithNestedTasksAndReminders() {
    ProjectRequestDto requestDto = new ProjectRequestDto(
        "P", "D", 1L, List.of(new TaskRequestDto(
        "Task 1", "Task desc", FIXED_TIME, null, null,
        List.of(new ReminderRequestDto(OTHER_TIME, "Ping", null)))));

    User user = user(1L, "john");
    Project saved = project(5L, "P", "D", user);

    Task savedTask = new Task();
    savedTask.setId(10L);
    savedTask.setTitle("Task 1");
    savedTask.setDescription("Task desc");
    savedTask.setDueDate(FIXED_TIME);
    savedTask.setProject(saved);
    saved.getTasks().add(savedTask);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(projectRepository.save(any(Project.class))).thenReturn(saved);

    ProjectResponseDto result = projectService.create(requestDto);

    assertThat(result.getId()).isEqualTo(5L);
    assertThat(result.getName()).isEqualTo("P");
    assertThat(result.getUsername()).isEqualTo("john");
    assertThat(result.getTasks()).hasSize(1);
    verify(projectRepository).save(any(Project.class));
  }

  @Test
  void createShouldSaveProjectWhenTasksNull() {
    ProjectRequestDto requestDto = new ProjectRequestDto("P", "D", 1L, null);
    User user = user(1L, "john");
    Project saved = project(5L, "P", "D", user);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(projectRepository.save(any(Project.class))).thenReturn(saved);

    ProjectResponseDto result = projectService.create(requestDto);

    assertThat(result.getId()).isEqualTo(5L);
    assertThat(result.getTasks()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("taskRequestWithoutReminders")
  void createShouldSaveTaskWhenNoRemindersProvided(TaskRequestDto taskRequestDto) {
    ProjectRequestDto requestDto = new ProjectRequestDto(
        "P", "D", 1L, List.of(taskRequestDto));

    User user = user(1L, "john");
    Project saved = project(6L, "P", "D", user);
    Task savedTask = task(11L, taskRequestDto.title(), taskRequestDto.description(),
        taskRequestDto.dueDate(), taskRequestDto.completed(), saved);
    savedTask.setProject(saved);
    saved.getTasks().add(savedTask);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(projectRepository.save(any(Project.class))).thenReturn(saved);

    ProjectResponseDto result = projectService.create(requestDto);

    assertThat(result.getTasks()).hasSize(1);
    assertThat(result.getTasks().getFirst().getTitle()).isEqualTo(taskRequestDto.title());
  }

  @Test
  void createShouldWrapRepositoryFailure() {
    ProjectRequestDto requestDto = new ProjectRequestDto("P", "D", 1L, List.of());
    when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "john")));
    when(projectRepository.save(any(Project.class)))
        .thenThrow(new DataAccessResourceFailureException("db down"));

    assertThatThrownBy(() -> projectService.create(requestDto))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to create project");
  }

  @Test
  void updateShouldThrowWhenProjectNotFound() {
    when(projectRepository.findById(20L)).thenReturn(Optional.empty());
    ProjectRequestDto requestDto = new ProjectRequestDto("P", "D", 1L, List.of());

    assertThatThrownBy(() -> projectService.update(20L, requestDto))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Project not found");
  }

  @Test
  void updateShouldLoadNewUserWhenUserChanged() {
    User oldUser = user(1L, "john");
    User newUser = user(2L, "alice");
    Project existing = project(9L, "Old", "Old desc", oldUser);

    when(projectRepository.findById(9L)).thenReturn(Optional.of(existing));
    when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
    when(projectRepository.save(existing)).thenReturn(existing);

    ProjectResponseDto result = projectService.update(
        9L, new ProjectRequestDto("Updated", "Updated desc", 2L, List.of()));

    assertThat(result.getUsername()).isEqualTo("alice");
    assertThat(result.getName()).isEqualTo("Updated");
  }

  @Test
  void updateShouldNotLoadUserWhenUserUnchanged() {
    User user = user(1L, "john");
    Project existing = project(9L, "Old", "Old desc", user);

    when(projectRepository.findById(9L)).thenReturn(Optional.of(existing));
    when(projectRepository.save(existing)).thenReturn(existing);

    ProjectResponseDto result = projectService.update(
        9L, new ProjectRequestDto("Updated", "Updated desc", 1L, List.of()));

    assertThat(result.getUsername()).isEqualTo("john");
    verify(userRepository, never()).findById(1L);
  }

  @Test
  void updateShouldThrowWhenNewUserNotFound() {
    Project existing = project(9L, "Old", "Old desc", user(1L, "john"));
    ProjectRequestDto requestDto = new ProjectRequestDto("Updated", "Updated desc", 2L, List.of());

    when(projectRepository.findById(9L)).thenReturn(Optional.of(existing));
    when(userRepository.findById(2L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> projectService.update(9L, requestDto))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to update project");
  }

  @Test
  void updateShouldWrapRepositoryFailure() {
    User user = user(1L, "john");
    Project existing = project(9L, "Old", "Old desc", user);
    ProjectRequestDto requestDto = new ProjectRequestDto("Updated", "Updated desc", 1L, List.of());
    when(projectRepository.findById(9L)).thenReturn(Optional.of(existing));
    when(projectRepository.save(existing)).thenThrow(new DataAccessResourceFailureException("db down"));

    assertThatThrownBy(() -> projectService.update(9L, requestDto))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to update project");
  }

  @Test
  void deleteShouldMapEmptyResultToNotFound() {
    doThrow(new EmptyResultDataAccessException(1)).when(projectRepository).deleteById(3L);

    assertThatThrownBy(() -> projectService.delete(3L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Project not found");
  }

  @Test
  void deleteShouldWrapRepositoryFailure() {
    doThrow(new DataAccessResourceFailureException("db down")).when(projectRepository).deleteById(3L);

    assertThatThrownBy(() -> projectService.delete(3L))
        .isInstanceOf(OperationFailedException.class)
        .hasMessage("Failed to delete project");
  }

  @Test
  void deleteShouldCallRepository() {
    projectService.delete(3L);
    verify(projectRepository).deleteById(3L);
  }

  @Test
  void getByIdShouldReturnProject() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L, "P", "D", user(1L, "john"))));

    ProjectResponseDto result = projectService.getById(1L);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("P");
  }

  @Test
  void getByIdShouldThrowWhenProjectNotFound() {
    when(projectRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> projectService.getById(999L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Project not found");
  }

  @Test
  void getAllShouldMapProjects() {
    when(projectRepository.findAll()).thenReturn(List.of(
        project(1L, "P1", "D1", user(1L, "john")),
        project(2L, "P2", "D2", user(2L, "alice"))));

    List<ProjectResponseDto> result = projectService.getAll();

    assertThat(result).hasSize(2);
    assertThat(result).extracting(ProjectResponseDto::getName).containsExactly("P1", "P2");
  }

  @Test
  void getAllShouldReturnEmptyList() {
    when(projectRepository.findAll()).thenReturn(List.of());

    List<ProjectResponseDto> result = projectService.getAll();

    assertThat(result).isEmpty();
  }

  @Test
  void searchByTaskJpqlShouldUseCacheForSameKey() {
    Pageable pageable = PageRequest.of(0, 10);
    LocalDateTime dueFrom = LocalDateTime.of(2026, 4, 1, 0, 0);
    LocalDateTime dueTo = LocalDateTime.of(2026, 4, 30, 23, 59);

    Page<Project> page = new PageImpl<>(List.of(project(1L, "P", "D", user(1L, "john"))));

    when(projectRepository.searchByTaskJpql(
        eq("P"), eq("Task"), eq(Boolean.TRUE), eq(dueFrom), eq(dueTo), eq(pageable)))
        .thenReturn(page);

    Page<ProjectResponseDto> first = projectService.searchByTaskJpql(
        "P", "Task", true, dueFrom, dueTo, pageable);
    Page<ProjectResponseDto> second = projectService.searchByTaskJpql(
        "P", "Task", true, dueFrom, dueTo, pageable);

    assertThat(first.getContent()).hasSize(1);
    assertThat(second.getContent()).hasSize(1);
    verify(projectRepository, times(1)).searchByTaskJpql(
        "P", "Task", true, dueFrom, dueTo, pageable);
  }

  @Test
  void createShouldInvalidateSearchCache() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Project> page = new PageImpl<>(List.of(project(1L, "P", "D", user(1L, "john"))));

    when(projectRepository.searchByTaskJpql(
        eq("P"), eq("Task"), eq(null), eq(null), eq(null), eq(pageable)))
        .thenReturn(page);

    projectService.searchByTaskJpql("P", "Task", null, null, null, pageable);
    projectService.searchByTaskJpql("P", "Task", null, null, null, pageable);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "john")));
    when(projectRepository.save(any(Project.class))).thenReturn(project(2L, "N", "D", user(1L, "john")));
    projectService.create(new ProjectRequestDto("N", "D", 1L, List.of()));

    projectService.searchByTaskJpql("P", "Task", null, null, null, pageable);

    verify(projectRepository, times(2)).searchByTaskJpql(
        "P", "Task", null, null, null, pageable);
  }

  private static Stream<TaskRequestDto> taskRequestWithoutReminders() {
    return Stream.of(
        new TaskRequestDto("Task 1", "Task desc", FIXED_TIME, null, null, null),
        new TaskRequestDto("Task 2", "Task desc", OTHER_TIME, null, null, List.of()));
  }
  @Test
  void createBulkShouldSaveMultipleProjects() {
    ProjectRequestDto dto1 = new ProjectRequestDto("Project 1", "Desc 1", 1L, List.of());
    ProjectRequestDto dto2 = new ProjectRequestDto("Project 2", "Desc 2", 1L, List.of());
    List<ProjectRequestDto> dtoList = List.of(dto1, dto2);

    User user = user(1L, "john");
    Project project1 = project(101L, "Project 1", "Desc 1", user);
    Project project2 = project(102L, "Project 2", "Desc 2", user);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(projectRepository.save(any(Project.class)))
        .thenReturn(project1)
        .thenReturn(project2);

    List<ProjectResponseDto> result = projectService.createBulk(dtoList);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(101L);
    assertThat(result.get(1).getId()).isEqualTo(102L);
    assertThat(result.get(0).getName()).isEqualTo("Project 1");
    assertThat(result.get(1).getName()).isEqualTo("Project 2");

    verify(projectRepository, times(2)).save(any(Project.class));
    verify(userRepository, times(2)).findById(1L);
  }


}
