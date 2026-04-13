package dev.notebook.notebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Payload for creating or updating a task")
public record TaskRequestDto(
    @Schema(description = "Task title")
    @NotBlank(message = "Title is required")
    String title,
    @Schema(description = "Task description")
    String description,
    @Schema(description = "Due date and time in ISO-8601 format")
    @NotNull(message = "Due date is required")
    LocalDateTime dueDate,
    @Schema(description = "Completion date and time if the task is already done")
    LocalDateTime completed,
    @Schema(description = "Related project identifier")
    Long projectId,
    @Schema(description = "Task reminders")
    List<@Valid ReminderRequestDto> reminders
) {
}
