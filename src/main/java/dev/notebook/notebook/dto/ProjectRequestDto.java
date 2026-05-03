package dev.notebook.notebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Schema(description = "Payload for creating or updating a project")
public record ProjectRequestDto(
    @Schema(description = "Project name")
    @NotBlank(message = "Name is required") String name,
    @Schema(description = "Project description")
    String description,
    @Schema(description = "Tasks attached to the project")
    List<@Valid TaskRequestDto> tasks
) {
}
