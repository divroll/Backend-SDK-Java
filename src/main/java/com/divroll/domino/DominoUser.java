package com.divroll.domino;

import com.divroll.domino.exception.BadRequestException;
import com.divroll.domino.exception.DominoException;
import com.divroll.domino.exception.InvalidEntityException;
import com.divroll.domino.exception.UnauthorizedException;
import com.divroll.domino.helper.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DominoUser extends DominoBase {

    private static final String usersUrl = "/entities/users";
    private static final String loginUrl = "/entities/users/login";

    private String entityId;
    private String username;
    private String password;
    private String authToken;
    private DominoACL acl;
    private List<DominoRole> roles;

    public void create(String username, String password) {
        try {

            setUsername(username);
            setPassword(password);

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
            if(Domino.getAuthToken() != null) {
                httpRequestWithBody.header("X-Domino-Auth-Key", Domino.getAuthToken());
            }
            JSONObject user = new JSONObject();
            user.put("username", username);
            user.put("password", password);
            JSONObject body = new JSONObject();

            JSONArray roles = new JSONArray();
            for(DominoRole role : getRoles()) {
                JSONObject roleObj = new JSONObject();
                roleObj.put("entityId", role.getEntityId());
                roles.put(roleObj);
            }
            user.put("roles", roles);

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

            System.out.println("REQUEST: " + body.toString());

            httpRequestWithBody.header("X-Domino-ACL-Read", aclRead.toString());
            httpRequestWithBody.header("X-Domino-ACL-Write", aclWrite.toString());
            httpRequestWithBody.header("Content-Type", "application/json");

            HttpResponse<JsonNode> response = httpRequestWithBody.asJson();

            System.out.println("RESPONSE: " + response.getBody().toString());

            if(response.getStatus() >= 500) {
                throwException(response);
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() == 201) {
                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();
                JSONObject responseUser = bodyObj.getJSONObject("user");
                String entityId = responseUser.getString("entityId");
                String webToken = responseUser.getString("webToken");
                setEntityId(entityId);
                setAuthToken(webToken);

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                Boolean publicRead = null;
                Boolean publicWrite = null;

                try {
                    publicRead = user.get("publicRead") != null ? user.getBoolean("publicRead") : null;
                } catch (Exception e) {

                }

                try {
                    publicWrite = user.get("publicWrite") != null ? user.getBoolean("publicWrite") : null;
                } catch (Exception e) {

                }

                try {
                    aclWriteList = JSON.toList(user.getJSONArray("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = JSON.toList(user.getJSONArray("aclRead"));
                } catch (Exception e) {

                }

                try {
                    aclWriteList = Arrays.asList(user.getString("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = Arrays.asList(user.getString("aclRead"));
                } catch (Exception e) {

                }

                List<DominoRole> dominoRoles = null;
                try {
                    Object rolesObj = user.get("roles");
                    if(roles instanceof JSONArray) {
                        dominoRoles = new LinkedList<DominoRole>();
                        JSONArray jsonArray = (JSONArray) roles;
                        for(int i=0;i<jsonArray.length();i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String roleId = jsonObject.getString("entityId");
                            DominoRole dominoRole = new DominoRole();
                            dominoRole.setEntityId(roleId);
                            dominoRoles.add(dominoRole);
                        }
                    } else if(rolesObj instanceof JSONObject) {
                        dominoRoles = new LinkedList<DominoRole>();
                        JSONObject jsonObject = (JSONObject) rolesObj;
                        String roleId = jsonObject.getString("entityId");
                        DominoRole dominoRole = new DominoRole();
                        dominoRole.setEntityId(roleId);
                        dominoRoles.add(dominoRole);
                    }
                } catch (Exception e) {
                    // do nothing
                }

                DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                acl.setPublicWrite(publicWrite);
                acl.setPublicRead(publicRead);
                setAcl(acl);
                setRoles(dominoRoles);

            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
    public void retrieve() {
        try {
            GetRequest getRequest = (GetRequest) Unirest.get(Domino.getServerUrl()
                    + usersUrl + "/" + getEntityId());

            if(Domino.getMasterKey() != null) {
                getRequest.header("X-Domino-Master-Key", Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                getRequest.header("X-Domino-App-Id", Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                getRequest.header("X-Domino-Api-Key", Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                getRequest.header("X-Domino-Auth-Token", Domino.getAuthToken());
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
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject userJsonObj = bodyObj.getJSONObject("user");
                String entityId = userJsonObj.getString("entityId");
                String username = userJsonObj.getString("username");

                Boolean publicRead = null;
                Boolean publicWrite = null;

                try {
                    publicRead = userJsonObj.get("publicRead") != null ? userJsonObj.getBoolean("publicRead") : null;
                } catch (Exception e) {

                }

                try {
                    publicWrite = userJsonObj.get("publicWrite") != null ? userJsonObj.getBoolean("publicWrite") : null;
                } catch (Exception e) {

                }

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                try {
                    aclWriteList = JSON.toList(userJsonObj.getJSONArray("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = JSON.toList(userJsonObj.getJSONArray("aclRead"));
                } catch (Exception e) {

                }

                try {
                    aclWriteList = Arrays.asList(userJsonObj.getString("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = Arrays.asList(userJsonObj.getString("aclRead"));
                } catch (Exception e) {

                }

                List<DominoRole> dominoRoles = null;
                try {
                    Object roles = userJsonObj.get("roles");
                    if(roles instanceof JSONArray) {
                        dominoRoles = new LinkedList<DominoRole>();
                        JSONArray jsonArray = (JSONArray) roles;
                        for(int i=0;i<jsonArray.length();i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String roleId = jsonObject.getString("entityId");
                            DominoRole dominoRole = new DominoRole();
                            dominoRole.setEntityId(roleId);
                            dominoRoles.add(dominoRole);
                        }
                    } else if(roles instanceof JSONObject) {
                        dominoRoles = new LinkedList<DominoRole>();
                        JSONObject jsonObject = (JSONObject) roles;
                        String roleId = jsonObject.getString("entityId");
                        DominoRole dominoRole = new DominoRole();
                        dominoRole.setEntityId(roleId);
                        dominoRoles.add(dominoRole);
                    }
                } catch (Exception e) {
                    // do nothing
                }

                DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                acl.setPublicWrite(publicWrite);
                acl.setPublicRead(publicRead);

                setEntityId(entityId);
                setUsername(username);
                setAcl(acl);
                setRoles(dominoRoles);

            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public void update(String newUsername, String newPassword) {
        try {

            String completeUrl = Domino.getServerUrl() + usersUrl + "/" + getEntityId();
            System.out.println(completeUrl);
            HttpRequestWithBody httpRequestWithBody = Unirest.put(completeUrl);
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header("X-Domino-Master-Key", Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header("X-Domino-App-Id", Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header("X-Domino-Api-Key", Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                httpRequestWithBody.header("X-Domino-Auth-Token", Domino.getAuthToken());
            }
            JSONObject user = new JSONObject();
            if(username != null) {
                user.put("username", newUsername);
            }
            if(username != null) {
                user.put("password", newPassword);
            }
            JSONObject body = new JSONObject();

            JSONArray roles = new JSONArray();
            for(DominoRole role : getRoles()) {
                JSONObject roleObj = new JSONObject();
                roleObj.put("entityId", role.getEntityId());
                roles.put(roleObj);
            }
            user.put("roles", roles);

            body.put("user", user);

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

            httpRequestWithBody.header("X-Domino-ACL-Read", aclRead.toString());
            httpRequestWithBody.header("X-Domino-ACL-Write", aclWrite.toString());
            httpRequestWithBody.header("Content-Type", "application/json");

            System.out.println("REQUEST: " + body.toString());

            HttpResponse<JsonNode> response =  httpRequestWithBody.body(body).asJson();

            if(response.getStatus() >= 500) {
                throw new DominoException(response.getStatusText());
            } else if(response.getStatus() == 400) {
                throw new InvalidEntityException(response.getStatusText());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() == 404) {
                throw new DominoException(response.getStatusText());
            } else if(response.getStatus() >= 400) {
                throwException(response);
            } else if(response.getStatus() == 200) {

                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();
                JSONObject responseUser = bodyObj.getJSONObject("user");
                String entityId = responseUser.getString("entityId");
                //String webToken = responseUser.getString("webToken");
                String updatedUsername = null;

                try {
                    updatedUsername = responseUser.getString("username");
                } catch (Exception e) {
                    // do nothing
                }

                setEntityId(entityId);
                //setAuthToken(webToken);

                Boolean publicRead = null;
                Boolean publicWrite = null;

                try {
                    publicRead = responseUser.getBoolean("publicRead");
                } catch (Exception e) {

                }

                try {
                    publicWrite = responseUser.getBoolean("publicWrite");
                } catch (Exception e) {

                }

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                try {
                    aclWriteList = JSON.toList(responseUser.getJSONArray("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = JSON.toList(responseUser.getJSONArray("aclRead"));
                } catch (Exception e) {

                }

                try {
                    aclWriteList = Arrays.asList(responseUser.getString("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = Arrays.asList(responseUser.getString("aclRead"));
                } catch (Exception e) {

                }

                List<DominoRole> dominoRoles = null;
                try {
                    Object roleObjects = user.get("roles");
                    if(roleObjects instanceof JSONArray) {
                        dominoRoles = new LinkedList<DominoRole>();
                        JSONArray jsonArray = (JSONArray) roles;
                        for(int i=0;i<jsonArray.length();i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
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
                } catch (Exception e) {

                }

                DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                acl.setPublicRead(publicRead);
                acl.setPublicWrite(publicWrite);
                setEntityId(entityId);
                if(newUsername != null) {
                    setUsername(updatedUsername);
                }
                setAcl(acl);
                setRoles(dominoRoles);
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        update(null, null);
    }

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

    public List<DominoRole> getRoles() {
        if(roles == null) {
            roles = new LinkedList<DominoRole>();
        }
        return roles;
    }

    public void setRoles(List<DominoRole> roles) {
        this.roles = roles;
    }
}
