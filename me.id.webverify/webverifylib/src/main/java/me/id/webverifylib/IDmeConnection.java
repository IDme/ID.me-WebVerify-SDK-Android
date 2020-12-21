package me.id.webverifylib;

import androidx.annotation.NonNull;

/**
 * <p>Supported connections interface. See {@link IDmeConnectionType} for a subset of supported connections.</p>
 *
 * <p>This interface provides flexibility on the connections type used with this SDK, in case of having support for
 * new connections types, a custom implementation of this interface can be passed in.
 * </p>
 *
 * @see IDmeConnectionType
 */
public interface IDmeConnection {
  @NonNull
  String getKey();
}
