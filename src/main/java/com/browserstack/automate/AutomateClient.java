package com.browserstack.automate;

import com.browserstack.automate.api.*;
import com.browserstack.automate.exception.AutomateException;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.ProjectNotFound;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.client.BrowserStackClient;
import com.browserstack.client.BrowserStackRequest;
import com.browserstack.client.api.Browser;
import com.browserstack.client.exception.BrowserStackException;
import com.browserstack.client.exception.BrowserStackObjectNotFound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Access and manage information about your BrowserStack Automate tests.
 */
public final class AutomateClient extends BrowserStackClient {
    private static final String BASE_URL = "https://www.browserstack.com/automate";

    public enum BuildStatus {
        RUNNING, DONE, FAILED
    }

    public enum SessionStatus {
        DONE, ERROR
    }

    private interface Filters {
        String LIMIT = "limit";
        String FILTER = "filter";
    }

    public AutomateClient(String username, String accessKey) {
        super(BASE_URL, username, accessKey);
    }

    public final Plan getPlan() throws AutomateException {
        try {
            return newRequest(Method.GET, "/plan.json").asObject(Plan.class);
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    public final List<Browser> getBrowsers() throws AutomateException {
        try {
            return Arrays.asList(newRequest(Method.GET, "/browsers.json").asObject(Browser[].class));
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    public final List<Project> getProjects() throws AutomateException {
        List<Project> projects = new ArrayList<Project>();
        ProjectNode[] projectNodes;

        try {
            projectNodes = newRequest(Method.GET, "/projects.json")
                    .asObject(ProjectNode[].class);
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }

        for (ProjectNode pn : projectNodes) {
            if (pn != null && pn.getProject() != null) {
                projects.add(pn.getProject().<Project>setClient(this));
            }
        }

        return projects;
    }

    public final Project getProject(final int projectId) throws ProjectNotFound, AutomateException {
        try {
            ProjectNode projectNode = newRequest(Method.GET, "/projects/{projectId}.json")
                    .routeParam("projectId", "" + projectId)
                    .asObject(ProjectNode.class);

            if (projectNode.getProject() == null) {
                throw new ProjectNotFound("Project not found: " + projectId);
            }

            return projectNode.getProject().setClient(this);
        } catch (BrowserStackObjectNotFound e) {
            throw new ProjectNotFound("Project not found: " + projectId);
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    public final boolean deleteProject(final int projectId) throws AutomateException {
        return new Project(this, projectId).delete();
    }

    public final List<Build> getBuilds(final BuildStatus filter, final int limit)
            throws AutomateException {

        BrowserStackRequest httpRequest = newRequest(Method.GET, "/builds.json");
        if (limit > 0) {
            httpRequest.queryString(Filters.LIMIT, limit);
        }

        if (filter != null) {
            httpRequest.queryString(Filters.FILTER, filter.name().toLowerCase());
        }

        List<BuildNode> buildNodes;
        try {
            buildNodes = Arrays.asList(httpRequest.asObject(BuildNode[].class));
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }

        final List<Build> builds = new ArrayList<Build>();
        for (BuildNode buildNode : buildNodes) {
            if (buildNode != null && buildNode.getBuild() != null) {
                builds.add(buildNode.getBuild().<Build>setClient(this));
            }
        }

        return builds;
    }

    public final List<Build> getBuilds() throws AutomateException {
        return getBuilds(null, 0);
    }

    public final List<Build> getBuilds(final int limit) throws AutomateException {
        return getBuilds(null, limit);
    }

    public final List<Build> getBuilds(final BuildStatus status) throws AutomateException {
        return getBuilds(status, 0);
    }

    public final Build getBuild(final String buildId) throws BuildNotFound, AutomateException {
        try {
            BuildNode buildNode = newRequest(Method.GET, "/builds/{buildId}.json")
                    .routeParam("buildId", buildId)
                    .asObject(BuildNode.class);

            if (buildNode == null) {
                throw new BuildNotFound("Build not found: " + buildId);
            }

            return buildNode.getBuild().setClient(this);
        } catch (BrowserStackObjectNotFound e) {
            throw new BuildNotFound("Build not found: " + buildId);
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    public final boolean deleteBuild(final String buildId) throws AutomateException {
        return new Build(this, buildId).delete();
    }

    public final List<Session> getSessions(final String buildId, final BuildStatus filter, final int limit)
            throws BuildNotFound, AutomateException {

        BrowserStackRequest httpRequest = newRequest(Method.GET, "/builds/{buildId}/sessions.json")
                .routeParam("buildId", buildId);

        if (limit > 0) {
            httpRequest.queryString(Filters.LIMIT, limit);
        }

        if (filter != null) {
            httpRequest.queryString(Filters.FILTER, filter);
        }

        List<SessionNode> sessionNodes;
        try {
            sessionNodes = Arrays.asList(httpRequest.asObject(SessionNode[].class));
        } catch (BrowserStackObjectNotFound e) {
            throw new BuildNotFound("Build not found: " + buildId);
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }

        List<Session> sessions = new ArrayList<Session>();
        for (SessionNode sessionNode : sessionNodes) {
            if (sessionNode != null && sessionNode.getSession() != null) {
                sessions.add(sessionNode.getSession().<Session>setClient(this));
            }
        }

        return sessions;
    }

    public final List<Session> getSessions(final String buildId) throws BuildNotFound, AutomateException {
        return getSessions(buildId, null, 0);
    }

    public final List<Session> getSessions(final String buildId, final int limit)
            throws BuildNotFound, AutomateException {
        return getSessions(buildId, null, limit);
    }

    public final List<Session> getSessions(final String buildId, final BuildStatus status)
            throws BuildNotFound, AutomateException {
        return getSessions(buildId, status, 0);
    }

    public final Session getSession(final String sessionId) throws SessionNotFound, AutomateException {
        try {
            SessionNode sessionNode = newRequest(Method.GET, "/sessions/{sessionId}.json")
                    .routeParam("sessionId", sessionId)
                    .asObject(SessionNode.class);

            if (sessionNode.getSession() == null) {
                throw new SessionNotFound("Session not found: " + sessionId);
            }

            return sessionNode.getSession().setClient(this);
        } catch (BrowserStackObjectNotFound e) {
            throw new SessionNotFound("Session not found: " + sessionId);
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    public final Session updateSessionStatus(final String sessionId,
                                             final SessionStatus sessionStatus,
                                             final String reason) throws SessionNotFound, AutomateException {
        return new Session(this, sessionId).updateStatus(sessionStatus, reason);
    }

    public final Session updateSessionStatus(final String sessionId,
                                             final SessionStatus sessionStatus)
            throws SessionNotFound, AutomateException {
        return new Session(this, sessionId).updateStatus(sessionStatus);
    }

    public final String getSessionLogs(final String sessionId) throws SessionNotFound, AutomateException {
        return getSession(sessionId).getLogs();
    }

    public final boolean deleteSession(final String sessionId) throws SessionNotFound, AutomateException {
        return new Session(this, sessionId).delete();
    }

}
