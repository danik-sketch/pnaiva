package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.TaskRequestDto;
import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.mapper.TaskMapper;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.TaskRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {

  private final TaskRepository repository;
  private final ProjectRepository projectRepository;

  public TaskService(TaskRepository repository, ProjectRepository projectRepository) {
    this.repository = repository;
    this.projectRepository = projectRepository;
  }

  @Transactional
  public TaskResponseDto create(TaskRequestDto dto) {
    if (dto.projectId() == null) {
      throw new IllegalArgumentException("Project id is required");
    }

    Project project = projectRepository.findById(dto.projectId())
        .orElseThrow(() -> new NotFoundException("Project not found"));

    try {
      Task task = new Task();
      task.setTitle(dto.title());
      task.setDescription(dto.description());
      task.setDueDate(dto.dueDate());
      task.setCompleted(dto.completed());
      task.setProject(project);

      Task savedTask = repository.save(task);
      return TaskMapper.toDto(savedTask);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to create task", exception);
    }
  }

  @Transactional
  public TaskResponseDto update(Long id, TaskRequestDto dto) {
    Task task = repository.findById(id)
        .orElseThrow(() -> new NotFoundException("Task not found"));

    try {
      if (dto.projectId() != null && (task.getProject() == null
          || !task.getProject().getId().equals(dto.projectId()))) {
        Project project = projectRepository.findById(dto.projectId())
            .orElseThrow(() -> new NotFoundException("Project not found"));
        task.setProject(project);
      }

      task.setTitle(dto.title());
      task.setDescription(dto.description());
      task.setDueDate(dto.dueDate());
      task.setCompleted(dto.completed());

      return TaskMapper.toDto(repository.save(task));
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to update task", exception);
    }
  }

  @Transactional
  public void delete(Long id) {
    try {
      repository.deleteById(id);
    } catch (EmptyResultDataAccessException e) {
      throw new NotFoundException("Task not found");
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to delete task", exception);
    }
  }

  public List<TaskResponseDto> getAll() {
    List<Task> tasks = repository.findAll();
    List<TaskResponseDto> result = new ArrayList<>();
    for (Task task : tasks) {
      result.add(TaskMapper.toDto(task));
    }
    return result;
  }

  public TaskResponseDto getById(Long id) {
    Task task = repository.findById(id)
        .orElseThrow(() -> new NotFoundException("Task not found"));
    return TaskMapper.toDto(task);
  }

  public List<TaskResponseDto> getByTitleContaining(String title) {
    List<Task> tasks = repository.findByTitleContaining(title);
    List<TaskResponseDto> result = new ArrayList<>();
    for (Task task : tasks) {
      result.add(TaskMapper.toDto(task));
    }
    return result;
  }

  public List<TaskResponseDto> getByDescription(String description) {
    List<Task> tasks = repository.findByDescription(description);
    List<TaskResponseDto> result = new ArrayList<>();
    for (Task task : tasks) {
      result.add(TaskMapper.toDto(task));
    }
    return result;
  }

  public List<TaskResponseDto> getByCompleted(boolean completed) {
    List<Task> tasks = completed
        ? repository.findByCompletedIsNotNull()
        : repository.findByCompletedIsNull();
    List<TaskResponseDto> result = new ArrayList<>();
    for (Task task : tasks) {
      result.add(TaskMapper.toDto(task));
    }
    return result;
  }

  public List<TaskResponseDto> getByDueDate(LocalDate dueDate) {
    List<Task> tasks = repository.findByDueDateBetween(
        dueDate.atStartOfDay(),
        dueDate.plusDays(1).atStartOfDay().minusNanos(1));
    List<TaskResponseDto> result = new ArrayList<>();
    for (Task task : tasks) {
      result.add(TaskMapper.toDto(task));
    }
    return result;
  }
}
