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

import org.fluttercode.datafactory.impl.DataFactory;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

public class TestData {

	public static TestApplication getNewApplication() {
		try {
			DataFactory df = new DataFactory();
			HttpRequestWithBody postRequest = Unirest.post(
					Divroll.getServerUrl() + "/applications/" + df.getName());
			if (Divroll.getAppId() != null) {
				postRequest.header(DivrollBase.HEADER_APP_ID, Divroll.getAppId());
			}
			if (Divroll.getApiKey() != null) {
				postRequest.header(DivrollBase.HEADER_API_KEY, Divroll.getApiKey());
			}
			JSONObject userObject = new JSONObject();
			userObject.put("username", df.getEmailAddress());
			userObject.put("password", "password");
			userObject.put("role", "role");

			JSONObject payload = new JSONObject();
			JSONObject applicationObj = new JSONObject();
			// applicationObj.put("user", userObject);

			payload.put("application", applicationObj);

			System.out.println("Payload: " + payload.toString());

			postRequest.body(payload.toString());
			postRequest.header("Content-Type", "application/json");

			HttpResponse<JsonNode> response = postRequest.asJson();
			if (response.getStatus() == 404) {

			} else if (response.getStatus() == 401) {

			} else if (response.getStatus() == 200 || response.getStatus() == 201) {
				JsonNode body = response.getBody();
				JSONObject bodyObj = body.getObject();
				JSONObject application = bodyObj.getJSONObject("application");
				return new TestApplication(application.getString("appId"), application.getString("apiKey"),
						application.getString("masterKey"));
			}
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return null;
	}
}
