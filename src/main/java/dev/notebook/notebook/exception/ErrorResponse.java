package dev.notebook.notebook.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Standard error response")
public record ErrorResponse(
    @Schema(description = "HTTP status code")
    int status,
    @Schema(description = "HTTP status text")
    String error,
    @Schema(description = "Short error summary")
    String message,
    @Schema(description = "Request path")
    String path,
    @Schema(description = "Detailed validation or processing errors")
    List<String> details,
    @Schema(description = "Timestamp when the error occurred")
    LocalDateTime timestamp
) {
}
