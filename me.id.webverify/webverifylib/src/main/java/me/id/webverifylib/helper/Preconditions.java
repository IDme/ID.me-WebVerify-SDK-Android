package me.id.webverifylib.helper;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;

/**
 * Utility class for guava style pre-condition checks. Not an official part of the IDme API;
 * only intended for internal use and no guarantees are given on source or binary compatibility
 * for this class between versions of IDme.
 */
public final class Preconditions {

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if `reference` is `null`
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @return the non-null reference that was validated
     * @throws NullPointerException if `reference` is `null`
     */
    public static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    /**
     * Ensures that a string is not null or empty.
     */
    @NonNull
    public static String checkNotEmpty(String str, @Nullable Object errorMessage) {
        // ensure that we throw NullPointerException if the value is null, otherwise,
        // IllegalArgumentException if it is empty
        checkNotNull(str, errorMessage);
        checkArgument(!TextUtils.isEmpty(str), errorMessage);
        return str;
    }

    /**
     * Ensures that a collection is not null or empty.
     */
    @NonNull
    public static <T extends Collection<?>> T checkCollectionNotEmpty(
            T collection, @Nullable Object errorMessage) {
        checkNotNull(collection, errorMessage);
        checkArgument(!collection.isEmpty(), errorMessage);
        return collection;
    }

    /**
     * Ensures that the string is either null, or a non-empty string.
     */
    @NonNull
    public static String checkNullOrNotEmpty(String str, @Nullable Object errorMessage) {
        if (str != null) {
            checkNotEmpty(str, errorMessage);
        }
        return str;
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @throws IllegalArgumentException if `expression` is `false`
     */
    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentException if `expression` is `false`
     */
    public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     * @param expression a boolean expression
     * @param errorTemplate the exception message to use if the check fails; this is used
     *     as the template for String.format.
     * @param params the parameters to the exception message.
     */
    public static void checkArgument(
            boolean expression,
            @NonNull String errorTemplate,
            Object... params) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(errorTemplate, params));
        }
    }

    private Preconditions() {
        throw new IllegalStateException("This type is not intended to be instantiated");
    }
}
