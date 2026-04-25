package dev.notebook.notebook.util;

import dev.notebook.notebook.entity.AsyncTaskStatus;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncTaskExecutor {

  private final AsyncTaskStorage asyncTaskStorage;

  @Async("asyncExecutorPool")
  public void executeTask(String taskId) {
    log.info("Async task {} started", taskId);
    try {
      asyncTaskStorage.updateStatus(taskId, AsyncTaskStatus.IN_PROGRESS);
      Thread.sleep(10000);
      asyncTaskStorage.updateStatus(taskId, AsyncTaskStatus.DONE);
      log.info("Async task {} completed", taskId);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      asyncTaskStorage.updateStatus(taskId, AsyncTaskStatus.FAILED);
      log.error("Async task {} was interrupted", taskId, exception);
    } catch (RuntimeException exception) {
      asyncTaskStorage.updateStatus(taskId, AsyncTaskStatus.FAILED);
      log.error("Async task {} failed", taskId, exception);
    }
    CompletableFuture.completedFuture(null);
  }
}
