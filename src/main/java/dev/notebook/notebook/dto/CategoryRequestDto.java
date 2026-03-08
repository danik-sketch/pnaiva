package dev.notebook.notebook.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDto(
    @NotBlank String title
) {}

