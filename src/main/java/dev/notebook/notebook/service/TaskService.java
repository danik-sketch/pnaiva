package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.mapper.TaskMapper;
import dev.notebook.notebook.repository.TaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final TaskRepository repository;

  public TaskService(TaskRepository repository) {
    this.repository = repository;
  }

  public TaskResponseDto create(Task task) {
    Task savedTask = repository.save(task);
    return TaskMapper.toDto(savedTask);
  }

  public TaskResponseDto update(Long id, Task taskDetails) {
    Task task = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    task.setTitle(taskDetails.getTitle());
    task.setDescription(taskDetails.getDescription());
    task.setDueDate(taskDetails.getDueDate());
    task.setCompleted(taskDetails.isCompleted());

    return TaskMapper.toDto(repository.save(task));
  }

  public void delete(Long id) {
    repository.deleteById(id);
  }

  public List<TaskResponseDto> getAll() {
    List<Task> tasks = repository.findAll();
    List<TaskResponseDto> dtos = new ArrayList<>();
    for (Task task : tasks) {
      dtos.add(TaskMapper.toDto(task));
    }
    return dtos;
  }

  public TaskResponseDto getById(Long id) {
    Task task = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    return TaskMapper.toDto(task);
  }

  public List<TaskResponseDto> getByTitleContaining(String title) {
    List<Task> tasks = repository.findByTitleContaining(title);
    List<TaskResponseDto> dtos = new ArrayList<>();
    for (Task task : tasks) {
      dtos.add(TaskMapper.toDto(task));
    }
    return dtos;
  }

  public List<TaskResponseDto> getByDescription(String description) {
    List<Task> tasks = repository.findByDescription(description);
    List<TaskResponseDto> dtos = new ArrayList<>();
    for (Task task : tasks) {
      dtos.add(TaskMapper.toDto(task));
    }
    return dtos;
  }

  public List<TaskResponseDto> getByCompleted(boolean completed) {
    List<Task> tasks = repository.findByCompleted(completed);
    List<TaskResponseDto> dtos = new ArrayList<>();
    for (Task task : tasks) {
      dtos.add(TaskMapper.toDto(task));
    }
    return dtos;
  }

  public List<TaskResponseDto> getByDueDate(LocalDate dueDate) {
    List<Task> tasks = repository.findByDueDate(dueDate.atStartOfDay());
    List<TaskResponseDto> dtos = new ArrayList<>();
    for (Task task : tasks) {
      dtos.add(TaskMapper.toDto(task));
    }
    return dtos;
  }
}