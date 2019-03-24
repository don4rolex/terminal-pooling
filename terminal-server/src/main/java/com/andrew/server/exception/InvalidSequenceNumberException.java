package com.andrew.server.exception;

/**
 * @author andrew
 */
public class InvalidSequenceNumberException extends RuntimeException {

  public InvalidSequenceNumberException(String message) {
    super(message);
  }
}