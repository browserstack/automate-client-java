package com.browserstack.client;

import com.browserstack.automate.exception.AutomateException;
import com.browserstack.client.exception.BrowserStackException;
import com.browserstack.client.model.BrowserListing;
import com.browserstack.client.util.BrowserStackCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.ObjectParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

public abstract class BrowserStackClient {
    private static final String BASE_URL = "https://www.browserstack.com";
    private static final String CACHE_KEY_PREFIX_BROWSERS = "browsers";

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
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

    protected final BrowserStackCache<String, Object> cacheMap;

    private final HttpRequestFactory requestFactory;

    private String baseUrl;

    private String username;

    private String accessKey;

    private BasicAuthentication authentication;

    protected BrowserStackClient() {
        this.cacheMap = new BrowserStackCache<String, Object>();
        this.requestFactory = newRequestFactory();
    }

    public BrowserStackClient(String baseUrl, String username, String accessKey) {
        this();

        if (baseUrl == null) {
            throw new IllegalArgumentException("Invalid baseUrl");
        }

        if (username == null || username.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid username");
        }

        if (accessKey == null || accessKey.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid access key");
        }

        this.baseUrl = baseUrl;
        this.username = username.trim();
        this.accessKey = accessKey.trim();
        this.authentication = new BasicAuthentication(this.username, this.accessKey);
    }

    protected String getAccessKey() {
        return accessKey;
    }

    protected void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
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

    private void checkAuthState() {
        if (authentication == null) {
            throw new IllegalStateException("Missing API credentials");
        }
    }

    public final BrowserListing getBrowsersForProduct(Product product) throws AutomateException {
        return getBrowsersForProduct(product, true);
    }

    @SuppressWarnings("unchecked")
    public final BrowserListing getBrowsersForProduct(Product product, boolean cache) throws AutomateException {
        String productName = product.name().toLowerCase();
        String cacheKey = (CACHE_KEY_PREFIX_BROWSERS + productName).toLowerCase();

        try {
            if (cache) {
                if (cacheMap.containsKey(cacheKey)) {
                    BrowserListing browserListing = (BrowserListing) cacheMap.get(cacheKey);
                    if (browserListing != null) {
                        return browserListing;
                    }
                }
            }

            BrowserListing browserListing;

            try {
                GenericUrl url = new GenericUrl(BASE_URL + "/list-of-browsers-and-platforms.json?product=" + productName);
                HttpResponse response = newRequest(requestFactory, Method.GET, url).execute();
                browserListing = response.parseAs(BrowserListing.class);
            } catch (IOException e) {
                throw new AutomateException(e.getMessage(), 400);
            }

            if (cache) {
                cacheMap.put(cacheKey, browserListing);
            }

            return browserListing;
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
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
        checkAuthState();

        try {
            authentication.intercept(request);
        } catch (IOException e) {
            throw new BrowserStackException(e);
        }

        return new BrowserStackRequest(request);
    }

    public enum Method {
        GET, POST, PUT, DELETE
    }

    public enum Product {
        LIVE, AUTOMATE, SCREENSHOTS
    }
}
