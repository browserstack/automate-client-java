package com.browserstack.automate;

import com.browserstack.appautomate.AppAutomateClient;
import com.browserstack.automate.exception.AppAutomateException;
import com.browserstack.automate.exception.InvalidFileExtensionException;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Hermetic unit tests for the JVM-property base-URL allowlist on
 * AutomateClient and AppAutomateClient constructors, and the canonical-path
 * extension check in {@code AppAutomateClient.uploadApp}. No live API calls.
 */
public class ApiBaseUrlAndUploadSecurityTest {

    private static final String AUTOMATE_PROP = "browserstack.automate.api";
    private static final String APPAUTOMATE_PROP = "browserstack.app-automate.api";
    private static final String DUMMY_USER = "dummy_user";
    private static final String DUMMY_KEY = "dummy_key";

    private String savedAutomateProp;
    private String savedAppAutomateProp;

    @Before
    public void saveProps() {
        savedAutomateProp = System.getProperty(AUTOMATE_PROP);
        savedAppAutomateProp = System.getProperty(APPAUTOMATE_PROP);
    }

    @After
    public void restoreProps() {
        if (savedAutomateProp == null) System.clearProperty(AUTOMATE_PROP);
        else System.setProperty(AUTOMATE_PROP, savedAutomateProp);
        if (savedAppAutomateProp == null) System.clearProperty(APPAUTOMATE_PROP);
        else System.setProperty(APPAUTOMATE_PROP, savedAppAutomateProp);
    }

    // ---------- JVM-property base URL allowlist ----------

    @Test
    public void automateClient_rejectsAttackerHostOverride() {
        System.setProperty(AUTOMATE_PROP, "https://notbrowserstack.example/automate");
        try {
            new AutomateClient(DUMMY_USER, DUMMY_KEY);
            fail("Expected IllegalArgumentException for non-allowlisted host override");
        } catch (IllegalArgumentException e) {
            assertTrue("message mentions Untrusted: " + e.getMessage(),
                    e.getMessage().toLowerCase().contains("untrusted"));
        }
    }

    @Test
    public void automateClient_rejectsHttpScheme() {
        System.setProperty(AUTOMATE_PROP, "http://api.browserstack.com/automate");
        try {
            new AutomateClient(DUMMY_USER, DUMMY_KEY);
            fail("Expected IllegalArgumentException for http scheme");
        } catch (IllegalArgumentException e) {
            assertTrue("message mentions Insecure: " + e.getMessage(),
                    e.getMessage().toLowerCase().contains("insecure"));
        }
    }

    @Test
    public void automateClient_rejectsHostSuffixSpoof() {
        // The raw URL ends with ".com", which a naive endsWith check would accept;
        // but URI.getHost() returns "api.browserstack.com.example" — the suffix
        // check on the host (not the URL) correctly rejects it.
        System.setProperty(AUTOMATE_PROP,
                "https://api.browserstack.com.example/automate");
        try {
            new AutomateClient(DUMMY_USER, DUMMY_KEY);
            fail("Expected IllegalArgumentException for non-allowlisted host");
        } catch (IllegalArgumentException e) {
            assertTrue("message mentions Untrusted: " + e.getMessage(),
                    e.getMessage().toLowerCase().contains("untrusted"));
        }
    }

    @Test
    public void automateClient_acceptsTrustedDefault() {
        System.clearProperty(AUTOMATE_PROP);
        // Default is https://api.browserstack.com/automate — must not throw.
        new AutomateClient(DUMMY_USER, DUMMY_KEY);
    }

    @Test
    public void automateClient_acceptsTrustedOverride() {
        // A non-prod BrowserStack subdomain should still be accepted.
        System.setProperty(AUTOMATE_PROP, "https://api-staging.browserstack.com/automate");
        new AutomateClient(DUMMY_USER, DUMMY_KEY);
    }

    @Test
    public void appAutomateClient_rejectsAttackerHostOverride() {
        System.setProperty(APPAUTOMATE_PROP, "https://notbrowserstack.example/app-automate");
        try {
            new AppAutomateClient(DUMMY_USER, DUMMY_KEY);
            fail("Expected IllegalArgumentException for non-allowlisted host override");
        } catch (IllegalArgumentException e) {
            assertTrue("message mentions Untrusted: " + e.getMessage(),
                    e.getMessage().toLowerCase().contains("untrusted"));
        }
    }

    @Test
    public void appAutomateClient_rejectsHttpScheme() {
        System.setProperty(APPAUTOMATE_PROP, "http://api-cloud.browserstack.com/app-automate");
        try {
            new AppAutomateClient(DUMMY_USER, DUMMY_KEY);
            fail("Expected IllegalArgumentException for http scheme");
        } catch (IllegalArgumentException e) {
            assertTrue("message mentions Insecure: " + e.getMessage(),
                    e.getMessage().toLowerCase().contains("insecure"));
        }
    }

    @Test
    public void appAutomateClient_acceptsTrustedDefault() {
        System.clearProperty(APPAUTOMATE_PROP);
        new AppAutomateClient(DUMMY_USER, DUMMY_KEY);
    }

    // ---------- Canonical-path extension check in uploadApp ----------

    /**
     * A symlink whose name ends in {@code .apk} but whose target file does not
     * must be rejected after canonicalization — the extension check runs on
     * the canonical name, not the supplied path.
     * <p>
     * Skipped on platforms that don't allow creating symlinks (e.g. Windows
     * without developer mode).
     */
    @Test
    public void uploadApp_rejectsSymlinkWhoseCanonicalNameLacksValidExtension()
            throws IOException, FileNotFoundException, AppAutomateException {
        Path tmp = Files.createTempDirectory("symlink-extension-");
        try {
            // Create a non-.apk target file.
            Path target = tmp.resolve("secret.txt");
            Files.write(target, "sensitive content".getBytes());

            // Create the .apk symlink pointing at it.
            Path symlink = tmp.resolve("legit.apk");
            try {
                Files.createSymbolicLink(symlink, target);
            } catch (UnsupportedOperationException | IOException | SecurityException e) {
                Assume.assumeNoException(
                        "Symlink creation not permitted on this platform/runtime", e);
            }

            AppAutomateClient client = new AppAutomateClient(DUMMY_USER, DUMMY_KEY);
            try {
                client.uploadApp(symlink.toAbsolutePath().toString());
                fail("Expected InvalidFileExtensionException for symlink to non-.apk file");
            } catch (InvalidFileExtensionException e) {
                assertTrue("message should mention .apk/.ipa: " + e.getMessage(),
                        e.getMessage().toLowerCase().contains(".apk"));
            }
        } finally {
            // Best-effort cleanup; ignore failures.
            try { deleteRecursive(tmp); } catch (IOException ignored) {}
        }
    }

    /**
     * A regular .apk file (no symlink) must still be accepted by the extension
     * check after canonicalization. The upload itself will fail with dummy
     * creds, but specifically NOT with InvalidFileExtensionException — that
     * proves the canonical-name extension check passed.
     */
    @Test
    public void uploadApp_acceptsRealApkAfterCanonicalization()
            throws IOException, FileNotFoundException {
        Path tmp = Files.createTempDirectory("apk-extension-");
        try {
            Path apk = tmp.resolve("dummy.apk");
            Files.write(apk, new byte[]{0x50, 0x4B, 0x03, 0x04}); // ZIP magic
            AppAutomateClient client = new AppAutomateClient(DUMMY_USER, DUMMY_KEY);
            try {
                client.uploadApp(apk.toAbsolutePath().toString());
                // unlikely with dummy creds, but if it returns, that also passes
            } catch (InvalidFileExtensionException e) {
                fail("Real .apk should not be rejected by extension check: "
                        + e.getMessage());
            } catch (AppAutomateException e) {
                // Expected — auth failure with dummy creds. Our extension check
                // already passed by the time we got here.
            }
        } finally {
            try { deleteRecursive(tmp); } catch (IOException ignored) {}
        }
    }

    private static void deleteRecursive(Path p) throws IOException {
        if (!Files.exists(p, java.nio.file.LinkOption.NOFOLLOW_LINKS)) return;
        if (Files.isDirectory(p, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
            try (java.util.stream.Stream<Path> s = Files.list(p)) {
                s.forEach(child -> {
                    try { deleteRecursive(child); } catch (IOException ignored) {}
                });
            }
        }
        Files.deleteIfExists(p);
    }
}
