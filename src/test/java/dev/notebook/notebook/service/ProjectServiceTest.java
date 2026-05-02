package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ProjectService projectService;

  private User testUser;
  private Project testProject;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("ivan_developer");

    testProject = new Project();
    testProject.setId(10L);
    testProject.setName("Test Project");
    testProject.setUser(testUser);
  }

  @Test
  void create_ShouldReturnResponseDto_WhenUserExists() {
    // GIVEN
    ProjectRequestDto requestDto = new ProjectRequestDto("New Project", "Desc", 1L, null);
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(projectRepository.save(any(Project.class))).thenReturn(testProject);

    // WHEN
    ProjectResponseDto result = projectService.create(requestDto);

    // THEN
    assertNotNull(result);
    // Исправлено: используем геттеры
    assertEquals("Test Project", result.getName());
    assertEquals("ivan_developer", result.getUsername());
    verify(projectRepository, times(1)).save(any(Project.class));
  }

  @Test
  void create_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
    // GIVEN
    ProjectRequestDto requestDto = new ProjectRequestDto("New Project", "Desc", 99L, null);
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // WHEN & THEN
    assertThrows(NotFoundException.class, () -> projectService.create(requestDto));
    verify(projectRepository, never()).save(any(Project.class));
  }

  @Test
  void getById_ShouldReturnDto_WhenProjectExists() {
    // GIVEN
    when(projectRepository.findById(10L)).thenReturn(Optional.of(testProject));

    // WHEN
    ProjectResponseDto result = projectService.getById(10L);

    // THEN
    // Исправлено: используем геттеры
    assertEquals(10L, result.getId());
    assertEquals("Test Project", result.getName());
  }

  @Test
  void getById_ShouldThrowNotFoundException_WhenProjectNotFound() {
    // GIVEN
    when(projectRepository.findById(10L)).thenReturn(Optional.empty());

    // WHEN & THEN
    assertThrows(NotFoundException.class, () -> projectService.getById(10L));
  }

  @Test
  void delete_ShouldInvokeRepositoryDelete() {
    // WHEN
    projectService.delete(10L);

    // THEN
    verify(projectRepository, times(1)).deleteById(10L);
  }

  @Test
  void update_ShouldReturnDto_WhenProjectExists() {
    // GIVEN
    ProjectRequestDto requestDto = new ProjectRequestDto("Updated", "Updated desc", 1L, null);
    Project updatedProject = new Project();
    updatedProject.setId(10L);
    updatedProject.setName("Updated");
    updatedProject.setUser(testUser);

    when(projectRepository.findById(10L)).thenReturn(Optional.of(testProject));
    when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);

    // WHEN
    ProjectResponseDto result = projectService.update(10L, requestDto);

    // THEN
    assertNotNull(result);
    assertEquals("Updated", result.getName());
    verify(projectRepository, times(1)).save(any(Project.class));
  }

  @Test
  void update_ShouldChangeUserWhenIdChanged() {
    // GIVEN
    User newUser = new User();
    newUser.setId(2L);
    newUser.setUsername("new_user");

    ProjectRequestDto requestDto = new ProjectRequestDto("Updated", "Updated desc", 2L, null);
    when(projectRepository.findById(10L)).thenReturn(Optional.of(testProject));
    when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
    when(projectRepository.save(any(Project.class))).thenReturn(testProject);

    // WHEN
    ProjectResponseDto result = projectService.update(10L, requestDto);

    // THEN
    assertNotNull(result);
    verify(userRepository, times(1)).findById(2L);
    verify(projectRepository, times(1)).save(any(Project.class));
  }

  @Test
  void update_ShouldThrowNotFoundException_WhenProjectNotFound() {
    // GIVEN
    ProjectRequestDto requestDto = new ProjectRequestDto("Updated", "Updated desc", 1L, null);
    when(projectRepository.findById(99L)).thenReturn(Optional.empty());

    // WHEN & THEN
    assertThrows(NotFoundException.class, () -> projectService.update(99L, requestDto));
  }

  @Test
  void update_ShouldThrowNotFoundException_WhenNewUserNotFound() {
    // GIVEN
    ProjectRequestDto requestDto = new ProjectRequestDto("Updated", "Updated desc", 99L, null);
    when(projectRepository.findById(10L)).thenReturn(Optional.of(testProject));
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // WHEN & THEN
    assertThrows(OperationFailedException.class, () -> projectService.update(10L, requestDto));
  }

  @Test
  void getAll_ShouldReturnAllProjects() {
    // GIVEN
    when(projectRepository.findAll()).thenReturn(List.of(testProject));

    // WHEN
    List<ProjectResponseDto> result = projectService.getAll();

    // THEN
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  void createBulk_ShouldCreateMultipleProjects() {
    // GIVEN
    ProjectRequestDto dto1 = new ProjectRequestDto("Project 1", "Desc 1", 1L, null);
    ProjectRequestDto dto2 = new ProjectRequestDto("Project 2", "Desc 2", 1L, null);

    User user = new User();
    user.setId(1L);

    Project savedProject1 = new Project();
    savedProject1.setId(10L);
    savedProject1.setName("Project 1");
    savedProject1.setUser(user);

    Project savedProject2 = new Project();
    savedProject2.setId(11L);
    savedProject2.setName("Project 2");
    savedProject2.setUser(user);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(projectRepository.save(any(Project.class)))
        .thenReturn(savedProject1)
        .thenReturn(savedProject2);

    // WHEN
    List<ProjectResponseDto> result = projectService.createBulk(List.of(dto1, dto2));

    // THEN
    assertEquals(2, result.size());
    verify(projectRepository, times(2)).save(any(Project.class));
  }
}