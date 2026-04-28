package dev.notebook.notebook.service.cache;

import java.time.LocalDateTime;

public record SearchKey(
    String projectName,
    String taskTitle,
    Boolean completed,
    LocalDateTime dueFrom,
    LocalDateTime dueTo,
    int pageNumber,
    int pageSize
) {

}