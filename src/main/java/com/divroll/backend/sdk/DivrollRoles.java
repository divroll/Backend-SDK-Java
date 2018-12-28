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
package com.divroll.backend.sdk;

import com.divroll.backend.sdk.exception.BadRequestException;
import com.divroll.backend.sdk.exception.UnauthorizedException;
import com.divroll.backend.sdk.helper.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DivrollRoles extends DivrollBase {

  private static final String rolesUrl = "/entities/roles";

  private List<DivrollRole> roles;
  private Long skip;
  private Long limit;
  private Long count;

  public List<DivrollRole> getRoles() {
    if (roles == null) {
      roles = new LinkedList<DivrollRole>();
    }
    return roles;
  }

  public void setRoles(List<DivrollRole> roles) {
    this.roles = roles;
  }

  public Long getSkip() {
    return skip;
  }

  public void setSkip(Long skip) {
    this.skip = skip;
  }

  public Long getLimit() {
    return limit;
  }

  public void setLimit(Long limit) {
    this.limit = limit;
  }

  public void query() {
    try {
      GetRequest getRequest = (GetRequest) Unirest.get(Divroll.getServerUrl() + rolesUrl);

      if (Divroll.getMasterKey() != null) {
        getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
      }
      if (Divroll.getAppId() != null) {
        getRequest.header(HEADER_APP_ID, Divroll.getAppId());
      }
      if (Divroll.getApiKey() != null) {
        getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
      }
      if (Divroll.getAuthToken() != null) {
        getRequest.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
      }
      if (Divroll.getNameSpace() != null) {
        getRequest.header(HEADER_NAMESPACE, Divroll.getNameSpace());
      }


        if (skip != null) {
            getRequest.queryString("skip", String.valueOf(getSkip()));
        }
        if (limit != null) {
            getRequest.queryString("limit", String.valueOf(getLimit()));
        }

        if (count != null) {
            getRequest.queryString("count", String.valueOf(count));
        }


      HttpResponse<JsonNode> response = getRequest.asJson();

      if (response.getStatus() >= 500) {
        throwException(response);
      } else if (response.getStatus() == 401) {
        throw new UnauthorizedException(response.getStatusText());
      } else if (response.getStatus() == 400) {
        throw new BadRequestException(response.getStatusText());
      } else if (response.getStatus() >= 400) {
        throwException(response);
      } else if (response.getStatus() == 200) {

        getRoles().clear();

        JsonNode body = response.getBody();
        JSONObject bodyObj = body.getObject();
        JSONObject roles = bodyObj.getJSONObject("roles");
        JSONArray results = roles.getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
          JSONObject roleObj = results.getJSONObject(i);
          String entityId = roleObj.getString("entityId");
          String name = roleObj.getString("name");
          Boolean publicRead = roleObj.getBoolean("publicRead");
          Boolean publicWrite = roleObj.getBoolean("publicWrite");

          List<String> aclWriteList = null;
          List<String> aclReadList = null;

          try {
            aclWriteList = JSON.aclJSONArrayToList(roleObj.getJSONArray("aclWrite"));
          } catch (Exception e) {

          }

          try {
            aclReadList = JSON.aclJSONArrayToList(roleObj.getJSONArray("aclRead"));
          } catch (Exception e) {

          }

          try {
            aclWriteList = Arrays.asList(roleObj.getString("aclWrite"));
          } catch (Exception e) {

          }

          try {
            aclReadList = Arrays.asList(roleObj.getString("aclRead"));
          } catch (Exception e) {

          }

          DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
          acl.setPublicWrite(publicWrite);
          acl.setPublicRead(publicRead);

          DivrollRole role = new DivrollRole();
          role.setEntityId(entityId);
          role.setName(name);
          role.setAcl(acl);

          getRoles().add(role);
        }
        //

      }
    } catch (UnirestException e) {
      e.printStackTrace();
    }
  }
}
