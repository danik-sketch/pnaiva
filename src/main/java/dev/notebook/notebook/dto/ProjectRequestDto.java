package dev.notebook.notebook.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record ProjectRequestDto(
    @NotBlank(message = "Name is required") String name,
    String description,
    @NotNull(message = "User id is required")
    Long userId,
    List<@Valid TaskRequestDto> tasks
) {
}
