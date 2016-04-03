package com.browserstack.client.api;

import com.browserstack.client.BrowserStackClient;


public abstract class BrowserStackObject {
    private BrowserStackClient client;

    public BrowserStackClient getClient() {
        return client;
    }

    @SuppressWarnings("unchecked")
    public <T> T setClient(BrowserStackClient client) {
        this.client = client;
        return (T) this;
    }
}
