package com.browserstack.client.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Apple on 04/05/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Device extends BrowserStackObject {

    @JsonProperty("os_version")
    private String osVersion;

    @JsonProperty("device")
    private String device;

    @JsonProperty("display_name")
    private String displayName;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

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
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
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
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * @return The displayName
     */
    @JsonProperty("display_name")
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName The display_name
     */
    @JsonProperty("display_name")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
