package com.browserstack.client.exception;


public class BrowserStackObjectNotFound extends BrowserStackException {
    public BrowserStackObjectNotFound(Exception e) {
        super(e);
    }

    public BrowserStackObjectNotFound(String msg) {
        super(msg);
    }
}
