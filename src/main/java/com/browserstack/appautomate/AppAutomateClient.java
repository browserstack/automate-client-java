package com.browserstack.appautomate;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import com.browserstack.automate.Automate.BuildStatus;
import com.browserstack.automate.exception.AppAutomateException;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.InvalidFileExtensionException;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.AppUploadResponse;
import com.browserstack.automate.model.Build;
import com.browserstack.automate.model.Session;
import com.browserstack.client.BrowserStackClient;
import com.browserstack.client.exception.BrowserStackException;
import com.browserstack.client.util.Tools;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.MultipartContent;

public class AppAutomateClient extends BrowserStackClient implements AppAutomate {

  private static final String BASE_URL = "https://api-cloud.browserstack.com/app-automate";

  public AppAutomateClient(String username, String accessKey) {
    super(BASE_URL, username, accessKey);
  }

  /**
   * Gets the session associated with the specified identifier.
   *
   * @param sessionId ID that uniquely identifies a session.
   * @return {@link Session} objects containing test session information.
   * @throws SessionNotFound
   * @throws AppAutomateException
   */
  public Session getSession(String sessionId) throws SessionNotFound, AppAutomateException {
    try {
      return super.getSession(sessionId);
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }
  }

  /**
   * Gets the filePath of app to be uploaded.
   *
   * @param filePath absolute path of app to be uploaded.
   * @return AppUploadResponse object containing app upload response details.
   * @throws AppAutomateException
   * @throws FileNotFoundException
   * @throws InvalidFileExtensionException
   */
  public AppUploadResponse uploadApp(String filePath)
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

      if (appUploadResponse == null || Tools.isStringEmpty(appUploadResponse.getAppUrl())) {
        throw new AppAutomateException("App upload failed!", 0);
      }
      return appUploadResponse;
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }
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
  public List<Build> getBuilds(final BuildStatus status, final int limit)
      throws AppAutomateException {
    try {
      return super.getBuilds(status, limit);
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }
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
  public List<Build> getBuilds() throws AppAutomateException {
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
  public List<Build> getBuilds(final int limit) throws AppAutomateException {
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
  public List<Build> getBuilds(final BuildStatus status) throws AppAutomateException {
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
  public Build getBuild(final String buildId) throws BuildNotFound, AppAutomateException {
    try {
      return super.getBuild(buildId);
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
  public boolean deleteBuild(final String buildId) throws AppAutomateException {
    try {
      return super.deleteBuild(buildId);
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
  public List<Session> getSessions(final String buildId, final BuildStatus status, final int limit)
      throws BuildNotFound, AppAutomateException {
    try {
      return super.getSessions(buildId, status, limit);
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }
  }

  /**
   * Retrieves the list of sessions existing under a specific build.
   *
   * @param buildId ID that uniquely identifies a build.
   * @return List of {@link Session} objects containing test session information.
   * @throws BuildNotFound
   * @throws AppAutomateException
   */
  public List<Session> getSessions(final String buildId)
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
  public List<Session> getSessions(final String buildId, final int limit)
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
  public List<Session> getSessions(final String buildId, final BuildStatus status)
      throws BuildNotFound, AppAutomateException {
    return getSessions(buildId, status, 0);
  }

}

