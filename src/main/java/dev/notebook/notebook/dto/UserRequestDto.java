package dev.notebook.notebook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDto(
    @NotBlank String username,
    @Email String email,
    @NotBlank String password
) {
}