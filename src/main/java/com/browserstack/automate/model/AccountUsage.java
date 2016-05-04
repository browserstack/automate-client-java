package com.browserstack.automate.model;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * Contains information about your group's Automate plan,
 * including the maximum number of parallel sessions allowed
 * and the number of parallel sessions currently running.
 */
public class AccountUsage implements Serializable {

    @JsonProperty("parallel_sessions_running")
    private int parallelSessionsRunning;

    @JsonProperty("automate_plan")
    private String automatePlan;

    @JsonProperty("parallel_sessions_max_allowed")
    private int parallelSessionsMaxAllowed;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The parallelSessionsRunning
     */
    @JsonProperty("parallel_sessions_running")
    public int getParallelSessionsRunning() {
        return parallelSessionsRunning;
    }

    /**
     * @param parallelSessionsRunning The parallel_sessions_running
     */
    @JsonProperty("parallel_sessions_running")
    private void setParallelSessionsRunning(int parallelSessionsRunning) {
        this.parallelSessionsRunning = parallelSessionsRunning;
    }

    /**
     * @return The automatePlan
     */
    @JsonProperty("automate_plan")
    public String getAutomatePlan() {
        return automatePlan;
    }

    /**
     * @param automatePlan The automate_plan
     */
    @JsonProperty("automate_plan")
    private void setAutomatePlan(String automatePlan) {
        this.automatePlan = automatePlan;
    }

    /**
     * @return The parallelSessionsMaxAllowed
     */
    @JsonProperty("parallel_sessions_max_allowed")
    public int getParallelSessionsMaxAllowed() {
        return parallelSessionsMaxAllowed;
    }

    /**
     * @param parallelSessionsMaxAllowed The parallel_sessions_max_allowed
     */
    @JsonProperty("parallel_sessions_max_allowed")
    private void setParallelSessionsMaxAllowed(int parallelSessionsMaxAllowed) {
        this.parallelSessionsMaxAllowed = parallelSessionsMaxAllowed;
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