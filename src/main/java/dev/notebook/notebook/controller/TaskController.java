package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.entity.Task;
import dev.notebook.notebook.service.TaskService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
  private final TaskService service;

  @GetMapping
  public List<TaskResponseDto> getTasks(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
      @RequestParam(required = false) Boolean completed) {

    if (title != null) return service.getByTitleContaining(title);
    if (dueDate != null) return service.getByDueDate(
        LocalDate.from(LocalDate.from(dueDate).atStartOfDay()));
    if (completed != null) return service.getByCompleted(completed);
    if (description != null) return service.getByDescription(description);

    return service.getAll();
  }

  @GetMapping("/{id}")
  public TaskResponseDto getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TaskResponseDto create(@RequestBody Task task) {
    return service.create(task);
  }

  @PutMapping("/{id}")
  public TaskResponseDto update(@PathVariable Long id, @RequestBody Task task) {
    return service.update(id, task);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}