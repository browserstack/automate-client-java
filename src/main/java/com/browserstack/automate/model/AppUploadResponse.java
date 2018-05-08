package com.browserstack.automate.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUploadResponse {
  
  @JsonProperty("app_url")
  private String appUrl;
  
  @JsonProperty("custom_id")
  private String customId;
  
  @JsonProperty("shareable_id")
  private String shareableId;
  
  @JsonProperty("app_url")
  public String getAppUrl() {
    return appUrl;
  }
  
  @JsonProperty("app_url")
  public void setAppUrl(String appUrl) {
    this.appUrl = appUrl;
  }
  
  @JsonProperty("custom_id")
  public String getCustomId() {
    return customId;
  }
  
  @JsonProperty("custom_id")
  public void setCustomId(String customId) {
    this.customId = customId;
  }
  
  @JsonProperty("shareable_id")
  public String getShareableId() {
    return shareableId;
  }
  
  @JsonProperty("shareable_id")
  public void setShareableId(String shareableId) {
    this.shareableId = shareableId;
  }

}
