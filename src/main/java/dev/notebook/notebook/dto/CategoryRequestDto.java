package dev.notebook.notebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload for creating or updating a category")
public record CategoryRequestDto(
    @Schema(description = "Category title")
    @NotBlank(message = "Title is required") String title
) {
}
