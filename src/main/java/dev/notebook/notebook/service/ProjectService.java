package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.ProjectRequestDto;
import dev.notebook.notebook.dto.ProjectResponseDto;
import dev.notebook.notebook.dto.TaskRequestDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.entity.User;
import dev.notebook.notebook.mapper.ProjectMapper;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.TaskRepository;
import dev.notebook.notebook.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

  private static final String USER_NOT_FOUND = "User not found";

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final TaskRepository taskRepository;

  @Transactional
  public ProjectResponseDto create(ProjectRequestDto dto) {
    User user = userRepository.findById(dto.userId())
        .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

    Project project = new Project();
    project.setName(dto.name());
    project.setDescription(dto.description());
    project.setUser(user);

    if (dto.tasks() != null && !dto.tasks().isEmpty()) {
      for (TaskRequestDto taskDto : dto.tasks()) {
        Task task = new Task();
        task.setTitle(taskDto.title());
        task.setDescription(taskDto.description());
        task.setDueDate(taskDto.dueDate());
        task.setCompleted(taskDto.completed());
        task.setProject(project);
        project.getTasks().add(task);
      }
    }

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
          .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
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




  public void createNonTransactional(ProjectRequestDto dto) {
    saveWithFailure(dto);
  }

  @Transactional
  public void createTransactional(ProjectRequestDto dto) {
    saveWithFailure(dto);
  }

  private void saveWithFailure(ProjectRequestDto dto) {
    User user = userRepository.findById(dto.userId())
        .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

    Project project = new Project();
    project.setName(dto.name());
    project.setDescription(dto.description());
    project.setUser(user);

    project = projectRepository.save(project);

    int index = 0;
    for (TaskRequestDto taskDto : dto.tasks()) {
      Task task = new Task();
      task.setTitle(taskDto.title());
      task.setDescription(taskDto.description());
      task.setDueDate(taskDto.dueDate());
      task.setCompleted(taskDto.completed());
      task.setProject(project);

      taskRepository.save(task);

      if (index == 0) {
        throw new IllegalStateException("Intentional failure after first task");
      }
      index++;
    }
  }
}
