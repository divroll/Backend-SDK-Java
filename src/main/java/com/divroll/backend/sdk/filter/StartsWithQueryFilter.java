package com.divroll.backend.sdk.filter;

import org.json.JSONObject;
import org.json.JSONString;

public class StartsWithQueryFilter implements QueryFilter {
    private JSONObject filter = new JSONObject();
    private StartsWithQueryFilter() {}
    public StartsWithQueryFilter(String propertyName, String propertyValue) {
        JSONObject opFind = new JSONObject();
        opFind.put(propertyName, propertyValue);
        filter.put("$findStartingWith", opFind);
    }
    @Override
    public String toString() {
        return filter.toString();
    }
}
