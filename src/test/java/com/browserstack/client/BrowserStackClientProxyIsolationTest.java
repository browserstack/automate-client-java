package com.browserstack.client;

import com.browserstack.automate.AutomateClient;
import com.google.api.client.http.HttpTransport;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * Verifies that {@code setProxy} mutates per-instance state only.
 *
 * <p>The {@code httpTransport} field is per-instance, so configuring a proxy
 * on one client must not affect another client constructed in the same
 * process.
 *
 * <p>This test snapshots both clients' {@code httpTransport} references
 * before and after a proxy is configured on one of them.
 */
public class BrowserStackClientProxyIsolationTest {

    private static HttpTransport getHttpTransport(BrowserStackClient client) throws Exception {
        Field f = BrowserStackClient.class.getDeclaredField("httpTransport");
        f.setAccessible(true);
        return (HttpTransport) f.get(client);
    }

    @Test
    public void setProxy_isInstanceScoped_doesNotLeakAcrossClients() throws Exception {
        AutomateClient clientA = new AutomateClient("user_a", "key_a");
        AutomateClient clientB = new AutomateClient("user_b", "key_b");

        HttpTransport beforeA = getHttpTransport(clientA);
        HttpTransport beforeB = getHttpTransport(clientB);
        assertNotNull("clientA must have a transport before setProxy", beforeA);
        assertNotNull("clientB must have a transport before setProxy", beforeB);

        // Configure a proxy on clientA only.
        clientA.setProxy("proxy.example.invalid", 3128, null, null);

        HttpTransport afterA = getHttpTransport(clientA);
        HttpTransport afterB = getHttpTransport(clientB);

        // clientA's transport should have been replaced.
        assertNotSame(
                "clientA.httpTransport must be replaced after setProxy",
                beforeA, afterA);

        // clientB's transport must NOT have changed.
        assertSame(
                "clientB.httpTransport must NOT change when setProxy is called on clientA "
                        + "(httpTransport must be per-instance, not shared across BrowserStackClient instances)",
                beforeB, afterB);

        // And clientA's new transport must not equal clientB's transport.
        assertNotSame(
                "clientA and clientB must have distinct transports after setProxy on A",
                afterA, afterB);
    }
}
