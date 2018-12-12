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

import com.divroll.backend.exception.DivrollException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.json.JSONObject;

public class DivrollBase {

  public static final String HEADER_MASTER_KEY = "X-Divroll-Master-Key";
  public static final String HEADER_APP_ID = "X-Divroll-App-Id";
  public static final String HEADER_API_KEY = "X-Divroll-Api-Key";
  public static final String HEADER_AUTH_TOKEN = "X-Divroll-Auth-Token";
  public static final String HEADER_NAMESPACE = "X-Divroll-Namespace";

  public void throwException(HttpResponse<JsonNode> response) {
    JsonNode body = response.getBody();
    JSONObject jsonObject = body.getObject();
    JSONObject statusInfo = jsonObject.getJSONObject("org.restlet.engine.application.StatusInfo");
    throw new DivrollException(statusInfo.getString("description"));
  }
}
