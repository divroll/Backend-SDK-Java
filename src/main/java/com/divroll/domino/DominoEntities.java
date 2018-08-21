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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DominoEntities extends DominoBase {

    private static String entityStoreUrl = "/entities/";

    private List<DominoEntity> entities;
    private int skip;
    private int limit;
    private String entityStore;

    private DominoEntities() {}

    public DominoEntities(String entityStore) {
        this.entityStore = entityStore;
        entityStoreUrl = entityStoreUrl + entityStore;
    }

    public List<DominoEntity> getEntities() {
        if(entities == null) {
            entities = new LinkedList<DominoEntity>();
        }
        return entities;
    }

    public void setEntities(List<DominoEntity> entities) {
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
            GetRequest getRequest = (GetRequest) Unirest.get(Domino.getServerUrl()
                    + entityStoreUrl);

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

                getEntities().clear();

                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject entitiesJSONObject = bodyObj.getJSONObject("entities");
                JSONArray results = entitiesJSONObject.getJSONArray("results");
                for(int i=0;i<results.length();i++){
                    DominoEntity dominoEntity = new DominoEntity(this.entityStore);
                    JSONObject entityJSONObject = results.getJSONObject(i);
                    Iterator<String> it = entityJSONObject.keySet().iterator();
                    while(it.hasNext()) {
                        String propertyKey = it.next();
                        if( propertyKey.equals("entityId")) {
                            dominoEntity.setEntityId(entityJSONObject.getString(propertyKey));
                        }
                        else if (propertyKey.equals("publicRead")) {
                            try {
                                Boolean value = entityJSONObject.getBoolean("publicRead");
                                dominoEntity.getAcl().setPublicRead(value);
                            } catch (Exception e) {

                            }
                        } else if(propertyKey.equals("publicWrite")) {
                            try {
                                Boolean value = entityJSONObject.getBoolean("publicWrite");
                                dominoEntity.getAcl().setPublicWrite(value);
                            } catch (Exception e) {

                            }
                        } else if(propertyKey.equals("aclRead")) {
                            try {
                                List<String> value = JSON.toList(entityJSONObject.getJSONArray("aclRead"));
                                dominoEntity.getAcl().setAclRead(value);
                            } catch (Exception e) {

                            }
                            try {
                                List<String> value = Arrays.asList(entityJSONObject.getString("aclRead"));
                                dominoEntity.getAcl().setAclRead(value);
                            } catch (Exception e) {

                            }
                        } else if(propertyKey.equals("aclWrite")) {
                            try {
                                List<String> value = JSON.toList(entityJSONObject.getJSONArray("aclWrite"));
                                dominoEntity.getAcl().setAclWrite(value);
                            } catch (Exception e) {

                            }
                            try {
                                List<String> value = Arrays.asList(entityJSONObject.getString("aclWrite"));
                                dominoEntity.getAcl().setAclWrite(value);
                            } catch (Exception e) {

                            }
                        } else {
                            dominoEntity.setProperty(propertyKey, entityJSONObject.get(propertyKey));
                        }
                    }
                    getEntities().add(dominoEntity);
                }

            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
}
