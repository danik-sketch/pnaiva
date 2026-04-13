package dev.notebook.notebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Payload for creating or updating a reminder")
public record ReminderRequestDto(
    @Schema(description = "Reminder date and time in ISO-8601 format")
    @NotNull(message = "Reminder time is required")
    LocalDateTime reminderTime,
    @Schema(description = "Reminder text")
    @NotBlank(message = "Message is required")
    String message,
    @Schema(description = "Related task identifier")
    Long taskId
) {
}
