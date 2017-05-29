package me.id.webverifylib.exception;

/** Exception thrown if exist an initialized process */
public final class ConcurrentOperationNotSupportedException extends IDmeException {
  public ConcurrentOperationNotSupportedException(String message) {
    super(message);
  }
}
