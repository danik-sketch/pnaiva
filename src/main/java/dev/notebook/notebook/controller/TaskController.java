package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.service.TaskService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService service;

  public TaskController(TaskService service) {
    this.service = service;
  }

  @GetMapping("/{id}")
  public TaskResponseDto getTaskById(@PathVariable Long id) {
    return service.getById(id);
  }

  @GetMapping
  public List<TaskResponseDto> getTasks(@RequestParam(required = false) Boolean completed,
      @RequestParam(required = false) String dueDate) {

    if (completed != null) {
      return service.getByCompleted(completed);
    } else if (dueDate != null) {
      return service.getByDueDate(LocalDate.parse(dueDate));
    }

    throw new IllegalArgumentException("Provide completed or dueDate parameter");
  }
}