package com.browserstack.client;

import com.browserstack.client.exception.BrowserStackAuthException;
import com.browserstack.client.exception.BrowserStackException;
import com.browserstack.client.exception.BrowserStackObjectNotFound;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.apache.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


public class BrowserStackRequest {

    private final BrowserStackClient client;
    private final HttpRequest httpRequest;

    public BrowserStackRequest(BrowserStackClient client, HttpRequest httpRequest) {
        if (client == null || httpRequest == null) {
            throw new IllegalArgumentException("Invalid request");
        }

        this.client = client;
        this.httpRequest = httpRequest;
    }

    public BrowserStackRequest header(String name, String value) {
        httpRequest.header(name, value);
        return this;
    }

    public BrowserStackRequest headers(Map<String, String> headers) {
        httpRequest.headers(headers);
        return this;
    }

    public BrowserStackRequest queryString(String name, Object value) {
        httpRequest.queryString(name, value);
        return this;
    }

    public BrowserStackRequest body(Object body) {
        if (!canContainBody()) {
            throw new IllegalStateException("Unsupported operation");
        }

        ((HttpRequestWithBody) httpRequest).body(body);
        return this;
    }

    public BrowserStackRequest body(String body) {
        if (!canContainBody()) {
            throw new IllegalStateException("Unsupported operation");
        }

        ((HttpRequestWithBody) httpRequest).body(body);
        return this;
    }

    public boolean canContainBody() {
        return (httpRequest instanceof HttpRequestWithBody);
    }

    public BrowserStackRequest routeParam(String name, String value) {
        httpRequest.routeParam(name, value);
        return this;
    }

    public <T> T asObject(Class<? extends T> responseClass) throws BrowserStackException {
        try {
            return throwIfError(httpRequest.asObject(responseClass)).getBody();
        } catch (UnirestException e) {
            throw client.newClientException(e.getMessage());
        }
    }

    public JsonNode asJson() throws BrowserStackException {
        try {
            return throwIfError(httpRequest.asJson()).getBody();
        } catch (UnirestException e) {
            throw client.newClientException(e.getMessage());
        }
    }

    public String asString() throws BrowserStackException {
        try {
            return throwIfError(httpRequest.asString()).getBody();
        } catch (UnirestException e) {
            throw client.newClientException(e.getMessage());
        }
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    protected <T> HttpResponse<T> throwIfError(HttpResponse<T> response) throws BrowserStackException {
        if (response.getBody() == null) {
            int status = response.getStatus();
            String resText;

            try {
                resText = getRawBody(response.getRawBody());
            } catch (IOException e) {
                resText = response.getStatusText();
            }

            if (status == HttpStatus.SC_UNAUTHORIZED) {
                throw new BrowserStackAuthException(resText, status);
            } else if (status == HttpStatus.SC_NOT_FOUND) {
                throw new BrowserStackObjectNotFound(resText);
            } else {
                throw client.newClientException(resText, status);
            }
        }

        return response;
    }

    private static String getRawBody(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString("UTF-8");
    }

}
