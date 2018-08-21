package com.divroll.domino.helper;

import org.json.JSONArray;

import java.util.LinkedList;
import java.util.List;

public class JSON {
    private JSON() {}
    public static List<String> toList(JSONArray jsonArray) {
        if(jsonArray == null)
            return null;
        List<String> list = new LinkedList<String>();
        for(int i=0;i<jsonArray.length();i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }
}
