package com.divroll.domino;

import com.divroll.domino.exception.DominoException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.json.JSONObject;

import java.io.InputStream;

public class DominoBase {
    public void throwException(HttpResponse<JsonNode> response) {
        JsonNode body = response.getBody();
        JSONObject jsonObject = body.getObject();
        JSONObject statusInfo = jsonObject.getJSONObject("org.restlet.engine.application.StatusInfo");
        throw new DominoException(statusInfo.getString("description"));
    }
}
