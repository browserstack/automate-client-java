package com.browserstack.automate.exception;

import com.browserstack.client.exception.BrowserStackException;


public class AutomateException extends BrowserStackException {

    public AutomateException(BrowserStackException e) {
        super(e.getMessage(), e.getStatusCode());
    }

    public AutomateException(String message, int statusCode) {
        super(message, statusCode);
    }
}
