package com.browserstack.automate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.browserstack.appautomate.AppAutomateClient;
import com.browserstack.automate.exception.AppAutomateException;
import com.browserstack.automate.exception.AutomateException;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.automate.exception.InvalidFileExtensionException;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.Build;
import com.browserstack.automate.model.Session;
import com.browserstack.client.util.Tools;

public class AppAutomateClientTest {
  
  private String username;
  private String key;
  private AppAutomateClient appAutomateClient;

  @Before
  public void setup() {
    username = System.getenv("BROWSERSTACK_USER");
    key = System.getenv("BROWSERSTACK_ACCESSKEY");
    appAutomateClient = new AppAutomateClient(username, key);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidClientArgs() {
    new AppAutomateClient(null, null);
  }
  
  @Test(expected = FileNotFoundException.class)
  public void testAppUploadInvalidFileLocation() throws FileNotFoundException, AppAutomateException, InvalidFileExtensionException {
    appAutomateClient.uploadApp("some_random_path");
  }
  
  @Test
  public void testAppUploadInvalidCredentials() throws FileNotFoundException, InvalidFileExtensionException  {
    AppAutomateClient appAutomateClient2 = new AppAutomateClient("invalid_username", "invalid_key");
    try {
      String absolutePath = new File(getClass().getClassLoader().getResource("dummy.apk").getFile()).getAbsolutePath();
      appAutomateClient2.uploadApp(absolutePath);
    } catch (AppAutomateException e) {
      assertTrue(e.getMessage().toLowerCase().contains("unauthorized"));
    }
  }
  
  @Test
  public void testAppUpload() {
    String appPath = System.getenv("APP_PATH");
    if(appPath != null) {
      String appId;
      try {
        appId = appAutomateClient.uploadApp(appPath);
        assertTrue("App uploaded successfully :",!Tools.isStringEmpty(appId));
      } catch (AppAutomateException e) {
        assertTrue("AutomateException ",false);
      } catch (FileNotFoundException e) {
        assertTrue("FileNotFound : ",false);
      } catch (InvalidFileExtensionException e) {
        assertTrue("InvalidExtension : ",false);
      }
      
    }
  }
  
  @Test
  public void testGetSession() {
    try {
      Build build = appAutomateClient.getBuilds().get(0);
      List<Session> sessions = appAutomateClient.getSessions(build.getId());
      
      Session session1 = sessions.get(0);
      assertTrue("Session", session1 != null);
      assertTrue("Session Id", session1.getId() != null);

      Session session2 = appAutomateClient.getSession(session1.getId());
      assertEquals(session1.getId(), session2.getId());
      assertEquals(session1.getBrowser(), session2.getBrowser());
    } catch (BuildNotFound e) {
      assertTrue(false);
    } catch (SessionNotFound e) {
      assertTrue(false);
    } catch (AppAutomateException e) {
      assertTrue(false);
    }
  }
 
}
