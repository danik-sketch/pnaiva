package dev.notebook.notebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TaskRequestDto(
    @NotBlank String title,
    String description,
    @NotNull LocalDateTime dueDate,
    LocalDateTime completed
) {
}