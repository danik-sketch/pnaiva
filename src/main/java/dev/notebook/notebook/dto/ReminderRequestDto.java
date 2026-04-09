package dev.notebook.notebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record ReminderRequestDto(
    @NotNull(message = "Reminder time is required")
    LocalDateTime reminderTime,
    @NotBlank(message = "Message is required")
    String message,
    @NotNull(message = "Task id is required")
    Long taskId
) {
}
