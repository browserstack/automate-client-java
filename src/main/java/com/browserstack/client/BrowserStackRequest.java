package com.browserstack.client;

import com.browserstack.client.exception.BrowserStackAuthException;
import com.browserstack.client.exception.BrowserStackException;
import com.browserstack.client.exception.BrowserStackObjectNotFound;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.http.*;
import com.google.api.client.util.escape.CharEscapers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BrowserStackRequest {

    private static final String USER_AGENT = "browserstack-automate-java/1.0";
    private final HttpRequest httpRequest;

    public BrowserStackRequest(HttpRequest httpRequest) {
        if (httpRequest == null) {
            throw new IllegalArgumentException("Invalid request");
        }

        this.httpRequest = httpRequest;
        this.httpRequest.getHeaders().setUserAgent(USER_AGENT);
        this.httpRequest.setFollowRedirects(false);
        this.httpRequest.setThrowExceptionOnExecuteError(false);
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

    public BrowserStackRequest header(String name, String value) {
        httpRequest.getHeaders().put(name, value);
        return this;
    }

    public BrowserStackRequest headers(Map<String, String> headers) {
        httpRequest.getHeaders().putAll(headers);
        return this;
    }

    public BrowserStackRequest queryString(String name, Object value) {
        httpRequest.getUrl().set(name, value);
        return this;
    }

    public BrowserStackRequest body(HttpContent httpContent) {
        if (!canContainBody()) {
            throw new IllegalStateException("Unsupported operation");
        }

        httpRequest.setContent(httpContent);
        return this;
    }

    public BrowserStackRequest body(String body) {
        if (!canContainBody()) {
            throw new IllegalStateException("Unsupported operation");
        }

        httpRequest.setContent(new UrlEncodedContent(body));
        return this;
    }

    public boolean canContainBody() {
        String requestMethod = httpRequest.getRequestMethod();
        requestMethod = (requestMethod != null) ? requestMethod.toUpperCase() : "";
        return (requestMethod.equals("POST") || requestMethod.equals("PUT") || requestMethod.equals("PATCH"));
    }

    public BrowserStackRequest routeParam(String name, String value) {
        List<String> pathParts = httpRequest.getUrl().getPathParts();
        Pattern namePattern = Pattern.compile("\\{" + name + "\\}");

        int count = 0;
        for (int i = 0; i < pathParts.size(); i++) {
            String path = pathParts.get(i);
            Matcher matcher = namePattern.matcher(path);
            if (matcher.find()) {
                pathParts.set(i, path.replaceFirst("\\{" + name + "\\}", CharEscapers.escapeUriPath(value)));
                count++;
            }
        }

        if (count == 0) {
            throw new RuntimeException("Can't find route parameter name \"" + name + "\"");
        }

        return this;
    }

    public <T> T asObject(Class<? extends T> responseClass) throws BrowserStackException {
        try {
            return execute().parseAs(responseClass);
        } catch (IOException e) {
            throw new BrowserStackException(e.getMessage());
        }
    }

    public ObjectNode asJsonObject() throws BrowserStackException {
        try {
            return execute().parseAs(ObjectNode.class);
        } catch (IOException e) {
            throw new BrowserStackException(e.getMessage());
        }
    }

    public ArrayNode asJsonArray() throws BrowserStackException {
        try {
            return execute().parseAs(ArrayNode.class);
        } catch (IOException e) {
            throw new BrowserStackException(e.getMessage());
        }
    }

    public String asString() throws BrowserStackException {
        try {
            return execute().parseAsString();
        } catch (IOException e) {
            throw new BrowserStackException(e.getMessage());
        }
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    private HttpResponse execute() throws BrowserStackException, IOException {
        HttpResponse response = httpRequest.execute();

        if (response != null) {
            int status = response.getStatusCode();
            switch (status) {
                case 401:
                    throw new BrowserStackAuthException(response.parseAsString(), status);

                case 404:
                    throw new BrowserStackObjectNotFound(response.parseAsString());

                case 302:
                    String redirectLocation = httpRequest.getResponseHeaders().getLocation();
                    httpRequest.setUrl(new GenericUrl(httpRequest.getUrl().toURL(redirectLocation)));
                    response = httpRequest.execute();
            }
        }

        return response;
    }

}
