package me.id.webverifylib.exception;

/** Exception thrown when the user cancels the current action */
public class UserCanceledException extends IDmeException {
  public UserCanceledException() {
    super();
  }

  public UserCanceledException(String message) {
    super(message);
  }
}
