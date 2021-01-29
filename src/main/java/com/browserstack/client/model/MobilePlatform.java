package com.browserstack.client.model;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobilePlatform extends Platform {

    @JsonProperty("devices")
    private List<Device> devices = new ArrayList<>();

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
