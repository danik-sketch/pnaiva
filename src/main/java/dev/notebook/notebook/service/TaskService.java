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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor // Автоматически создает конструктор для всех final полей
@Transactional
public class TaskService {

  private final TaskRepository repository;
  private final ProjectRepository projectRepository;
  private final CategoryRepository categoryRepository;

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
      log.info("Task created with id: {}", savedTask.getId());
      return TaskMapper.toDto(savedTask);
    } catch (Exception exception) { // Sonar может ругаться, но здесь это обертка над логикой
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
      log.info("Task updated with id: {}", saved.getId());
      return TaskMapper.toDto(saved);
    } catch (Exception exception) {
      throw new OperationFailedException("Failed to update task", exception);
    }
  }

  @Transactional
   public void delete(Long id) {
     try {
       repository.deleteById(id);
       log.info("Task deleted with id: {}", id);
     } catch (EmptyResultDataAccessException e) {
       throw new NotFoundException("Task not found");
     } catch (Exception e) {
       throw new OperationFailedException("Failed to delete task", e);
     }
   }

  @Transactional(readOnly = true)
  public List<TaskResponseDto> getAll() {
    Long userId = getCurrentUserId();
    List<Task> tasks = (userId != null)
        ? repository.findByProject_UserId(userId)
        : repository.findAll();

    return tasks.stream().map(TaskMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public TaskResponseDto getById(Long id) {
    return repository.findById(id)
        .map(TaskMapper::toDto)
        .orElseThrow(() -> new NotFoundException("Task not found"));
  }

  @Transactional(readOnly = true)
  public List<TaskResponseDto> getByTitleContaining(String title) {
    Long userId = getCurrentUserId();
    List<Task> tasks = (userId != null)
        ? repository.findByProject_UserIdAndTitleContaining(userId, title)
        : repository.findByTitleContaining(title);

    return tasks.stream().map(TaskMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<TaskResponseDto> getByDescription(String description) {
    Long userId = getCurrentUserId();
    List<Task> tasks = (userId != null)
        ? repository.findByProject_UserIdAndDescription(userId, description)
        : repository.findByDescription(description);

    return tasks.stream().map(TaskMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<TaskResponseDto> getByCompleted(boolean completed) {
    Long userId = getCurrentUserId();
    List<Task> tasks;
    if (userId != null) {
      tasks = completed
          ? repository.findByProject_UserIdAndCompletedIsNotNull(userId)
          : repository.findByProject_UserIdAndCompletedIsNull(userId);
    } else {
      tasks = completed
          ? repository.findByCompletedIsNotNull()
          : repository.findByCompletedIsNull();
    }
    return tasks.stream().map(TaskMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<TaskResponseDto> getByDueDate(LocalDate dueDate) {
    Long userId = getCurrentUserId();
    var start = dueDate.atStartOfDay();
    var end = dueDate.plusDays(1).atStartOfDay().minusNanos(1);

    List<Task> tasks = (userId != null)
        ? repository.findByProject_UserIdAndDueDateBetween(userId, start, end)
        : repository.findByDueDateBetween(start, end);

    return tasks.stream().map(TaskMapper::toDto).toList();
  }

  private void applyCategories(Task task, List<Long> categoryIds) {
    if (task.getCategories() == null) {
      task.setCategories(new HashSet<>());
    }

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
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(Authentication::getPrincipal)
        .map(principal -> {
          if (principal instanceof Long id) return id;
          try {
            return Long.valueOf(principal.toString());
          } catch (NumberFormatException e) {
            log.warn("Failed to parse principal to Long: {}", principal);
            return null;
          }
        })
        .orElse(null);
  }
}