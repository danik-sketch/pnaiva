package dev.notebook.notebook.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalException {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> methodArgumentNotValid(
      MethodArgumentNotValidException exception,
      HttpServletRequest request
  ) {
    List<String> errors = exception.getBindingResult()
        .getAllErrors()
        .stream()
        .map(error -> {
          if (error instanceof FieldError fieldError) {
            return fieldError.getField() + ": " + fieldError.getDefaultMessage();
          }
          return error.getDefaultMessage();
        })
        .toList();

    return buildResponse(
        HttpStatus.BAD_REQUEST,
        "Validation failed",
        request,
        errors
    );
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> notFound(
      NotFoundException exception,
      HttpServletRequest request
  ) {
    return buildResponse(
        HttpStatus.NOT_FOUND,
        exception.getMessage(),
        request,
        List.of(exception.getMessage())
    );
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> constraintViolation(
      ConstraintViolationException exception,
      HttpServletRequest request
  ) {
    List<String> errors = exception.getConstraintViolations()
        .stream()
        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
        .toList();

    return buildResponse(
        HttpStatus.BAD_REQUEST,
        "Validation failed",
        request,
        errors
    );
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> illegalArgument(
      IllegalArgumentException exception,
      HttpServletRequest request
  ) {
    return buildResponse(
        HttpStatus.BAD_REQUEST,
        exception.getMessage(),
        request,
        List.of(exception.getMessage())
    );
  }

  @ExceptionHandler(OperationFailedException.class)
  public ResponseEntity<ErrorResponse> operationFailed(
      OperationFailedException exception,
      HttpServletRequest request
  ) {
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        exception.getMessage(),
        request,
        List.of(exception.getMessage())
    );
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> emailAlreadyExists(
      EmailAlreadyExistsException exception,
      HttpServletRequest request
  ) {
    return buildResponse(
        HttpStatus.CONFLICT,
        exception.getMessage(),
        request,
        List.of(exception.getMessage())
    );
  }

  @ExceptionHandler({
      MethodArgumentTypeMismatchException.class,
      MissingServletRequestParameterException.class,
      HttpMessageNotReadableException.class
  })
  public ResponseEntity<ErrorResponse> badRequestExceptions(
      Exception exception,
      HttpServletRequest request
  ) {
    return buildResponse(
        HttpStatus.BAD_REQUEST,
        "Request is invalid",
        request,
        List.of(exception.getMessage())
    );
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> methodNotSupported(
      HttpRequestMethodNotSupportedException exception,
      HttpServletRequest request
  ) {
    return buildResponse(
        HttpStatus.METHOD_NOT_ALLOWED,
        "HTTP method is not supported",
        request,
        List.of(exception.getMessage())
    );
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> noResourceFound(
      NoResourceFoundException exception,
      HttpServletRequest request
  ) {
    return buildResponse(
        HttpStatus.NOT_FOUND,
        "Endpoint not found",
        request,
        List.of("No handler found for the requested endpoint")
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> unexpected(
      Exception exception,
      HttpServletRequest request
  ) {
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Unexpected server error",
        request,
        List.of("Internal error. Please contact support if the issue persists")
    );
  }

  private ResponseEntity<ErrorResponse> buildResponse(
      HttpStatus status,
      String message,
      HttpServletRequest request,
      List<String> details
  ) {
    ErrorResponse response = new ErrorResponse(
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        details,
        LocalDateTime.now()
    );

    return ResponseEntity.status(status).body(response);
  }
}
