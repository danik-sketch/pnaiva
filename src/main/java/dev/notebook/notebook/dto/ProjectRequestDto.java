package dev.notebook.notebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectRequestDto(
    @NotBlank String name,
    String description,
    @NotNull Long userId // Связь с User (FK)
) {}