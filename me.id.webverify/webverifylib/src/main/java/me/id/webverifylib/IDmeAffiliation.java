package me.id.webverifylib;

import androidx.annotation.NonNull;

/**
 * <p>Supported affiliations interface. See {@link IDmeAffiliationType} for a subset of supported affiliations.</p>
 *
 * <p>This interface provides flexibility on the affiliations type used with this SDK, in case of having support for
 * new affiliations types, a custom implementation of this interface can be passed in.
 * </p>
 *
 * @see IDmeAffiliationType
 */
public interface IDmeAffiliation {
  @NonNull
  String getKey();
}
