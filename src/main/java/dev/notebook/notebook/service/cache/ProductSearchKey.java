package dev.notebook.notebook.service.cache;

import java.time.LocalDateTime;

public record ProductSearchKey(
    String projectName,
    String taskTitle,
    Boolean completed,
    LocalDateTime dueFrom,
    LocalDateTime dueTo,
    int pageNumber,
    int pageSize
) {

}