package com.browserstack.automate.exception;


import com.browserstack.client.exception.BrowserStackObjectNotFound;

public class SessionNotFound extends BrowserStackObjectNotFound {

    /**
     *
     * @param msg error string for session not found.
     */
    public SessionNotFound(String msg) {
        super(msg);
    }
}
