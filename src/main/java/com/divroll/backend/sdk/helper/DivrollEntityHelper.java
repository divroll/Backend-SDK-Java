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
package com.divroll.backend.sdk.helper;

import org.json.JSONArray;
import org.json.JSONObject;

import com.divroll.backend.sdk.DivrollEntity;
import com.divroll.backend.sdk.DivrollEntityStub;

public class DivrollEntityHelper {
	public static JSONObject convert(DivrollEntity entity) {
		if (entity == null) {
			return null;
		}
		JSONObject jsonObject = entity.getProperties();
		jsonObject.put("entityId", entity.getEntityId());
		jsonObject.put("entityType", entity.getEntityType());
		jsonObject.put("publicRead", entity.getAcl().getPublicRead());
		jsonObject.put("publicWrite", entity.getAcl().getPublicWrite());

		JSONArray aclRead = new JSONArray();
		JSONArray aclWrite = new JSONArray();
		if (entity.getAcl() != null) {
			for (String uuid : entity.getAcl().getAclRead()) {
				JSONObject entityStub = new JSONObject();
				entityStub.put("entityId", uuid);
				aclRead.put(entityStub);
			}
			for (String uuid : entity.getAcl().getAclWrite()) {
				JSONObject entityStub = new JSONObject();
				entityStub.put("entityId", uuid);
				aclWrite.put(entityStub);
			}
		}
		jsonObject.put("aclRead", aclRead);
		jsonObject.put("aclWrite", aclWrite);

		return jsonObject;
	}

	public static DivrollEntity convert(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}
		String entityType = jsonObject.getString("entityType");
		DivrollEntity divrollEntity = new DivrollEntity(entityType);
		divrollEntity.setEntityId(jsonObject.getString("entityId"));
		return divrollEntity;
	}

	public static DivrollEntityStub convertStub(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}
		String entityType = jsonObject.getString("entityType");
		DivrollEntityStub divrollEntity = new DivrollEntityStub(entityType, jsonObject.getString("entityId"));
		divrollEntity.setEntityId(jsonObject.getString("entityId"));
		return divrollEntity;
	}
}
