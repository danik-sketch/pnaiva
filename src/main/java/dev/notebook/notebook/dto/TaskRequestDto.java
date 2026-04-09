package dev.notebook.notebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TaskRequestDto(
    @NotBlank(message = "Title is required")
    String title,
    String description,
    @NotNull(message = "Due date is required")
    LocalDateTime dueDate,
    LocalDateTime completed
) {
}
