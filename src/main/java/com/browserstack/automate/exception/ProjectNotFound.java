package com.browserstack.automate.exception;


import com.browserstack.client.exception.BrowserStackObjectNotFound;

public class ProjectNotFound extends BrowserStackObjectNotFound {

    public ProjectNotFound(String msg) {
        super(msg);
    }
}
