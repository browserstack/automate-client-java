package com.browserstack.automate.helper;

import com.browserstack.client.model.Browser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;

public class AutomateTestHelper {
    private static final String ENV_BROWSERSTACK_BROWSERS = "BROWSERSTACK_BROWSERS";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();


    public static Browser[] parseBrowsers() throws IOException {
        return parseBrowsers(System.getenv(ENV_BROWSERSTACK_BROWSERS));
    }

    public static Browser[] parseBrowsers(String jsonBrowsers) throws IOException {
        return JSON_MAPPER.readValue(jsonBrowsers, Browser[].class);
    }

    public static void applyBrowser(Browser browser, DesiredCapabilities capabilities) {
        if (browser == null) {
            throw new IllegalArgumentException("Invalid browser instance.");
        }

        if (capabilities == null) {
            throw new IllegalArgumentException("Invalid capabilities.");
        }

        capabilities.setCapability(AutomateCapabilities.OS, browser.getOs());
        capabilities.setCapability(AutomateCapabilities.OS_VERSION, browser.getOsVersion());
        capabilities.setBrowserName(browser.getBrowser());

        if (browser.getDevice() != null) {
            capabilities.setCapability(AutomateCapabilities.DEVICE, browser.getBrowserVersion());
        } else {
            capabilities.setCapability(AutomateCapabilities.BROWSER_VERSION, browser.getBrowserVersion());
        }
    }

    private interface AutomateCapabilities {
        String OS = "os";
        String OS_VERSION = "os_version";
        String BROWSER = "browser";
        String BROWSER_VERSION = "browser_version";
        String DEVICE = "device";
    }
}
