package com.browserstack.automate;

import com.browserstack.automate.Automate.BuildStatus;
import com.browserstack.automate.exception.AutomateException;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.ProjectNotFound;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.AccountUsage;
import com.browserstack.automate.model.Build;
import com.browserstack.automate.model.Project;
import com.browserstack.automate.model.Session;
import com.browserstack.client.model.Browser;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AutomateClientTest {

  private AutomateClient automateClient;

  @Before
  public void setup() {
    final String username = System.getenv("BROWSERSTACK_USER");
    final String key = System.getenv("BROWSERSTACK_ACCESSKEY");
    automateClient = new AutomateClient(username, key);
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
    try {
      AccountUsage accountUsage = automateClient.getAccountUsage();
      assertTrue("Automate account usage", accountUsage.getAutomatePlan().length() > 0);
      assertTrue("Parallel session count", accountUsage.getParallelSessionsRunning() >= 0);
      assertTrue("Max parallel count", accountUsage.getParallelSessionsMaxAllowed() > 0);
    } catch (AutomateException e) {
      fail();
    }
  }

  @Test
  public void testGetBrowsers() {
    try {
      List<Browser> browsers = automateClient.getBrowsers();
      assertTrue("Automate: Browsers", browsers.size() > 0);
      assertNotNull("Automate: Browser", browsers.get(0).getBrowser());
    } catch (AutomateException e) {
      fail();
    }
  }

  @Test
  public void testGetBrowsersCached() {
    try {
      automateClient.getBrowsers();

      long startTime = System.currentTimeMillis();
      List<Browser> browsers = automateClient.getBrowsers();
      long timeDiff = System.currentTimeMillis() - startTime;
      if (timeDiff > 100) {
        fail();
      }

      assertTrue("Automate: Browsers", browsers.size() > 0);
      assertNotNull("Automate: Browser", browsers.get(0).getBrowser());
    } catch (AutomateException e) {
      fail();
    }
  }

  @Test
  public void testGetProjects() {
    try {
      List<Project> projects = automateClient.getProjects();
      assertTrue("Project count", projects.size() > 0);
      assertTrue("Project Id of first project", projects.get(0).getId() > 0);
    } catch (AutomateException e) {
      fail();
    }
  }

  @Test
  public void testGetProject() {
    try {
      List<Project> projects = automateClient.getProjects();
      Project project = automateClient.getProject(projects.get(0).getId());
      assertTrue("Project Id", project.getId() > 0);
      assertTrue("Group Id", project.getGroupId() > 0);
      assertTrue("Builds", project.getBuilds().size() > 0);
    } catch (ProjectNotFound | AutomateException e) {
      fail();
    }
  }

  @Test
  public void testGetBuilds() {
    try {
      assertTrue("Builds", automateClient.getBuilds().size() > 0);

      assertNotNull("Builds: Done", automateClient.getBuilds(BuildStatus.DONE));
      assertNotNull("Builds: Failed", automateClient.getBuilds(BuildStatus.FAILED));
      assertNotNull("Builds: Running", automateClient.getBuilds(BuildStatus.RUNNING));

      assertEquals("Builds: Limit", 3, automateClient.getBuilds(3).size());
      assertEquals("Builds: Done + Limit", 3, automateClient.getBuilds(BuildStatus.DONE, 3).size());
    } catch (AutomateException e) {
      fail();
    }
  }

  @Test
  public void testGetBuild() {
    try {
      Build build = automateClient.getBuild(automateClient.getBuilds().get(0).getId());
      assertNotNull("Build", build);
      assertNotNull("Build Id", build.getId());
      assertNotNull("Build Status", build.getStatus());
      assertNotNull("Build Name", build.getName());
      assertTrue("Build Sessions", build.getSessions().size() > 0);
      assertNotNull("Build Session Id", build.getSessions().get(0).getId());
    } catch (BuildNotFound | AutomateException buildNotFound) {
      fail();
    }
  }

  @Test
  public void testGetSessions() {
    try {
      String buildId = automateClient.getBuilds().get(0).getId();
      assertTrue("Sessions", automateClient.getSessions(buildId).size() > 0);

      assertNotNull("Sessions: Running", automateClient.getSessions(buildId, BuildStatus.RUNNING));
      assertNotNull("Sessions: Failed", automateClient.getSessions(buildId, BuildStatus.FAILED));
      assertNotNull("Sessions: Done", automateClient.getSessions(buildId, BuildStatus.DONE));

      List<Session> sessions = automateClient.getSessions(buildId);
      if (sessions.size() > 1) {
        assertEquals("Sessions: Limit", 1, automateClient.getSessions(buildId, 1).size());
        assertEquals("Sessions: Done + Limit", 1, automateClient.getSessions(buildId, BuildStatus.DONE, 1).size());
      }
    } catch (BuildNotFound | AutomateException e) {
      fail();
    }
  }

  @Test
  public void testGetSession() {
    try {
      Build build = automateClient.getBuilds().get(0);
      List<Session> sessions1 = build.getSessions();
      List<Session> sessions2 = automateClient.getSessions(build.getId());
      assertEquals(sessions1.size(), sessions2.size());

      Session session1 = sessions1.get(0);
      assertNotNull("Session", session1);
      assertNotNull("Session Id", session1.getId());

      Session session2 = automateClient.getSession(sessions2.get(0).getId());
      assertEquals(session1.getId(), session2.getId());
      assertEquals(session1.getBrowser(), session2.getBrowser());
    } catch (BuildNotFound | SessionNotFound | AutomateException e) {
      fail();
    }
  }

  @Test
  public void testGetSessionLogs() {
    // TODO: Verify if logs are non-empty
    // Cannot currently be tested during in-progress sessions
    try {
      String buildId = automateClient.getBuilds().get(0).getId();
      List<Session> sessions = automateClient.getSessions(buildId);

      String logs = sessions.get(0).getLogs();
      assertNotNull("Session Logs", logs);

      logs = automateClient.getSessionLogs(sessions.get(0).getId());
      assertNotNull("Session Logs", logs);
    } catch (BuildNotFound | SessionNotFound | AutomateException e) {
      fail();
    }
  }

  @Test
  public void testGetSessionVideo() {
    try {
      String buildId = automateClient.getBuilds().get(0).getId();
      List<Session> sessions = automateClient.getSessions(buildId);

      String videoUrl1 = sessions.get(0).getVideoUrl();
      String videoUrl2 = automateClient.getSessionVideo(sessions.get(0).getId());
      assertEquals(videoUrl1.split(Pattern.quote("?"))[0], videoUrl2.split(Pattern.quote("?"))[0]);
    } catch (BuildNotFound | SessionNotFound | AutomateException e) {
      fail();
    }
  }

  // @Test
  public void testRecycleKey() {
    try {
      String newAccessKey = automateClient.recycleKey();
      assertTrue(newAccessKey != null && newAccessKey.length() > 0);
      testGetPlan();
    } catch (AutomateException e) {
      fail();
    }
  }
}
