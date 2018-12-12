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
