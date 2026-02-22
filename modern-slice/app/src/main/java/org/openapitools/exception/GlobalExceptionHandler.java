package org.openapitools.exception;

import java.util.stream.Collectors;

import org.openapitools.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(AuthenticationFailedException.class)
  public ResponseEntity<ErrorResponse> handleAuthError(AuthenticationFailedException ex) {
    return error(
        HttpStatus.UNAUTHORIZED,
        "AUTH_FAILED",
        "Authentication failed",
        ex.getMessage());
  }

  @ExceptionHandler(BusinessValidationException.class)
  public ResponseEntity<ErrorResponse> handleBusinessValidation(BusinessValidationException ex) {
    return error(
        HttpStatus.BAD_REQUEST,
        "TRANSFER_INVALID",
        "Transfer request invalid",
        ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    String details = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(this::formatFieldError)
        .collect(Collectors.joining("; "));

    return error(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Request validation failed",
        details);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
    String details = ex.getConstraintViolations()
        .stream()
        .map(v -> v.getPropertyPath() + " " + v.getMessage())
        .collect(Collectors.joining("; "));

    return error(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Request validation failed",
        details);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
    return error(
        HttpStatus.BAD_REQUEST,
        "MALFORMED_JSON",
        "Request validation failed",
        "Malformed request body");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
    return error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Unexpected server error",
        "An unexpected error occurred");
  }

  private ResponseEntity<ErrorResponse> error(
      HttpStatus status,
      String code,
      String message,
      String details) {
    String safeDetails = (details == null || details.isBlank()) ? "N/A" : details;
    return ResponseEntity.status(status)
        .body(new ErrorResponse(code, message, safeDetails));
  }

  private String formatFieldError(FieldError fieldError) {
    return fieldError.getField() + " " + fieldError.getDefaultMessage();
  }
}
