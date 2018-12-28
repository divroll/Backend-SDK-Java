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
import com.divroll.backend.sdk.filter.QueryFilter;
import com.divroll.backend.sdk.helper.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DivrollUsers extends LinkableDivrollBase {

  private static final String usersUrl = "/entities/users";

  private List<DivrollUser> users;
  private Integer skip = 0;
  private Integer limit = 100;
  private Boolean count;
  private Long result;
  private List<String> roles;
  private List<String> include;
  private String sort;
  private String authToken;

  public List<DivrollUser> getUsers() {
    if (users == null) {
      users = new LinkedList<DivrollUser>();
    }
    return users;
  }

  public void setUsers(List<DivrollUser> users) {
    this.users = users;
  }

  public int getSkip() {
    return skip;
  }

  public void setSkip(int skip) {
    this.skip = skip;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public void query(QueryFilter filter) {
      try {
          String completeUrl = Divroll.getServerUrl() + usersUrl;

          GetRequest getRequest = (GetRequest) Unirest.get(completeUrl);

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

          final JSONArray rolesArray = new JSONArray();
          if(roles != null && !roles.isEmpty()) {
              roles.forEach(role -> {
                  rolesArray.put(role);
              });
          }

          if(rolesArray.length() > 0) {
              getRequest.queryString("roles", rolesArray.toString());
          }

          if (count != null) {
              getRequest.queryString("count", String.valueOf(count));
          }

          if(filter != null) {
              getRequest.queryString("queries", filter.toString());
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

          if(sort != null) {
              getRequest.queryString("sort", sort);
          }

          if (include != null && !include.isEmpty()) {
              JSONArray linkNameArray = new JSONArray();
              for (String linkName : include) {
                  linkNameArray.put(linkName);
              }
              getRequest.queryString("include", linkNameArray.toString());
          }

          if (authToken != null && !authToken.isEmpty()) {
              getRequest.queryString("authToken", authToken);
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

              getUsers().clear();

              JsonNode body = response.getBody();

              JSONObject bodyObj = body.getObject();
              JSONObject usersJSONObj = bodyObj.getJSONObject("users");
              JSONArray results = new JSONArray();

              try {
                  results = usersJSONObj.getJSONArray("results");
              } catch (Exception e) {

              }

              try {
                  JSONObject jsonObject = usersJSONObj.getJSONObject("results");
                  results.put(jsonObject);
              } catch (Exception e) {

              }

              try{
                  result = usersJSONObj.getLong("count");
              } catch (Exception e) {

              }

              for (int i = 0; i < results.length(); i++) {
                  JSONObject userObj = results.getJSONObject(i);
                  String entityId = userObj.getString("entityId");
                  String username = userObj.getString("username");
                  Boolean publicRead = userObj.getBoolean("publicRead");
                  Boolean publicWrite = userObj.getBoolean("publicWrite");

                  List<String> aclWriteList = null;
                  List<String> aclReadList = null;

                  try {
                      aclWriteList = JSON.aclJSONArrayToList(userObj.getJSONArray("aclWrite"));
                  } catch (Exception e) {

                  }

                  try {
                      aclReadList = JSON.aclJSONArrayToList(userObj.getJSONArray("aclRead"));
                  } catch (Exception e) {

                  }

                  try {
                      aclWriteList = Arrays.asList(userObj.getString("aclWrite"));
                  } catch (Exception e) {

                  }

                  try {
                      aclReadList = Arrays.asList(userObj.getString("aclRead"));
                  } catch (Exception e) {

                  }

                  JSONArray userRoles = null;
                  try {
                      userRoles = userObj.getJSONArray("roles");
                  } catch (Exception e) {

                  }

                  List<DivrollRole> divrollRoles = null;
                  try {
                      if (userRoles != null) {
                          Object roleObjects = userObj.get("roles");
                          if (roleObjects instanceof JSONArray) {
                              divrollRoles = new LinkedList<DivrollRole>();
                              for (int j = 0; j < userRoles.length(); j++) {
                                  JSONObject jsonObject = userRoles.getJSONObject(j);
                                  String roleId = jsonObject.getString("entityId");
                                  DivrollRole divrollRole = new DivrollRole();
                                  divrollRole.setEntityId(roleId);
                                  divrollRoles.add(divrollRole);
                              }
                          } else if (roleObjects instanceof JSONObject) {
                              divrollRoles = new LinkedList<DivrollRole>();
                              JSONObject jsonObject = (JSONObject) roleObjects;
                              String roleId = jsonObject.getString("entityId");
                              DivrollRole divrollRole = new DivrollRole();
                              divrollRole.setEntityId(roleId);
                              divrollRoles.add(divrollRole);
                          }
                      }

                  } catch (Exception e) {
                      // do nothing
                  }

                  try{
                      result = userObj.getLong("count");
                  } catch (Exception e) {

                  }

                  DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                  acl.setPublicWrite(publicWrite);
                  acl.setPublicRead(publicRead);

                  DivrollUser user = new DivrollUser();
                  user.setEntityId(entityId);
                  user.setAcl(acl);
                  user.setRoles(divrollRoles);
                  user.setUsername(username);

                  try{
                      String dateCreated = userObj.getString("dateCreated");
                      user.setDateCreated(dateCreated);
                  } catch (Exception e) {

                  }

                  try{
                      String dateUpdated = userObj.getString("dateUpdated");
                      user.setDateUpdated(dateUpdated);
                  } catch (Exception e) {

                  }


                  JSONArray links = null;
                  try{
                      links = userObj.getJSONArray("links");
                  } catch (Exception e) {

                  }
                  if(links != null) {
                      for(int j=0;j<links.length();j++) {
                          JSONObject linksObj = links.getJSONObject(j);
                          DivrollLink divrollLink = processLink(linksObj);
                          user.getLinks().add(divrollLink);
                      }
                  } else {
                      JSONObject linksObj = null;
                      try{
                          linksObj = userObj.getJSONObject("links");
                      } catch (Exception e) {

                      }
                      DivrollLink divrollLink = processLink(linksObj);
                      user.getLinks().add(divrollLink);
                  }

                  getUsers().add(user);
              }
              //

          }
      } catch (UnirestException e) {
          e.printStackTrace();
      }
  }

  public void query() {
      query(null);
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

    public void setCount(Boolean count) {
        this.count = count;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setInclude(List<String> include) {
        this.include = include;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Long getResult() {
        return result;
    }
}
