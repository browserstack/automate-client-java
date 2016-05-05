package com.browserstack.automate;

import com.browserstack.automate.exception.AutomateException;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.ProjectNotFound;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.AccountUsage;
import com.browserstack.automate.model.Build;
import com.browserstack.automate.model.Project;
import com.browserstack.automate.model.Session;
import com.browserstack.client.model.Browser;

import java.util.List;
import java.util.Map;

public interface Automate {

    AccountUsage getAccountUsage() throws AutomateException;

    List<Browser> getBrowsers() throws AutomateException;

    List<Browser> getBrowsers(boolean cache) throws AutomateException;

    List<Project> getProjects() throws AutomateException;

    Project getProject(int projectId) throws ProjectNotFound, AutomateException;

    boolean deleteProject(int projectId) throws AutomateException;

    List<Build> getBuilds(BuildStatus filter, int limit) throws AutomateException;

    List<Build> getBuilds(int limit) throws AutomateException;

    List<Build> getBuilds(BuildStatus status) throws AutomateException;

    List<Build> getBuilds() throws AutomateException;

    Build getBuild(String buildId) throws BuildNotFound, AutomateException;

    boolean deleteBuild(String buildId) throws AutomateException;

    List<Session> getSessions(String buildId, BuildStatus filter,
                              int limit) throws BuildNotFound, AutomateException;

    List<Session> getSessions(String buildId) throws BuildNotFound, AutomateException;

    List<Session> getSessions(String buildId, int limit) throws BuildNotFound, AutomateException;

    List<Session> getSessions(String buildId, BuildStatus status) throws BuildNotFound, AutomateException;

    Session getSession(String sessionId) throws SessionNotFound, AutomateException;

    Session updateSessionStatus(String sessionId, Map<String, Object> data) throws AutomateException;

    Session updateSessionStatus(String sessionId,
                                SessionStatus sessionStatus,
                                String reason) throws SessionNotFound, AutomateException;

    Session updateSessionStatus(String sessionId,
                                SessionStatus sessionStatus) throws SessionNotFound, AutomateException;

    String getSessionLogs(String sessionId) throws SessionNotFound, AutomateException;

    String getSessionLogs(Session session) throws AutomateException;

    String getSessionVideo(String sessionId) throws SessionNotFound, AutomateException;

    boolean deleteSession(String sessionId) throws SessionNotFound, AutomateException;

    String recycleKey() throws AutomateException;


    enum BuildStatus {
        RUNNING, DONE, FAILED
    }

    enum SessionStatus {
        DONE, ERROR
    }
}
