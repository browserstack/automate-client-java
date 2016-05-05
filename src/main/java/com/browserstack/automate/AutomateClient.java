package com.browserstack.automate;

import com.browserstack.automate.exception.AutomateException;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.ProjectNotFound;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.*;
import com.browserstack.client.BrowserStackClient;
import com.browserstack.client.BrowserStackRequest;
import com.browserstack.client.exception.BrowserStackException;
import com.browserstack.client.exception.BrowserStackObjectNotFound;
import com.browserstack.client.model.Browser;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * Access and manage information about your BrowserStack Automate tests.
 */
public final class AutomateClient extends BrowserStackClient implements Automate {

    private static final String BASE_URL = "https://www.browserstack.com/automate";
    private static final String CACHE_KEY_BROWSERS = "browsers";

    /**
     * Construct an instance of {@link AutomateClient} with your BrowserStack account credentials.
     * <a href="https://www.browserstack.com/accounts/settings">Go here</a> to get them.
     *
     * @param username     Username for your BrowserStack Automate account.
     * @param accessKey    Access Key for your BrowserStack Automate account.
     */
    public AutomateClient(String username, String accessKey) {
        super(BASE_URL, username, accessKey);
    }

    /**
     * Returns details for the BrowserStack Automate plan.
     *
     * @return an instance of {@link AccountUsage} containing subscription details.
     * @throws AutomateException
     */
    public final AccountUsage getAccountUsage() throws AutomateException {
        try {
            return newRequest(Method.GET, "/plan.json").asObject(AccountUsage.class);
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    /**
     * Returns a (cached) list of Desktop and Mobile browsers offered for Automate.
     *
     * @return List of {@link Browser} objects
     * @throws AutomateException
     */
    public final List<Browser> getBrowsers() throws AutomateException {
        return getBrowsers(true);
    }


    /**
     * Returns a (cached) list of Desktop and Mobile browsers offered for Automate.
     *
     * @param  cache     Enable (true) or disable (false) returning of cached responses.
     * @return List of {@link Browser} objects
     * @throws AutomateException
     */
    @SuppressWarnings("unchecked")
    public final List<Browser> getBrowsers(final boolean cache) throws AutomateException {
        try {
            if (cache && cacheMap.containsKey(CACHE_KEY_BROWSERS)) {
                List<Browser> browsers = (List<Browser>) cacheMap.get(CACHE_KEY_BROWSERS);
                if (browsers != null && browsers.size() > 0) {
                    return browsers;
                }
            }

            List<Browser> browsers = Arrays.asList(newRequest(Method.GET, "/browsers.json").asObject(Browser[].class));
            if (cache) {
                cacheMap.put(CACHE_KEY_BROWSERS, browsers);
            }

            return browsers;
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    /**
     * Gets a list of projects
     *
     * <p>Projects are organizational structures for builds.</p>
     *
     * @return List of {@link Project} objects
     * @throws AutomateException
     */
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


    /**
     * Gets the project identified by its identifier.
     *
     * @param  projectId    id for the project to be retrieved.
     * @return List of {@link Project} objects
     * @throws ProjectNotFound
     * @throws AutomateException
     */
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

    /**
     * Deletes the project identified by the specified project identifier.
     *
     * @param  projectId    id for the project to be deleted.
     * @return true or false based on successful deletion of the project.
     * @throws AutomateException
     */
    public final boolean deleteProject(final int projectId) throws AutomateException {
        try {
            ObjectNode result = newRequest(BrowserStackClient.Method.DELETE, "/projects/{projectId}.json")
                    .routeParam("projectId", "" + projectId)
                    .asJsonObject();

            return (result != null && result.path("status").asText("").equals("ok"));
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    /**
     * Gets the list of builds.
     *
     * <p>A build is an organizational structure for tests.</p>
     *
     * @param filter    Return only builds that match the specified build status.
     * @param limit     Limit results to the specified count.
     * @return List of {@link Build} objects.
     * @throws AutomateException
     */
    public final List<Build> getBuilds(final BuildStatus filter, final int limit) throws AutomateException {
        BrowserStackRequest httpRequest;
        try {
            httpRequest = newRequest(Method.GET, "/builds.json");
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }

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

    /**
     * Gets the list of builds.
     *
     * <p>A build is an organizational structure for tests.</p>
     *
     * @return List of {@link Build} objects.
     * @throws AutomateException
     */
    public final List<Build> getBuilds() throws AutomateException {
        return getBuilds(null, 0);
    }

    /**
     * Gets the list of builds.
     *
     * <p>A build is an organizational structure for tests.</p>
     *
     * @param limit     Limit results to the specified count.
     * @return List of {@link Build} objects.
     * @throws AutomateException
     */
    public final List<Build> getBuilds(final int limit) throws AutomateException {
        return getBuilds(null, limit);
    }

    /**
     * Gets the list of builds.
     *
     * <p>A build is an organizational structure for tests.</p>
     *
     * @param status    Include only builds that match the specified build status.
     * @return List of {@link Build} objects.
     * @throws AutomateException
     */
    public final List<Build> getBuilds(final BuildStatus status) throws AutomateException {
        return getBuilds(status, 0);
    }

    /**
     * Gets the build identified by the build identifier.
     *
     * @param buildId   ID that uniquely identifies a build.
     * @return List of {@link Build} objects.
     * @throws BuildNotFound
     * @throws AutomateException
     */
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

    /**
     * Delete the build identified by the build identifier.
     *
     * @param buildId   ID that uniquely identifies a build.
     * @return true or false based on successful deletion of the build.
     * @throws AutomateException
     */
    public final boolean deleteBuild(final String buildId) throws AutomateException {
        try {
            ObjectNode result = newRequest(BrowserStackClient.Method.DELETE, "/builds/{buildId}.json")
                    .routeParam("buildId", buildId)
                    .asJsonObject();

            return (result != null && result.path("status").asText("").equals("ok"));
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    /**
     * Retrieves the list of sessions existing under a specific build.
     *
     * @param buildId   ID that uniquely identifies a build.
     * @param filter    Include only builds that match the specified build status.
     * @param limit     Limit results to the specified count.
     * @return List of {@link Session} objects containing test session information.
     * @throws BuildNotFound
     * @throws AutomateException
     */
    public final List<Session> getSessions(final String buildId, final BuildStatus filter, final int limit)
            throws BuildNotFound, AutomateException {

        BrowserStackRequest httpRequest = null;
        try {
            httpRequest = newRequest(Method.GET, "/builds/{buildId}/sessions.json")
                    .routeParam("buildId", buildId);
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }

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

    /**
     * Retrieves the list of sessions existing under a specific build.
     *
     * @param buildId   ID that uniquely identifies a build.
     * @return List of {@link Session} objects containing test session information.
     * @throws BuildNotFound
     * @throws AutomateException
     */
    public final List<Session> getSessions(final String buildId) throws BuildNotFound, AutomateException {
        return getSessions(buildId, null, 0);
    }

    /**
     * Retrieves the list of sessions existing under a specific build.
     *
     * @param buildId   ID that uniquely identifies a build.
     * @param limit     Limit results to the specified count.
     * @return List of {@link Session} objects containing test session information.
     * @throws BuildNotFound
     * @throws AutomateException
     */
    public final List<Session> getSessions(final String buildId, final int limit)
            throws BuildNotFound, AutomateException {
        return getSessions(buildId, null, limit);
    }

    /**
     * Retrieves the list of sessions existing under a specific build.
     *
     * @param buildId   ID that uniquely identifies a build.
     * @param status    Include only builds that match the specified build status.
     * @return List of {@link Session} objects containing test session information.
     * @throws BuildNotFound
     * @throws AutomateException
     */
    public final List<Session> getSessions(final String buildId,
                                           final BuildStatus status) throws BuildNotFound, AutomateException {
        return getSessions(buildId, status, 0);
    }

    /**
     * Gets the session associated with the specified identifier.
     *
     * @param sessionId   ID that uniquely identifies a session.
     * @return {@link Session} objects containing test session information.
     * @throws BuildNotFound
     * @throws AutomateException
     */
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

    /**
     * Updates the status for a session.
     *
     * @param sessionId   ID that uniquely identifies a session.
     * @param data        Key-Value pairs containing session update information.
     * @return Updated {@link Session} object.
     * @throws AutomateException
     */
    public final Session updateSessionStatus(final String sessionId,
                                             final Map<String, Object> data) throws AutomateException {
        try {
            return newRequest(Method.PUT, "/sessions/{sessionId}.json", data)
                    .routeParam("sessionId", sessionId)
                    .asObject(SessionNode.class)
                    .getSession()
                    .setClient(this);
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    /**
     * Updates the status for a session.
     *
     * @param sessionId         ID that uniquely identifies a session.
     * @param sessionStatus     State with which the session should be marked (Error, Done).
     * @param reason            Message containing reason for marking session with new status.
     * @return Updated {@link Session} object.
     * @throws SessionNotFound
     * @throws AutomateException
     */
    public final Session updateSessionStatus(final String sessionId,
                                             final SessionStatus sessionStatus,
                                             final String reason) throws SessionNotFound, AutomateException {
        final Map<String, Object> data = new HashMap<String, Object>();
        if (sessionStatus != null) {
            data.put("status", sessionStatus.name().toLowerCase());
        }

        if (reason != null && reason.trim().length() > 0) {
            data.put("reason", reason);
        }

        return updateSessionStatus(sessionId, data);
    }

    /**
     * Updates the status for a session.
     *
     * @param sessionId         ID that uniquely identifies a session.
     * @param sessionStatus     State with which the session should be marked (Error, Done).
     * @return Updated {@link Session} object.
     * @throws SessionNotFound
     * @throws AutomateException
     */
    public final Session updateSessionStatus(final String sessionId, final SessionStatus sessionStatus)
            throws SessionNotFound, AutomateException {
        return updateSessionStatus(sessionId, sessionStatus);
    }

    /**
     * Fetches the text logs for a session.
     *
     * @param sessionId     ID that uniquely identifies a session.
     * @return Raw text logs for the session.
     * @throws SessionNotFound
     * @throws AutomateException
     */
    public final String getSessionLogs(final String sessionId) throws SessionNotFound, AutomateException {
        return getSessionLogs(getSession(sessionId));
    }

    /**
     * Fetches the text logs for a session.
     *
     * @param session {@link Session} for which to retrieve logs.
     * @return Raw text logs for the session.
     * @throws AutomateException
     */
    public final String getSessionLogs(final Session session) throws AutomateException {
        if (session == null) {
            throw new AutomateException("Invalid session", 400);
        }

        if (session.getLogUrl() == null) {
            throw new AutomateException("Session logs not found", 404);
        }

        try {
            BrowserStackRequest request = newRequest(Method.GET, session.getLogUrl(), false);
            request.getHttpRequest().getHeaders().setAccept("*/*");
            return request.asString();
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    /**
     * Returns the link for the session video.
     *
     * @param sessionId     ID that uniquely identifies a session.
     * @return Web link to the video for the session.
     * @throws SessionNotFound
     * @throws AutomateException
     */
    public final String getSessionVideo(final String sessionId) throws SessionNotFound, AutomateException {
        return getSession(sessionId).getVideoUrl();
    }

    /**
     * Deletes the session identified by the supplied identifier.
     *
     * @param sessionId     ID that uniquely identifies a session.
     * @return true or false depending on successful deletion.
     * @throws SessionNotFound
     * @throws AutomateException
     */
    public final boolean deleteSession(final String sessionId) throws SessionNotFound, AutomateException {
        try {
            ObjectNode result = newRequest(Method.DELETE, "/sessions/{sessionId}.json")
                    .routeParam("sessionId", sessionId)
                    .asJsonObject();

            return (result != null && result.path("status").asText("").equals("ok"));
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    /**
     * Destroys the current access key and returns a new access key.
     *
     * <p>Note that all uses of the current key will need to be updated.</p>
     *
     * @return the new access key.
     * @throws AutomateException
     */
    public final String recycleKey() throws AutomateException {
        try {
            return newRequest(Method.PUT, "/recycle_key.json").body("{}").asString();
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    private interface Filters {
        String LIMIT = "limit";
        String FILTER = "filter";
    }
}
