package com.andrew.server.exception;

/**
 * @author andrew
 */
public class InvalidTerminalIdException extends RuntimeException {

  public InvalidTerminalIdException(String message) {
    super(message);
  }
}