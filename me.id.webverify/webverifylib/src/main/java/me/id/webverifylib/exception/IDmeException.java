package me.id.webverifylib.exception;

/**
 * This exception will be thrown when something went wrong.
 */
public class IDmeException extends RuntimeException {
  public IDmeException() {
    super();
  }

  public IDmeException(String message) {
    super(message);
  }
}
