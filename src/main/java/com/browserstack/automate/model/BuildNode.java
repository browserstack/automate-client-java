package com.browserstack.automate.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuildNode {

    @JsonProperty("automation_build")
    private Build build;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The build
     */
    @JsonProperty("automation_build")
    public Build getAutomationBuild() {
        return build;
    }

    /**
     * @param build The automation_build
     */
    @JsonProperty("automation_build")
    public void setAutomationBuild(Build build) {
        this.build = build;
    }

    /**
     * @return The build
     */
    @JsonProperty("build")
    public Build getBuild() {
        return build;
    }

    /**
     * @param build The build
     */
    @JsonProperty("build")
    public void setBuild(Build build) {
        this.build = build;
    }

    @JsonAnyGetter
    protected Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}