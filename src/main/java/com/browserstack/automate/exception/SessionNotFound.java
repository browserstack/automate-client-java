package com.browserstack.automate.exception;


import com.browserstack.client.exception.BrowserStackObjectNotFound;

public class SessionNotFound extends BrowserStackObjectNotFound {

    public SessionNotFound(String msg) {
        super(msg);
    }
}
