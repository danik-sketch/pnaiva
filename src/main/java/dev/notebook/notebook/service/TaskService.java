package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.TaskRequestDto;
import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.mapper.TaskMapper;
import dev.notebook.notebook.repository.TaskRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {

  private final TaskRepository repository;

  public TaskService(TaskRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public TaskResponseDto create(TaskRequestDto dto) {
    Task task = new Task();
    task.setTitle(dto.title());
    task.setDescription(dto.description());
    task.setDueDate(dto.dueDate());
    task.setCompleted(dto.completed());

    Task savedTask = repository.save(task);
    return TaskMapper.toDto(savedTask);
  }

  @Transactional
  public TaskResponseDto update(Long id, TaskRequestDto dto) {
    Task task = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    task.setTitle(dto.title());
    task.setDescription(dto.description());
    task.setDueDate(dto.dueDate());
    task.setCompleted(dto.completed());

    return TaskMapper.toDto(repository.save(task));
  }

  @Transactional
  public void delete(Long id) {
    repository.deleteById(id);
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
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));
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
