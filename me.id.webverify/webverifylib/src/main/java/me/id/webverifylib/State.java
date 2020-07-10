package me.id.webverifylib;

import androidx.annotation.NonNull;

import me.id.webverifylib.helper.CodeVerifierUtil;
import me.id.webverifylib.listener.IDmeScope;

/**
 * Used to handle the current app state
 */
enum State {
  LOGIN,
  LOGOUT,
  REGISTER_AFFILIATION,
  REGISTER_CONNECTION,
  ;

  private static final String CODE_VERIFIER_METHOD = CodeVerifierUtil.getCodeVerifierChallengeMethod();

  private IDmeScope scope;
  private String codeVerifier;
  private String codeVerifierChallenge;

  IDmeScope getScope() {
    return scope;
  }

  void setScope(IDmeScope scope) {
    this.scope = scope;
  }

  String getCodeChallenge() {
    return codeVerifierChallenge;
  }

  String getCodeVerifier() {
    return codeVerifier;
  }

  String getCodeVerifierMethod() {
    return CODE_VERIFIER_METHOD;
  }

  void setCodeVerifier(@NonNull String codeVerifier) {
    this.codeVerifier = codeVerifier;
    codeVerifierChallenge = CodeVerifierUtil.deriveCodeVerifierChallenge(codeVerifier);
  }
}
