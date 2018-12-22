package com.divroll.backend.sdk.helper;

import com.divroll.backend.sdk.DivrollEntity;
import com.divroll.backend.sdk.DivrollEntityStub;
import org.json.JSONArray;
import org.json.JSONObject;

public class DivrollEntityHelper {
    public static JSONObject convert(DivrollEntity entity) {
        if(entity == null) {
            return null;
        }
        JSONObject jsonObject = entity.getProperties();
        jsonObject.put("entityId", entity.getEntityId());
        jsonObject.put("entityType", entity.getEntityType());
        jsonObject.put("publicRead", entity.getAcl().getPublicRead());
        jsonObject.put("publicWrite", entity.getAcl().getPublicWrite());

        JSONArray aclRead = new JSONArray();
        JSONArray aclWrite = new JSONArray();
        if(entity.getAcl() != null) {
            for(String uuid : entity.getAcl().getAclRead()) {
                JSONObject entityStub = new JSONObject();
                entityStub.put("entityId", uuid);
                aclRead.put(entityStub);
            }
            for(String uuid : entity.getAcl().getAclWrite()) {
                JSONObject entityStub = new JSONObject();
                entityStub.put("entityId", uuid);
                aclWrite.put(entityStub);
            }
        }
        jsonObject.put("aclRead", aclRead);
        jsonObject.put("aclWrite", aclWrite);

        return jsonObject;
    }
    public static DivrollEntity convert(JSONObject jsonObject) {
        if(jsonObject == null) {
            return null;
        }
        String entityType = jsonObject.getString("entityType");
        DivrollEntity divrollEntity = new DivrollEntity(entityType);
        divrollEntity.setEntityId(jsonObject.getString("entityId"));
        return divrollEntity;
    }

    public static DivrollEntityStub convertStub(JSONObject jsonObject) {
        if(jsonObject == null) {
            return null;
        }
        String entityType = jsonObject.getString("entityType");
        DivrollEntityStub divrollEntity = new DivrollEntityStub(entityType, jsonObject.getString("entityId"));
        divrollEntity.setEntityId(jsonObject.getString("entityId"));
        return divrollEntity;
    }
}
