package com.browserstack.client;

import com.browserstack.automate.Automate.BuildStatus;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.Build;
import com.browserstack.automate.model.BuildNode;
import com.browserstack.automate.model.Session;
import com.browserstack.automate.model.SessionNode;
import com.browserstack.client.exception.BrowserStackException;
import com.browserstack.client.exception.BrowserStackObjectNotFound;
import com.browserstack.client.model.BrowserListing;
import com.browserstack.client.util.BrowserStackCache;
import com.browserstack.client.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.util.ObjectParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class BrowserStackClient implements BrowserStackClientInterface {
    private static final String BASE_URL = "https://k8s-stagace.bsstag.com";
    private static final String CACHE_KEY_PREFIX_BROWSERS = "browsers";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectParser OBJECT_PARSER = new ObjectParser() {
        @Override
        public <T> T parseAndClose(InputStream inputStream, Charset charset, Class<T> aClass)
                throws IOException {
            return JSON_MAPPER.readValue(inputStream, aClass);
        }

        @Override
        public <T> T parseAndClose(Reader reader, Class<T> aClass) throws IOException {
            return JSON_MAPPER.readValue(reader, aClass);
        }

        @Override
        public Object parseAndClose(InputStream inputStream, Charset charset, Type type)
                throws IOException {
            throw new IOException("Unsupported operation");
        }

        @Override
        public Object parseAndClose(Reader reader, Type type) throws IOException {
            throw new IOException("Unsupported operation");
        }
    };
    private static HttpTransport HTTP_TRANSPORT = new ApacheHttpTransport();
    protected final BrowserStackCache<String, Object> cacheMap;

    private HttpRequestFactory requestFactory;

    private String baseUrl;

    private String username;

    private String accessKey;

    protected BrowserStackClient() {
        this.cacheMap = new BrowserStackCache<>();
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
    }

    static HttpRequestFactory newRequestFactory() {
        return HTTP_TRANSPORT.createRequestFactory(httpRequest -> httpRequest.setParser(OBJECT_PARSER));
    }

    static HttpRequest newRequest(final HttpRequestFactory requestFactory, final Method method, final GenericUrl url) throws BrowserStackException {
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

    /**
     * Sets proxy configuration for requests
     *
     * @param proxyHost     Host of the proxy
     * @param proxyPort     Port of the proxy
     * @param proxyUsername Username of proxy
     * @param proxyPassword password of the proxy
     */

    @Override
    public void setProxy(final String proxyHost, final int proxyPort, final String proxyUsername, final String proxyPassword) {

        if (proxyHost == null || proxyPort == 0) {
            return;
        }

        final HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        HttpClientBuilder clientBuilder = HttpClientBuilder.create().setProxy(proxy);

        if (proxyUsername != null && proxyUsername.length() != 0 && proxyPassword != null && proxyPassword.length() != 0) {
            final BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
            final AuthScope proxyAuthScope = new AuthScope(proxyHost, proxyPort);
            UsernamePasswordCredentials proxyAuthentication =
                    new UsernamePasswordCredentials(proxyUsername, proxyPassword);
            basicCredentialsProvider.setCredentials(proxyAuthScope, proxyAuthentication);

            clientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider);
        }

        final HttpClient client = clientBuilder.build();
        HTTP_TRANSPORT = new ApacheHttpTransport(client);
        this.requestFactory = newRequestFactory();
    }

    protected String getAccessKey() {
        return accessKey;
    }

    protected synchronized void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

    private void checkAuthState() {
        if (this.accessKey == null && this.username == null) {
            throw new IllegalStateException("Missing API credentials");
        }
    }

    protected BrowserListing getBrowsersForProduct(Product product) throws BrowserStackException {
        return getBrowsersForProduct(product, true);
    }

    @SuppressWarnings("unchecked")
    protected BrowserListing getBrowsersForProduct(Product product, boolean cache)
            throws BrowserStackException {
        String productName = product.name().toLowerCase();
        String cacheKey = (CACHE_KEY_PREFIX_BROWSERS + productName).toLowerCase();

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
            GenericUrl url =
                    new GenericUrl(BASE_URL + "/list-of-browsers-and-platforms.json?product=" + productName);
            HttpResponse response = newRequest(requestFactory, Method.GET, url).execute();
            browserListing = response.parseAs(BrowserListing.class);
        } catch (IOException e) {
            throw new BrowserStackException(e.getMessage(), 400);
        }

        if (cache) {
            cacheMap.put(cacheKey, browserListing);
        }

        return browserListing;
    }

    protected BrowserStackRequest newRequest(final Method method, final String path)
            throws BrowserStackException {
        return newRequest(method, path, true);
    }

    protected BrowserStackRequest newRequest(final Method method, final String path,
                                             final boolean prependUrl) throws BrowserStackException {
        String urlPath = (path == null) ? "" : path;
        GenericUrl url = new GenericUrl(prependUrl ? this.baseUrl + urlPath : urlPath);
        return signRequest(newRequest(requestFactory, method, url));
    }

    protected BrowserStackRequest newRequest(final Method method, final String path,
                                             final Map<String, Object> data) throws BrowserStackException {
        return newRequest(method, path, data, null);
    }

    protected BrowserStackRequest newRequest(final Method method, final String path,
                                             final Map<String, Object> data, final Map<String, String> headers)
            throws BrowserStackException {
        BrowserStackRequest request = newRequest(method, path);
        if (headers != null && headers.size() > 0) {
            request.headers(headers);
        }

        if (data != null && data.size() > 0 && request.canContainBody()) {
            try {
                request.header("Content-Type", "application/json")
                        .body(JSON_MAPPER.writeValueAsString(data));
            } catch (JsonProcessingException e) {
                throw new BrowserStackException(e);
            }
        }

        return request;
    }

    protected BrowserStackRequest signRequest(final HttpRequest request) {
        checkAuthState();
        final HttpHeaders header = new HttpHeaders();
        final String combined = this.username + ":" + this.accessKey;
        final String encoded = new String(Base64.encodeBase64(combined.getBytes()));
        final String credential = "Basic " + encoded;
        header.set("Authorization", credential);
        request.setHeaders(header);
        return new BrowserStackRequest(request);
    }

    /**
     * Gets the list of builds.
     *
     * <p>
     * A build is an organizational structure for tests.
     * </p>
     *
     * @param status    Return only builds that match the specified build status.
     * @param limit     Limit results to the specified count.
     * @param buildName build name to be searched with.
     * @return List of {@link Build} objects.
     * @throws BrowserStackException
     */
    @Override
    public List<Build> getBuilds(final BuildStatus status, final int limit, final String buildName)
            throws BrowserStackException {
        BrowserStackRequest httpRequest;
        try {
            httpRequest = newRequest(Method.GET, "/builds.json");
        } catch (BrowserStackException e) {
            throw new BrowserStackException(e);
        }

        if (limit > 0) {
            httpRequest.queryString(Constants.Filter.LIMIT, limit);
        }

        if (status != null) {
            httpRequest.queryString(Constants.Filter.FILTER, status.name().toLowerCase());
        }

        if (buildName != null && !buildName.isEmpty()) {
            httpRequest.queryString(Constants.Filter.BUILD_NAME, buildName);
        }

        final BuildNode[] buildNodes = httpRequest.asObject(BuildNode[].class);

        final List<Build> builds = new ArrayList<>();
        for (BuildNode buildNode : buildNodes) {
            if (buildNode != null && buildNode.getBuild() != null) {
                builds.add(buildNode.getBuild().setClient(this));
            }
        }

        return builds;
    }

    /**
     * Gets the list of builds via build status and the count required
     *
     * <p>
     * A build is an organizational structure for tests.
     * </p>
     *
     * @param status Return only builds that match the specified build status.
     * @param limit  Limit results to the specified count.
     * @return List of {@link Build} objects.
     * @throws BrowserStackException
     */
    @Override
    public List<Build> getBuilds(final BuildStatus status, final int limit)
            throws BrowserStackException {
        return getBuilds(status, limit, null);
    }

    /**
     * Gets the list of builds.
     *
     * <p>
     * A build is an organizational structure for tests.
     * </p>
     *
     * @return List of {@link Build} objects.
     * @throws BrowserStackException
     */
    @Override
    public List<Build> getBuilds() throws BrowserStackException {
        return getBuilds(null, 0);
    }

    /**
     * Gets the list of builds.
     *
     * <p>
     * A build is an organizational structure for tests.
     * </p>
     *
     * @param limit Limit results to the specified count.
     * @return List of {@link Build} objects.
     * @throws BrowserStackException
     */
    @Override
    public List<Build> getBuilds(final int limit) throws BrowserStackException {
        return getBuilds(null, limit);
    }

    /**
     * Gets the list of builds.
     *
     * <p>
     * A build is an organizational structure for tests.
     * </p>
     *
     * @param status Include only builds that match the specified build status.
     * @return List of {@link Build} objects.
     * @throws BrowserStackException
     */
    @Override
    public List<Build> getBuilds(final BuildStatus status) throws BrowserStackException {
        return getBuilds(status, 0);
    }

    /**
     * Gets the build identified by the build identifier.
     *
     * @param buildId ID that uniquely identifies a build.
     * @return List of {@link Build} objects.
     * @throws BuildNotFound
     * @throws BrowserStackException
     */
    @Override
    public Build getBuild(final String buildId) throws BuildNotFound, BrowserStackException {
        try {
            BuildNode buildNode = newRequest(Method.GET, "/builds/{buildId}.json")
                    .routeParam("buildId", buildId).asObject(BuildNode.class);

            if (buildNode == null) {
                throw new BuildNotFound("Build not found: " + buildId);
            }

            return buildNode.getBuild().setClient(this);
        } catch (BrowserStackObjectNotFound e) {
            throw new BuildNotFound("Build not found: " + buildId);
        }
    }

    /**
     * Gets the build identified using the build name.
     *
     * @param buildName Name of the build which will be used for searching
     * @return {@link Build} object
     * @throws BuildNotFound
     * @throws BrowserStackException
     */
    @Override
    public Build getBuildByName(@Nonnull final String buildName) throws BuildNotFound, BrowserStackException {
        final List<Build> build = getBuilds(null, 1, buildName);
        if (build.size() == 1) {
            return build.get(0);
        }
        throw new BuildNotFound("Build not found by name: " + buildName);
    }

    /**
     * Delete the build identified by the build identifier.
     *
     * @param buildId ID that uniquely identifies a build.
     * @return true or false based on successful deletion of the build.
     * @throws BrowserStackException
     */
    @Override
    public boolean deleteBuild(final String buildId) throws BrowserStackException {
        ObjectNode result = newRequest(Method.DELETE, "/builds/{buildId}.json")
                .routeParam("buildId", buildId).asJsonObject();

        String status = (result != null) ? result.path("status").asText() : null;
        return (status != null && status.equals("ok"));
    }

    /**
     * Retrieves the list of sessions existing under a specific build.
     * If no limit is specified, all the sessions will be fetched from that build
     *
     * @param buildId ID that uniquely identifies a build.
     * @param status  Include only builds that match the specified build status.
     * @param limit   Limit results to the specified count.
     * @return List of {@link Session} objects containing test session information.
     * @throws BuildNotFound
     * @throws BrowserStackException
     */
    @Override
    public List<Session> getSessions(final String buildId, final BuildStatus status, final int limit)
            throws BuildNotFound, BrowserStackException {

        // validation of the limit field. Default will be set to 1000 if 0 is provided
        final int totalLimit =
                (limit <= 0 || limit > Constants.Filter.MAX_SESSIONS)
                        ? Constants.Filter.MAX_SESSIONS
                        : limit;
        int totalRequests = totalLimit / Constants.Filter.MAX_LIMIT;

        // An extra request to fetch the remainder sessions
        if ((totalLimit % Constants.Filter.MAX_LIMIT) > 0) {
            totalRequests++;
        }

        final List<Session> sessions = new ArrayList<>();

        // currReq will act as offset to fetch all* sessions from the build
        for (int currReq = 0; currReq < totalRequests; currReq++) {
            final List<SessionNode> sessionNodes = getSessionNodes(buildId, status, totalLimit, currReq * Constants.Filter.MAX_LIMIT);

            for (SessionNode sessionNode : sessionNodes) {
                if (sessionNode != null && sessionNode.getSession() != null) {
                    sessions.add(sessionNode.getSession().setClient(this));
                }
            }

            // break the loop since there are no more sessions left to fetch
            if (sessionNodes.size() < Constants.Filter.MAX_LIMIT) {
                break;
            }
        }

        return sessions;
    }

    private List<SessionNode> getSessionNodes(String buildId, BuildStatus status, int totalLimit, int offset) throws BrowserStackException {
        BrowserStackRequest httpRequest = newRequest(Method.GET, "/builds/{buildId}/sessions.json").routeParam(
                "buildId", buildId);

        httpRequest.queryString(Constants.Filter.LIMIT, totalLimit);
        httpRequest.queryString(Constants.Filter.OFFSET, offset);

        if (status != null) {
            httpRequest.queryString(Constants.Filter.FILTER, status);
        }

        final List<SessionNode> sessionNodes;
        try {
            sessionNodes = Arrays.asList(httpRequest.asObject(SessionNode[].class));
        } catch (BrowserStackObjectNotFound e) {
            throw new BuildNotFound("Build not found: " + buildId);
        }
        return sessionNodes;
    }

    /**
     * Retrieves the list of sessions existing under a specific build.
     *
     * @param buildId ID that uniquely identifies a build.
     * @return List of {@link Session} objects containing test session information.
     * @throws BuildNotFound
     * @throws BrowserStackException
     */
    @Override
    public List<Session> getSessions(final String buildId)
            throws BuildNotFound, BrowserStackException {
        return getSessions(buildId, null, 0);
    }

    /**
     * Retrieves the list of sessions existing under a specific build.
     *
     * @param buildId ID that uniquely identifies a build.
     * @param limit   Limit results to the specified count.
     * @return List of {@link Session} objects containing test session information.
     * @throws BuildNotFound
     * @throws BrowserStackException
     */
    @Override
    public List<Session> getSessions(final String buildId, final int limit)
            throws BuildNotFound, BrowserStackException {
        return getSessions(buildId, null, limit);
    }

    /**
     * Retrieves the list of sessions existing under a specific build.
     *
     * @param buildId ID that uniquely identifies a build.
     * @param status  Include only builds that match the specified build status.
     * @return List of {@link Session} objects containing test session information.
     * @throws BuildNotFound
     * @throws BrowserStackException
     */
    @Override
    public List<Session> getSessions(final String buildId, final BuildStatus status)
            throws BuildNotFound, BrowserStackException {
        return getSessions(buildId, status, 0);
    }

    /**
     * Gets the session associated with the specified identifier.
     *
     * @param sessionId ID that uniquely identifies a session.
     * @return {@link Session} objects containing test session information.
     * @throws SessionNotFound, BrowserStackException
     */
    @Override
    public Session getSession(String sessionId) throws SessionNotFound, BrowserStackException {
        try {
            SessionNode sessionNode = newRequest(Method.GET, "/sessions/{sessionId}.json")
                    .routeParam("sessionId", sessionId).asObject(SessionNode.class);

            if (sessionNode.getSession() == null) {
                throw new SessionNotFound("Session not found: " + sessionId);
            }

            return sessionNode.getSession().setClient(this);
        } catch (BrowserStackObjectNotFound e) {
            throw new SessionNotFound("Session not found: " + sessionId);
        }
    }

    public enum Method {
        GET, POST, PUT, DELETE
    }

    public enum Product {
        LIVE, AUTOMATE, SCREENSHOTS
    }
}
