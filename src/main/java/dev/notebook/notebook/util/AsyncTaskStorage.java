package dev.notebook.notebook.util;

import dev.notebook.notebook.entity.AsyncTask;
import dev.notebook.notebook.entity.AsyncTaskStatus;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class AsyncTaskStorage {

  private final Map<String, AsyncTask> tasks = new ConcurrentHashMap<>();

  public String createTask() {
    String taskId = UUID.randomUUID().toString();
    tasks.put(taskId, new AsyncTask(taskId));
    return taskId;
  }

  public Optional<AsyncTask> getTask(String taskId) {
    return Optional.ofNullable(tasks.get(taskId));
  }

  public void updateStatus(String taskId, AsyncTaskStatus status) {
    tasks.computeIfPresent(taskId, (id, task) -> {
      task.setStatus(status);
      task.setUpdatedAt(LocalDateTime.now());
      return task;
    });
  }
}
