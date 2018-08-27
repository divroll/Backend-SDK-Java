package com.divroll.roll.helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class JSON {
    private JSON() {}
    public static List<String> aclJSONArrayToList(JSONArray jsonArray) {
        if(jsonArray == null)
            return null;
        List<String> list = new LinkedList<String>();
        for(int i=0;i<jsonArray.length();i++) {
            JSONObject aclObject = jsonArray.getJSONObject(i);
            if(aclObject != null) {
                list.add(aclObject.getString("entityId"));
            }
        }
        return list;
    }
    public static List<Object> toArray(JSONArray jsonArray) {
        List<Object> list = new LinkedList<Object>();
        for(int i=0;i<jsonArray.length();i++){

            try {
                if(jsonArray.get(i) == null) {
                    list.add(null);
                }
            } catch (Exception e) {

            }

            try {
                JSONObject value = jsonArray.getJSONObject(i);
                list.add(toMap(value));
            } catch (Exception e) {

            }

            try {
                JSONArray value = jsonArray.getJSONArray(i);
                list.add(toArray(value));
            } catch (Exception e) {

            }

            try {
                Double value = jsonArray.getDouble(i);
                list.add(value);
            } catch (Exception e) {

            }

            try {
                Boolean value = jsonArray.getBoolean(i);
                list.add(value);
            } catch (Exception e) {

            }
            try {
                String value = jsonArray.getString(i);
                list.add(value);
            } catch (Exception e) {

            }

        }
        return list;
    }
    public static Map<String, Object> toMap(JSONObject jsonObject) {
        Iterator<String> it = jsonObject.keySet().iterator();
        Map<String, Object> enittyMap = new LinkedHashMap<String, Object>();
        while (it.hasNext()) {
            String k = it.next();
            try {
                JSONObject jso = jsonObject.getJSONObject(k);
                enittyMap.put(k, toMap(jso));
            } catch (Exception e) {

            }
            try {
                JSONArray jsa = jsonObject.getJSONArray(k);
                enittyMap.put(k, toArray(jsa));
            } catch (Exception e) {

            }
            try {
                Boolean value = jsonObject.getBoolean(k);
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                Long value = jsonObject.getLong(k);
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                Double value = jsonObject.getDouble(k);
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                String value = jsonObject.getString(k);
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
        }
        return enittyMap;
    }
}
