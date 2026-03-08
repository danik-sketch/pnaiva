package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.TaskRequestDto;
import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.service.TaskService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService service;

  @GetMapping
  public List<TaskResponseDto> getTasks(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) @DateTimeFormat(
          iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
      @RequestParam(required = false) Boolean completed) {

    if (title != null) {
      return service.getByTitleContaining(title);
    }
    if (dueDate != null) {
      return service.getByDueDate(
          LocalDate.from(LocalDate.from(dueDate).atStartOfDay()));
    }
    if (completed != null) {
      return service.getByCompleted(completed);
    }
    if (description != null) {
      return service.getByDescription(description);
    }

    return service.getAll();
  }

  @GetMapping("/optimized")
  public List<TaskResponseDto> getTasksOptimized() {
    return service.getAllOptimized();
  }

  @GetMapping("/{id}")
  public TaskResponseDto getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TaskResponseDto create(@RequestBody TaskRequestDto requestDto) {
    return service.create(requestDto);
  }

  @PutMapping("/{id}")
  public TaskResponseDto update(@PathVariable Long id, @RequestBody TaskRequestDto dto) {
    return service.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}