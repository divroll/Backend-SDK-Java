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

public class DivrollUsers extends DivrollBase {

    private static final String usersUrl = "/entities/users";

    private List<DivrollUser> users;
    private int skip = 0;
    private int limit = 100;

    public List<DivrollUser> getUsers() {
        if(users == null) {
            users = new LinkedList<DivrollUser>();
        }
        return users;
    }

    public void setUsers(List<DivrollUser> users) {
        this.users = users;
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
            String completeUrl = Divroll.getServerUrl() + usersUrl;

            GetRequest getRequest = (GetRequest) Unirest.get(completeUrl);

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

                getUsers().clear();

                JsonNode body = response.getBody();



                JSONObject bodyObj = body.getObject();
                JSONObject roles = bodyObj.getJSONObject("users");
                JSONArray results = new JSONArray();

                try {
                    results = roles.getJSONArray("results");
                } catch (Exception e) {

                }

                try {
                    JSONObject jsonObject = roles.getJSONObject("results");
                    results.put(jsonObject);
                } catch (Exception e) {

                }

                for(int i=0;i<results.length();i++){
                    JSONObject userObj = results.getJSONObject(i);
                    String entityId = userObj.getString("entityId");
                    String username = userObj.getString("username");
                    Boolean publicRead = userObj.getBoolean("publicRead");
                    Boolean publicWrite = userObj.getBoolean("publicWrite");

                    List<String> aclWriteList = null;
                    List<String> aclReadList = null;

                    try {
                        aclWriteList = JSON.aclJSONArrayToList(userObj.getJSONArray("aclWrite"));
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = JSON.aclJSONArrayToList(userObj.getJSONArray("aclRead"));
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = Arrays.asList(userObj.getString("aclWrite"));
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = Arrays.asList(userObj.getString("aclRead"));
                    } catch (Exception e) {

                    }

                    JSONArray userRoles = null;
                    try {
                        userRoles = userObj.getJSONArray("roles");
                    } catch (Exception e) {

                    }

                    List<DivrollRole> divrollRoles = null;
                    try {
                        if(userRoles != null) {
                            Object roleObjects = userObj.get("roles");
                            if(roleObjects instanceof JSONArray) {
                                divrollRoles = new LinkedList<DivrollRole>();
                                for(int j=0;j<userRoles.length();j++) {
                                    JSONObject jsonObject = userRoles.getJSONObject(j);
                                    String roleId = jsonObject.getString("entityId");
                                    DivrollRole divrollRole = new DivrollRole();
                                    divrollRole.setEntityId(roleId);
                                    divrollRoles.add(divrollRole);
                                }
                            } else if(roleObjects instanceof JSONObject) {
                                divrollRoles = new LinkedList<DivrollRole>();
                                JSONObject jsonObject = (JSONObject) roleObjects;
                                String roleId = jsonObject.getString("entityId");
                                DivrollRole divrollRole = new DivrollRole();
                                divrollRole.setEntityId(roleId);
                                divrollRoles.add(divrollRole);
                            }
                        }

                    } catch (Exception e) {
                        // do nothing
                    }

                    DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                    acl.setPublicWrite(publicWrite);
                    acl.setPublicRead(publicRead);

                    DivrollUser user = new DivrollUser();
                    user.setEntityId(entityId);
                    user.setAcl(acl);
                    user.setRoles(divrollRoles);
                    user.setUsername(username);

                    getUsers().add(user);

                }
                //

            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
}
