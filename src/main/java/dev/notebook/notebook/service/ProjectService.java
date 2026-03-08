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
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final TaskRepository taskRepository;

  public ProjectResponseDto create(ProjectRequestDto dto) {
    User user = userRepository.findById(dto.userId())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Project project = new Project();
    project.setName(dto.name());
    project.setDescription(dto.description());
    project.setUser(user);

    Project saved = projectRepository.save(project);
    return ProjectMapper.toDto(saved);
  }

  public ProjectResponseDto update(Long id, ProjectRequestDto dto) {
    Project project = projectRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Project not found"));

    project.setName(dto.name());
    project.setDescription(dto.description());

    if (!project.getUser().getId().equals(dto.userId())) {
      User user = userRepository.findById(dto.userId())
          .orElseThrow(() -> new IllegalArgumentException("User not found"));
      project.setUser(user);
    }

    Project saved = projectRepository.save(project);
    return ProjectMapper.toDto(saved);
  }

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

  /**
   * Метод, демонстрирующий частичное сохранение без @Transactional.
   * После создания проекта и первой задачи выбрасывается исключение,
   * данные частично останутся в БД.
   */
  public void createProjectWithTasksNonTransactional(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Project project = new Project();
    project.setName("Non-Transactional Project");
    project.setDescription("Частичное сохранение без @Transactional");
    project.setUser(user);
    project = projectRepository.save(project);

    Task firstTask = new Task();
    firstTask.setTitle("First task");
    firstTask.setDescription("Создана до ошибки");
    firstTask.setDueDate(LocalDateTime.now().plusDays(1));
    firstTask.setCompleted(false);
    firstTask.setProject(project);
    taskRepository.save(firstTask);

    // Искусственно вызываем ошибку после части сохранений
    if (true) {
      throw new RuntimeException("Искусственная ошибка без @Transactional");
    }

    // Вторая задача никогда не будет сохранена
  }

  /**
   * Метод, демонстрирующий полное откатывание с @Transactional.
   * При возникновении исключения ни проект, ни задачи не будут сохранены.
   */
  @Transactional
  public void createProjectWithTasksTransactional(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Project project = new Project();
    project.setName("Transactional Project");
    project.setDescription("Полное откатывание с @Transactional");
    project.setUser(user);
    project = projectRepository.save(project);

    Task firstTask = new Task();
    firstTask.setTitle("First transactional task");
    firstTask.setDescription("Будет откатена при ошибке");
    firstTask.setDueDate(LocalDateTime.now().plusDays(1));
    firstTask.setCompleted(false);
    firstTask.setProject(project);
    taskRepository.save(firstTask);

    Task secondTask = new Task();
    secondTask.setTitle("Second transactional task");
    secondTask.setDescription("Тоже будет откатена");
    secondTask.setDueDate(LocalDateTime.now().plusDays(2));
    secondTask.setCompleted(false);
    secondTask.setProject(project);
    taskRepository.save(secondTask);

    // Искусственно вызываем ошибку — вся транзакция откатывается
    if (true) {
      throw new RuntimeException("Искусственная ошибка с @Transactional");
    }
  }

}
