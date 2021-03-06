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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.divroll.backend.sdk.exception.BadRequestException;
import com.divroll.backend.sdk.exception.DivrollException;
import com.divroll.backend.sdk.exception.UnauthorizedException;
import com.divroll.backend.sdk.helper.JSON;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

public class DivrollRole extends DivrollBase {

	private static final String rolesUrl = "/entities/roles";

	private String entityId;
	private String name;
	private DivrollACL acl;

	public DivrollRole() {
	}

	public DivrollRole(String name) {
		setName(name);
	}

	public void create() {
		try {
			HttpRequestWithBody httpRequestWithBody = Unirest.post(Divroll.getServerUrl() + rolesUrl);
			if (Divroll.getMasterKey() != null) {
				httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
			}
			if (Divroll.getAppId() != null) {
				httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
			}
			if (Divroll.getNameSpace() != null) {
				httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
			}
			JSONObject roleObj = new JSONObject();
			roleObj.put("name", name);
			roleObj.put("publicRead",
					(acl != null && acl.getPublicRead() != null) ? acl.getPublicRead() : JSONObject.NULL);
			roleObj.put("publicWrite",
					(acl != null && acl.getPublicWrite() != null) ? acl.getPublicWrite() : JSONObject.NULL);
			JSONObject body = new JSONObject();
			body.put("role", roleObj);

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
			roleObj.put("aclRead", getAcl() != null ? aclRead : null);
			roleObj.put("aclWrite", getAcl() != null ? aclWrite : null);

			httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
			httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
			httpRequestWithBody.header("Content-Type", "application/json");

			HttpResponse<JsonNode> response = httpRequestWithBody.body(body).asJson();
			if (response.getStatus() >= 500) {
				throw new DivrollException(response.getStatusText());
			} else if (response.getStatus() == 400) {
				throw new BadRequestException(response.getStatusText());
			} else if (response.getStatus() == 404) {
				throw new DivrollException(response.getStatusText());
			} else if (response.getStatus() == 401) {
				throw new UnauthorizedException(response.getStatusText());
			} else if (response.getStatus() == 401) {
				throw new DivrollException(response.getStatusText());
			} else if (response.getStatus() == 201) {
				JsonNode responseBody = response.getBody();
				JSONObject bodyObj = responseBody.getObject();

				JSONObject role = bodyObj.getJSONObject("role");
				String entityId = role.getString("entityId");
				String name = role.getString("name");

				Boolean publicRead = null;
				Boolean publicWrite = null;

				try {
					publicRead = role.getBoolean("publicRead");
				} catch (Exception e) {

				}

				try {
					publicWrite = role.getBoolean("publicWrite");
				} catch (Exception e) {

				}

				List<String> aclWriteList = null;
				List<String> aclReadList = null;

				try {
					aclWriteList = JSON.aclJSONArrayToList(role.getJSONArray("aclWrite"));
				} catch (Exception e) {

				}

				try {
					aclReadList = JSON.aclJSONArrayToList(role.getJSONArray("aclRead"));
				} catch (Exception e) {

				}

				try {
					JSONObject jsonObject = role.getJSONObject("aclWrite");
					aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
				} catch (Exception e) {

				}
				try {
					JSONObject jsonObject = role.getJSONObject("aclRead");
					aclReadList = Arrays.asList(jsonObject.getString("entityId"));
				} catch (Exception e) {

				}

				DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
				acl.setPublicRead(publicRead);
				acl.setPublicWrite(publicWrite);
				setEntityId(entityId);
				setName(name);
				setAcl(acl);
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public void update() {
		try {
			HttpRequestWithBody httpRequestWithBody = Unirest.put(
					Divroll.getServerUrl() + rolesUrl + "/" + getEntityId());
			if (Divroll.getMasterKey() != null) {
				httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
			}
			if (Divroll.getAppId() != null) {
				httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
			}
			if (Divroll.getNameSpace() != null) {
				httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNameSpace());
			}
			JSONObject roleObj = new JSONObject();

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
			roleObj.put("aclRead", aclRead);
			roleObj.put("aclWrite", aclWrite);
			roleObj.put("name", name);
			roleObj.put("publicRead",
					(acl != null && acl.getPublicRead() != null) ? acl.getPublicRead() : JSONObject.NULL);
			roleObj.put("publicWrite",
					(acl != null && acl.getPublicWrite() != null) ? acl.getPublicWrite() : JSONObject.NULL);
			JSONObject body = new JSONObject();
			body.put("role", roleObj);

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
			} else if (response.getStatus() == 201) {
				JsonNode responseBody = response.getBody();
				JSONObject bodyObj = responseBody.getObject();
				JSONObject role = bodyObj.getJSONObject("role");
				String entityId = role.getString("entityId");
				String name = role.getString("name");
				Boolean publicRead = role.getBoolean("publicRead");
				Boolean publicWrite = role.getBoolean("publicWrite");

				List<String> aclWriteList = null;
				List<String> aclReadList = null;

				try {
					aclWriteList = JSON.aclJSONArrayToList(role.getJSONArray("aclWrite"));
				} catch (Exception e) {

				}

				try {
					aclReadList = JSON.aclJSONArrayToList(role.getJSONArray("aclRead"));
				} catch (Exception e) {

				}

				try {
					JSONObject jsonObject = role.getJSONObject("aclWrite");
					aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
				} catch (Exception e) {

				}
				try {
					JSONObject jsonObject = role.getJSONObject("aclRead");
					aclReadList = Arrays.asList(jsonObject.getString("entityId"));
				} catch (Exception e) {

				}

				DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
				acl.setPublicRead(publicRead);
				acl.setPublicWrite(publicWrite);
				setEntityId(entityId);
				setName(name);
				setAcl(acl);
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public boolean delete() {
		try {
			HttpRequestWithBody httpRequestWithBody = Unirest.delete(
					Divroll.getServerUrl() + rolesUrl + "/" + getEntityId());
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
				httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getApiKey());
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
				throwException(response);
			} else if (response.getStatus() == 204) {
				setEntityId(null);
				setAcl(null);
				setName(name);
				return true;
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void retrieve() throws DivrollException {
		try {
			GetRequest getRequest = Unirest.get(Divroll.getServerUrl() + rolesUrl + "/" + getEntityId());

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
				JSONObject role = bodyObj.getJSONObject("role");
				String entityId = role.getString("entityId");
				String name = role.getString("name");
				Boolean publicRead = role.getBoolean("publicRead");
				Boolean publicWrite = role.getBoolean("publicWrite");
				setEntityId(entityId);
				setName(name);

				List<String> aclWriteList = null;
				List<String> aclReadList = null;

				try {
					aclWriteList = JSON.aclJSONArrayToList(role.getJSONArray("aclWrite"));
				} catch (Exception e) {

				}

				try {
					aclReadList = JSON.aclJSONArrayToList(role.getJSONArray("aclRead"));
				} catch (Exception e) {

				}

				try {
					JSONObject jsonObject = role.getJSONObject("aclWrite");
					aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
				} catch (Exception e) {

				}
				try {
					JSONObject jsonObject = role.getJSONObject("aclRead");
					aclReadList = Arrays.asList(jsonObject.getString("entityId"));
				} catch (Exception e) {

				}

				DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
				acl.setPublicWrite(publicWrite);
				acl.setPublicRead(publicRead);
				setEntityId(entityId);
				setName(name);
				setAcl(acl);
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DivrollACL getAcl() {
		return acl;
	}

	public void setAcl(DivrollACL acl) {
		this.acl = acl;
	}
}
