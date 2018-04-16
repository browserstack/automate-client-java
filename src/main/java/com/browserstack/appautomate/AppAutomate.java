package com.browserstack.appautomate;

import java.io.FileNotFoundException;
import java.util.List;
import com.browserstack.automate.Automate.BuildStatus;
import com.browserstack.automate.exception.AppAutomateException;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.InvalidFileExtensionException;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.Build;
import com.browserstack.automate.model.Session;

public interface AppAutomate {

  public Session getSession(String sessionId) throws SessionNotFound, AppAutomateException;

  public String uploadApp(String filePath)
      throws AppAutomateException, FileNotFoundException, InvalidFileExtensionException;
  
  List<Build> getBuilds(BuildStatus status, int limit) throws AppAutomateException;

  List<Build> getBuilds(int limit) throws AppAutomateException;

  List<Build> getBuilds(BuildStatus status) throws AppAutomateException;

  List<Build> getBuilds() throws AppAutomateException;

  Build getBuild(String buildId) throws BuildNotFound, AppAutomateException;

  boolean deleteBuild(String buildId) throws AppAutomateException;

  List<Session> getSessions(String buildId, BuildStatus status,
                            int limit) throws BuildNotFound, AppAutomateException;

  List<Session> getSessions(String buildId) throws BuildNotFound, AppAutomateException;

  List<Session> getSessions(String buildId, int limit) throws BuildNotFound, AppAutomateException;

  List<Session> getSessions(String buildId, BuildStatus status) throws BuildNotFound, AppAutomateException;


}
