package dev.notebook.notebook.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating or updating a user")
public record UserRequestDto(
    @Schema(description = "Unique username")
    @NotBlank(message = "Name is required")
    String username,
    @Schema(description = "User email address")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    String email,
    @Schema(description = "User password")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    String password
) {
}
