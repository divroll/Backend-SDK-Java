/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.backend;

import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.util.List;
import java.util.Map;

public class DivrollPropertyValue {
  private Object value = null;

  public DivrollPropertyValue(Object value) {
    //         //java.lang.String
    //             //java.lang.String
    if (!value.getClass().getName().equalsIgnoreCase(String.class.getName())
        || !value.getClass().getName().equalsIgnoreCase(Boolean.class.getName())
        || !value.getClass().getName().equalsIgnoreCase(Integer.class.getName())
        || !value.getClass().getName().equalsIgnoreCase(Long.class.getName())
        || !value.getClass().getName().equalsIgnoreCase(Short.class.getName())
        || !value.getClass().getName().equalsIgnoreCase(Float.class.getName())
        || !value.getClass().getName().equalsIgnoreCase(Double.class.getName())
        || !value.getClass().getName().equalsIgnoreCase(Map.class.getName())
        || !value.getClass().getName().equalsIgnoreCase(List.class.getName())) {
      // throw new UnsupportedPropertyValueException(value.getClass().getName());
    }
    if (value.getClass().getName().equals(Map.class.getName())) {
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
