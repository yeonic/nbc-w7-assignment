package org.example.expert.domain.common.exception;

public class InvalidAdminAccessException extends RuntimeException {

  public InvalidAdminAccessException(String message) {
    super(message);
  }
}
