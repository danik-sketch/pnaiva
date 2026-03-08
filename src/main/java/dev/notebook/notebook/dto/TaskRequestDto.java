package dev.notebook.notebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TaskRequestDto(
    @NotBlank(message = "Заголовок не может быть пустым")
    String title,

    String description,

    @NotNull(message = "Дата выполнения обязательна")
    LocalDateTime dueDate,

    boolean completed,

    Long projectId
) {

}