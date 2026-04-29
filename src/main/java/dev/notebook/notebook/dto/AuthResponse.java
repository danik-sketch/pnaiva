package dev.notebook.notebook.dto;

public record AuthResponse(
    String token,
    Long userId,
    String username,
    String email
) {

}
