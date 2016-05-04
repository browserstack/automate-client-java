package com.browserstack.client.model;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrowserListing implements Serializable {
    @JsonProperty("desktop")
    private List<DesktopPlatform> desktopPlatforms = new ArrayList<DesktopPlatform>();

    @JsonProperty("mobile")
    private List<MobilePlatform> mobilePlatforms = new ArrayList<MobilePlatform>();

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    /**
     * @return The desktop
     */
    @JsonProperty("desktop")
    public List<DesktopPlatform> getDesktopPlatforms() {
        return desktopPlatforms;
    }

    /**
     * @param desktop The desktop
     */
    @JsonProperty("desktop")
    public void setDesktopPlatforms(List<DesktopPlatform> desktopPlatforms) {
        this.desktopPlatforms = desktopPlatforms;
    }

    /**
     * @return The mobile
     */
    @JsonProperty("mobile")
    public List<MobilePlatform> getMobilePlatforms() {
        return mobilePlatforms;
    }

    /**
     * @param mobile The mobile
     */
    @JsonProperty("mobile")
    public void setMobilePlatforms(List<MobilePlatform> mobilePlatforms) {
        this.mobilePlatforms = mobilePlatforms;
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
