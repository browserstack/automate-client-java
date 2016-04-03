package com.browserstack.automate.api;

import com.browserstack.automate.AutomateClient.SessionStatus;
import com.browserstack.automate.exception.AutomateException;
import com.browserstack.client.BrowserStackClient;
import com.browserstack.client.BrowserStackClient.Method;
import com.browserstack.client.api.BrowserStackObject;
import com.browserstack.client.exception.BrowserStackException;
import com.fasterxml.jackson.annotation.*;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Session extends BrowserStackObject {

    @JsonProperty("hashed_id")
    private String id;

    @JsonProperty("build_name")
    private String buildName;

    @JsonProperty("browser")
    private String browser;

    @JsonProperty("browser_version")
    private String browserVersion;

    @JsonProperty("os")
    private String os;

    @JsonProperty("os_version")
    private String osVersion;

    @JsonProperty("device")
    private String device;

    @JsonProperty("browser_url")
    private String browserUrl;

    @JsonProperty("video_url")
    private String videoUrl;

    @JsonProperty("logs")
    private String logUrl;

    @JsonProperty("project_name")
    private String projectName;

    @JsonProperty("status")
    private String status;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("name")
    private String name;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Session() {

    }

    public Session(BrowserStackClient client, String sessionId) {
        setClient(client);
        this.id = sessionId;
    }

    public final boolean delete() throws AutomateException {
        try {
            JsonNode result = getClient()
                    .newRequest(Method.DELETE, "/sessions/{sessionId}.json")
                    .routeParam("sessionId", id)
                    .asJson();

            return (result != null && result.getObject() != null &&
                    result.getObject().optString("status", "").equals("ok"));
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    public final Session updateStatus(final SessionStatus sessionStatus,
                                      final String reason) throws AutomateException {
        final Map<String, Object> data = new HashMap<String, Object>();
        if (sessionStatus != null) {
            data.put("status", sessionStatus.name().toLowerCase());
        }

        if (reason != null && !reason.trim().isEmpty()) {
            data.put("reason", reason);
        }

        try {
            return getClient()
                    .newRequest(Method.PUT, "/sessions/{sessionId}.json", data)
                    .routeParam("sessionId", id)
                    .asObject(SessionNode.class)
                    .getSession()
                    .setClient(getClient());
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    public final Session updateStatus(final SessionStatus sessionStatus)
            throws AutomateException {
        return updateStatus(sessionStatus, null);
    }

    public final String getLogs() throws AutomateException {
        try {
            if (logUrl == null) {
                throw new BrowserStackException("Invalid logUrl");
            }

            return getClient().signRequest(Unirest.get(logUrl)).asString();
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    /**
     * @return The id
     */
    @JsonProperty("hashed_id")
    public String getId() {
        return id;
    }

    /**
     * @param id The hashed_id
     */
    @JsonProperty("hashed_id")
    private void setId(String id) {
        this.id = id;
    }

    /**
     * @return The buildName
     */
    @JsonProperty("build_name")
    private String getBuildName() {
        return buildName;
    }

    /**
     * @param buildName The build_name
     */
    @JsonProperty("build_name")
    private void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    /**
     * @return The browser
     */
    @JsonProperty("browser")
    public String getBrowser() {
        return browser;
    }

    /**
     * @param browser The browser
     */
    @JsonProperty("browser")
    private void setBrowser(String browser) {
        this.browser = browser;
    }

    /**
     * @return The browserUrl
     */
    @JsonProperty("browser_url")
    public String getBrowserUrl() {
        return browserUrl;
    }

    /**
     * @param browserUrl The browser_url
     */
    @JsonProperty("browser_url")
    private void setBrowserUrl(String browserUrl) {
        this.browserUrl = browserUrl;
    }

    /**
     * @return The videoUrl
     */
    @JsonProperty("video_url")
    public String getVideoUrl() {
        return videoUrl;
    }

    /**
     * @param videoUrl The video_url
     */
    @JsonProperty("video_url")
    private void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    /**
     * @return The logs
     */
    @JsonProperty("logs")
    public String getLogUrl() {
        return logUrl;
    }

    /**
     * @param logUrl The logUrl
     */
    @JsonProperty("logs")
    private void setLogUrl(String logUrl) {
        this.logUrl = logUrl;
    }

    /**
     * @return The projectName
     */
    @JsonProperty("project_name")
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param projectName The project_name
     */
    @JsonProperty("project_name")
    private void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return The status
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    @JsonProperty("status")
    private void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The browserVersion
     */
    @JsonProperty("browser_version")
    public String getBrowserVersion() {
        return browserVersion;
    }

    /**
     * @param browserVersion The browser_version
     */
    @JsonProperty("browser_version")
    private void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    /**
     * @return The osVersion
     */
    @JsonProperty("os_version")
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * @param osVersion The os_version
     */
    @JsonProperty("os_version")
    private void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    /**
     * @return The reason
     */
    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    /**
     * @param reason The reason
     */
    @JsonProperty("reason")
    private void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return The duration
     */
    @JsonProperty("duration")
    public Integer getDuration() {
        return duration;
    }

    /**
     * @param duration The duration
     */
    @JsonProperty("duration")
    private void setDuration(Integer duration) {
        this.duration = duration;
    }

    /**
     * @return The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    @JsonProperty("name")
    private void setName(String name) {
        this.name = name;
    }

    /**
     * @return The device
     */
    @JsonProperty("device")
    public String getDevice() {
        return device;
    }

    /**
     * @param device The device
     */
    @JsonProperty("device")
    private void setDevice(String device) {
        this.device = device;
    }

    /**
     * @return The os
     */
    @JsonProperty("os")
    public String getOs() {
        return os;
    }

    /**
     * @param os The os
     */
    @JsonProperty("os")
    private void setOs(String os) {
        this.os = os;
    }

    @JsonAnyGetter
    protected Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    private void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}