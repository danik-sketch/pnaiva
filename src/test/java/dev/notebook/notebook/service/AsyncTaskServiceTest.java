package dev.notebook.notebook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.notebook.notebook.dto.AsyncTaskResponseDto;
import dev.notebook.notebook.entity.AsyncTask;
import dev.notebook.notebook.entity.AsyncTaskStatus;
import dev.notebook.notebook.exception.NotFoundException;
import dev.notebook.notebook.util.AsyncTaskExecutor;
import dev.notebook.notebook.util.AsyncTaskStorage;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AsyncTaskServiceTest {

  @Mock
  private AsyncTaskStorage asyncTaskStorage;

  @Mock
  private AsyncTaskExecutor asyncTaskExecutor;

  @InjectMocks
  private AsyncTaskService asyncTaskService;

  @Test
  void startTaskShouldCreateAndExecuteTask() {
    when(asyncTaskStorage.createTask()).thenReturn("task-1");

    String taskId = asyncTaskService.startTask();

    assertThat(taskId).isEqualTo("task-1");
    verify(asyncTaskStorage).createTask();
    verify(asyncTaskExecutor).executeTask("task-1");
  }

  @Test
  void getTaskStatusShouldReturnDtoWhenTaskExists() {
    AsyncTask task = new AsyncTask("task-1");
    task.setStatus(AsyncTaskStatus.IN_PROGRESS);
    when(asyncTaskStorage.getTask("task-1")).thenReturn(Optional.of(task));

    AsyncTaskResponseDto responseDto = asyncTaskService.getTaskStatus("task-1");

    assertThat(responseDto.getTaskId()).isEqualTo("task-1");
    assertThat(responseDto.getStatus()).isEqualTo(AsyncTaskStatus.IN_PROGRESS);
  }

  @Test
  void getTaskStatusShouldThrowWhenTaskNotFound() {
    when(asyncTaskStorage.getTask("unknown")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> asyncTaskService.getTaskStatus("unknown"))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Task not found");
  }
}
