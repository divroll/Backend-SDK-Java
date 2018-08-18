package com.divroll.domino;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.json.JSONArray;
import org.json.JSONObject;

public class DominoUser {

    private static final String usersUrl = "/entities/users";
    private static final String loginUrl = "/entities/users/login";

    private String entityId;
    private String username;
    private String password;
    private String authToken;
    private DominoACL acl;

    public void create(String username, String password) {
        try {
            HttpRequestWithBody httpRequestWithBody = Unirest.post(Domino.getServerUrl() + usersUrl);
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header("X-Domino-Master-Key", Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header("X-Domino-App-Id", Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header("X-Domino-Api-Key", Domino.getApiKey());
            }
            JSONObject user = new JSONObject();
            user.put("username", username);
            user.put("password", password);
            JSONObject body = new JSONObject();
            body.put("user", user);
            httpRequestWithBody.body(body);
            JSONArray aclRead = new JSONArray();
            JSONArray aclWrite = new JSONArray();
            if(acl != null) {
                for(String uuid : acl.getAclRead()) {
                    aclRead.put(uuid);
                }
                for(String uuid : acl.getAclWrite()) {
                    aclWrite.put(uuid);
                }
            }

            System.out.println(body.toString());

            httpRequestWithBody.header("X-Domino-ACL-Read", aclRead.toString());
            httpRequestWithBody.header("X-Domino-ACL-Write", aclWrite.toString());
            httpRequestWithBody.header("Content-Type", "application/json");

            HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
            if(response.getStatus() == 404) {

            } else if(response.getStatus() == 401) {

            } else if(response.getStatus() == 201) {
                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();
                JSONObject responseUser = bodyObj.getJSONObject("user");
                String entityId = responseUser.getString("entityId");
                String webToken = responseUser.getString("webToken");
                setEntityId(entityId);
                setAuthToken(webToken);
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
    public void retrieve() {}
    public void update(String newUsername, String newPassword) {}
    public boolean delete() { return  false; }

    public void login(String username, String password) {
        setUsername(username);
        setPassword(password);
        try {
            GetRequest getRequest = (GetRequest) Unirest.get(Domino.getServerUrl() + loginUrl)
                    .queryString("username", getUsername())
                    .queryString("password", getPassword());
            if(Domino.getMasterKey() != null) {
                getRequest.header("X-Domino-Master-Key", Domino.getMasterKey());
            }
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
                JSONObject user = bodyObj.getJSONObject("user");
                String entityId = user.getString("entityId");
                String webToken = user.getString("webToken");
                setEntityId(entityId);
                setAuthToken(webToken);
                Domino.setAuthToken(webToken);
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        Domino.setAuthToken(null);
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DominoACL getAcl() {
        return acl;
    }

    public void setAcl(DominoACL acl) {
        this.acl = acl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
