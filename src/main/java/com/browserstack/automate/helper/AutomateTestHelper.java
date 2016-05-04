package com.browserstack.automate.helper;

import com.browserstack.client.model.Browser;
import com.fasterxml.jackson.databind.ObjectMapper;

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
}
