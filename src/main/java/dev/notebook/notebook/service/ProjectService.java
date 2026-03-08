package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.mapper.ProjectMapper;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.TaskRepository;
import dev.notebook.notebook.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

  private static final String USER_NOT_FOUND_MESSAGE = "User not found";

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final TaskRepository taskRepository;

  @Transactional
  public ProjectResponseDto create(ProjectRequestDto dto) {
    User user = userRepository.findById(dto.userId())
        .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE));

    Project project = new Project();
    project.setName(dto.name());
    project.setDescription(dto.description());
    project.setUser(user);

    Project saved = projectRepository.save(project);
    return ProjectMapper.toDto(saved);
  }

  @Transactional
  public ProjectResponseDto update(Long id, ProjectRequestDto dto) {
    Project project = projectRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Project not found"));

    project.setName(dto.name());
    project.setDescription(dto.description());

    if (!project.getUser().getId().equals(dto.userId())) {
      User user = userRepository.findById(dto.userId())
          .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE));
      project.setUser(user);
    }

    Project saved = projectRepository.save(project);
    return ProjectMapper.toDto(saved);
  }

  @Transactional
  public void delete(Long id) {
    projectRepository.deleteById(id);
  }

  public ProjectResponseDto getById(Long id) {
    Project project = projectRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    return ProjectMapper.toDto(project);
  }

  public List<ProjectResponseDto> getAll() {
    List<Project> projects = projectRepository.findAll();
    List<ProjectResponseDto> result = new ArrayList<>();
    for (Project project : projects) {
      result.add(ProjectMapper.toDto(project));
    }
    return result;
  }

  public List<ProjectResponseDto> getAllOptimized() {
    List<Project> projects = projectRepository.findAllWithUserAndTasks();
    List<ProjectResponseDto> result = new ArrayList<>();
    for (Project project : projects) {
      result.add(ProjectMapper.toDto(project));
    }
    return result;
  }

  public void createProjectWithTasksNonTransactional(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE));

    Project project = new Project();
    project.setName("Non-Transactional Project");
    project.setDescription("Partial save demo");
    project.setUser(user);
    project = projectRepository.save(project);

    Task firstTask = new Task();
    firstTask.setTitle("First task");
    firstTask.setDescription("Will be persisted before failure");
    firstTask.setDueDate(LocalDateTime.now().plusDays(1));
    firstTask.setCompleted(false);
    firstTask.setProject(project);
    taskRepository.save(firstTask);

    throw new IllegalStateException("Intentional failure without @Transactional");
  }

  @Transactional
  public void createProjectWithTasksTransactional(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE));

    Project project = new Project();
    project.setName("Transactional Project");
    project.setDescription("Rollback demo");
    project.setUser(user);
    project = projectRepository.save(project);

    Task firstTask = new Task();
    firstTask.setTitle("First transactional task");
    firstTask.setDescription("Will be rolled back");
    firstTask.setDueDate(LocalDateTime.now().plusDays(1));
    firstTask.setCompleted(false);
    firstTask.setProject(project);
    taskRepository.save(firstTask);

    Task secondTask = new Task();
    secondTask.setTitle("Second transactional task");
    secondTask.setDescription("Will be rolled back too");
    secondTask.setDueDate(LocalDateTime.now().plusDays(2));
    secondTask.setCompleted(false);
    secondTask.setProject(project);
    taskRepository.save(secondTask);

    throw new IllegalStateException("Intentional failure with @Transactional");
  }
}
