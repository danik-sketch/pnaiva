package dev.notebook.notebook.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsyncTask {

  private String id;
  private AsyncTaskStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public AsyncTask(String id) {
    this.id = id;
    this.status = AsyncTaskStatus.PENDING;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
  }
}
