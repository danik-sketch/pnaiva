package dev.notebook.notebook.exception;

public class OperationFailedException extends RuntimeException {

  public OperationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
