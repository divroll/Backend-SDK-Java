package com.divroll.backend.sdk;

import com.divroll.backend.sdk.helper.DivrollEntityHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public abstract class LinkableDivrollBase extends DivrollBase {
    protected DivrollLink processLink(JSONObject linksObj) {
        try{
            if(linksObj != null) {
                String linkName = linksObj.getString("linkName");
                JSONArray entities = linksObj.getJSONArray("entities");
                List<DivrollEntityStub> divrollEntities = new LinkedList<>();
                if(entities != null) {
                    for (int i=0; i<entities.length(); i++) {
                        JSONObject entity = entities.getJSONObject(i);
                        DivrollEntityStub divrollEntity = DivrollEntityHelper.convertStub(entity);
                        divrollEntities.add(divrollEntity);
                    }
                } else {
                    JSONObject entity = linksObj.getJSONObject("entities");
                    DivrollEntityStub divrollEntity = DivrollEntityHelper.convertStub(entity);
                    divrollEntities.add(divrollEntity);
                }
                return new DivrollLink(linkName, divrollEntities);
            }
        } catch (Exception e) {

        }
        return null;
    }

}
