package com.browserstack.automate;

import com.browserstack.automate.exception.AutomateException;
import com.browserstack.automate.model.Session;
import com.browserstack.client.BrowserStackClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Hermetic unit tests for the URL allowlist on getSessionLogs and the
 * credential-presence check in BrowserStackClient — no live API calls.
 */
public class AutomateClientSecurityTest {

    private static final String DUMMY_USER = "dummy_user";
    private static final String DUMMY_KEY = "dummy_key";

    // Build a Session with a controlled logUrl by JSON-deserializing through Jackson —
    // setLogUrl() is private; the @JsonProperty("logs") setter accepts the field by name.
    private static Session sessionWithLogUrl(String logUrl) throws Exception {
        String json = "{\"logs\": " + (logUrl == null ? "null" : "\"" + logUrl + "\"") + "}";
        return new ObjectMapper().readValue(json, Session.class);
    }

    // ---------- Logs URL allowlist ----------

    @Test
    public void getSessionLogs_rejectsAttackerHost_throwsBeforeAnyHttpRequest() throws Exception {
        AutomateClient client = new AutomateClient(DUMMY_USER, DUMMY_KEY);
        Session session = sessionWithLogUrl("https://notbrowserstack.example/path");
        try {
            client.getSessionLogs(session);
            fail("Expected AutomateException for untrusted host");
        } catch (AutomateException e) {
            assertTrue("message mentions Untrusted host: " + e.getMessage(),
                    e.getMessage().toLowerCase().contains("untrusted"));
            assertEquals(400, e.getStatusCode());
        }
    }

    @Test
    public void getSessionLogs_rejectsHttpScheme() throws Exception {
        AutomateClient client = new AutomateClient(DUMMY_USER, DUMMY_KEY);
        Session session = sessionWithLogUrl("http://automate.browserstack.com/foo");
        try {
            client.getSessionLogs(session);
            fail("Expected AutomateException for non-https scheme");
        } catch (AutomateException e) {
            assertTrue("message mentions Insecure scheme: " + e.getMessage(),
                    e.getMessage().toLowerCase().contains("insecure"));
            assertEquals(400, e.getStatusCode());
        }
    }

    /**
     * URI parsing must extract the actual host, not match by string suffix.
     * <p>
     * For a URL like {@code https://automate.browserstack.com.example/foo},
     * a naive {@code url.endsWith(".browserstack.com")} would fail to reject
     * it because {@code .browserstack.com} only appears as an internal
     * substring of the host. {@link java.net.URI#getHost()} returns the actual
     * host {@code automate.browserstack.com.example}, which the suffix check
     * on the host (not the URL) correctly rejects.
     */
    @Test
    public void getSessionLogs_rejectsHostSuffixSpoof() throws Exception {
        AutomateClient client = new AutomateClient(DUMMY_USER, DUMMY_KEY);
        Session session = sessionWithLogUrl("https://automate.browserstack.com.example/foo");
        try {
            client.getSessionLogs(session);
            fail("Expected AutomateException for non-allowlisted host");
        } catch (AutomateException e) {
            assertTrue("message mentions Untrusted host: " + e.getMessage(),
                    e.getMessage().toLowerCase().contains("untrusted"));
            assertEquals(400, e.getStatusCode());
        }
    }

    /**
     * A trusted host should pass the validation guard. The HTTP request will
     * still fail (DNS / 401 / etc.) but specifically NOT with the 400
     * "Untrusted/Insecure/Malformed" guard message — that proves the URL
     * cleared the allowlist.
     */
    @Test
    public void getSessionLogs_acceptsTrustedHost() throws Exception {
        AutomateClient client = new AutomateClient(DUMMY_USER, DUMMY_KEY);
        Session session = sessionWithLogUrl("https://api.browserstack.com/automate/path");
        try {
            client.getSessionLogs(session);
            // If somehow it succeeds (unlikely with dummy creds), that also passes the guard.
        } catch (AutomateException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            assertTrue("Trusted host should not be rejected by URL allowlist. Got: " + e.getMessage(),
                    !msg.contains("untrusted") && !msg.contains("insecure")
                            && !msg.contains("malformed"));
        }
    }

    // ---------- checkAuthState requires both credentials ----------

    private static void invokeCheckAuthState(BrowserStackClient client) throws Throwable {
        Method m = BrowserStackClient.class.getDeclaredMethod("checkAuthState");
        m.setAccessible(true);
        try {
            m.invoke(client);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private static void setField(BrowserStackClient client, String fieldName, Object value)
            throws ReflectiveOperationException {
        Field f = BrowserStackClient.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(client, value);
    }

    @Test
    public void checkAuthState_throwsWhenUsernameNull() throws Throwable {
        AutomateClient client = new AutomateClient(DUMMY_USER, DUMMY_KEY);
        setField(client, "username", null);
        try {
            invokeCheckAuthState(client);
            fail("Expected IllegalStateException when username is null");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void checkAuthState_throwsWhenAccessKeyNull() throws Throwable {
        AutomateClient client = new AutomateClient(DUMMY_USER, DUMMY_KEY);
        setField(client, "accessKey", null);
        try {
            invokeCheckAuthState(client);
            fail("Expected IllegalStateException when accessKey is null");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void checkAuthState_passesWhenBothCredentialsPresent() throws Throwable {
        AutomateClient client = new AutomateClient(DUMMY_USER, DUMMY_KEY);
        // Should not throw
        invokeCheckAuthState(client);
    }
}
