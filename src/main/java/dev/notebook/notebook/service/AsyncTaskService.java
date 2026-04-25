package dev.notebook.notebook.service;

import dev.notebook.notebook.dto.AsyncTaskResponseDto;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.mapper.AsyncTaskMapper;
import dev.notebook.notebook.util.AsyncTaskExecutor;
import dev.notebook.notebook.util.AsyncTaskStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncTaskService {

  private final AsyncTaskStorage asyncTaskStorage;
  private final AsyncTaskExecutor asyncTaskExecutor;

  public String startTask() {
    String taskId = asyncTaskStorage.createTask();
    asyncTaskExecutor.executeTask(taskId);
    return taskId;
  }

  public AsyncTaskResponseDto getTaskStatus(String taskId) {
    return asyncTaskStorage.getTask(taskId)
        .map(AsyncTaskMapper::toDto)
        .orElseThrow(() -> new NotFoundException("Task not found"));
  }
}
