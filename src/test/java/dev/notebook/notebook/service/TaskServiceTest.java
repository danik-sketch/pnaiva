package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.TaskRequestDto;
import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Category;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.repository.CategoryRepository;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock private TaskRepository repository;
  @Mock private ProjectRepository projectRepository;
  @Mock private CategoryRepository categoryRepository;

  @InjectMocks private TaskService service;

  @BeforeEach
  void setup() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(1L, null)
    );
  }

  private TaskRequestDto dto(Long projectId) {
    return new TaskRequestDto(
        "title",
        "desc",
        LocalDateTime.now(),
        null,
        projectId,
        List.of(),
        List.of()
    );
  }

  private User user(Long id) {
    User u = new User();
    u.setId(id);
    return u;
  }

  private Project project(Long userId) {
    Project p = new Project();
    p.setId(1L);
    p.setUser(user(userId));
    return p;
  }

  private Task task(Long userId) {
    Task t = new Task();
    t.setId(1L);
    t.setProject(project(userId));
    return t;
  }

  @Test
  void create_success() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

    TaskResponseDto result = service.create(dto(1L));

    assertNotNull(result);
    verify(repository).save(any());
  }

  @Test
  void create_noProjectId() {
    assertThrows(IllegalArgumentException.class,
        () -> service.create(dto(null)));
  }

  @Test
  void create_projectNotFound() {
    when(projectRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> service.create(dto(1L)));
  }

  @Test
  void create_notOwner() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(2L)));

    assertThrows(OperationFailedException.class,
        () -> service.create(dto(1L)));
  }

  @Test
  void create_categoryMismatch() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(categoryRepository.findAllById(List.of(1L, 2L)))
        .thenReturn(List.of(new Category()));

    TaskRequestDto dto = new TaskRequestDto(
        "t", "d", LocalDateTime.now(), null, 1L,
        List.of(1L, 2L), List.of()
    );

    assertThrows(NotFoundException.class,
        () -> service.create(dto));
  }

  @Test
  void create_withCategories_success() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(categoryRepository.findAllById(List.of(1L)))
        .thenReturn(List.of(new Category()));
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

    TaskRequestDto dto = new TaskRequestDto(
        "t", "d", LocalDateTime.now(), null, 1L,
        List.of(1L), List.of()
    );

    assertNotNull(service.create(dto));
  }

  @Test
  void update_success() {
    Task t = task(1L);

    when(repository.findById(1L)).thenReturn(Optional.of(t));
    when(repository.save(any())).thenReturn(t);

    assertNotNull(service.update(1L, dto(1L)));
  }

  @Test
  void update_notFound() {
    when(repository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> service.update(1L, dto(1L)));
  }

  @Test
  void update_notOwner() {
    when(repository.findById(1L)).thenReturn(Optional.of(task(2L)));

    assertThrows(OperationFailedException.class,
        () -> service.update(1L, dto(1L)));
  }

  @Test
  void update_moveProject_notOwner() {
    Task t = task(1L);

    when(repository.findById(1L)).thenReturn(Optional.of(t));
    when(projectRepository.findById(2L)).thenReturn(Optional.of(project(2L)));

    TaskRequestDto dto = new TaskRequestDto(
        "t","d",LocalDateTime.now(),null,2L,List.of(),List.of()
    );

    assertThrows(OperationFailedException.class,
        () -> service.update(1L, dto));
  }

  @Test
  void update_withoutProjectChange() {
    Task t = task(1L);

    when(repository.findById(1L)).thenReturn(Optional.of(t));
    when(repository.save(any())).thenReturn(t);

    TaskRequestDto dto = new TaskRequestDto(
        "t","d",LocalDateTime.now(),null,null,List.of(),List.of()
    );

    assertNotNull(service.update(1L, dto));
  }

  @Test
  void delete_success() {
    when(repository.findById(1L)).thenReturn(Optional.of(task(1L)));

    service.delete(1L);

    verify(repository).deleteById(1L);
  }

  @Test
  void delete_notOwner() {
    when(repository.findById(1L)).thenReturn(Optional.of(task(2L)));

    assertThrows(OperationFailedException.class,
        () -> service.delete(1L));
  }

  @Test
  void delete_emptyResult() {
    when(repository.findById(1L)).thenReturn(Optional.of(task(1L)));
    doThrow(new EmptyResultDataAccessException(1))
        .when(repository).deleteById(1L);

    assertThrows(NotFoundException.class,
        () -> service.delete(1L));
  }

  @Test
  void getById_success() {
    when(repository.findById(1L)).thenReturn(Optional.of(task(1L)));

    assertNotNull(service.getById(1L));
  }

  @Test
  void getById_notOwner() {
    when(repository.findById(1L)).thenReturn(Optional.of(task(2L)));

    assertThrows(NotFoundException.class,
        () -> service.getById(1L));
  }

  @Test
  void getAll_auth() {
    when(repository.findByProject_UserId(1L))
        .thenReturn(List.of(task(1L)));

    assertEquals(1, service.getAll().size());
  }

  @Test
  void getAll_noAuth() {
    SecurityContextHolder.clearContext();

    when(repository.findAll()).thenReturn(List.of(task(1L)));

    assertEquals(1, service.getAll().size());
  }

  @Test
  void getByTitle() {
    when(repository.findByProject_UserIdAndTitleContaining(1L, "t"))
        .thenReturn(List.of(task(1L)));

    assertEquals(1, service.getByTitleContaining("t").size());
  }

  @Test
  void getByCompleted() {
    when(repository.findByProject_UserIdAndCompletedIsNotNull(1L))
        .thenReturn(List.of(task(1L)));

    assertEquals(1, service.getByCompleted(true).size());
  }

  @Test
  void getByDueDate() {
    when(repository.findByProject_UserIdAndDueDateBetween(any(), any(), any()))
        .thenReturn(List.of(task(1L)));

    assertEquals(1, service.getByDueDate(LocalDate.now()).size());
  }

  @Test
  void getCurrentUserId_invalidStringPrincipal() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("abc", null)
    );

    when(repository.findAll()).thenReturn(List.of(task(1L)));

    assertEquals(1, service.getAll().size());
  }

  @Test
  void getCurrentUserId_unknownPrincipalType() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(new Object(), null)
    );

    when(repository.findAll()).thenReturn(List.of(task(1L)));

    assertEquals(1, service.getAll().size());
  }

  @Test
  void create_repositoryThrowsException() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(repository.save(any())).thenThrow(new RuntimeException("DB error"));

    assertThrows(OperationFailedException.class,
        () -> service.create(dto(1L)));
  }

  @Test
  void update_repositoryThrowsException() {
    Task t = task(1L);

    when(repository.findById(1L)).thenReturn(Optional.of(t));
    when(repository.save(any())).thenThrow(new RuntimeException("DB error"));

    assertThrows(OperationFailedException.class,
        () -> service.update(1L, dto(1L)));
  }

  @Test
  void getByDescription_success() {
    when(repository.findByProject_UserIdAndDescription(1L, "desc"))
        .thenReturn(List.of(task(1L)));

    assertEquals(1, service.getByDescription("desc").size());
  }

  @Test
  void getByCompleted_falseBranch() {
    when(repository.findByProject_UserIdAndCompletedIsNull(1L))
        .thenReturn(List.of(task(1L)));

    assertEquals(1, service.getByCompleted(false).size());
  }

  @Test
  void create_nullCategories() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

    TaskRequestDto dto = new TaskRequestDto(
        "t", "d", LocalDateTime.now(), null, 1L, null, List.of()
    );

    assertNotNull(service.create(dto));
  }

  @Test
  void create_emptyCategories() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

    TaskRequestDto dto = new TaskRequestDto(
        "t", "d", LocalDateTime.now(), null, 1L, List.of(), List.of()
    );

    assertNotNull(service.create(dto));
  }

  @Test
  void create_categoryMismatch_strict() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(categoryRepository.findAllById(List.of(1L, 2L)))
        .thenReturn(List.of(new Category()));

    TaskRequestDto dto = new TaskRequestDto(
        "t", "d", LocalDateTime.now(), null, 1L,
        List.of(1L, 2L), List.of()
    );

    assertThrows(NotFoundException.class,
        () -> service.create(dto));
  }

  @Test
  void update_changeProject_success() {
    Task t = task(1L);

    Project newProject = project(1L);
    newProject.setId(2L);

    when(repository.findById(1L)).thenReturn(Optional.of(t));
    when(projectRepository.findById(2L)).thenReturn(Optional.of(newProject));
    when(repository.save(any())).thenReturn(t);

    TaskRequestDto dto = new TaskRequestDto(
        "t",
        "d",
        LocalDateTime.now(),
        null,
        2L,
        List.of(),
        List.of()
    );

    assertNotNull(service.update(1L, dto));

    verify(projectRepository).findById(2L);
  }

  @Test
  void create_taskWithExistingCategoriesSet() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(categoryRepository.findAllById(List.of(1L)))
        .thenReturn(List.of(new Category()));
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

    TaskRequestDto dto = new TaskRequestDto(
        "t", "d", LocalDateTime.now(), null, 1L,
        List.of(1L), List.of()
    );

    assertNotNull(service.create(dto));
  }

  @Test
  void update_completedNull() {
    Task t = task(1L);

    when(repository.findById(1L)).thenReturn(Optional.of(t));
    when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

    TaskRequestDto dto = new TaskRequestDto(
        "t","d",LocalDateTime.now(),null,1L,List.of(),List.of()
    );

    assertNotNull(service.update(1L, dto));
  }

  @Test
  void getByDueDate_exactRange() {
    LocalDate date = LocalDate.of(2025, 1, 1);

    when(repository.findByProject_UserIdAndDueDateBetween(any(), any(), any()))
        .thenReturn(List.of(task(1L)));

    assertEquals(1, service.getByDueDate(date).size());
  }
}