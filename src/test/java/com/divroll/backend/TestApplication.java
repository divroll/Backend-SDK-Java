package com.divroll.backend;

public class TestApplication {

  private String appId;
  private String apiToken;
  private String masterKey;

  public TestApplication(String appId, String apiToken, String masterKey) {
    setAppId(appId);
    setApiToken(apiToken);
    setMasterKey(masterKey);
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getApiToken() {
    return apiToken;
  }

  public void setApiToken(String apiToken) {
    this.apiToken = apiToken;
  }

  public String getMasterKey() {
    return masterKey;
  }

  public void setMasterKey(String masterKey) {
    this.masterKey = masterKey;
  }
}
