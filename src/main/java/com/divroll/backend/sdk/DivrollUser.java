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
import com.divroll.backend.sdk.exception.DivrollException;
import com.divroll.backend.sdk.exception.NotFoundRequestException;
import com.divroll.backend.sdk.exception.UnauthorizedException;
import com.divroll.backend.sdk.helper.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

public class DivrollUser extends DivrollBase {

	private static final String usersUrl = "/entities/users";
	private static final String loginUrl = "/entities/users/login";

	private String entityId;
	private String username;
	private String password;
	private String authToken;
	private DivrollACL acl;
	private List<DivrollRole> roles;

	private List<String> linkNames;
	private List<String> blobNames;
	private String dateCreated;
	private String dateUpdated;
	private List<DivrollLink> links;
	private String linkName;
	private String linkFrom;

	public void create(String username, String password) {
		try {

			setUsername(username);
			setPassword(password);

			HttpRequestWithBody httpRequestWithBody = Unirest.post(Divroll.getServerUrl() + usersUrl);
			if (Divroll.getMasterKey() != null) {
				httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
			}
			if (Divroll.getAppId() != null) {
				httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
			}
			if (Divroll.getAuthToken() != null) {
				httpRequestWithBody.header("X-Divroll-Auth-Key", Divroll.getAuthToken());
			}
			if (Divroll.getNameSpace() != null) {
				httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
			}
			JSONObject userObj = new JSONObject();
			userObj.put("username", username);
			userObj.put("password", password);
			userObj.put("publicRead", (this.acl != null && this.acl.getPublicRead() != null) ? this.acl.getPublicRead()
					: JSONObject.NULL);
			userObj.put("publicWrite",
					(this.acl != null && this.acl.getPublicWrite() != null) ? this.acl.getPublicWrite()
							: JSONObject.NULL);
			JSONObject body = new JSONObject();

			JSONArray roles = new JSONArray();
			for (DivrollRole role : getRoles()) {
				JSONObject roleObj = new JSONObject();
				roleObj.put("entityId", role.getEntityId());
				roles.put(roleObj);
			}
			userObj.put("roles", roles);

			body.put("user", userObj);
			httpRequestWithBody.body(body);
			JSONArray aclRead = new JSONArray();
			JSONArray aclWrite = new JSONArray();
			if (this.acl != null) {
				for (String uuid : this.acl.getAclRead()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclRead.put(entityStub);
				}
				for (String uuid : this.acl.getAclWrite()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclWrite.put(entityStub);
				}
			}

			httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
			httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
			httpRequestWithBody.header("Content-Type", "application/json");

			HttpResponse<JsonNode> response = httpRequestWithBody.asJson();

			if (response.getStatus() >= 500) {
				throwException(response);
			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() == 400) {
				throw new BadRequestException(response.getStatusText());
			} else if (response.getStatus() == 201) {
				JsonNode responseBody = response.getBody();
				JSONObject bodyObj = responseBody.getObject();
				JSONObject responseUser = bodyObj.getJSONObject("user");
				String entityId = responseUser.getString("entityId");
				String authToken = responseUser.getString("authToken");
				setEntityId(entityId);
				setAuthToken(authToken);

				List<String> aclWriteList = null;
				List<String> aclReadList = null;

				Boolean publicRead = null;
				Boolean publicWrite = null;

				try {
					publicRead = responseUser.get("publicRead") != null ? responseUser.getBoolean("publicRead") : null;
				} catch (Exception e) {

				}

				try {
					publicWrite = responseUser.get("publicWrite") != null ? responseUser.getBoolean("publicWrite")
							: null;
				} catch (Exception e) {

				}

				try {
					aclWriteList = JSON.aclJSONArrayToList(responseUser.getJSONArray("aclWrite"));
				} catch (Exception e) {

				}

				try {
					aclReadList = JSON.aclJSONArrayToList(responseUser.getJSONArray("aclRead"));
				} catch (Exception e) {

				}

				try {
					JSONObject jsonObject = responseUser.getJSONObject("aclWrite");
					aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
				} catch (Exception e) {

				}
				try {
					JSONObject jsonObject = responseUser.getJSONObject("aclRead");
					aclReadList = Arrays.asList(jsonObject.getString("entityId"));
				} catch (Exception e) {

				}

				List<DivrollRole> divrollRoles = null;
				try {
					Object rolesObj = responseUser.get("roles");
					if (roles instanceof JSONArray) {
						divrollRoles = new LinkedList<DivrollRole>();
						JSONArray jsonArray = roles;
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							String roleId = jsonObject.getString("entityId");
							DivrollRole divrollRole = new DivrollRole();
							divrollRole.setEntityId(roleId);
							divrollRoles.add(divrollRole);
						}
					} else if (rolesObj instanceof JSONObject) {
						divrollRoles = new LinkedList<DivrollRole>();
						JSONObject jsonObject = (JSONObject) rolesObj;
						String roleId = jsonObject.getString("entityId");
						DivrollRole divrollRole = new DivrollRole();
						divrollRole.setEntityId(roleId);
						divrollRoles.add(divrollRole);
					}
				} catch (Exception e) {
					// do nothing
				}

				DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
				acl.setPublicWrite(publicWrite);
				acl.setPublicRead(publicRead);
				setAcl(acl);
				setRoles(divrollRoles);
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public void retrieve() {
		try {
			GetRequest getRequest = Unirest.get(Divroll.getServerUrl() + usersUrl + "/" + getEntityId());

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
			HttpResponse<JsonNode> response = getRequest.asJson();

			if (response.getStatus() >= 500) {
				throwException(response);
			} else if (response.getStatus() == 404) {
				throw new NotFoundRequestException(response.getStatusText());
			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() == 400) {
				throw new BadRequestException(response.getStatusText());
			} else if (response.getStatus() >= 400) {
				throwException(response);
			} else if (response.getStatus() == 200) {
				JsonNode body = response.getBody();
				JSONObject bodyObj = body.getObject();
				JSONObject userJsonObj = bodyObj.getJSONObject("user");
				String entityId = userJsonObj.getString("entityId");
				String username = userJsonObj.getString("username");

				Boolean publicRead = null;
				Boolean publicWrite = null;

				try {
					publicRead = userJsonObj.get("publicRead") != null ? userJsonObj.getBoolean("publicRead") : null;
				} catch (Exception e) {

				}

				try {
					publicWrite = userJsonObj.get("publicWrite") != null ? userJsonObj.getBoolean("publicWrite") : null;
				} catch (Exception e) {

				}

				List<String> aclWriteList = null;
				List<String> aclReadList = null;

				try {
					aclWriteList = JSON.aclJSONArrayToList(userJsonObj.getJSONArray("aclWrite"));
				} catch (Exception e) {

				}

				try {
					aclReadList = JSON.aclJSONArrayToList(userJsonObj.getJSONArray("aclRead"));
				} catch (Exception e) {

				}

				try {
					aclWriteList = Arrays.asList(userJsonObj.getString("aclWrite"));
				} catch (Exception e) {

				}

				try {
					aclReadList = Arrays.asList(userJsonObj.getString("aclRead"));
				} catch (Exception e) {

				}

				List<DivrollRole> divrollRoles = null;
				try {
					Object roles = userJsonObj.get("roles");
					if (roles instanceof JSONArray) {
						divrollRoles = new LinkedList<DivrollRole>();
						JSONArray jsonArray = (JSONArray) roles;
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							String roleId = jsonObject.getString("entityId");
							DivrollRole divrollRole = new DivrollRole();
							divrollRole.setEntityId(roleId);
							divrollRoles.add(divrollRole);
						}
					} else if (roles instanceof JSONObject) {
						divrollRoles = new LinkedList<DivrollRole>();
						JSONObject jsonObject = (JSONObject) roles;
						String roleId = jsonObject.getString("entityId");
						DivrollRole divrollRole = new DivrollRole();
						divrollRole.setEntityId(roleId);
						divrollRoles.add(divrollRole);
					}
				} catch (Exception e) {
					// do nothing
				}

				DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
				acl.setPublicWrite(publicWrite);
				acl.setPublicRead(publicRead);

				setEntityId(entityId);
				setUsername(username);
				setAcl(acl);
				setRoles(divrollRoles);
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public List<DivrollEntity> links(String linkName) {
		List<DivrollEntity> entities = new LinkedList<DivrollEntity>();
		if (entityId == null) {
			throw new DivrollException("Save the entity first before getting links");
		}
		try {
			String completeUrl = Divroll.getServerUrl() + usersUrl + "/" + getEntityId() + "/links/" + linkName;

			GetRequest getRequest = Unirest.get(completeUrl);

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

				JsonNode body = response.getBody();
				JSONObject bodyObj = body.getObject();
				JSONObject entitiesJSONObject = bodyObj.getJSONObject("entities");
				JSONArray results = entitiesJSONObject.getJSONArray("results");
				for (int i = 0; i < results.length(); i++) {
					JSONObject jsonObject = results.getJSONObject(i);
					DivrollEntity divrollEntity = new DivrollEntity(jsonObject.getString("entityType"));
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
					entities.add(divrollEntity);
				}
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return entities;
	}

	public List<DivrollEntity> retrieveLinked(List<String> linkNames) {
		List<DivrollEntity> divrollEntities = new LinkedList<>();
		try {
			GetRequest getRequest = Unirest.get(Divroll.getServerUrl() + usersUrl + "/" + getEntityId());

			JSONArray linkNameArray = new JSONArray();
			for (String linkName : linkNames) {
				linkNameArray.put(linkName);
			}
			getRequest.queryString("include", linkNameArray.toString());

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
			HttpResponse<JsonNode> response = getRequest.asJson();

			if (response.getStatus() >= 500) {
				throwException(response);
			} else if (response.getStatus() == 404) {
				throw new NotFoundRequestException(response.getStatusText());
			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() == 400) {
				throw new BadRequestException(response.getStatusText());
			} else if (response.getStatus() >= 400) {
				throwException(response);
			} else if (response.getStatus() == 200) {
				JsonNode body = response.getBody();

				System.out.println("BODY=" + body.toString());

				JSONObject bodyObj = body.getObject();
				JSONObject userJsonObj = bodyObj.getJSONObject("user");
				String entityId = userJsonObj.getString("entityId");
				String username = userJsonObj.getString("username");

				Boolean publicRead = null;
				Boolean publicWrite = null;

				try {
					publicRead = userJsonObj.get("publicRead") != null ? userJsonObj.getBoolean("publicRead") : null;
				} catch (Exception e) {

				}

				try {
					publicWrite = userJsonObj.get("publicWrite") != null ? userJsonObj.getBoolean("publicWrite") : null;
				} catch (Exception e) {

				}

				List<String> aclWriteList = null;
				List<String> aclReadList = null;

				try {
					aclWriteList = JSON.aclJSONArrayToList(userJsonObj.getJSONArray("aclWrite"));
				} catch (Exception e) {

				}

				try {
					aclReadList = JSON.aclJSONArrayToList(userJsonObj.getJSONArray("aclRead"));
				} catch (Exception e) {

				}

				try {
					aclWriteList = Arrays.asList(userJsonObj.getString("aclWrite"));
				} catch (Exception e) {

				}

				try {
					aclReadList = Arrays.asList(userJsonObj.getString("aclRead"));
				} catch (Exception e) {

				}

				List<DivrollRole> divrollRoles = null;
				try {
					Object roles = userJsonObj.get("roles");
					if (roles instanceof JSONArray) {
						divrollRoles = new LinkedList<DivrollRole>();
						JSONArray jsonArray = (JSONArray) roles;
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							String roleId = jsonObject.getString("entityId");
							DivrollRole divrollRole = new DivrollRole();
							divrollRole.setEntityId(roleId);
							divrollRoles.add(divrollRole);
						}
					} else if (roles instanceof JSONObject) {
						divrollRoles = new LinkedList<DivrollRole>();
						JSONObject jsonObject = (JSONObject) roles;
						String roleId = jsonObject.getString("entityId");
						DivrollRole divrollRole = new DivrollRole();
						divrollRole.setEntityId(roleId);
						divrollRoles.add(divrollRole);
					}
				} catch (Exception e) {
					// do nothing
				}

				DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
				acl.setPublicWrite(publicWrite);
				acl.setPublicRead(publicRead);

				setEntityId(entityId);
				setUsername(username);
				setAcl(acl);
				setRoles(divrollRoles);

				JSONArray links = userJsonObj.getJSONArray("links");
				System.out.println("links=" + links.toString());

			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return divrollEntities;
	}

	public void update(String newUsername, String newPassword) {
		try {

			String completeUrl = Divroll.getServerUrl() + usersUrl + "/" + getEntityId();

			HttpRequestWithBody httpRequestWithBody = Unirest.put(completeUrl);
			if (Divroll.getMasterKey() != null) {
				httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
			}
			if (Divroll.getAppId() != null) {
				httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
			}
			if (Divroll.getAuthToken() != null) {
				httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
			}
			if (Divroll.getNameSpace() != null) {
				httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
			}
			JSONObject userObj = new JSONObject();

			JSONArray aclRead = new JSONArray();
			JSONArray aclWrite = new JSONArray();
			if (acl != null) {
				for (String uuid : this.acl.getAclRead()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclRead.put(entityStub);
				}
				for (String uuid : this.acl.getAclWrite()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclWrite.put(entityStub);
				}
			}

			userObj.put("aclRead", aclRead);
			userObj.put("aclWrite", aclWrite);

			if (username != null) {
				userObj.put("username", newUsername);
			}
			if (username != null) {
				userObj.put("password", newPassword);
			}
			userObj.put("publicRead",
					(acl != null && acl.getPublicRead() != null) ? acl.getPublicRead() : JSONObject.NULL);
			userObj.put("publicWrite",
					(acl != null && acl.getPublicWrite() != null) ? acl.getPublicWrite() : JSONObject.NULL);
			JSONObject body = new JSONObject();

			JSONArray roles = new JSONArray();
			for (DivrollRole role : getRoles()) {
				JSONObject roleObj = new JSONObject();
				roleObj.put("entityId", role.getEntityId());
				roles.put(roleObj);
			}
			userObj.put("roles", roles);

			body.put("user", userObj);

			httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
			httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
			httpRequestWithBody.header("Content-Type", "application/json");

			HttpResponse<JsonNode> response = httpRequestWithBody.body(body).asJson();

			if (response.getStatus() >= 500) {
				throw new DivrollException(response.getStatusText());
			} else if (response.getStatus() == 400) {
				throw new BadRequestException(response.getStatusText());
			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() == 404) {
				throw new DivrollException(response.getStatusText());
			} else if (response.getStatus() >= 400) {
				throwException(response);
			} else if (response.getStatus() == 200) {

				JsonNode responseBody = response.getBody();
				JSONObject bodyObj = responseBody.getObject();
				JSONObject responseUser = bodyObj.getJSONObject("user");
				String entityId = responseUser.getString("entityId");
				// String authToken = responseUser.getString("authToken");
				String updatedUsername = null;

				try {
					updatedUsername = responseUser.getString("username");
				} catch (Exception e) {
					// do nothing
				}

				setEntityId(entityId);
				// setAuthToken(authToken);

				Boolean publicRead = null;
				Boolean publicWrite = null;

				try {
					publicRead = responseUser.getBoolean("publicRead");
				} catch (Exception e) {

				}

				try {
					publicWrite = responseUser.getBoolean("publicWrite");
				} catch (Exception e) {

				}

				List<String> aclWriteList = null;
				List<String> aclReadList = null;

				try {
					aclWriteList = JSON.aclJSONArrayToList(responseUser.getJSONArray("aclWrite"));
				} catch (Exception e) {

				}

				try {
					aclReadList = JSON.aclJSONArrayToList(responseUser.getJSONArray("aclRead"));
				} catch (Exception e) {

				}

				try {
					JSONObject jsonObject = responseUser.getJSONObject("aclWrite");
					aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
				} catch (Exception e) {

				}
				try {
					JSONObject jsonObject = responseUser.getJSONObject("aclRead");
					aclReadList = Arrays.asList(jsonObject.getString("entityId"));
				} catch (Exception e) {

				}

				List<DivrollRole> divrollRoles = null;
				try {
					Object roleObjects = userObj.get("roles");
					if (roleObjects instanceof JSONArray) {
						divrollRoles = new LinkedList<DivrollRole>();
						JSONArray jsonArray = roles;
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
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
				} catch (Exception e) {

				}

				DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
				acl.setPublicRead(publicRead);
				acl.setPublicWrite(publicWrite);
				setEntityId(entityId);
				if (newUsername != null) {
					setUsername(updatedUsername);
				}
				setAcl(acl);
				setRoles(divrollRoles);
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public void update() {
		update(null, null);
	}

	public boolean delete() {
		try {
			HttpRequestWithBody httpRequestWithBody = Unirest.delete(
					Divroll.getServerUrl() + usersUrl + "/" + getEntityId());
			if (Divroll.getMasterKey() != null) {
				httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
			}
			if (Divroll.getAppId() != null) {
				httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
			}
			if (Divroll.getAuthToken() != null) {
				httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
			}
			if (Divroll.getNameSpace() != null) {
				httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
			}
			HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
			if (response.getStatus() >= 500) {
				throwException(response);
			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() >= 400) {
				throw new BadRequestException(response.getStatusText());
			} else if (response.getStatus() == 204) {
				setEntityId(null);
				setAcl(null);
				setUsername(null);
				setRoles(null);
				setAcl(null);
				setPassword(null);
				setAuthToken(null);
				return true;
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void login(String username, String password) {
		setUsername(username);
		setPassword(password);
		try {
			GetRequest getRequest = (GetRequest) Unirest.get(Divroll.getServerUrl() + loginUrl).queryString("username",
					getUsername()).queryString("password", getPassword());
			if (Divroll.getMasterKey() != null) {
				getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
			}
			if (Divroll.getAppId() != null) {
				getRequest.header(HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
			}
			if (Divroll.getNameSpace() != null) {
				getRequest.header(HEADER_NAMESPACE, Divroll.getNameSpace());
			}

			HttpResponse<JsonNode> response = getRequest.asJson();
			if (response.getStatus() == 404) {

			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() == 200) {
				JsonNode body = response.getBody();
				JSONObject bodyObj = body.getObject();
				JSONObject user = bodyObj.getJSONObject("user");
				String entityId = user.getString("entityId");
				String authToken = user.getString("authToken");
				setEntityId(entityId);
				setAuthToken(authToken);
				Divroll.setAuthToken(authToken);
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public void logout() {
		Divroll.setAuthToken(null);
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public DivrollACL getAcl() {
		return acl;
	}

	public void setAcl(DivrollACL acl) {
		this.acl = acl;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public List<DivrollRole> getRoles() {
		if (roles == null) {
			roles = new LinkedList<DivrollRole>();
		}
		return roles;
	}

	public void setRoles(List<DivrollRole> roles) {
		this.roles = roles;
	}

	public void setLink(String linkName, String entityId) {
		if (entityId == null) {
			throw new DivrollException("Save the entity first before creating a link");
		}
		try {
			HttpRequestWithBody httpRequestWithBody = Unirest.post(
					Divroll.getServerUrl() + usersUrl + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);

			httpRequestWithBody.queryString("linkType", "set");

			if (Divroll.getMasterKey() != null) {
				httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
			}
			if (Divroll.getAppId() != null) {
				httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
			}
			if (Divroll.getAuthToken() != null) {
				httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
			}
			if (Divroll.getNameSpace() != null) {
				httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
			}
			JSONArray aclRead = new JSONArray();
			JSONArray aclWrite = new JSONArray();
			if (acl != null) {
				for (String uuid : this.acl.getAclRead()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclRead.put(entityStub);
				}
				for (String uuid : this.acl.getAclWrite()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclWrite.put(entityStub);
				}
			}

			httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
			httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
			httpRequestWithBody.header("Content-Type", "application/json");

			HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
			if (response.getStatus() >= 500) {
				throw new DivrollException(response.getStatusText()); // TODO
			} else if (response.getStatus() == 404) {
				throw new NotFoundRequestException(response.getStatusText());
			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() == 400) {
				throw new BadRequestException(response.getStatusText());
			} else if (response.getStatus() >= 400) {
				throw new DivrollException("Client error"); // TODO
			} else if (response.getStatus() == 201) {
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public void addLink(String linkName, String entityId) {
		if (entityId == null) {
			throw new DivrollException("Save the entity first before creating a link");
		}
		try {
			HttpRequestWithBody httpRequestWithBody = Unirest.post(
					Divroll.getServerUrl() + usersUrl + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);
			if (Divroll.getMasterKey() != null) {
				httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
			}
			if (Divroll.getAppId() != null) {
				httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
			}
			if (Divroll.getAuthToken() != null) {
				httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
			}
			if (Divroll.getNameSpace() != null) {
				httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
			}
			JSONArray aclRead = new JSONArray();
			JSONArray aclWrite = new JSONArray();
			if (acl != null) {
				for (String uuid : this.acl.getAclRead()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclRead.put(entityStub);
				}
				for (String uuid : this.acl.getAclWrite()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclWrite.put(entityStub);
				}
			}

			httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
			httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
			httpRequestWithBody.header("Content-Type", "application/json");

			HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
			if (response.getStatus() >= 500) {
				throw new DivrollException(response.getStatusText()); // TODO
			} else if (response.getStatus() == 404) {
				throw new NotFoundRequestException(response.getStatusText());
			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() == 400) {
				throw new BadRequestException(response.getStatusText());
			} else if (response.getStatus() >= 400) {
				throw new DivrollException("Client error"); // TODO
			} else if (response.getStatus() == 201) {
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public void removeLink(String linkName, String entityId) {
		if (entityId == null) {
			throw new DivrollException("Save the entity first before removing a link");
		}
		try {
			HttpRequestWithBody httpRequestWithBody = Unirest.delete(
					Divroll.getServerUrl() + usersUrl + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);
			if (Divroll.getMasterKey() != null) {
				httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
			}
			if (Divroll.getAppId() != null) {
				httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
			}
			if (Divroll.getAuthToken() != null) {
				httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
			}
			if (Divroll.getNameSpace() != null) {
				httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
			}
			JSONArray aclRead = new JSONArray();
			JSONArray aclWrite = new JSONArray();
			if (acl != null) {
				for (String uuid : this.acl.getAclRead()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclRead.put(entityStub);
				}
				for (String uuid : this.acl.getAclWrite()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclWrite.put(entityStub);
				}
			}

			httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
			httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
			httpRequestWithBody.header("Content-Type", "application/json");

			HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
			if (response.getStatus() >= 500) {
				throw new DivrollException(response.getStatusText()); // TODO
			} else if (response.getStatus() == 404) {
				throw new NotFoundRequestException(response.getStatusText());
			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() == 400) {
				throw new BadRequestException(response.getStatusText());
			} else if (response.getStatus() >= 400) {
				throw new DivrollException("Client error"); // TODO
			} else if (response.getStatus() == 201) {
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public void removeLinks(String linkName) {
		if (entityId == null) {
			throw new DivrollException("Save the entity first before removing links");
		}
		try {
			HttpRequestWithBody httpRequestWithBody = Unirest.delete(
					Divroll.getServerUrl() + usersUrl + "/" + getEntityId() + "/links/" + linkName);
			if (Divroll.getMasterKey() != null) {
				httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
			}
			if (Divroll.getAppId() != null) {
				httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
			}
			if (Divroll.getAuthToken() != null) {
				httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
			}
			if (Divroll.getNameSpace() != null) {
				httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
			}
			JSONArray aclRead = new JSONArray();
			JSONArray aclWrite = new JSONArray();
			if (acl != null) {
				for (String uuid : this.acl.getAclRead()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclRead.put(entityStub);
				}
				for (String uuid : this.acl.getAclWrite()) {
					JSONObject entityStub = new JSONObject();
					entityStub.put("entityId", uuid);
					aclWrite.put(entityStub);
				}
			}

			httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
			httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
			httpRequestWithBody.header("Content-Type", "application/json");

			HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
			if (response.getStatus() >= 500) {
				throw new DivrollException(response.getStatusText()); // TODO
			} else if (response.getStatus() == 404) {
				throw new NotFoundRequestException(response.getStatusText());
			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() == 400) {
				throw new BadRequestException(response.getStatusText());
			} else if (response.getStatus() >= 400) {
				throw new DivrollException("Client error"); // TODO
			} else if (response.getStatus() == 201) {
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public List<String> getLinkNames() {
		return linkNames;
	}

	public void setLinkNames(List<String> linkNames) {
		this.linkNames = linkNames;
	}

	public List<String> getBlobNames() {
		return blobNames;
	}

	public void setBlobNames(List<String> blobNames) {
		this.blobNames = blobNames;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public List<DivrollLink> getLinks() {
		if (links == null) {
			links = new LinkedList<DivrollLink>();
		}
		return links;
	}

	public void setLinks(List<DivrollLink> links) {
		this.links = links;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public String getLinkFrom() {
		return linkFrom;
	}

	public void setLinkFrom(String linkFrom) {
		this.linkFrom = linkFrom;
	}
}
