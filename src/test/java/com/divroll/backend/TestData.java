package com.divroll.backend;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.fluttercode.datafactory.impl.DataFactory;
import org.json.JSONObject;

public class TestData {

  public static TestApplication getNewApplication() {
    try {
      DataFactory df = new DataFactory();
      HttpRequestWithBody postRequest =
          (HttpRequestWithBody)
              Unirest.post(Divroll.getServerUrl() + "/applications/" + df.getName());
      if (Divroll.getAppId() != null) {
        postRequest.header(DivrollBase.HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        postRequest.header(DivrollBase.HEADER_API_KEY, Divroll.getApiKey());
      }
      JSONObject userObject = new JSONObject();
      userObject.put("username", df.getEmailAddress());
      userObject.put("password", "password");
      userObject.put("role", "role");

      JSONObject payload = new JSONObject();
      JSONObject applicationObj = new JSONObject();
      //            applicationObj.put("user", userObject);

      payload.put("application", applicationObj);

      System.out.println("Payload: " + payload.toString());

      postRequest.body(payload.toString());
      postRequest.header("Content-Type", "application/json");

      HttpResponse<JsonNode> response = postRequest.asJson();
      if (response.getStatus() == 404) {

      } else if (response.getStatus() == 401) {

      } else if (response.getStatus() == 200 || response.getStatus() == 201) {
        JsonNode body = response.getBody();
        JSONObject bodyObj = body.getObject();
        JSONObject application = bodyObj.getJSONObject("application");
        return new TestApplication(
            application.getString("appId"),
            application.getString("apiKey"),
            application.getString("masterKey"));
      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
    return null;
  }
}
