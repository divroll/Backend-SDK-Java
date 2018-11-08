package com.divroll.backend;

import com.divroll.backend.exception.BadRequestException;
import com.divroll.backend.exception.UnauthorizedException;
import com.divroll.backend.helper.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DivrollRoles extends DivrollBase {

    private static final String rolesUrl = "/entities/roles";

    private List<DivrollRole> roles;
    private int skip;
    private int limit;

    public List<DivrollRole> getRoles() {
        if(roles == null) {
            roles = new LinkedList<DivrollRole>();
        }
        return roles;
    }

    public void setRoles(List<DivrollRole> roles) {
        this.roles = roles;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void query() {
        try {
            GetRequest getRequest = (GetRequest) Unirest.get(Divroll.getServerUrl()
                    + rolesUrl);

            if(Divroll.getMasterKey() != null) {
                getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
            }
            if(Divroll.getAppId() != null) {
                getRequest.header(HEADER_APP_ID, Divroll.getAppId());
            }
            if(Divroll.getApiKey() != null) {
                getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
            }
            if(Divroll.getAuthToken() != null) {
                getRequest.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
            }

            HttpResponse<JsonNode> response = getRequest.asJson();


            if(response.getStatus() >= 500) {
                throwException(response);
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText());
            }  else if(response.getStatus() >= 400) {
                throwException(response);
            } else if(response.getStatus() == 200) {

                getRoles().clear();

                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject roles = bodyObj.getJSONObject("roles");
                JSONArray results = roles.getJSONArray("results");
                for(int i=0;i<results.length();i++){
                    JSONObject roleObj = results.getJSONObject(i);
                    String entityId = roleObj.getString("entityId");
                    String name = roleObj.getString("name");
                    Boolean publicRead = roleObj.getBoolean("publicRead");
                    Boolean publicWrite = roleObj.getBoolean("publicWrite");

                    List<String> aclWriteList = null;
                    List<String> aclReadList = null;

                    try {
                        aclWriteList = JSON.aclJSONArrayToList(roleObj.getJSONArray("aclWrite"));
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = JSON.aclJSONArrayToList(roleObj.getJSONArray("aclRead"));
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = Arrays.asList(roleObj.getString("aclWrite"));
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = Arrays.asList(roleObj.getString("aclRead"));
                    } catch (Exception e) {

                    }

                    DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                    acl.setPublicWrite(publicWrite);
                    acl.setPublicRead(publicRead);

                    DivrollRole role = new DivrollRole();
                    role.setEntityId(entityId);
                    role.setName(name);
                    role.setAcl(acl);

                    getRoles().add(role);

                }
                //

            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
}
