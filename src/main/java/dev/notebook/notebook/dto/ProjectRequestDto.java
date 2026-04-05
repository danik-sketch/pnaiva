package dev.notebook.notebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProjectRequestDto(
    @NotBlank String name,
    String description,
    @NotNull Long userId,
    List<TaskRequestDto> tasks
) {
}
