package com.divroll.backend.filter;

import org.json.JSONObject;
import org.json.JSONString;

public class EqualQueryFilter implements QueryFilter {
    private JSONObject filter = new JSONObject();
    private EqualQueryFilter() {}
    public EqualQueryFilter(String propertyName, String propertyValue) {
        JSONObject opFind = new JSONObject();
        opFind.put(propertyName, propertyValue);
        filter.put("$find", opFind);
    }
    public EqualQueryFilter(String propertyName, Double propertyValue) {
        JSONObject opFind = new JSONObject();
        opFind.put(propertyName, propertyValue);
        filter.put("$find", opFind);
    }
    public EqualQueryFilter(String propertyName, Boolean propertyValue) {
        JSONObject opFind = new JSONObject();
        opFind.put(propertyName, propertyValue);
        filter.put("$find", opFind);
    }
    @Override
    public String toString() {
        return filter.toString();
    }
}
