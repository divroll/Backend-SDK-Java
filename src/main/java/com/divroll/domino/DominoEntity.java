package com.divroll.domino;

import com.divroll.domino.exception.DominoException;
import com.google.common.io.ByteStreams;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class DominoEntity extends DominoBase {

    private String entityStoreBase = "/entities/";
    private String entityId;
    private DominoACL acl;
    private JSONObject entityObj = new JSONObject();

    public DominoEntity(String entityStore) {
        entityStoreBase = entityStoreBase + entityStore;
    }

    public byte[] getBlobProperty(String blobKey) {
        try {
            GetRequest getRequest = (GetRequest) Unirest.get(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey);
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


            HttpResponse<InputStream> response = getRequest.asBinary();

            if(response.getStatus() >= 500) {

            }
            if(response.getStatus() >= 400) {

            } else if(response.getStatus() == 200) {
                InputStream is = response.getBody();
                byte[] bytes = ByteStreams.toByteArray(is);
                return bytes;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setBlobProperty(String blobKey, byte[] value) {
        if(entityId == null) {
            throw new DominoException("Save the entity first before setting a Blob property");
        }
        try {
            HttpRequestWithBody httpRequestWithBody = Unirest.put(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey);
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

            HttpResponse<JsonNode> response =  httpRequestWithBody.body(value).asJson();
            if(response.getStatus() >= 400) {
                throwException(response);
            } else if(response.getStatus() == 201) {
                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();
                JSONObject role = bodyObj.getJSONObject("role");
                String entityId = role.getString("entityId");
                setEntityId(entityId);
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public void setProperty(String propertyName, Object propertyValue) {
        if(propertyValue == null) {
            entityObj.put(propertyName, JSONObject.NULL);
        } else {
            DominoPropertyValue dominoPropertyValue = new DominoPropertyValue(propertyValue);
            entityObj.put(propertyName, dominoPropertyValue.getValue());
        }
    }

    public Object getProperty(String propertyName) {
        return entityObj.get(propertyName);
    }

    public DominoACL getAcl() {
        return acl;
    }

    public void setAcl(DominoACL acl) {
        this.acl = acl;
    }


    public void create() {
        try {
            HttpRequestWithBody httpRequestWithBody = Unirest.post(Domino.getServerUrl() + entityStoreBase);
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
            JSONArray aclReadArray = new JSONArray();
            JSONArray aclWriteArray = new JSONArray();

            entityObj.put("aclRead", aclReadArray);
            entityObj.put("aclWrite", aclWriteArray);

            JSONObject body = new JSONObject();
            body.put("entity", entityObj);

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

            System.out.println(body.toString());

            HttpResponse<JsonNode> response =  httpRequestWithBody.body(body).asJson();
            if(response.getStatus() == 404) {

            } else if(response.getStatus() == 401) {

            } else if(response.getStatus() == 201) {
                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();
                JSONObject entity = bodyObj.getJSONObject("entity");
                String entityId = entity.getString("entityId");
                setEntityId(entityId);
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
}
