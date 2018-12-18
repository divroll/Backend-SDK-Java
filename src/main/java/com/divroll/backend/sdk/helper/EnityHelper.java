package com.divroll.backend.sdk.helper;

import com.divroll.backend.sdk.DivrollACL;
import com.divroll.backend.sdk.DivrollEntity;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EnityHelper {
    public static DivrollEntity JSONObjectToEntity(JSONObject entityJsonObject, String entityType) {

        DivrollEntity divrollEntity = new DivrollEntity(entityType);

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
                divrollEntity.setEntityId(entityJsonObject.getString(propertyKey));
            } else if (propertyKey.equals("publicRead")
                    || propertyKey.equals("publicWrite")
                    || propertyKey.equals("aclRead")
                    || propertyKey.equals("aclWrite")) {
                // skip
            } else {
                Object obj = entityJsonObject.get(propertyKey);
                divrollEntity.put(propertyKey, obj);
            }
        }

        DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
        acl.setPublicWrite(publicWrite);
        acl.setPublicRead(publicRead);
        divrollEntity.setEntityId(entityId);
        divrollEntity.setAcl(acl);
        return divrollEntity;
    }
}
