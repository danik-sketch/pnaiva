package dev.notebook.notebook.controller;

import dev.notebook.notebook.dto.AsyncTaskResponseDto;
import dev.notebook.notebook.service.AsyncTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/async-tasks")
@RequiredArgsConstructor
@Tag(name = "Async Tasks", description = "Operations for asynchronous background jobs")
public class AsyncTaskController {

  private final AsyncTaskService asyncTaskService;

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(
      summary = "Start async task",
      description = "Starts a background task and returns task id"
  )
  public String startTask() {
    return asyncTaskService.startTask();
  }

  @GetMapping("/{taskId}")
  @Operation(summary = "Get task status", description = "Returns current status for async task")
  public AsyncTaskResponseDto getTaskStatus(
      @Parameter(description = "Async task identifier") @PathVariable String taskId
  ) {
    return asyncTaskService.getTaskStatus(taskId);
  }
}
