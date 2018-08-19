package com.divroll.domino;

import com.divroll.domino.exception.BadRequestException;
import com.divroll.domino.exception.DominoException;
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
import java.util.List;

public class DominoRole extends DominoBase {

    private static final String rolesUrl = "/entities/roles";

    private String entityId;
    private String name;
    private DominoACL acl;

    public DominoRole() {}

    public DominoRole(String name) {
        setName(name);
    }

    public void create() {
        try {
            HttpRequestWithBody httpRequestWithBody = Unirest.post(Domino.getServerUrl() + rolesUrl);
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header("X-Domino-Master-Key", Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header("X-Domino-App-Id", Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header("X-Domino-Api-Key", Domino.getApiKey());
            }

            JSONObject roleObj = new JSONObject();
            roleObj.put("name", name);
            roleObj.put("aclRead", getAcl() != null ? getAcl().getAclRead() : null);
            roleObj.put("aclWrite", getAcl() != null ? getAcl().getAclWrite() : null);
            roleObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                    ? acl.getPublicRead() : JSONObject.NULL);
            roleObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                    ? acl.getPublicWrite() : JSONObject.NULL);
            JSONObject body = new JSONObject();
            body.put("role", roleObj);

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
                throw new BadRequestException(response.getStatusText());
            } else if(response.getStatus() == 404) {
                throw new DominoException(response.getStatusText());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() == 401) {
                throw new DominoException(response.getStatusText());
            } else if(response.getStatus() == 201) {
                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();
                System.out.println("RESPONSE: " + bodyObj.toString());

                JSONObject role = bodyObj.getJSONObject("role");
                String entityId = role.getString("entityId");
                String name = role.getString("name");
                Boolean publicRead = role.getBoolean("publicRead");
                Boolean publicWrite = role.getBoolean("publicWrite");

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                try {
                    aclWriteList = JSON.toList(role.getJSONArray("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = JSON.toList(role.getJSONArray("aclRead"));
                } catch (Exception e) {

                }

                try {
                    aclWriteList = Arrays.asList(role.getString("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = Arrays.asList(role.getString("aclRead"));
                } catch (Exception e) {

                }

                DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                acl.setPublicRead(publicRead);
                acl.setPublicWrite(publicWrite);
                setEntityId(entityId);
                setName(name);
                setAcl(acl);
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
    public void update() {
        try {
            HttpRequestWithBody httpRequestWithBody = Unirest.put(Domino.getServerUrl() + rolesUrl + "/" + getEntityId());
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header("X-Domino-Master-Key", Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header("X-Domino-App-Id", Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header("X-Domino-Api-Key", Domino.getApiKey());
            }

            JSONObject roleObj = new JSONObject();
            roleObj.put("name", name);

            roleObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                    ? acl.getPublicRead() : JSONObject.NULL);
            roleObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                    ? acl.getPublicWrite() : JSONObject.NULL);
            JSONObject body = new JSONObject();
            body.put("role", roleObj);

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
                throw new BadRequestException(response.getStatusText());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() == 404) {
                throw new DominoException(response.getStatusText());
            } else if(response.getStatus() >= 400) {
                throwException(response);
            } else if(response.getStatus() == 201) {
                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();
                JSONObject role = bodyObj.getJSONObject("role");
                String entityId = role.getString("entityId");
                String name = role.getString("name");
                Boolean publicRead = role.getBoolean("publicRead");
                Boolean publicWrite = role.getBoolean("publicWrite");

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                try {
                    aclWriteList = JSON.toList(role.getJSONArray("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = JSON.toList(role.getJSONArray("aclRead"));
                } catch (Exception e) {

                }

                try {
                    aclWriteList = Arrays.asList(role.getString("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = Arrays.asList(role.getString("aclRead"));
                } catch (Exception e) {

                }

                DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                acl.setPublicRead(publicRead);
                acl.setPublicWrite(publicWrite);
                setEntityId(entityId);
                setName(name);
                setAcl(acl);
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public boolean delete() {
        try {
            HttpRequestWithBody httpRequestWithBody = Unirest.delete(Domino.getServerUrl()
                    + rolesUrl + "/" + getEntityId());
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
                httpRequestWithBody.header("X-Domino-Auth-Token", Domino.getApiKey());
            }
            HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
            if(response.getStatus() >= 500) {
                throwException(response);
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() >= 400) {
                throwException(response);
            } else if(response.getStatus() == 204) {
                setEntityId(null);
                setAcl(null);
                setName(name);
                return true;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void retrieve() throws DominoException {
        try {
            GetRequest getRequest = (GetRequest) Unirest.get(Domino.getServerUrl()
                    + rolesUrl + "/" + getEntityId());

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
                JSONObject role = bodyObj.getJSONObject("role");
                String entityId = role.getString("entityId");
                String name = role.getString("name");
                Boolean publicRead = role.getBoolean("publicRead");
                Boolean publicWrite = role.getBoolean("publicWrite");
                setEntityId(entityId);
                setName(name);

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                try {
                    aclWriteList = JSON.toList(role.getJSONArray("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = JSON.toList(role.getJSONArray("aclRead"));
                } catch (Exception e) {

                }

                try {
                    aclWriteList = Arrays.asList(role.getString("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = Arrays.asList(role.getString("aclRead"));
                } catch (Exception e) {

                }

                DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                acl.setPublicWrite(publicWrite);
                acl.setPublicRead(publicRead);
                setEntityId(entityId);
                setName(name);
                setAcl(acl);

            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DominoACL getAcl() {
        return acl;
    }

    public void setAcl(DominoACL acl) {
        this.acl = acl;
    }

}
