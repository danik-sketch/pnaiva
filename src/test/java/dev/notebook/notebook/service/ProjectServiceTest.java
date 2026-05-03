package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ProjectService service;

  @BeforeEach
  void setupSecurity() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(1L, null)
    );
  }

  private ProjectRequestDto dto() {
    return new ProjectRequestDto("name", "desc", List.of());
  }

  private User user() {
    User u = new User();
    u.setId(1L);
    return u;
  }

  private Project project() {
    Project p = new Project();
    p.setId(1L);
    p.setUser(user());
    return p;
  }

  @Test
  void create_success() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
    when(projectRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    ProjectResponseDto result = service.create(dto());

    assertNotNull(result);
    verify(projectRepository).save(any());
  }

  @Test
  void create_noAuth() {
    SecurityContextHolder.clearContext();

    assertThrows(OperationFailedException.class,
        () -> service.create(dto()));
  }

  @Test
  void create_userNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> service.create(dto()));
  }

  @Test
  void update_success() {
    Project p = project();

    when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
    when(projectRepository.save(any())).thenReturn(p);

    ProjectResponseDto result = service.update(1L, dto());

    assertNotNull(result);
    verify(projectRepository).save(p);
  }

  @Test
  void update_notOwner() {
    Project p = project();
    p.getUser().setId(2L);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

    assertThrows(OperationFailedException.class,
        () -> service.update(1L, dto()));
  }

  @Test
  void delete_success() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project()));

    service.delete(1L);

    verify(projectRepository).deleteById(1L);
  }

  @Test
  void delete_notOwner() {
    Project p = project();
    p.getUser().setId(2L);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

    assertThrows(OperationFailedException.class,
        () -> service.delete(1L));
  }

  @Test
  void getById_success() {
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project()));

    ProjectResponseDto result = service.getById(1L);

    assertNotNull(result);
  }

  @Test
  void getById_notOwner() {
    Project p = project();
    p.getUser().setId(2L);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

    assertThrows(NotFoundException.class,
        () -> service.getById(1L));
  }

  @Test
  void getAll_authenticated() {
    when(projectRepository.findAllByUserId(1L))
        .thenReturn(List.of(project()));

    List<ProjectResponseDto> result = service.getAll();

    assertEquals(1, result.size());
  }

  @Test
  void getAll_noAuth() {
    SecurityContextHolder.clearContext();

    when(projectRepository.findAll())
        .thenReturn(List.of(project()));

    List<ProjectResponseDto> result = service.getAll();

    assertEquals(1, result.size());
  }

  @Test
  void search_cacheWorks() {
    Pageable pageable = PageRequest.of(0, 10);

    LocalDateTime from = LocalDateTime.now();
    LocalDateTime to = LocalDateTime.now();

    Page<Project> page = new PageImpl<>(List.of(project()));

    when(projectRepository.searchByTaskJpql(
        any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(page);

    Page<ProjectResponseDto> first =
        service.searchByTaskJpql("p", "t", true, from, to, pageable);

    Page<ProjectResponseDto> second =
        service.searchByTaskJpql("p", "t", true, from, to, pageable);

    assertEquals(first.getTotalElements(), second.getTotalElements());

    verify(projectRepository, times(1))
        .searchByTaskJpql(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void createBulk_callsCreate() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
    when(projectRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    List<ProjectResponseDto> result =
        service.createBulk(List.of(dto(), dto()));

    assertEquals(2, result.size());
  }
}