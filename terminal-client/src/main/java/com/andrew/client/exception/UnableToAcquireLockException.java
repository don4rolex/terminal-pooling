package com.andrew.client.exception;

/**
 * @author andrew
 */
public class UnableToAcquireLockException extends RuntimeException {

  public UnableToAcquireLockException(String message) {
    super(message);
  }
}