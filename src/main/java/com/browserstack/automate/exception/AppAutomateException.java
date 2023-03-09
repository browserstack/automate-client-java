package com.browserstack.automate.exception;

import com.browserstack.client.exception.BrowserStackException;

public class AppAutomateException extends BrowserStackException {

  /**
   *
   * @param e BrowserStackException object.
   */
  public AppAutomateException(BrowserStackException e) {
    super(e.getMessage(), e.getStatusCode());
  }

  /**
   *
   * @param message exception string for AppAutomate session.
   * @param statusCode status code for exception.
   */
  public AppAutomateException(String message, int statusCode) {
    super(message, statusCode);
  }

}
