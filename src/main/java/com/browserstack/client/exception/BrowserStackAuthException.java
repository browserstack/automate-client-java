package com.browserstack.client.exception;


public class BrowserStackAuthException extends BrowserStackException {

    public BrowserStackAuthException(String message, int statusCode) {
        super(message, statusCode);
    }

}
