package com.browserstack.client.model;


import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DesktopPlatform {

    @JsonProperty("os_version")
    private String osVersion;

    @JsonProperty("os")
    private String os;

    @JsonProperty("browsers")
    private List<Browser> browsers = new ArrayList<Browser>();

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     * @return The browsers
     */
    @JsonProperty("browsers")
    public List<Browser> getBrowsers() {
        return browsers;
    }

    /**
     * @param browsers The browsers
     */
    @JsonProperty("browsers")
    public void setBrowsers(List<Browser> browsers) {
        this.browsers = browsers;
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
