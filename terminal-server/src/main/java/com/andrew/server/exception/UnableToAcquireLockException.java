package com.andrew.server.exception;

/**
 * @author andrew
 */
public class UnableToAcquireLockException extends RuntimeException {

  public UnableToAcquireLockException(String message) {
    super(message);
  }
}