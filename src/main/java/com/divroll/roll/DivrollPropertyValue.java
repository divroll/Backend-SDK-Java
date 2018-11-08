package com.divroll.roll;

import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.util.List;
import java.util.Map;

public class DivrollPropertyValue {
    private Object value = null;
    public DivrollPropertyValue(Object value) {
//         //java.lang.String
//             //java.lang.String
        if(!value.getClass().getName().equalsIgnoreCase(String.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Boolean.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Integer.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Long.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Short.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Float.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Double.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Map.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(List.class.getName())) {
            //throw new UnsupportedPropertyValueException(value.getClass().getName());
        }
        if(value.getClass().getName().equals(Map.class.getName())) {
            String jsonText = JSONValue.toJSONString(value);
            JSONObject mapObject = new JSONObject(jsonText);
            this.value = mapObject;
        } else {
            this.value = value;
        }
    }
    public Object getValue() {
        return value;
    }
}
