package dev.notebook.notebook.mapper;

import dev.notebook.notebook.dto.AsyncTaskResponseDto;
import dev.notebook.notebook.entity.AsyncTask;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AsyncTaskMapper {

  public static AsyncTaskResponseDto toDto(AsyncTask task) {
    AsyncTaskResponseDto dto = new AsyncTaskResponseDto();
    dto.setTaskId(task.getId());
    dto.setStatus(task.getStatus());
    dto.setCreatedAt(task.getCreatedAt());
    dto.setUpdatedAt(task.getUpdatedAt());
    return dto;
  }
}
