package com.browserstack.automate.api;

import com.browserstack.automate.AutomateClient;
import com.browserstack.automate.exception.AutomateException;
import com.browserstack.automate.exception.BuildNotFound;
import com.browserstack.client.BrowserStackClient;
import com.browserstack.client.api.BrowserStackObject;
import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * A build is an organizational structure for tests.
 */
public class Build extends BrowserStackObject {

    @JsonProperty("status")
    private String status;

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("name")
    private String name;

    @JsonProperty("hashed_id")
    private String id;

    @JsonProperty("sessions")
    private List<Session> sessions;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Build() {

    }

    public Build(BrowserStackClient client, String buildId) {
        this.id = buildId;
        setClient(client);
    }

    public final boolean delete() throws AutomateException {
        return ((AutomateClient) getClient()).deleteBuild(getId());
    }

    @Override
    public <T> T setClient(BrowserStackClient client) {
        if (sessions != null) {
            for (Session session : sessions) {
                if (session != null) {
                    session.setClient(client);
                }
            }
        }

        return super.setClient(client);
    }

    /**
     * @return The id
     */
    @JsonProperty("hashed_id")
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    @JsonProperty("hashed_id")
    private void setId(String id) {
        this.id = id;
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
     * @return The duration
     */
    @JsonProperty("duration")
    public int getDuration() {
        return duration;
    }

    /**
     * @param duration The duration
     */
    @JsonProperty("duration")
    private void setDuration(int duration) {
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
     * @return The sessions
     */
    @JsonProperty("sessions")
    public List<Session> getSessions() throws BuildNotFound, AutomateException {
        if (sessions == null) {
            sessions = ((AutomateClient) getClient()).getSessions(this.getId());
        }

        return sessions;
    }

    /**
     * @param sessionNodes The sessions
     */
    @JsonProperty("sessions")
    private void setSessions(List<SessionNode> sessionNodes) {
        List<Session> sessions = new ArrayList<Session>();

        if (sessionNodes != null) {
            for (SessionNode sessionNode : sessionNodes) {
                if (sessionNode != null && sessionNode.getSession() != null) {
                    sessions.add(sessionNode.getSession());
                }
            }
        }

        this.sessions = sessions;
    }

    @JsonAnyGetter
    protected Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    @SuppressWarnings("unchecked")
    private void setAdditionalProperty(String name, Object value) {
        if (name != null && name.equals("automation_build") && value instanceof HashMap) {
            // Until the API is fixed

            try {
                Map<String, String> properties = (HashMap<String, String>) value;
                if (properties.containsKey("hashed_id")) {
                    this.id = properties.get("hashed_id");
                }

                if (properties.containsKey("name")) {
                    this.name = properties.get("name");
                }

                if (properties.containsKey("status")) {
                    this.status = properties.get("status");
                }

                if (properties.containsKey("duration")) {
                    this.duration = Integer.parseInt(properties.get("duration"));
                }
            } catch (RuntimeException e) {
                // best-effort read
            }
        } else {
            this.additionalProperties.put(name, value);
        }
    }

}