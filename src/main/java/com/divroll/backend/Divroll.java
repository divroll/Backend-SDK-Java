package com.divroll.backend;

public class Divroll {

  private static String divrollServerUrl = "http://localhost:8080/divroll";
  private static String applicationId;
  private static String applicationKey;
  private static String applicationMasterKey;
  private static String authenticationToken;

  private Divroll() {}

  public static void initialize(String applicationId, String apiKey) {
    Divroll.applicationId = applicationId;
    applicationKey = apiKey;
  }

  public static void initialize(String appId, String apiKey, String masterKey) {
    applicationId = appId;
    applicationKey = apiKey;
    applicationMasterKey = masterKey;
  }

  public static String getServerUrl() {
    return divrollServerUrl;
  }

  public static void initialize(String serverUrl, String appId, String apiKey, String masterKey) {
    applicationId = appId;
    applicationKey = apiKey;
    applicationMasterKey = masterKey;
    divrollServerUrl = serverUrl;
  }

  public static String getAppId() {
    return applicationId;
  }

  public static String getApiKey() {
    return applicationKey;
  }

  public static String getMasterKey() {
    return applicationMasterKey;
  }

  public static String getAuthToken() {
    return authenticationToken;
  }

  public static void setAuthToken(String authToken) {
    authenticationToken = authToken;
  }
}
