package dev.notebook.notebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Payload for creating or updating a project")
public record ProjectRequestDto(
    @Schema(description = "Project name")
    @NotBlank(message = "Name is required") String name,
    @Schema(description = "Project description")
    String description,
    @Schema(description = "Identifier of the user who owns the project")
    @NotNull(message = "User id is required")
    Long userId,
    @Schema(description = "Tasks attached to the project")
    List<@Valid TaskRequestDto> tasks
) {
}
