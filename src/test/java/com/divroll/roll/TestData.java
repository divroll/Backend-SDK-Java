package com.divroll.roll;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import org.fluttercode.datafactory.impl.DataFactory;
import org.json.JSONObject;

public class TestData {

    public static TestApplication getNewApplication() {
        try {
            DataFactory df = new DataFactory();
            GetRequest getRequest = (GetRequest) Unirest.get(
                    Divroll.getServerUrl() + "/applications/" + df.getName());
            if(Divroll.getAppId() != null) {
                getRequest.header(DivrollBase.HEADER_APP_ID, Divroll.getAppId());
            }
            if(Divroll.getApiKey() != null) {
                getRequest.header(DivrollBase.HEADER_API_KEY, Divroll.getApiKey());
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
