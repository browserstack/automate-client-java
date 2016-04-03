package com.browserstack.client.exception;

public class BrowserStackException extends Exception {

    private final int statusCode;

    public BrowserStackException(Exception e) {
        this(e, 0);
    }

    public BrowserStackException(Exception e, int statusCode) {
        super(e);
        this.statusCode = statusCode;
    }

    public BrowserStackException(String message) {
        this(message, 0);
    }

    public BrowserStackException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
