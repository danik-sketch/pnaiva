package dev.notebook.notebook.exception;

import dev.notebook.notebook.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

  private final Logger log = LoggerFactory.getLogger(GlobalException.class);

  @ExceptionHandler({NotFoundException.class, EmailAlreadyExistsException.class,
      OperationFailedException.class, IllegalArgumentException.class})
  public ResponseEntity<ErrorResponseDto> handleCustomExceptions(
      Exception ex,
      HttpServletRequest request
  ) {
    HttpStatus status = switch (ex) {
      case NotFoundException _ -> HttpStatus.NOT_FOUND;
      case EmailAlreadyExistsException _ -> HttpStatus.CONFLICT;
      case OperationFailedException _ -> HttpStatus.INTERNAL_SERVER_ERROR;
      default -> HttpStatus.BAD_REQUEST; // для IllegalArgumentException и др.
    };

    log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
    return buildResponse(status, ex.getMessage(), request, List.of(ex.getMessage()));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
  public ResponseEntity<ErrorResponseDto> handleValidation(
      Exception ex,
      HttpServletRequest request
  ) {
    List<String> errors;

    if (ex instanceof MethodArgumentNotValidException methodArgEx) {
      errors = methodArgEx.getBindingResult().getFieldErrors().stream()
          .map(f -> f.getField() + ": " + f.getDefaultMessage()).toList();
    } else { // ConstraintViolationException
      errors = ((ConstraintViolationException) ex).getConstraintViolations().stream()
          .map(v -> v.getPropertyPath() + ": " + v.getMessage()).toList();
    }

    log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleUnexpected(
      Exception ex,
      HttpServletRequest request
  ) {
    log.error("Unexpected error", ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request,
        List.of("Internal server error"));
  }

  private ResponseEntity<ErrorResponseDto> buildResponse(
      HttpStatus status, String message,
      HttpServletRequest request,
      List<String> details
  ) {
    return ResponseEntity.status(status).body(
        new ErrorResponseDto(status.value(), status.getReasonPhrase(), message,
            request.getRequestURI(), details, LocalDateTime.now()));
  }
}