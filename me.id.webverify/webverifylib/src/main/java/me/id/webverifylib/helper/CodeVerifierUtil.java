package me.id.webverifylib.helper;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

import me.id.webverifylib.IDmeWebVerify;

/**
 * Generates code verifiers and challenges for PKCE exchange.
 *
 * Based on: <https://github.com/openid/AppAuth-Android/blob/master/library/java/net/openid/appauth/CodeVerifierUtil.java>
 *
 * @see "Proof Key for Code Exchange by OAuth Public Clients (RFC 7636)
 * <https://tools.ietf.org/html/rfc7636>"
 */
public class CodeVerifierUtil {
  /**
   * SHA-256 based code verifier challenge method.
   *
   * @see "Proof Key for Code Exchange by OAuth Public Clients (RFC 7636), Section 4.3
   * <https://tools.ietf.org/html/rfc7636#section-4.3>"
   */
  public static final String CODE_CHALLENGE_METHOD_S256 = "S256";

  /**
   * Plain-text code verifier challenge method. This is only used by AppAuth for Android if
   * SHA-256 is not supported on this platform.
   *
   * @see "Proof Key for Code Exchange by OAuth Public Clients (RFC 7636), Section 4.4
   * <https://tools.ietf.org/html/rfc7636#section-4.4>"
   */
  public static final String CODE_CHALLENGE_METHOD_PLAIN = "plain";

  /**
   * The minimum permitted length for a code verifier.
   *
   * @see "Proof Key for Code Exchange by OAuth Public Clients (RFC 7636), Section 4.1
   * <https://tools.ietf.org/html/rfc7636#section-4.1>"
   */
  public static final int MIN_CODE_VERIFIER_LENGTH = 43;

  /**
   * The maximum permitted length for a code verifier.
   *
   * @see "Proof Key for Code Exchange by OAuth Public Clients (RFC 7636), Section 4.1
   * <https://tools.ietf.org/html/rfc7636#section-4.1>"
   */
  public static final int MAX_CODE_VERIFIER_LENGTH = 128;

  /**
   * The default entropy (in bytes) used for the code verifier.
   */
  public static final int DEFAULT_CODE_VERIFIER_ENTROPY = 64;

  /**
   * The minimum permitted entropy (in bytes) for use with
   * {@link #generateRandomCodeVerifier(SecureRandom, int)}.
   */
  public static final int MIN_CODE_VERIFIER_ENTROPY = 32;

  /**
   * The maximum permitted entropy (in bytes) for use with
   * {@link #generateRandomCodeVerifier(SecureRandom, int)}.
   */
  public static final int MAX_CODE_VERIFIER_ENTROPY = 96;

  /**
   * Base64 encoding settings used for generated code verifiers.
   */
  private static final int PKCE_BASE64_ENCODE_SETTINGS =
      Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE;

  /**
   * Regex for legal code verifier strings, as defined in the spec.
   *
   * @see "Proof Key for Code Exchange by OAuth Public Clients (RFC 7636), Section 4.1
   * <https://tools.ietf.org/html/rfc7636#section-4.1>"
   */
  private static final Pattern REGEX_CODE_VERIFIER =
      Pattern.compile("^[0-9a-zA-Z\\-\\.\\_\\~]{43,128}$");

  private CodeVerifierUtil() {
    throw new IllegalStateException("This type is not intended to be instantiated");
  }

  /**
   * Throws an IllegalArgumentException if the provided code verifier is invalid.
   *
   * @see "Proof Key for Code Exchange by OAuth Public Clients (RFC 7636), Section 4.1
   * <https://tools.ietf.org/html/rfc7636#section-4.1>"
   */
  public static void checkCodeVerifier(String codeVerifier) {
    Preconditions.checkArgument(MIN_CODE_VERIFIER_LENGTH <= codeVerifier.length(),
        "codeVerifier length is shorter than allowed by the PKCE specification");
    Preconditions.checkArgument(codeVerifier.length() <= MAX_CODE_VERIFIER_LENGTH,
        "codeVerifier length is longer than allowed by the PKCE specification");
    Preconditions.checkArgument(REGEX_CODE_VERIFIER.matcher(codeVerifier).matches(),
        "codeVerifier string contains illegal characters");
  }

  /**
   * Generates a random code verifier string using {@link SecureRandom} as the source of
   * entropy, with the default entropy quantity as defined by
   * {@link #DEFAULT_CODE_VERIFIER_ENTROPY}.
   */
  public static String generateRandomCodeVerifier() {
    return generateRandomCodeVerifier(new SecureRandom(), DEFAULT_CODE_VERIFIER_ENTROPY);
  }

  /**
   * Generates a random code verifier string using the provided entropy source and the specified
   * number of bytes of entropy.
   */
  public static String generateRandomCodeVerifier(SecureRandom entropySource, int entropyBytes) {
    Preconditions.checkNotNull(entropySource, "entropySource cannot be null");
    Preconditions.checkArgument(MIN_CODE_VERIFIER_ENTROPY <= entropyBytes,
        "entropyBytes is less than the minimum permitted");
    Preconditions.checkArgument(entropyBytes <= MAX_CODE_VERIFIER_ENTROPY,
        "entropyBytes is greater than the maximum permitted");
    byte[] randomBytes = new byte[entropyBytes];
    entropySource.nextBytes(randomBytes);
    return Base64.encodeToString(randomBytes, PKCE_BASE64_ENCODE_SETTINGS);
  }

  /**
   * Produces a challenge from a code verifier, using SHA-256 as the challenge method if the
   * system supports it (all Android devices _should_ support SHA-256).
   */
  public static String deriveCodeVerifierChallenge(String codeVerifier) {
    try {
      MessageDigest sha256Digester = MessageDigest.getInstance("SHA-256");
      sha256Digester.update(codeVerifier.getBytes("ISO_8859_1"));
      byte[] digestBytes = sha256Digester.digest();
      return Base64.encodeToString(digestBytes, PKCE_BASE64_ENCODE_SETTINGS);
    } catch (NoSuchAlgorithmException e) {
      Log.d(IDmeWebVerify.TAG, "SHA-256 is not supported on this device! Using plain challenge");
      return codeVerifier;
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("ISO-8859-1 encoding not supported", e);
    }
  }

  /**
   * Returns the challenge method utilized on this system: typically
   * {@link CodeVerifierUtil#CODE_CHALLENGE_METHOD_S256 SHA-256} if supported by
   * the system, {@link CodeVerifierUtil#CODE_CHALLENGE_METHOD_PLAIN plain} otherwise.
   */
  public static String getCodeVerifierChallengeMethod() {
    try {
      MessageDigest.getInstance("SHA-256");
      // no exception, so SHA-256 is supported
      return CodeVerifierUtil.CODE_CHALLENGE_METHOD_S256;
    } catch (NoSuchAlgorithmException e) {
      return CodeVerifierUtil.CODE_CHALLENGE_METHOD_PLAIN;
    }
  }
}
