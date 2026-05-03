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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock
  private TaskRepository repository;
  @Mock
  private ProjectRepository projectRepository;
  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private TaskService service;

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
    t.setCategories(new HashSet<>());
    return t;
  }

  @Test
  void create_success() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    TaskResponseDto result = service.create(dto(1L));

    assertNotNull(result);
    verify(repository).save(any());
  }

  @Test
  void create_noProjectId() {
    TaskRequestDto dto = dto(null);
    assertThrows(IllegalArgumentException.class, () -> service.create(dto));
  }

  @Test
  void create_projectNotFound() {
    when(projectRepository.findById(1L)).thenReturn(Optional.empty());
    TaskRequestDto dto = dto(1L);

    assertThrows(NotFoundException.class, () -> service.create(dto));
  }

  @Test
  void create_notOwner() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(2L)));
    TaskRequestDto dto = dto(1L);

    assertThrows(OperationFailedException.class, () -> service.create(dto));
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

    assertThrows(NotFoundException.class, () -> service.create(dto));
  }

  @Test
  void create_withCategories_success() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(categoryRepository.findAllById(List.of(1L)))
        .thenReturn(List.of(new Category()));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

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
    TaskRequestDto dto = dto(1L);

    assertNotNull(service.update(1L, dto));
  }

  @Test
  void update_notFound() {
    when(repository.findById(1L)).thenReturn(Optional.empty());
    TaskRequestDto dto = dto(1L);

    assertThrows(NotFoundException.class, () -> service.update(1L, dto));
  }

  @Test
  void update_notOwner() {
    when(repository.findById(1L)).thenReturn(Optional.of(task(2L)));
    TaskRequestDto dto = dto(1L);

    assertThrows(OperationFailedException.class, () -> service.update(1L, dto));
  }

  @Test
  void delete_success() {
    when(repository.findById(1L)).thenReturn(Optional.of(task(1L)));

    service.delete(1L);

    verify(repository).deleteById(1L);
  }

  @Test
  void getById_success() {
    when(repository.findById(1L)).thenReturn(Optional.of(task(1L)));

    assertNotNull(service.getById(1L));
  }

  @Test
  void getAll_auth() {
    when(repository.findByProject_UserId(1L))
        .thenReturn(List.of(task(1L)));

    assertEquals(1, service.getAll().size());
  }

  @Test
  void create_repositoryThrowsException() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project(1L)));
    when(repository.save(any())).thenThrow(new RuntimeException("DB error"));
    TaskRequestDto dto = dto(1L);

    assertThrows(OperationFailedException.class, () -> service.create(dto));
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
        "t", "d", LocalDateTime.now(), null, 2L, List.of(), List.of()
    );

    assertNotNull(service.update(1L, dto));
    verify(projectRepository).findById(2L);
  }

  @Test
  void getByDueDate_exactRange() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    when(repository.findByProject_UserIdAndDueDateBetween(any(), any(), any()))
        .thenReturn(List.of(task(1L)));

    assertEquals(1, service.getByDueDate(date).size());
  }
}