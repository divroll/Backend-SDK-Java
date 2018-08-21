package com.divroll.domino;

public class Domino {

    private static String dominoServerUrl = "http://localhost:8080/domino";
    private static String applicationId;
    private static String applicationKey;
    private static String applicationMasterKey;
    private static String authenticationToken;

    private Domino() {}

    public static void initialize(String applicationId, String apiKey) {
        Domino.applicationId = applicationId;
        applicationKey = apiKey;
    }

    public static void initialize(String appId, String apiKey, String masterKey) {
        applicationId = appId;
        applicationKey = apiKey;
        applicationMasterKey = masterKey;
    }

    public static String getServerUrl() {
        return dominoServerUrl;
    }

    public static void initialize(String serverUrl, String appId, String apiKey, String masterKey) {
        applicationId = appId;
        applicationKey = apiKey;
        applicationMasterKey = masterKey;
        dominoServerUrl = serverUrl;
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
