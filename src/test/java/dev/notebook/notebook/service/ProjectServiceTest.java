package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
}