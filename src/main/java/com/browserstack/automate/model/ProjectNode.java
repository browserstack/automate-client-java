package com.browserstack.automate.model;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectNode implements Serializable {

    private Project project;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The automation project
     */
    @JsonProperty("automation_project")
    public Project getAutomationProject() {
        return project;
    }

    /**
     * @param project The automation project
     */
    @JsonProperty("automation_project")
    private void setAutomationProject(Project project) {
        this.project = project;
    }

    /**
     * @return The project
     */
    @JsonProperty("project")
    public Project getProject() {
        return project;
    }

    /**
     * @param project The project
     */
    @JsonProperty("project")
    private void setProject(Project project) {
        this.project = project;
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