package com.divroll.domino;

import com.divroll.domino.exception.BadRequestException;
import com.divroll.domino.exception.DominoException;
import com.divroll.domino.exception.NotFoundRequestException;
import com.divroll.domino.exception.UnauthorizedException;
import com.divroll.domino.helper.JSON;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DominoEntity extends DominoBase {

    private String entityStoreBase = "/entities/";
    private String entityId;
    private DominoACL acl;
    private JSONObject entityObj = new JSONObject();

    private DominoEntity() {}

    public DominoEntity(String entityStore) {
        entityStoreBase = entityStoreBase + entityStore;
    }

    public byte[] getBlobProperty(String blobKey) {
        try {
            GetRequest getRequest = (GetRequest) Unirest.get(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey);
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

            HttpResponse<InputStream> response = getRequest.asBinary();

            if(response.getStatus() >= 500) {
                throw new DominoException("Internal Server error"); // TODO
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText());
            } else if(response.getStatus() >= 400) {
                throw new DominoException("Client error"); // TODO
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
            HttpRequestWithBody httpRequestWithBody = Unirest.post(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey);
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header(HEADER_API_KEY, Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                httpRequestWithBody.header(HEADER_AUTH_TOKEN, Domino.getAuthToken());
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

            HttpResponse<InputStream> response =  httpRequestWithBody.body(value).asBinary();
            if(response.getStatus() >= 500) {
                throw new DominoException("Internal Server error"); // TODO
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText());
            } else if(response.getStatus() >= 400) {
                throw new DominoException("Client error"); // TODO
            } else if(response.getStatus() == 201) {
                InputStream responseBody = response.getBody();
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public void deleteBlobProperty(String blobKey) {
        try {
            HttpRequestWithBody getRequest = (HttpRequestWithBody) Unirest.delete(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey);
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

            HttpResponse<InputStream> response = getRequest.asBinary();

            if(response.getStatus() >= 500) {
                throw new DominoException("Internal Server error"); // TODO
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText());
            } else if(response.getStatus() >= 400) {
                throw new DominoException("Client error"); // TODO
            } else if(response.getStatus() == 200) {

            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (Exception e) {
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

    public void addLink(String linkName, String entityId) {}

    public void removeLink(String linkName, String entityId) {}

    public void removeLinks(String linkName) {}

    public DominoACL getAcl() {
        return acl;
    }

    public void setAcl(DominoACL acl) {
        this.acl = acl;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void create() {
        try {
            HttpRequestWithBody httpRequestWithBody = Unirest.post(Domino.getServerUrl() + entityStoreBase);
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header(HEADER_API_KEY, Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                httpRequestWithBody.header(HEADER_AUTH_TOKEN, Domino.getAuthToken());
            }
            JSONArray aclReadArray = new JSONArray();
            JSONArray aclWriteArray = new JSONArray();

            entityObj.put("aclRead", aclReadArray);
            entityObj.put("aclWrite", aclWriteArray);
            entityObj.put("publicRead", (this.acl != null && this.acl.getPublicRead() != null)
                    ? this.acl.getPublicRead() : JSONObject.NULL);
            entityObj.put("publicWrite", (this.acl != null && this.acl.getPublicWrite() != null)
                    ? this.acl.getPublicWrite() : JSONObject.NULL);
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

            System.out.println("REQUEST: " + body.toString());

            HttpResponse<JsonNode> response =  httpRequestWithBody.body(body).asJson();

            System.out.println("RESPONSE: " + response.getBody().toString());

            if(response.getStatus() >= 500) {
                throw new DominoException(response.getStatusText());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() >= 401) {
                throw new DominoException(response.getStatusText());
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

    public boolean update() {
        try {
            String completeUrl = Domino.getServerUrl() + entityStoreBase + "/" + getEntityId();
            HttpRequestWithBody httpRequestWithBody = Unirest.put(completeUrl);
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header(HEADER_API_KEY, Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                httpRequestWithBody.header(HEADER_AUTH_TOKEN, Domino.getAuthToken());
            }
            JSONArray aclReadArray = new JSONArray();
            JSONArray aclWriteArray = new JSONArray();

            entityObj.put("aclRead", aclReadArray);
            entityObj.put("aclWrite", aclWriteArray);
            entityObj.put("publicRead", (this.acl != null && this.acl.getPublicRead() != null)
                    ? this.acl.getPublicRead() : JSONObject.NULL);
            entityObj.put("publicWrite", (this.acl != null && this.acl.getPublicWrite() != null)
                    ? this.acl.getPublicWrite() : JSONObject.NULL);

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

            System.out.println("REQUEST: " + body.toString());

            HttpResponse<JsonNode> response =  httpRequestWithBody.body(body).asJson();

            System.out.println("RESPONSE: " + response.getBody().toString());

            if(response.getStatus() >= 500) {
                throw new DominoException(response.getStatusText());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() >= 401) {
                throw new DominoException(response.getStatusText());
            } else if(response.getStatus() == 201) {
                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();
                JSONObject entity = bodyObj.getJSONObject("entity");
                String entityId = entity.getString("entityId");
                setEntityId(entityId);
                return true;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void retrieve() {
        try {
            String completeUrl = Domino.getServerUrl() + entityStoreBase + "/" + getEntityId();
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

            if(response.getStatus() >= 500) {
                throwException(response);
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText());
            }  else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText());
            }  else if(response.getStatus() >= 400) {
                throwException(response);
            } else if(response.getStatus() == 200) {
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject entityJsonObject = bodyObj.getJSONObject("entity");
                String entityId = entityJsonObject.getString("entityId");

                Boolean publicRead = null;
                Boolean publicWrite = null;

                try {
                    publicWrite = entityJsonObject.getBoolean("publicWrite");
                } catch (Exception e) {

                }

                try {
                    publicRead = entityJsonObject.getBoolean("publicRead");
                } catch (Exception e) {

                }

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                try {
                    aclWriteList = JSON.toList(entityJsonObject.getJSONArray("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = JSON.toList(entityJsonObject.getJSONArray("aclRead"));
                } catch (Exception e) {

                }

                try {
                    aclWriteList = Arrays.asList(entityJsonObject.getString("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = Arrays.asList(entityJsonObject.getString("aclRead"));
                } catch (Exception e) {

                }

                Iterator<String> it = entityJsonObject.keySet().iterator();
                while(it.hasNext()) {
                    String propertyKey = it.next();
                    if( propertyKey.equals("entityId")) {
                        setEntityId(entityJsonObject.getString(propertyKey));
                    }/* else if(propertyKey.equals("publicRead")) {
                        Boolean value = entityJsonObject.getBoolean(propertyKey);
                        getAcl().setPublicRead(value);
                    } else if(propertyKey.equals("publicWrite")) {
                        Boolean value = entityJsonObject.getBoolean(propertyKey);
                        getAcl().setPublicWrite(value);
                    }  else if(propertyKey.equals("aclRead")) {
                        try {
                            String value = entityJsonObject.getString(propertyKey);
                            getAcl().getAclRead().add(value);
                        } catch (Exception e) {
                            // do nothing
                        }
                        try {
                            JSONArray array = entityJsonObject.getJSONArray(propertyKey);
                            for(int i=0;i<array.length();i++) {
                                try {
                                    String stringValue = array.getString(i);
                                    getAcl().getAclRead().add(stringValue);
                                } catch (Exception e) {
                                }
                                try {
                                    JSONObject jsonObjectValue = array.getJSONObject(i);
                                    String entityIdValue = jsonObjectValue.getString("entityId");
                                    getAcl().getAclRead().add(entityIdValue);
                                } catch (Exception e) {
                                    // do nothing
                                }
                            }
                        } catch (Exception e) {
                            // do nothing
                        }
                    }  else if(propertyKey.equals("aclWrite")) {
                        try {
                            String value = entityJsonObject.getString(propertyKey);
                            getAcl().getAclRead().add(value);
                        } catch (Exception e) {
                            // do nothing
                        }
                        try {
                            JSONArray array = entityJsonObject.getJSONArray(propertyKey);
                            for(int i=0;i<array.length();i++) {
                                try {
                                    String stringValue = array.getString(i);
                                    getAcl().getAclWrite().add(stringValue);
                                } catch (Exception e) {
                                }
                                try {
                                    JSONObject jsonObjectValue = array.getJSONObject(i);
                                    String entityIdValue = jsonObjectValue.getString("entityId");
                                    getAcl().getAclWrite().add(entityIdValue);
                                } catch (Exception e) {
                                    // do nothing
                                }
                            }
                        } catch (Exception e) {
                            // do nothing
                        }
                    }*/
                    else if (propertyKey.equals("publicRead")
                            || propertyKey.equals("publicWrite")
                            || propertyKey.equals("aclRead")
                            || propertyKey.equals("aclWrite")) {
                        // skip
                    } else {
                        entityObj.put(propertyKey, entityJsonObject.get(propertyKey));
                    }
                }

                DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                acl.setPublicWrite(publicWrite);
                acl.setPublicRead(publicRead);
                setEntityId(entityId);
                setAcl(acl);

            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public boolean delete() {
        try {
            String completeUrl = Domino.getServerUrl() + entityStoreBase + "/" + getEntityId();
            HttpRequestWithBody httpRequestWithBody = Unirest.delete(completeUrl);
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header(HEADER_API_KEY, Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                httpRequestWithBody.header(HEADER_AUTH_TOKEN, Domino.getAuthToken());
            }
            HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
            if(response.getStatus() >= 500) {
                throwException(response);
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedException(response.getStatusText());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText());
            } else if(response.getStatus() >= 400) {
                throwException(response);
            } else if(response.getStatus() == 204) {
                return true;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return false;
    }

}
