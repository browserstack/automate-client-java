package com.browserstack.client.model;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobilePlatform {

    @JsonProperty("devices")
    private List<Device> devices = new ArrayList<Device>();

    @JsonProperty("os")
    private String os;

    @JsonProperty("os_display_name")
    private String osDisplayName;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The devices
     */
    @JsonProperty("devices")
    public List<Device> getDevices() {
        return devices;
    }

    /**
     * @param devices The devices
     */
    @JsonProperty("devices")
    public void setDevices(List<Device> devices) {
        this.devices = devices;
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
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * @return The osDisplayName
     */
    @JsonProperty("os_display_name")
    public String getOsDisplayName() {
        return osDisplayName;
    }

    /**
     * @param osDisplayName The os_display_name
     */
    @JsonProperty("os_display_name")
    public void setOsDisplayName(String osDisplayName) {
        this.osDisplayName = osDisplayName;
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
