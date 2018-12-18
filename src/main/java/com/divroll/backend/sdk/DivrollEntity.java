/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.backend.sdk;

import com.divroll.backend.sdk.exception.BadRequestException;
import com.divroll.backend.sdk.exception.DivrollException;
import com.divroll.backend.sdk.exception.NotFoundRequestException;
import com.divroll.backend.sdk.exception.UnauthorizedException;
import com.divroll.backend.sdk.helper.JSON;
import com.google.common.io.ByteStreams;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DivrollEntity extends DivrollBase {

  private String entityStoreBase = "/entities/";
  private String entityId;
  private DivrollACL acl;
  protected JSONObject entityObj = new JSONObject();

  private DivrollEntity() {}

  public DivrollEntity(String entityStore) {
    entityStoreBase = entityStoreBase + entityStore;
  }

  public byte[] getBlobProperty(String blobKey) {
    try {
      GetRequest getRequest =
          (GetRequest)
              Unirest.get(
                  Divroll.getServerUrl()
                      + entityStoreBase
                      + "/"
                      + getEntityId()
                      + "/blobs/"
                      + blobKey);
      if (Divroll.getMasterKey() != null) {
        getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        getRequest.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        getRequest.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        getRequest.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      HttpResponse<InputStream> response = getRequest.asBinary();

      if (response.getStatus() >= 500) {
        throw new DivrollException("Internal Server error"); // TODO
      } else if (response.getStatus() == 404) {
        throw new NotFoundRequestException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throw new DivrollException("Client error"); // TODO
      } else if (response.getStatus() == 200) {
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
    if (entityId == null) {
      throw new DivrollException("Save the entity first before setting a Blob property");
    }
    try {
      HttpRequestWithBody httpRequestWithBody =
          Unirest.post(
              Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey);
      if (Divroll.getMasterKey() != null) {
        httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      JSONArray aclRead = new JSONArray();
      JSONArray aclWrite = new JSONArray();
      if (acl != null) {
        for (String uuid : this.acl.getAclRead()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclRead.put(entityStub);
        }
        for (String uuid : this.acl.getAclWrite()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclWrite.put(entityStub);
        }
      }

      httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
      httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
      httpRequestWithBody.header("Content-Type", "application/octet-stream");

      HttpResponse<InputStream> response = httpRequestWithBody.body(value).asBinary();
      if (response.getStatus() >= 500) {
        throw new DivrollException("Internal Server error"); // TODO
      } else if (response.getStatus() == 404) {
        throw new NotFoundRequestException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throw new DivrollException("Client error"); // TODO
      } else if (response.getStatus() == 201) {
        InputStream responseBody = response.getBody();
      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
  }

  public void deleteBlobProperty(String blobKey) {
    try {
      HttpRequestWithBody getRequest =
          (HttpRequestWithBody)
              Unirest.delete(
                  Divroll.getServerUrl()
                      + entityStoreBase
                      + "/"
                      + getEntityId()
                      + "/blobs/"
                      + blobKey);
      if (Divroll.getMasterKey() != null) {
        getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        getRequest.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        getRequest.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        getRequest.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      HttpResponse<InputStream> response = getRequest.asBinary();

      if (response.getStatus() >= 500) {
        throw new DivrollException("Internal Server error"); // TODO
      } else if (response.getStatus() == 404) {
        throw new NotFoundRequestException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throw new DivrollException("Client error"); // TODO
      } else if (response.getStatus() == 200) {

      }
    } catch (UnirestException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setProperty(String propertyName, Object propertyValue) {
    if (propertyValue == null) {
      entityObj.put(propertyName, JSONObject.NULL);
    } else {
      DivrollPropertyValue divrollPropertyValue = new DivrollPropertyValue(propertyValue);
      entityObj.put(propertyName, divrollPropertyValue.getValue());
    }
  }

  public Object getProperty(String propertyName) {
    Object value = entityObj.get(propertyName);
    if (value instanceof JSONObject) {
      JSONObject jsonObject = (JSONObject) value;
      Map<String, Object> entityMap = JSON.toMap(jsonObject);
      return entityMap;
    } else if (value instanceof JSONArray) {
      JSONArray jsonArray = (JSONArray) value;
      List<Object> list = JSON.toArray(jsonArray);
      return list;
    }
    return value;
  }

  public List<DivrollEntity> links(String linkName) {
    List<DivrollEntity> entities = new LinkedList<DivrollEntity>();
    if (entityId == null) {
      throw new DivrollException("Save the entity first before getting links");
    }
    try {
      String completeUrl =
          Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId() + "/links/" + linkName;

      GetRequest getRequest = (GetRequest) Unirest.get(completeUrl);

      if (Divroll.getMasterKey() != null) {
        getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        getRequest.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        getRequest.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        getRequest.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      HttpResponse<JsonNode> response = getRequest.asJson();

      if (response.getStatus() >= 500) {
        throwException(response);
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throwException(response);
      } else if (response.getStatus() == 200) {

        JsonNode body = response.getBody();
        JSONObject bodyObj = body.getObject();
        JSONObject entitiesJSONObject = bodyObj.getJSONObject("entities");
        JSONArray results = entitiesJSONObject.getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
          DivrollEntity divrollEntity = new DivrollEntity();
          JSONObject entityJSONObject = results.getJSONObject(i);
          Iterator<String> it = entityJSONObject.keySet().iterator();
          while (it.hasNext()) {
            String propertyKey = it.next();
            if (propertyKey.equals("entityId")) {
              divrollEntity.setEntityId(entityJSONObject.getString(propertyKey));
            } else if (propertyKey.equals("publicRead")) {
              try {
                Boolean value = entityJSONObject.getBoolean("publicRead");
                divrollEntity.getAcl().setPublicRead(value);
              } catch (Exception e) {

              }
            } else if (propertyKey.equals("publicWrite")) {
              try {
                Boolean value = entityJSONObject.getBoolean("publicWrite");
                divrollEntity.getAcl().setPublicWrite(value);
              } catch (Exception e) {

              }
            } else if (propertyKey.equals("aclRead")) {
              try {
                List<String> value =
                    JSON.aclJSONArrayToList(entityJSONObject.getJSONArray("aclRead"));
                divrollEntity.getAcl().setAclRead(value);
              } catch (Exception e) {

              }
              try {
                List<String> value = Arrays.asList(entityJSONObject.getString("aclRead"));
                divrollEntity.getAcl().setAclRead(value);
              } catch (Exception e) {

              }
            } else if (propertyKey.equals("aclWrite")) {
              try {
                List<String> value =
                    JSON.aclJSONArrayToList(entityJSONObject.getJSONArray("aclWrite"));
                divrollEntity.getAcl().setAclWrite(value);
              } catch (Exception e) {

              }
              try {
                List<String> value = Arrays.asList(entityJSONObject.getString("aclWrite"));
                divrollEntity.getAcl().setAclWrite(value);
              } catch (Exception e) {

              }
            } else {
              divrollEntity.setProperty(propertyKey, entityJSONObject.get(propertyKey));
            }
          }
          entities.add(divrollEntity);
        }
      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
    return entities;
  }

  public List<DivrollEntity> getEntities(String linkName) {
    List<DivrollEntity> entities = new LinkedList<DivrollEntity>();
    if (entityId == null) {
      throw new DivrollException("Save the entity first before getting links");
    }
    try {
      DivrollEntity divrollEntity = new DivrollEntity();
      String completeUrl =
          Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId() + "/links/" + linkName;
      GetRequest getRequest = (GetRequest) Unirest.get(completeUrl);

      if (Divroll.getMasterKey() != null) {
        getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        getRequest.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        getRequest.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        getRequest.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      HttpResponse<JsonNode> response = getRequest.asJson();

      if (response.getStatus() >= 500) {
        throwException(response);
      } else if (response.getStatus() == 404) {
        throw new NotFoundRequestException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throwException(response);
      } else if (response.getStatus() == 200) {
        JsonNode body = response.getBody();
        JSONObject bodyObj = body.getObject();
        JSONObject entityJsonObject = bodyObj.getJSONObject("entities");
        JSONObject resultJsonObject = entityJsonObject.getJSONObject("results");
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
          aclWriteList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclWrite"));
        } catch (Exception e) {

        }

        try {
          aclReadList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclRead"));
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
        while (it.hasNext()) {
          String propertyKey = it.next();
          if (propertyKey.equals("entityId")) {
            setEntityId(entityJsonObject.getString(propertyKey));
          } else if (propertyKey.equals("publicRead")
              || propertyKey.equals("publicWrite")
              || propertyKey.equals("aclRead")
              || propertyKey.equals("aclWrite")) {
            // skip
          } else {
            Object obj = entityJsonObject.get(propertyKey);
            divrollEntity.setProperty(propertyKey, obj);
          }
        }

        DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
        acl.setPublicWrite(publicWrite);
        acl.setPublicRead(publicRead);
        divrollEntity.setEntityId(entityId);
        divrollEntity.setAcl(acl);
      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
    return entities;
  }

  public void setLink(String linkName, String entityId) {
    if (entityId == null) {
      throw new DivrollException("Save the entity first before creating a link");
    }
    try {
      HttpRequestWithBody httpRequestWithBody =
              Unirest.post(
                      Divroll.getServerUrl()
                              + entityStoreBase
                              + "/"
                              + getEntityId()
                              + "/links/"
                              + linkName
                              + "/"
                              + entityId);

      httpRequestWithBody.queryString("linkType", "set");

      if (Divroll.getMasterKey() != null) {
        httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      JSONArray aclRead = new JSONArray();
      JSONArray aclWrite = new JSONArray();
      if (acl != null) {
        for (String uuid : this.acl.getAclRead()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclRead.put(entityStub);
        }
        for (String uuid : this.acl.getAclWrite()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclWrite.put(entityStub);
        }
      }

      httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
      httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
      httpRequestWithBody.header("Content-Type", "application/json");

      HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
      if (response.getStatus() >= 500) {
        throw new DivrollException(response.getStatusText()); // TODO
      } else if (response.getStatus() == 404) {
        throw new NotFoundRequestException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throw new DivrollException("Client error"); // TODO
      } else if (response.getStatus() == 201) {
      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
  }

  public void addLink(String linkName, String entityId) {
    if (entityId == null) {
      throw new DivrollException("Save the entity first before creating a link");
    }
    try {
      HttpRequestWithBody httpRequestWithBody =
          Unirest.post(
              Divroll.getServerUrl()
                  + entityStoreBase
                  + "/"
                  + getEntityId()
                  + "/links/"
                  + linkName
                  + "/"
                  + entityId);
      if (Divroll.getMasterKey() != null) {
        httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      JSONArray aclRead = new JSONArray();
      JSONArray aclWrite = new JSONArray();
      if (acl != null) {
        for (String uuid : this.acl.getAclRead()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclRead.put(entityStub);
        }
        for (String uuid : this.acl.getAclWrite()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclWrite.put(entityStub);
        }
      }

      httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
      httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
      httpRequestWithBody.header("Content-Type", "application/json");

      HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
      if (response.getStatus() >= 500) {
        throw new DivrollException(response.getStatusText()); // TODO
      } else if (response.getStatus() == 404) {
        throw new NotFoundRequestException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throw new DivrollException("Client error"); // TODO
      } else if (response.getStatus() == 201) {
      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
  }

  public void removeLink(String linkName, String entityId) {
    if (entityId == null) {
      throw new DivrollException("Save the entity first before removing a link");
    }
    try {
      HttpRequestWithBody httpRequestWithBody =
          Unirest.delete(
              Divroll.getServerUrl()
                  + entityStoreBase
                  + "/"
                  + getEntityId()
                  + "/links/"
                  + linkName
                  + "/"
                  + entityId);
      if (Divroll.getMasterKey() != null) {
        httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      JSONArray aclRead = new JSONArray();
      JSONArray aclWrite = new JSONArray();
      if (acl != null) {
        for (String uuid : this.acl.getAclRead()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclRead.put(entityStub);
        }
        for (String uuid : this.acl.getAclWrite()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclWrite.put(entityStub);
        }
      }

      httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
      httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
      httpRequestWithBody.header("Content-Type", "application/json");

      HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
      if (response.getStatus() >= 500) {
        throw new DivrollException(response.getStatusText()); // TODO
      } else if (response.getStatus() == 404) {
        throw new NotFoundRequestException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throw new DivrollException("Client error"); // TODO
      } else if (response.getStatus() == 201) {
      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
  }

  public void removeLinks(String linkName) {
    if (entityId == null) {
      throw new DivrollException("Save the entity first before removing links");
    }
    try {
      HttpRequestWithBody httpRequestWithBody =
          Unirest.delete(
              Divroll.getServerUrl()
                  + entityStoreBase
                  + "/"
                  + getEntityId()
                  + "/links/"
                  + linkName);
      if (Divroll.getMasterKey() != null) {
        httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      JSONArray aclRead = new JSONArray();
      JSONArray aclWrite = new JSONArray();
      if (acl != null) {
        for (String uuid : this.acl.getAclRead()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclRead.put(entityStub);
        }
        for (String uuid : this.acl.getAclWrite()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclWrite.put(entityStub);
        }
      }

      httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
      httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
      httpRequestWithBody.header("Content-Type", "application/json");

      HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
      if (response.getStatus() >= 500) {
        throw new DivrollException(response.getStatusText()); // TODO
      } else if (response.getStatus() == 404) {
        throw new NotFoundRequestException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throw new DivrollException("Client error"); // TODO
      } else if (response.getStatus() == 201) {
      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
  }

  public DivrollACL getAcl() {
    return acl;
  }

  public void setAcl(DivrollACL acl) {
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
      HttpRequestWithBody httpRequestWithBody =
          Unirest.post(Divroll.getServerUrl() + entityStoreBase);
      if (Divroll.getMasterKey() != null) {
        httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      JSONArray aclRead = new JSONArray();
      JSONArray aclWrite = new JSONArray();
      if (acl != null) {
        for (String uuid : this.acl.getAclRead()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclRead.put(entityStub);
        }
        for (String uuid : this.acl.getAclWrite()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclWrite.put(entityStub);
        }
      }

      entityObj.put("aclRead", aclRead);
      entityObj.put("aclWrite", aclWrite);
      entityObj.put(
          "publicRead",
          (this.acl != null && this.acl.getPublicRead() != null)
              ? this.acl.getPublicRead()
              : JSONObject.NULL);
      entityObj.put(
          "publicWrite",
          (this.acl != null && this.acl.getPublicWrite() != null)
              ? this.acl.getPublicWrite()
              : JSONObject.NULL);
      JSONObject body = new JSONObject();
      body.put("entity", entityObj);

      httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
      httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
      httpRequestWithBody.header("Content-Type", "application/json");

      System.out.println("BODY - " + body.toString());

      HttpResponse<JsonNode> response = httpRequestWithBody.body(body).asJson();

      if (response.getStatus() >= 500) {
        throw new DivrollException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() >= 401) {
        throw new DivrollException(response.getStatusText());
      } else if (response.getStatus() == 201) {
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
      String completeUrl = Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId();
      HttpRequestWithBody httpRequestWithBody = Unirest.put(completeUrl);
      if (Divroll.getMasterKey() != null) {
        httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      JSONArray aclRead = new JSONArray();
      JSONArray aclWrite = new JSONArray();
      if (acl != null) {
        for (String uuid : this.acl.getAclRead()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclRead.put(entityStub);
        }
        for (String uuid : this.acl.getAclWrite()) {
          JSONObject entityStub = new JSONObject();
          entityStub.put("entityId", uuid);
          aclWrite.put(entityStub);
        }
      }

      entityObj.put("aclRead", aclRead);
      entityObj.put("aclWrite", aclWrite);
      entityObj.put(
          "publicRead",
          (this.acl != null && this.acl.getPublicRead() != null)
              ? this.acl.getPublicRead()
              : JSONObject.NULL);
      entityObj.put(
          "publicWrite",
          (this.acl != null && this.acl.getPublicWrite() != null)
              ? this.acl.getPublicWrite()
              : JSONObject.NULL);

      JSONObject body = new JSONObject();
      body.put("entity", entityObj);

      httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
      httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
      httpRequestWithBody.header("Content-Type", "application/json");

      HttpResponse<JsonNode> response = httpRequestWithBody.body(body).asJson();

      if (response.getStatus() >= 500) {
        throw new DivrollException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() >= 401) {
        throw new DivrollException(response.getStatusText());
      } else if (response.getStatus() == 201) {
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
      String completeUrl = Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId();
      GetRequest getRequest = (GetRequest) Unirest.get(completeUrl);

      if (Divroll.getMasterKey() != null) {
        getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        getRequest.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        getRequest.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        getRequest.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      HttpResponse<JsonNode> response = getRequest.asJson();

      if (response.getStatus() >= 500) {
        throwException(response);
      } else if (response.getStatus() == 404) {
        throw new NotFoundRequestException(response.getStatusText());
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throwException(response);
      } else if (response.getStatus() == 200) {
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
          aclWriteList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclWrite"));
        } catch (Exception e) {

        }

        try {
          aclReadList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclRead"));
        } catch (Exception e) {

        }

        try {
          JSONObject jsonObject = entityJsonObject.getJSONObject("aclWrite");
          aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
        } catch (Exception e) {

        }
        try {
          JSONObject jsonObject = entityJsonObject.getJSONObject("aclRead");
          aclReadList = Arrays.asList(jsonObject.getString("entityId"));
        } catch (Exception e) {

        }

        Iterator<String> it = entityJsonObject.keySet().iterator();
        while (it.hasNext()) {
          String propertyKey = it.next();
          if (propertyKey.equals("entityId")) {
            setEntityId(entityJsonObject.getString(propertyKey));
          } else if (propertyKey.equals("publicRead")
              || propertyKey.equals("publicWrite")
              || propertyKey.equals("aclRead")
              || propertyKey.equals("aclWrite")) {
            // skip
          } else {
            Object obj = entityJsonObject.get(propertyKey);
            entityObj.put(propertyKey, obj);
          }
        }

        DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
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
      String completeUrl = Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId();
      HttpRequestWithBody httpRequestWithBody = Unirest.delete(completeUrl);
      if (Divroll.getMasterKey() != null) {
        httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }
      HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
      if (response.getStatus() >= 500) {
        throwException(response);
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throwException(response);
      } else if (response.getStatus() == 204) {
        return true;
      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
    return false;
  }

  public void put(String propertyKey, Object propertyValue) {
    // TODO - add check for propertyValue
    entityObj.put(propertyKey, propertyValue);
  }

}
