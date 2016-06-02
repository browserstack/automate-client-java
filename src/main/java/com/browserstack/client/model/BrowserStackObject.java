package com.browserstack.client.model;

import com.browserstack.client.BrowserStackClient;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;


public abstract class BrowserStackObject implements Serializable {
    private transient BrowserStackClient client;

    @JsonIgnore
    public BrowserStackClient getClient() {
        return client;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public <T> T setClient(BrowserStackClient client) {
        this.client = client;
        return (T) this;
    }

    public boolean hasClient() {
        return (client != null);
    }
}
