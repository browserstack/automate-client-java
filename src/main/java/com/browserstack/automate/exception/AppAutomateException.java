package com.browserstack.automate.exception;

import com.browserstack.client.exception.BrowserStackException;

public class AppAutomateException extends BrowserStackException {

  public AppAutomateException(BrowserStackException e) {
    super(e.getMessage(), e.getStatusCode());
  }

  public AppAutomateException(String message, int statusCode) {
    super(message, statusCode);
  }

}
