package org.example.expert.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidAdminAccessException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidRequestException(
          InvalidRequestException ex) {
    log.info("handleInvalidRequestExceptionException", ex);

    HttpStatus status = HttpStatus.BAD_REQUEST;
    return getErrorResponse(status, ex.getMessage());
  }

  @ExceptionHandler(InvalidAdminAccessException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidAdminAccessException(
          InvalidAdminAccessException ex) {
    log.info("handleInvalidAdminAccessException", ex);

    HttpStatus status = HttpStatus.FORBIDDEN;
    return getErrorResponse(status, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
          MethodArgumentNotValidException ex) {
    log.info("handleMethodArgumentNotValidException", ex);

    HttpStatus status = HttpStatus.BAD_REQUEST;
    String errorMessage = ex.getFieldError() == null
            ? ex.getMessage()
            : ex.getFieldError().getDefaultMessage();

    return getErrorResponse(status, errorMessage);
  }

  @ExceptionHandler(AuthException.class)
  public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
    log.info("handleAuthException", ex);

    HttpStatus status = HttpStatus.UNAUTHORIZED;
    return getErrorResponse(status, ex.getMessage());
  }

  @ExceptionHandler(ServerException.class)
  public ResponseEntity<Map<String, Object>> handleServerException(ServerException ex) {
    log.info("handleServerException", ex);
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    return getErrorResponse(status, ex.getMessage());
  }

  public ResponseEntity<Map<String, Object>> getErrorResponse(HttpStatus status, String message) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", status.name());
    errorResponse.put("code", status.value());
    errorResponse.put("message", message);

    return new ResponseEntity<>(errorResponse, status);
  }
}

