package me.id.webverifylib.exception;

/**
 * This exception will be thrown when the session doesn't exist or when the session isn't valid.
 */
public final class UnauthenticatedException extends RuntimeException {
  public UnauthenticatedException() {
  }

  public UnauthenticatedException(String message) {
    super(message);
  }
}
