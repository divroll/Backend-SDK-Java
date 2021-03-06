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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.divroll.backend.sdk.exception.BadRequestException;
import com.divroll.backend.sdk.exception.UnauthorizedException;
import com.divroll.backend.sdk.filter.QueryFilter;
import com.divroll.backend.sdk.helper.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

public class DivrollEntities extends LinkableDivrollBase {

	private String entityStoreUrl = "/entities/";

	private List<DivrollEntity> entities;
	private Integer skip;
	private Integer limit;
	private Boolean count;
	private Long result;
	private String sort;
	private String entityStore;
	private List<String> include;
	private String authToken;

	private DivrollEntities() {
	}

	public DivrollEntities(String entityStore) {
		this.entityStore = entityStore;
		entityStoreUrl = entityStoreUrl + entityStore;
	}

	public List<DivrollEntity> getEntities() {
		if (entities == null) {
			entities = new LinkedList<DivrollEntity>();
		}
		return entities;
	}

	public void setEntities(List<DivrollEntity> entities) {
		this.entities = entities;
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
			GetRequest getRequest = Unirest.get(Divroll.getServerUrl() + entityStoreUrl);

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
			if (filter != null) {
				getRequest.queryString("queries", filter.toString());
			}

			if (count != null) {
				getRequest.queryString("count", Boolean.valueOf(count));
			}

			if (sort != null) {
				getRequest.queryString("sort", sort);
			}

			if (skip != null) {
				getRequest.queryString("skip", String.valueOf(getSkip()));
			}
			if (limit != null) {
				getRequest.queryString("limit", String.valueOf(getLimit()));
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

				getEntities().clear();

				JsonNode body = response.getBody();
				JSONObject bodyObj = body.getObject();
				JSONObject entitiesJSONObject = bodyObj.getJSONObject("entities");
				JSONArray results = entitiesJSONObject.getJSONArray("results");
				try {
					result = entitiesJSONObject.getLong("count");
				} catch (Exception e) {

				}
				for (int i = 0; i < results.length(); i++) {
					DivrollEntity divrollEntity = new DivrollEntity(this.entityStore);
					JSONObject entityJSONObject = results.getJSONObject(i);
					Iterator<String> it = entityJSONObject.keySet().iterator();
					while (it.hasNext()) {
						String propertyKey = it.next();
						if (propertyKey.equals("entityId")) {
							divrollEntity.setEntityId(entityJSONObject.getString(propertyKey));
						} else if (propertyKey.equals("publicRead")) {
							try {
								Boolean value = entityJSONObject.getBoolean("publicRead");
								divrollEntity.getAcl().setPublicRead(value);
							} catch (Exception e) {

							}
						} else if (propertyKey.equals("publicWrite")) {
							try {
								Boolean value = entityJSONObject.getBoolean("publicWrite");
								divrollEntity.getAcl().setPublicWrite(value);
							} catch (Exception e) {

							}
						} else if (propertyKey.equals("aclRead")) {
							try {
								List<String> value = JSON.aclJSONArrayToList(entityJSONObject.getJSONArray("aclRead"));
								divrollEntity.getAcl().setAclRead(value);
							} catch (Exception e) {

							}
							try {
								List<String> value = Arrays.asList(entityJSONObject.getString("aclRead"));
								divrollEntity.getAcl().setAclRead(value);
							} catch (Exception e) {

							}
						} else if (propertyKey.equals("aclWrite")) {
							try {
								List<String> value = JSON.aclJSONArrayToList(entityJSONObject.getJSONArray("aclWrite"));
								divrollEntity.getAcl().setAclWrite(value);
							} catch (Exception e) {

							}
							try {
								List<String> value = Arrays.asList(entityJSONObject.getString("aclWrite"));
								divrollEntity.getAcl().setAclWrite(value);
							} catch (Exception e) {

							}
						} else {
							divrollEntity.setProperty(propertyKey, entityJSONObject.get(propertyKey));
						}
					}

					try {
						String dateCreated = entityJSONObject.getString("dateCreated");
						divrollEntity.setDateCreated(dateCreated);
					} catch (Exception e) {

					}

					try {
						String dateUpdated = entityJSONObject.getString("dateUpdated");
						divrollEntity.setDateUpdated(dateUpdated);
					} catch (Exception e) {

					}

					JSONArray links = null;
					try {
						links = entityJSONObject.getJSONArray("links");
					} catch (Exception e) {

					}
					if (links != null) {
						for (int j = 0; j < links.length(); j++) {
							JSONObject linksObj = links.getJSONObject(j);
							DivrollLink divrollLink = processLink(linksObj);
							divrollEntity.getLinks().add(divrollLink);
						}
					} else {
						JSONObject linksObj = null;
						try {
							linksObj = entityJSONObject.getJSONObject("links");
						} catch (Exception e) {

						}
						DivrollLink divrollLink = processLink(linksObj);
						divrollEntity.getLinks().add(divrollLink);
					}

					getEntities().add(divrollEntity);
				}
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public void query() {
		query(null);
	}

	public void setCount(Boolean count) {
		this.count = count;
	}

	public Long getResult() {
		return result;
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

}
