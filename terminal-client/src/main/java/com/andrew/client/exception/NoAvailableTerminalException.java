package com.andrew.client.exception;

/**
 * @author andrew
 */
public class NoAvailableTerminalException extends RuntimeException {

  public NoAvailableTerminalException(String message) {
    super(message);
  }
}