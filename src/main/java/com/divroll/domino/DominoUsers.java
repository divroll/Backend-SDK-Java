package com.divroll.domino;

import com.divroll.domino.exception.BadRequestException;
import com.divroll.domino.exception.UnauthorizedException;
import com.divroll.domino.helper.JSON;
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

public class DominoUsers extends DominoBase {

    private static final String usersUrl = "/entities/users";

    private List<DominoUser> users;
    private int skip = 0;
    private int limit = 100;

    public List<DominoUser> getUsers() {
        if(users == null) {
            users = new LinkedList<DominoUser>();
        }
        return users;
    }

    public void setUsers(List<DominoUser> users) {
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
            String completeUrl = Domino.getServerUrl() + usersUrl;
            System.out.println(completeUrl);
            GetRequest getRequest = (GetRequest) Unirest.get(completeUrl);

            if(Domino.getMasterKey() != null) {
                getRequest.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                getRequest.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                getRequest.header(HEADER_API_KEY, Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                getRequest.header(HEADER_AUTH_TOKEN, Domino.getAuthToken());
            }

            HttpResponse<JsonNode> response = getRequest.asJson();
            System.out.println(response.getBody().toString());

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

                System.out.println("HERE: " + body.toString());

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
                        aclWriteList = JSON.toList(userObj.getJSONArray("aclWrite"));
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = JSON.toList(userObj.getJSONArray("aclRead"));
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

                    List<DominoRole> dominoRoles = null;
                    try {
                        if(userRoles != null) {
                            Object roleObjects = userObj.get("roles");
                            if(roleObjects instanceof JSONArray) {
                                dominoRoles = new LinkedList<DominoRole>();
                                for(int j=0;j<userRoles.length();j++) {
                                    JSONObject jsonObject = userRoles.getJSONObject(j);
                                    String roleId = jsonObject.getString("entityId");
                                    DominoRole dominoRole = new DominoRole();
                                    dominoRole.setEntityId(roleId);
                                    dominoRoles.add(dominoRole);
                                }
                            } else if(roleObjects instanceof JSONObject) {
                                dominoRoles = new LinkedList<DominoRole>();
                                JSONObject jsonObject = (JSONObject) roleObjects;
                                String roleId = jsonObject.getString("entityId");
                                DominoRole dominoRole = new DominoRole();
                                dominoRole.setEntityId(roleId);
                                dominoRoles.add(dominoRole);
                            }
                        }

                    } catch (Exception e) {
                        // do nothing
                    }

                    DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                    acl.setPublicWrite(publicWrite);
                    acl.setPublicRead(publicRead);

                    DominoUser user = new DominoUser();
                    user.setEntityId(entityId);
                    user.setAcl(acl);
                    user.setRoles(dominoRoles);
                    user.setUsername(username);

                    getUsers().add(user);

                }
                //System.out.println(bodyObj.toString());

            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
}
