package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.mapper.TaskMapper;
import dev.notebook.notebook.repository.TaskRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final TaskRepository repository;

  public TaskService(TaskRepository repository) {
    this.repository = repository;
  }

  public TaskResponseDto getById(Long id) {
    Task task = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    return TaskMapper.toDto(task);
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
    List<Task> tasks = repository.findByDueDate(dueDate);
    List<TaskResponseDto> dtos = new ArrayList<>();
    for (Task task : tasks) {
      dtos.add(TaskMapper.toDto(task));
    }
    return dtos;
  }
}