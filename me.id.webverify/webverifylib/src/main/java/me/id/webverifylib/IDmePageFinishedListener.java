package me.id.webverifylib;

/**
 * Created by remer on 3/2/17.
 */

interface IDmePageFinishedListener {
  /**
   * Check if the given URL match with the redirect uri passed to the original request
   * @param url - an url string about to be loaded from the web view.
   * @return true if the given url matches with the redirect uri
   */
  boolean isCallbackUrl(String url);
  /**
   * Pass the response URL come from backend.
   * @param responseUrl - the callback url with all the parameters sent back from the backend
   * @param scope - the scope tied to the last request from which this response was obtained
   */
  void onCallbackResponse(String responseUrl, IDmeScope scope);
}
