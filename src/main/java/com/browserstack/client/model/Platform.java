package com.browserstack.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Platform implements Serializable {

    @JsonProperty("os")
    private String os;

    @JsonProperty("os_display_name")
    private String osDisplayName;

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
     * @return The os_display_name
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
}
