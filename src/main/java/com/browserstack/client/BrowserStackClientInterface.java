package com.browserstack.client;

import java.util.List;
import com.browserstack.automate.Automate.BuildStatus;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.Build;
import com.browserstack.automate.model.Session;
import com.browserstack.client.exception.BrowserStackException;

public interface BrowserStackClientInterface {

  public Session getSession(String sessionId) throws SessionNotFound, BrowserStackException;

  List<Build> getBuilds(BuildStatus status, int limit, String buildName) throws BrowserStackException;

  List<Build> getBuilds(BuildStatus status, int limit) throws BrowserStackException;

  List<Build> getBuilds(int limit) throws BrowserStackException;

  List<Build> getBuilds(BuildStatus status) throws BrowserStackException;

  List<Build> getBuilds() throws BrowserStackException;

  Build getBuild(String buildId) throws BuildNotFound, BrowserStackException;

  Build getBuildByName(String buildName) throws BuildNotFound, BrowserStackException;

  boolean deleteBuild(String buildId) throws BrowserStackException;

  List<Session> getSessions(String buildId, BuildStatus status, int limit)
      throws BuildNotFound, BrowserStackException;

  List<Session> getSessions(String buildId) throws BuildNotFound, BrowserStackException;

  List<Session> getSessions(String buildId, int limit) throws BuildNotFound, BrowserStackException;

  List<Session> getSessions(String buildId, BuildStatus status)
      throws BuildNotFound, BrowserStackException;

  void setProxy(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword);

}
