package com.divroll.domino;

public class Domino {

    private static String serverUrl = "http://localhost:8080/domino";
    private static String _appId;
    private static String _apiKey;
    private static String _masterKey;
    private static String _authToken;

    private Domino() {}

    public static void initialize(String appId, String apiKey) {
        _appId = appId;
        _apiKey = apiKey;
    }
    public static void initialize(String appId, String apiKey, String masterKey) {
        _appId = appId;
        _apiKey = apiKey;
        _masterKey = masterKey;
    }

    public static String getServerUrl() {
        return serverUrl;
    }

    public static String getAppId() {
        return _appId;
    }

    public static String getApiKey() {
        return _apiKey;
    }

    public static String getMasterKey() {
        return _masterKey;
    }

    public static String getAuthToken() {
        return _authToken;
    }

    public static void setAuthToken(String authToken) {
        _authToken = authToken;
    }

}
