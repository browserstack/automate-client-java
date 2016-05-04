package com.browserstack.client;

import com.browserstack.client.exception.BrowserStackException;
import com.browserstack.client.util.BrowserStackCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.util.ObjectParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

public class BrowserStackClient {

    private final String baseUrl;

    private final String username;

    private final String accessKey;

    private final BasicAuthentication authentication;

    private final HttpRequestFactory requestFactory;

    protected final BrowserStackCache<String, Object> cacheMap;

    public enum Method {
        GET, POST, PUT, DELETE
    }

    private static final HttpTransport HTTP_TRANSPORT = new ApacheHttpTransport();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static final ObjectParser OBJECT_PARSER = new ObjectParser() {
        public <T> T parseAndClose(InputStream inputStream, Charset charset, Class<T> aClass) throws IOException {
            return JSON_MAPPER.readValue(inputStream, aClass);
        }

        public <T> T parseAndClose(Reader reader, Class<T> aClass) throws IOException {
            return JSON_MAPPER.readValue(reader, aClass);
        }

        public Object parseAndClose(InputStream inputStream, Charset charset, Type type) throws IOException {
            throw new IOException("Unsupported operation");
        }

        public Object parseAndClose(Reader reader, Type type) throws IOException {
            throw new IOException("Unsupported operation");
        }
    };

    public BrowserStackClient(String baseUrl, String username, String accessKey) {
        if (baseUrl == null) {
            throw new IllegalArgumentException("Invalid baseUrl");
        }

        if (username == null || username.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid username");
        }

        if (accessKey == null || accessKey.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid accessKey");
        }

        this.baseUrl = baseUrl;
        this.username = username.trim();
        this.accessKey = accessKey.trim();
        this.authentication = new BasicAuthentication(this.username, this.accessKey);

        this.cacheMap = new BrowserStackCache<String, Object>();
        this.requestFactory = newRequestFactory();
    }

    protected BrowserStackRequest newRequest(final Method method, final String path) throws BrowserStackException {
        return newRequest(method, path, true);
    }

    protected BrowserStackRequest newRequest(final Method method, final String path, final boolean prependUrl) throws BrowserStackException {
        String urlPath = (path == null) ? "" : path;
        GenericUrl url = new GenericUrl(prependUrl ? this.baseUrl + urlPath : urlPath);
        return signRequest(newRequest(requestFactory, method, url));
    }

    protected BrowserStackRequest newRequest(final Method method,
                                          final String path,
                                          final Map<String, Object> data) throws BrowserStackException {
        return newRequest(method, path, data, null);
    }

    protected BrowserStackRequest newRequest(final Method method, final String path,
                                          final Map<String, Object> data,
                                          final Map<String, String> headers) throws BrowserStackException {
        BrowserStackRequest request = newRequest(method, path);
        if (headers != null && headers.size() > 0) {
            request.headers(headers);
        }

        if (data != null && data.size() > 0 && request.canContainBody()) {
            try {
                request.header("Content-Type", "application/json").body(JSON_MAPPER.writeValueAsString(data));
            } catch (JsonProcessingException e) {
                throw new BrowserStackException(e);
            }
        }

        return request;
    }

    protected BrowserStackRequest signRequest(final HttpRequest request) throws BrowserStackException {
        try {
            authentication.intercept(request);
        } catch (IOException e) {
            throw new BrowserStackException(e);
        }

        return new BrowserStackRequest(request);
    }

    static HttpRequestFactory newRequestFactory() {
        return HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest httpRequest) throws IOException {
                httpRequest.setParser(OBJECT_PARSER);
            }
        });
    }

    static HttpRequest newRequest(final HttpRequestFactory requestFactory, final Method method,
                                         final GenericUrl url) throws BrowserStackException {
        if (method == null) {
            throw new IllegalArgumentException("Invalid method");
        }

        final HttpRequest request;

        try {
            switch (method) {
                case GET:
                    request = requestFactory.buildGetRequest(url);
                    break;

                case POST:
                    request = requestFactory.buildPostRequest(url, null);
                    break;

                case PUT:
                    request = requestFactory.buildPutRequest(url, null);
                    break;

                case DELETE:
                    request = requestFactory.buildDeleteRequest(url);
                    break;

                default:
                    throw new IllegalArgumentException("Invalid method");
            }
        } catch (IOException e) {
            throw new BrowserStackException(e);
        }

        return request;
    }
}
