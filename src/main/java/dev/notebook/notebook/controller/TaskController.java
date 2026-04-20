package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.TaskRequestDto;
import dev.notebook.notebook.dto.TaskResponseDto;
import dev.notebook.notebook.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@Validated
@Tag(name = "Tasks", description = "Operations for managing tasks")
public class TaskController {

  private final TaskService service;

  @GetMapping
  @Operation(
      summary = "Get tasks",
      description = "Returns all tasks or filters them by one query parameter"
  )
  public List<TaskResponseDto> getTasks(
      @Parameter(description = "Filter by task title fragment")
      @RequestParam(required = false) String title,
      @Parameter(description = "Filter by exact description")
      @RequestParam(required = false) String description,
      @Parameter(description = "Filter by due date in ISO format")
      @RequestParam(required = false) @DateTimeFormat(
          iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
      @Parameter(description = "Filter by completion status")
      @RequestParam(required = false) Boolean completed
  ) {

    if (title != null) {
      return service.getByTitleContaining(title);
    }
    if (dueDate != null) {
      return service.getByDueDate(dueDate);
    }
    if (completed != null) {
      return service.getByCompleted(completed);
    }
    if (description != null) {
      return service.getByDescription(description);
    }

    return service.getAll();
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get task by id",
      description = "Returns a single task by identifier"
  )
  public TaskResponseDto getById(
      @Parameter(description = "Task identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id
  ) {
    return service.getById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create task", description = "Creates a new task")
  public TaskResponseDto create(@Valid @RequestBody TaskRequestDto requestDto) {
    return service.create(requestDto);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update task", description = "Updates an existing task")
  public TaskResponseDto update(
      @Parameter(description = "Task identifier")
      @PathVariable @Positive(message = "Id must be positive") Long id,
      @Valid @RequestBody TaskRequestDto dto
  ) {
    return service.update(id, dto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete task", description = "Deletes a task by identifier")
  public void delete(@PathVariable @Positive(message = "Id must be positive") Long id) {
    service.delete(id);
  }
}
