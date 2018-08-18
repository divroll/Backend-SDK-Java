package com.divroll.domino;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import org.json.JSONObject;

public class TestData {

    public static TestApplication getNewApplication() {
        try {
            GetRequest getRequest = (GetRequest) Unirest.get(Domino.getServerUrl() + "/applications");
            if(Domino.getAppId() != null) {
                getRequest.header("X-Domino-App-Id", Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                getRequest.header("X-Domino-Api-Key", Domino.getApiKey());
            }
            HttpResponse<JsonNode> response = getRequest.asJson();
            if(response.getStatus() == 404) {

            } else if(response.getStatus() == 401) {

            } else if(response.getStatus() == 200) {
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject application = bodyObj.getJSONObject("application");
                return new TestApplication(application.getString("appId"),
                        application.getString("apiKey"),
                        application.getString("masterKey"));
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }

}
