package dev.notebook.notebook.exception;

import dev.notebook.notebook.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

  @ExceptionHandler({NotFoundException.class, EmailAlreadyExistsException.class,
      OperationFailedException.class, IllegalArgumentException.class})
  public ResponseEntity<ErrorResponseDto> handleCustomExceptions(
      Exception ex,
      HttpServletRequest request
  ) {
    HttpStatus status = switch (ex) {
      case NotFoundException e -> HttpStatus.NOT_FOUND;
      case EmailAlreadyExistsException e -> HttpStatus.CONFLICT;
      case OperationFailedException e -> HttpStatus.INTERNAL_SERVER_ERROR;
      default -> HttpStatus.BAD_REQUEST;
    };

    return buildResponse(status, ex.getMessage(), request, List.of(ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidation(
      MethodArgumentNotValidException ex,
      HttpServletRequest request
  ) {
    List<String> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(f -> f.getField() + ": " + f.getDefaultMessage())
        .toList();

    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponseDto> handleConstraint(
      ConstraintViolationException ex,
      HttpServletRequest request
  ) {
    List<String> errors = ex.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .toList();

    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleUnexpected(
      Exception ex,
      HttpServletRequest request
  ) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request,
        List.of("Internal server error"));
  }

  private ResponseEntity<ErrorResponseDto> buildResponse(
      HttpStatus status, String msg, HttpServletRequest req, List<String> details) {
    return ResponseEntity.status(status).body(
        new ErrorResponseDto(status.value(), status.getReasonPhrase(), msg, req.getRequestURI(),
            details, LocalDateTime.now()));
  }
}