package com.browserstack.automate.exception;


import com.browserstack.client.exception.BrowserStackObjectNotFound;

public class BuildNotFound extends BrowserStackObjectNotFound {

    public BuildNotFound(String msg) {
        super(msg);
    }
}
