package com.browserstack.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import org.apache.http.entity.mime.MIME;

import java.io.IOException;
import java.util.Map;

public class BrowserStackClient {

    protected static final ObjectMapper objectMapper = new ObjectMapper() {
        private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                = new com.fasterxml.jackson.databind.ObjectMapper();

        public <BrowserstackObject> BrowserstackObject readValue(String value, Class<BrowserstackObject> valueType) {
            try {
                return jacksonObjectMapper.readValue(value, valueType);
            } catch (JsonParseException e) {
                // Until all endpoints respond with valid JSON
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String writeValue(Object value) {
            try {
                return jacksonObjectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final String baseUrl;
    private final String username;
    private final String accessKey;

    public enum Method {
        GET, POST, PUT, DELETE
    }

    static {
        Unirest.setObjectMapper(objectMapper);
        Unirest.setDefaultHeader("accept", "application/json");
    }


    public BrowserStackClient(String baseUrl, String username, String accessKey) {
        if (baseUrl == null) {
            throw new IllegalArgumentException("Invalid baseUrl");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid username");
        }

        if (accessKey == null || accessKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid accessKey");
        }

        this.baseUrl = baseUrl;
        this.username = username.trim();
        this.accessKey = accessKey.trim();
    }

    public BrowserStackRequest newRequest(final Method method, final String path) {
        if (method == null) {
            throw new IllegalArgumentException("Invalid method");
        }

        final String url = (path != null) ? this.baseUrl + path : this.baseUrl;
        HttpRequest request;
        switch (method) {
            case GET:
                request = Unirest.get(url);
                break;

            case POST:
                request = Unirest.post(url);
                break;

            case PUT:
                request = Unirest.put(url);
                break;

            case DELETE:
                request = Unirest.delete(url);
                break;

            default:
                throw new IllegalArgumentException("Invalid method");
        }

        return signRequest(request);
    }

    public BrowserStackRequest newRequest(final Method method,
                                          final String path,
                                          final Map<String, Object> data) {
        return newRequest(method, path, data, null);
    }

    public BrowserStackRequest newRequest(final Method method, final String path,
                                          final Map<String, Object> data,
                                          final Map<String, String> headers) {

        BrowserStackRequest request = newRequest(method, path);
        if (headers != null && headers.size() > 0) {
            request.headers(headers);
        }

        if (data != null && data.size() > 0 && request.canContainBody()) {
            request.header(MIME.CONTENT_TYPE, "application/json").body(data);
        }

        return request;
    }

    public BrowserStackRequest signRequest(final HttpRequest request) {
        return new BrowserStackRequest(request.basicAuth(this.username, this.accessKey));
    }

}
