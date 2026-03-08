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
@Transactional
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

  private void saveProjectAndTasks(Long userId, String projectName, String projectDesc, int taskCount) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MESSAGE));

    Project project = new Project();
    project.setName(projectName);
    project.setDescription(projectDesc);
    project.setUser(user);
    project = projectRepository.save(project);

    for (int i = 1; i <= taskCount; i++) {
      Task task = new Task();
      task.setTitle(projectName + " - Task " + i);
      task.setDescription("Task part of " + projectDesc);
      task.setDueDate(LocalDateTime.now().plusDays(i));
      task.setCompleted(false);
      task.setProject(project);
      taskRepository.save(task);
    }

    throw new IllegalStateException("Intentional failure for: " + projectName);
  }

  public void createProjectWithTasksNonTransactional(Long userId) {
    saveProjectAndTasks(userId, "Non-Transactional Project", "Partial save demo", 1);
  }

  @Transactional
  public void createProjectWithTasksTransactional(Long userId) {
    saveProjectAndTasks(userId, "Transactional Project", "Rollback demo", 2);
  }
}
