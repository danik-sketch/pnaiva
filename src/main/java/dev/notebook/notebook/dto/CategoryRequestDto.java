package dev.notebook.notebook.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDto(
    @NotBlank(message = "Title is required") String title
) {
}

