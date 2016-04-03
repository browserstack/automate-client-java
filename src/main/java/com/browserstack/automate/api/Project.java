package com.browserstack.automate.api;

import com.browserstack.automate.exception.AutomateException;
import com.browserstack.client.BrowserStackClient;
import com.browserstack.client.api.BrowserStackObject;
import com.browserstack.client.exception.BrowserStackException;
import com.fasterxml.jackson.annotation.*;
import com.mashape.unirest.http.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * Projects are organizational structures for builds.
 */
public class Project extends BrowserStackObject {

    @JsonProperty("name")
    private String name;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("group_id")
    private int groupId;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("id")
    private int id;

    @JsonProperty("user_id")
    private int userId;

    @JsonProperty("builds")
    private List<Build> builds;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Project() {

    }

    public Project(BrowserStackClient client, int projectId) {
        this.id = projectId;
        setClient(client);
    }

    public final boolean delete() throws AutomateException {
        try {
            JsonNode result = getClient()
                    .newRequest(BrowserStackClient.Method.DELETE, "/projects/{projectId}.json")
                    .routeParam("projectId", "" + id)
                    .asJson();

            return (result != null && result.getObject() != null &&
                    result.getObject().optString("status", "").equals("ok"));
        } catch (BrowserStackException e) {
            throw new AutomateException(e);
        }
    }

    @Override
    public <T> T setClient(BrowserStackClient client) {
        if (builds != null) {
            for (Build build : builds) {
                if (build != null) {
                    build.setClient(client);
                }
            }
        }

        return super.setClient(client);
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
     * @return The updatedAt
     */
    @JsonProperty("updated_at")
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt The updated_at
     */
    @JsonProperty("updated_at")
    private void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return The groupId
     */
    @JsonProperty("group_id")
    public int getGroupId() {
        return groupId;
    }

    /**
     * @param groupId The group_id
     */
    @JsonProperty("group_id")
    private void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * @return The createdAt
     */
    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt The created_at
     */
    @JsonProperty("created_at")
    private void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return The id
     */
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    /**
     * @param id The id
     */
    @JsonProperty("id")
    private void setId(int id) {
        this.id = id;
    }

    /**
     * @return The userId
     */
    @JsonProperty("user_id")
    public int getUserId() {
        return userId;
    }

    /**
     * @param buildNodes The builds
     */
    @JsonProperty("builds")
    private void setBuilds(List<BuildNode> buildNodes) {
        List<Build> builds = new ArrayList<Build>();

        if (buildNodes != null) {
            for (BuildNode buildNode : buildNodes) {
                if (buildNode != null && buildNode.getBuild() != null) {
                    builds.add(buildNode.getBuild());
                }
            }
        }

        this.builds = builds;
    }

    /**
     * @return The builds
     */
    @JsonProperty("builds")
    public List<Build> getBuilds() {
        return builds;
    }

    /**
     * @param userId The user_id
     */
    @JsonProperty("user_id")
    private void setUserId(int userId) {
        this.userId = userId;
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