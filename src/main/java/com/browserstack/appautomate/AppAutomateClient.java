package com.browserstack.appautomate;

import java.io.File;
import java.io.FileNotFoundException;
import com.browserstack.automate.exception.AppAutomateException;
import com.browserstack.automate.exception.InvalidFileExtensionException;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.Session;
import com.browserstack.automate.model.SessionNode;
import com.browserstack.client.BrowserStackClient;
import com.browserstack.client.exception.BrowserStackException;
import com.browserstack.client.exception.BrowserStackObjectNotFound;
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

      ObjectNode response = newRequest(Method.POST, "/upload").body(content).asJsonObject();

      String appId = (response != null) ? response.path("app_url").asText() : null;

      return appId;
    } catch (BrowserStackException e) {
      throw new AppAutomateException(e);
    }
  }

}

