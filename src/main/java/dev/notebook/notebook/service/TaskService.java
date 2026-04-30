package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.TaskRequestDto;
import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Category;
import dev.notebook.notebook.entity.Project;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.exception.OperationFailedException;
import dev.notebook.notebook.mapper.TaskMapper;
import dev.notebook.notebook.repository.CategoryRepository;
import dev.notebook.notebook.repository.ProjectRepository;
import dev.notebook.notebook.repository.TaskRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TaskService {

  private final TaskRepository repository;
  private final ProjectRepository projectRepository;
  private final CategoryRepository categoryRepository;

  public TaskService(
      TaskRepository repository,
      ProjectRepository projectRepository,
      CategoryRepository categoryRepository
  ) {
    this.repository = repository;
    this.projectRepository = projectRepository;
    this.categoryRepository = categoryRepository;
  }

  @Transactional
  public TaskResponseDto create(TaskRequestDto dto) {
    if (dto.projectId() == null) {
      throw new IllegalArgumentException("Project id is required");
    }

    Project project = projectRepository.findById(dto.projectId())
        .orElseThrow(() -> new NotFoundException("Project not found"));

    try {
      Task task = TaskMapper.toEntity(dto, project);
      applyCategories(task, dto.categoryIds());

      Task savedTask = repository.save(task);
      log.info("TaskService.create completed");
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
      applyCategories(task, dto.categoryIds());

      Task saved = repository.save(task);
      log.info("TaskService.update completed");
      return TaskMapper.toDto(saved);
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to update task", exception);
    }
  }

  @Transactional
  public void delete(Long id) {
    try {
      repository.deleteById(id);
      log.info("TaskService.delete completed");
    } catch (EmptyResultDataAccessException _) {
      throw new NotFoundException("Task not found");
    } catch (RuntimeException exception) {
      throw new OperationFailedException("Failed to delete task", exception);
    }
  }

  public List<TaskResponseDto> getAll() {
    Long currentUserId = getCurrentUserId();
    List<Task> tasks;
    if (currentUserId != null) {
      tasks = repository.findByProject_UserId(currentUserId);
      log.info("TaskService.getAll - returning tasks for userId: {}", currentUserId);
    } else {
      tasks = repository.findAll();
      log.info("TaskService.getAll - returning all tasks (no auth)");
    }
    List<TaskResponseDto> result = new ArrayList<>();
    for (Task task : tasks) {
      result.add(TaskMapper.toDto(task));
    }
    log.info("TaskService.getAll completed");
    return result;
  }

  public TaskResponseDto getById(Long id) {
    Task task = repository.findById(id)
        .orElseThrow(() -> {
          return new NotFoundException("Task not found");
        });
    log.info("TaskService.getById completed");
    return TaskMapper.toDto(task);
  }

  public List<TaskResponseDto> getByTitleContaining(String title) {
    Long currentUserId = getCurrentUserId();
    List<Task> tasks;
    if (currentUserId != null) {
      tasks = repository.findByProject_UserIdAndTitleContaining(currentUserId, title);
    } else {
      tasks = repository.findByTitleContaining(title);
    }
    List<TaskResponseDto> result = new ArrayList<>();
    for (Task task : tasks) {
      result.add(TaskMapper.toDto(task));
    }
    log.info("TaskService.getByTitleContaining completed");
    return result;
  }

  public List<TaskResponseDto> getByDescription(String description) {
    Long currentUserId = getCurrentUserId();
    List<Task> tasks;
    if (currentUserId != null) {
      tasks = repository.findByProject_UserIdAndDescription(currentUserId, description);
    } else {
      tasks = repository.findByDescription(description);
    }
    List<TaskResponseDto> result = new ArrayList<>();
    for (Task task : tasks) {
      result.add(TaskMapper.toDto(task));
    }
    log.info("TaskService.getByDescription completed");
    return result;
  }

  public List<TaskResponseDto> getByCompleted(boolean completed) {
    Long currentUserId = getCurrentUserId();
    List<Task> tasks;
    if (currentUserId != null) {
      tasks = completed
          ? repository.findByProject_UserIdAndCompletedIsNotNull(currentUserId)
          : repository.findByProject_UserIdAndCompletedIsNull(currentUserId);
    } else {
      tasks = completed
          ? repository.findByCompletedIsNotNull()
          : repository.findByCompletedIsNull();
    }
    List<TaskResponseDto> result = new ArrayList<>();
    for (Task task : tasks) {
      result.add(TaskMapper.toDto(task));
    }
    log.info("TaskService.getByCompleted completed");
    return result;
  }

  public List<TaskResponseDto> getByDueDate(LocalDate dueDate) {
    Long currentUserId = getCurrentUserId();
    List<Task> tasks;
    if (currentUserId != null) {
      tasks = repository.findByProject_UserIdAndDueDateBetween(
          currentUserId,
          dueDate.atStartOfDay(),
          dueDate.plusDays(1).atStartOfDay().minusNanos(1));
    } else {
      tasks = repository.findByDueDateBetween(
          dueDate.atStartOfDay(),
          dueDate.plusDays(1).atStartOfDay().minusNanos(1));
    }
    List<TaskResponseDto> result = new ArrayList<>();
    for (Task task : tasks) {
      result.add(TaskMapper.toDto(task));
    }
    log.info("TaskService.getByDueDate completed");
    return result;
  }

  private void applyCategories(Task task, List<Long> categoryIds) {
    task.getCategories().clear();
    if (categoryIds == null || categoryIds.isEmpty()) {
      return;
    }
    List<Category> categories = categoryRepository.findAllById(categoryIds);
    if (categories.size() != categoryIds.size()) {
      throw new NotFoundException("One or more categories not found");
    }
    task.getCategories().addAll(categories);
  }

  private Long getCurrentUserId() {
    try {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (principal instanceof Long id) {
        return id;
      }
      return Long.valueOf(principal.toString());
    } catch (Exception e) {
      log.warn("Could not get currentUserId from SecurityContext: {}", e.getMessage());
      return null;
    }
  }
}
