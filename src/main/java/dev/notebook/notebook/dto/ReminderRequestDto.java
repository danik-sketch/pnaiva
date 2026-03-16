package dev.notebook.notebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ReminderRequestDto(
    @NotNull LocalDateTime reminderTime,
    @NotBlank String message,
    @NotNull Long taskId
) {}

