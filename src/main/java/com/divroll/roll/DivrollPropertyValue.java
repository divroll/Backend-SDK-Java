package com.divroll.roll;

import java.util.List;
import java.util.Map;

public class DivrollPropertyValue {
    private Object value = null;
    public DivrollPropertyValue(Object value) {
//        System.out.println(value.getClass().getName()); //java.lang.String
//        System.out.println(String.class.getName());     //java.lang.String
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
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
}