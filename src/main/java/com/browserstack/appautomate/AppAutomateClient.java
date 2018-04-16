package com.browserstack.appautomate;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.browserstack.automate.Automate.BuildStatus;
import com.browserstack.automate.exception.AppAutomateException;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.InvalidFileExtensionException;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.AppUploadResponse;
import com.browserstack.automate.model.Build;
import com.browserstack.automate.model.BuildNode;
import com.browserstack.automate.model.Session;
import com.browserstack.automate.model.SessionNode;
import com.browserstack.client.BrowserStackClient;
import com.browserstack.client.BrowserStackRequest;
import com.browserstack.client.exception.BrowserStackException;
import com.browserstack.client.exception.BrowserStackObjectNotFound;
import com.browserstack.client.util.Constants;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.MultipartContent;

public class AppAutomateClient extends BrowserStackClient implements AppAutomate {

  private static final String BASE_URL = "https://api-cloud.browserstack.com/app-automate";

  public AppAutomateClient(String username, String accessKey) {
    super(BASE_URL, username, accessKey);
  }

  public Session getSession(String sessionId) throws SessionNotFound, AppAutomateException {
    try {
      SessionNode sessionNode = newRequest(Method.GET, "/sessions/{sessionId}.json")
          .routeParam("sessionId", sessionId).asObject(SessionNode.class);

      if (sessionNode.getSession() == null) {
        throw new SessionNotFound("Session not found: " + sessionId);
      }

      return sessionNode.getSession().setClient(this);
    } catch (BrowserStackObjectNotFound e) {
      throw new SessionNotFound("Session not found: " + sessionId);
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }
  }

  public String uploadApp(String filePath)
      throws AppAutomateException, FileNotFoundException, InvalidFileExtensionException {
    try {
      File file = new File(filePath);

      if (!file.exists()) {
        throw new FileNotFoundException("File not found at " + filePath);
      }

      if (!filePath.endsWith(".apk") && !filePath.endsWith(".ipa")) {
        throw new InvalidFileExtensionException("File extension should be only .apk or .ipa.");
      }

      MultipartContent content = new MultipartContent().setMediaType(
          new HttpMediaType("multipart/form-data").setParameter("boundary", "__END_OF_PART__"));

      FileContent fileContent = new FileContent("multipart/form-data", file);

      MultipartContent.Part part = new MultipartContent.Part(fileContent);
      part.setHeaders(new HttpHeaders().set("Content-Disposition",
          String.format("form-data; name=\"file\"; filename=\"%s\"", file.getName())));
      content.addPart(part);

      AppUploadResponse appUploadResponse =
          newRequest(Method.POST, "/upload").body(content).asObject(AppUploadResponse.class);

      if (appUploadResponse.getAppUrl() != null) {
        return appUploadResponse.getAppUrl();
      }
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }
    return null;
  }

  /**
   * Gets the list of builds.
   *
   * <p>
   * A build is an organizational structure for tests.
   * </p>
   *
   * @param status Return only builds that match the specified build status.
   * @param limit Limit results to the specified count.
   * @return List of {@link Build} objects.
   * @throws AppAutomateException
   */
  public final List<Build> getBuilds(final BuildStatus status, final int limit)
      throws AppAutomateException {
    BrowserStackRequest httpRequest;
    try {
      httpRequest = newRequest(Method.GET, "/builds.json");
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }

    if (limit > 0) {
      httpRequest.queryString(Constants.Filter.LIMIT, limit);
    }

    if (status != null) {
      httpRequest.queryString(Constants.Filter.FILTER, status.name().toLowerCase());
    }

    List<BuildNode> buildNodes;
    try {
      buildNodes = Arrays.asList(httpRequest.asObject(BuildNode[].class));
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }

    final List<Build> builds = new ArrayList<Build>();
    for (BuildNode buildNode : buildNodes) {
      if (buildNode != null && buildNode.getBuild() != null) {
        builds.add(buildNode.getBuild().<Build>setClient(this));
      }
    }

    return builds;
  }

  /**
   * Gets the list of builds.
   *
   * <p>
   * A build is an organizational structure for tests.
   * </p>
   *
   * @return List of {@link Build} objects.
   * @throws AppAutomateException
   */
  public final List<Build> getBuilds() throws AppAutomateException {
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
   * @throws AppAutomateException
   */
  public final List<Build> getBuilds(final int limit) throws AppAutomateException {
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
   * @throws AppAutomateException
   */
  public final List<Build> getBuilds(final BuildStatus status) throws AppAutomateException {
    return getBuilds(status, 0);
  }

  /**
   * Gets the build identified by the build identifier.
   *
   * @param buildId ID that uniquely identifies a build.
   * @return List of {@link Build} objects.
   * @throws BuildNotFound
   * @throws AppAutomateException
   */
  public final Build getBuild(final String buildId) throws BuildNotFound, AppAutomateException {
    try {
      BuildNode buildNode = newRequest(Method.GET, "/builds/{buildId}.json")
          .routeParam("buildId", buildId).asObject(BuildNode.class);

      if (buildNode == null) {
        throw new BuildNotFound("Build not found: " + buildId);
      }

      return buildNode.getBuild().setClient(this);
    } catch (BrowserStackObjectNotFound e) {
      throw new BuildNotFound("Build not found: " + buildId);
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }
  }

  /**
   * Delete the build identified by the build identifier.
   *
   * @param buildId ID that uniquely identifies a build.
   * @return true or false based on successful deletion of the build.
   * @throws AppAutomateException
   */
  public final boolean deleteBuild(final String buildId) throws AppAutomateException {
    try {
      ObjectNode result = newRequest(BrowserStackClient.Method.DELETE, "/builds/{buildId}.json")
          .routeParam("buildId", buildId).asJsonObject();

      String status = (result != null) ? result.path("status").asText() : null;
      return (status != null && status.equals("ok"));
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }
  }

  /**
   * Retrieves the list of sessions existing under a specific build.
   *
   * @param buildId ID that uniquely identifies a build.
   * @param status Include only builds that match the specified build status.
   * @param limit Limit results to the specified count.
   * @return List of {@link Session} objects containing test session information.
   * @throws BuildNotFound
   * @throws AppAutomateException
   */
  public final List<Session> getSessions(final String buildId, final BuildStatus status,
      final int limit) throws BuildNotFound, AppAutomateException {

    BrowserStackRequest httpRequest = null;
    try {
      httpRequest =
          newRequest(Method.GET, "/builds/{buildId}/sessions.json").routeParam("buildId", buildId);
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }

    if (limit > 0) {
      httpRequest.queryString(Constants.Filter.LIMIT, limit);
    }

    if (status != null) {
      httpRequest.queryString(Constants.Filter.FILTER, status);
    }

    List<SessionNode> sessionNodes;
    try {
      sessionNodes = Arrays.asList(httpRequest.asObject(SessionNode[].class));
    } catch (BrowserStackObjectNotFound e) {
      throw new BuildNotFound("Build not found: " + buildId);
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }

    List<Session> sessions = new ArrayList<Session>();
    for (SessionNode sessionNode : sessionNodes) {
      if (sessionNode != null && sessionNode.getSession() != null) {
        sessions.add(sessionNode.getSession().<Session>setClient(this));
      }
    }

    return sessions;
  }

  /**
   * Retrieves the list of sessions existing under a specific build.
   *
   * @param buildId ID that uniquely identifies a build.
   * @return List of {@link Session} objects containing test session information.
   * @throws BuildNotFound
   * @throws AppAutomateException
   */
  public final List<Session> getSessions(final String buildId)
      throws BuildNotFound, AppAutomateException {
    return getSessions(buildId, null, 0);
  }

  /**
   * Retrieves the list of sessions existing under a specific build.
   *
   * @param buildId ID that uniquely identifies a build.
   * @param limit Limit results to the specified count.
   * @return List of {@link Session} objects containing test session information.
   * @throws BuildNotFound
   * @throws AppAutomateException
   */
  public final List<Session> getSessions(final String buildId, final int limit)
      throws BuildNotFound, AppAutomateException {
    return getSessions(buildId, null, limit);
  }

  /**
   * Retrieves the list of sessions existing under a specific build.
   *
   * @param buildId ID that uniquely identifies a build.
   * @param status Include only builds that match the specified build status.
   * @return List of {@link Session} objects containing test session information.
   * @throws BuildNotFound
   * @throws AppAutomateException
   */
  public final List<Session> getSessions(final String buildId, final BuildStatus status)
      throws BuildNotFound, AppAutomateException {
    return getSessions(buildId, status, 0);
  }

}

