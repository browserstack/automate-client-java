package com.browserstack.automate;

import com.browserstack.automate.AutomateClient.BuildStatus;
import com.browserstack.automate.api.AccountUsage;
import com.browserstack.automate.api.Build;
import com.browserstack.automate.api.Project;
import com.browserstack.automate.api.Session;
import com.browserstack.automate.exception.AutomateException;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.ProjectNotFound;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.client.api.Browser;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AutomateClientTest {

    private String username;
    private String key;

    @Before
    public void setup() {
        username = System.getenv("BROWSERSTACK_USERNAME");
        key = System.getenv("BROWSERSTACK_KEY");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidClientArgs() {
        new AutomateClient(null, null);
    }

    @Test
    public void testValidClientArgs() {
        AutomateClient automateClient = new AutomateClient("invalid_username", "invalid_key");
        try {
            automateClient.getAccountUsage();
        } catch (AutomateException e) {
            assertTrue(e.getMessage().toLowerCase().contains("access denied"));
        }
    }

    @Test
    public void testGetPlan() {
        AutomateClient automateClient = new AutomateClient(username, key);
        try {
            AccountUsage accountUsage = automateClient.getAccountUsage();
            assertTrue("Automate account usage", accountUsage.getAutomatePlan().equals("Basic"));
            assertTrue("Parallel session count", accountUsage.getParallelSessionsRunning() >= 0);
            assertTrue("Max parallel count", accountUsage.getParallelSessionsMaxAllowed() > 0);
        } catch (AutomateException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testGetBrowsers() {
        AutomateClient automateClient = new AutomateClient(username, key);
        try {
            List<Browser> browsers = automateClient.getBrowsers();
            assertTrue("Automate: Browsers", browsers.size() > 0);
            assertTrue("Automate: Browser", browsers.get(0).getBrowser() != null);
        } catch (AutomateException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testGetBrowsersCached() {
        AutomateClient automateClient = new AutomateClient(username, key);
        try {
            automateClient.getBrowsers();

            long startTime = System.currentTimeMillis();
            List<Browser> browsers = automateClient.getBrowsers();
            long timeDiff = System.currentTimeMillis() - startTime;
            if (timeDiff > 100) {
                assertTrue(false);
            }

            assertTrue("Automate: Browsers", browsers.size() > 0);
            assertTrue("Automate: Browser", browsers.get(0).getBrowser() != null);
        } catch (AutomateException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testGetProjects() {
        AutomateClient automateClient = new AutomateClient(username, key);
        try {
            List<Project> projects = automateClient.getProjects();
            assertTrue("Project count", projects.size() > 0);
            assertTrue("Project Id of first project", projects.get(0).getId() > 0);
        } catch (AutomateException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testGetProject() {
        AutomateClient automateClient = new AutomateClient(username, key);
        try {
            List<Project> projects = automateClient.getProjects();
            Project project = automateClient.getProject(projects.get(0).getId());
            assertTrue("Project Id", project.getId() > 0);
            assertTrue("Group Id", project.getGroupId() > 0);
            assertTrue("Builds", project.getBuilds().size() > 0);
        } catch (ProjectNotFound e) {
            assertTrue(false);
        } catch (AutomateException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testGetBuilds() {
        AutomateClient automateClient = new AutomateClient(username, key);
        try {
            assertTrue("Builds", automateClient.getBuilds().size() > 0);

            assertTrue("Builds: Done", automateClient.getBuilds(BuildStatus.DONE) != null);
            assertTrue("Builds: Failed", automateClient.getBuilds(BuildStatus.FAILED) != null);
            assertTrue("Builds: Running", automateClient.getBuilds(BuildStatus.RUNNING) != null);

            assertTrue("Builds: Limit", automateClient.getBuilds(3).size() == 3);
            assertTrue("Builds: Done + Limit", automateClient.getBuilds(BuildStatus.DONE, 3).size() == 3);
        } catch (AutomateException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testGetBuild() {
        AutomateClient automateClient = new AutomateClient(username, key);
        try {
            Build build = automateClient.getBuild(automateClient.getBuilds().get(0).getId());
            assertTrue("Build", build != null);
            assertTrue("Build Id", build.getId() != null);
            assertTrue("Build Status", build.getStatus() != null);
            assertTrue("Build Name", build.getName() != null);
            assertTrue("Build Sessions", build.getSessions().size() > 0);
            assertTrue("Build Session Id", build.getSessions().get(0).getId() != null);
        } catch (BuildNotFound buildNotFound) {
            assertTrue(false);
        } catch (AutomateException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testGetSessions() {
        AutomateClient automateClient = new AutomateClient(username, key);
        try {
            String buildId = automateClient.getBuilds().get(0).getId();
            assertTrue("Sessions", automateClient.getSessions(buildId).size() > 0);

            assertTrue("Sessions: Running", automateClient.getSessions(buildId, BuildStatus.RUNNING) != null);
            assertTrue("Sessions: Failed", automateClient.getSessions(buildId, BuildStatus.FAILED) != null);
            assertTrue("Sessions: Done", automateClient.getSessions(buildId, BuildStatus.DONE) != null);

            assertTrue("Sessions: Limit", automateClient.getSessions(buildId, 3).size() == 3);
            assertTrue("Sessions: Done + Limit", automateClient.getSessions(buildId, BuildStatus.DONE, 3).size() == 3);
        } catch (BuildNotFound e) {
            assertTrue(false);
        } catch (AutomateException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testGetSession() {
        AutomateClient automateClient = new AutomateClient(username, key);
        try {
            String buildId = automateClient.getBuilds().get(0).getId();
            List<Session> sessions = automateClient.getSessions(buildId);
            Session session = automateClient.getSession(sessions.get(0).getId());

            assertTrue("Session", session != null);
            assertTrue("Session Id", session.getId() != null);
            assertTrue("Session Browser", session.getBrowser() != null);
        } catch (BuildNotFound e) {
            assertTrue(false);
        } catch (SessionNotFound e) {
            assertTrue(false);
        } catch (AutomateException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testGetSessionLogs() {
        AutomateClient automateClient = new AutomateClient(username, key);
        try {
            String buildId = automateClient.getBuilds().get(0).getId();
            List<Session> sessions = automateClient.getSessions(buildId);

            String logs = sessions.get(0).getLogs();
            assertTrue("Session Logs", logs != null && !logs.isEmpty());

            logs = automateClient.getSessionLogs(sessions.get(0).getId());
            assertTrue("Session Logs", logs != null && !logs.isEmpty());
        } catch (BuildNotFound e) {
            assertTrue(false);
        } catch (SessionNotFound e) {
            assertTrue(false);
        } catch (AutomateException e) {
            assertTrue(false);
        }
    }
}
