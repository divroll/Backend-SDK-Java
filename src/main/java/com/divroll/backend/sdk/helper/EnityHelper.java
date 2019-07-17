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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import com.divroll.backend.sdk.DivrollACL;
import com.divroll.backend.sdk.DivrollEntity;

public class EnityHelper {
	public static DivrollEntity JSONObjectToEntity(JSONObject entityJsonObject, String entityType) {

		DivrollEntity divrollEntity = new DivrollEntity(entityType);

		String entityId = entityJsonObject.getString("entityId");

		Boolean publicRead = null;
		Boolean publicWrite = null;

		try {
			publicWrite = entityJsonObject.getBoolean("publicWrite");
		} catch (Exception e) {

		}

		try {
			publicRead = entityJsonObject.getBoolean("publicRead");
		} catch (Exception e) {

		}

		List<String> aclWriteList = null;
		List<String> aclReadList = null;

		try {
			aclWriteList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclWrite"));
		} catch (Exception e) {

		}

		try {
			aclReadList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclRead"));
		} catch (Exception e) {

		}

		try {
			JSONObject jsonObject = entityJsonObject.getJSONObject("aclWrite");
			aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
		} catch (Exception e) {

		}
		try {
			JSONObject jsonObject = entityJsonObject.getJSONObject("aclRead");
			aclReadList = Arrays.asList(jsonObject.getString("entityId"));
		} catch (Exception e) {

		}

		Iterator<String> it = entityJsonObject.keySet().iterator();
		while (it.hasNext()) {
			String propertyKey = it.next();
			if (propertyKey.equals("entityId")) {
				divrollEntity.setEntityId(entityJsonObject.getString(propertyKey));
			} else if (propertyKey.equals("publicRead") || propertyKey.equals("publicWrite")
					|| propertyKey.equals("aclRead") || propertyKey.equals("aclWrite")) {
				// skip
			} else {
				Object obj = entityJsonObject.get(propertyKey);
				divrollEntity.put(propertyKey, obj);
			}
		}

		DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
		acl.setPublicWrite(publicWrite);
		acl.setPublicRead(publicRead);
		divrollEntity.setEntityId(entityId);
		divrollEntity.setAcl(acl);
		return divrollEntity;
	}
}
