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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DivrollEntities extends DivrollBase {

  private static String entityStoreUrl = "/entities/";

  private List<DivrollEntity> entities;
  private int skip;
  private int limit;
  private String entityStore;

  private DivrollEntities() {}

  public DivrollEntities(String entityStore) {
    this.entityStore = entityStore;
    entityStoreUrl = entityStoreUrl + entityStore;
  }

  public List<DivrollEntity> getEntities() {
    if (entities == null) {
      entities = new LinkedList<DivrollEntity>();
    }
    return entities;
  }

  public void setEntities(List<DivrollEntity> entities) {
    this.entities = entities;
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
      GetRequest getRequest = (GetRequest) Unirest.get(Divroll.getServerUrl() + entityStoreUrl);

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

        getEntities().clear();

        JsonNode body = response.getBody();
        JSONObject bodyObj = body.getObject();
        JSONObject entitiesJSONObject = bodyObj.getJSONObject("entities");
        JSONArray results = entitiesJSONObject.getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
          DivrollEntity divrollEntity = new DivrollEntity(this.entityStore);
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
          getEntities().add(divrollEntity);
        }
      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
  }
}
