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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.divroll.backend.sdk.exception.NotFoundRequestException;
import com.divroll.backend.sdk.exception.UnauthorizedException;

import junit.framework.TestCase;

@RunWith(JUnit4.class)
public class TestDivrollEntity extends TestCase {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testCreateEntityUsingMasterKey() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

		DivrollEntity entity = new DivrollEntity("TestEntity");
		entity.setProperty("username", "TestUser");
		entity.setProperty("age", 30);
		entity.setProperty("nickname", "testo");
		entity.create();

		Assert.assertNotNull(entity.getEntityId());
	}

	@Test
	public void testCreateEntityWithMapUsingMasterKey() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

		DivrollEntity entity = new DivrollEntity("TestEntity");
		entity.setProperty("username", "TestUser");
		entity.setProperty("age", 30);
		entity.setProperty("nickname", "testo");

		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("test1", "Test Data");
		map.put("test2", 123);
		map.put("test3", false);

		entity.setProperty("embed", map);

		List<Object> list = new LinkedList<Object>();
		list.add("Hello");
		list.add("World");
		list.add(456);
		list.add(true);

		entity.setProperty("list", list);

		entity.create();

		String entityId = entity.getEntityId();

		Assert.assertNotNull(entityId);

		DivrollEntity entity1 = new DivrollEntity("TestEntity");
		entity1.setEntityId(entityId);

		entity1.retrieve();

		Assert.assertNotNull(entity1.getEntityId());
		Assert.assertNotNull(entity1.getProperty("embed"));

		Assert.assertEquals("Test Data", ((Map<String, Object>) (entity1.getProperty("embed"))).get("test1"));
		Assert.assertEquals(123,
				((Double) ((Map<String, Object>) (entity1.getProperty("embed"))).get("test2")).longValue());
		Assert.assertEquals(false, ((Map<String, Object>) (entity1.getProperty("embed"))).get("test3"));

		Assert.assertNotNull(entity1.getProperty("list"));
		Assert.assertEquals("Hello", ((List) entity1.getProperty("list")).get(0));
		Assert.assertEquals("World", ((List) entity1.getProperty("list")).get(1));
		Assert.assertEquals(456, ((Double) ((List) entity1.getProperty("list")).get(2)).longValue());
		Assert.assertEquals(true, ((List) entity1.getProperty("list")).get(3));

		Assert.assertEquals("TestUser", entity1.getProperty("username"));
		Assert.assertEquals(30, entity1.getProperty("age"));
		Assert.assertEquals("testo", entity1.getProperty("nickname"));
	}

	@Test
	public void testCreatePublicEntityThenUpdate() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

		DivrollEntity entity = new DivrollEntity("TestEntity");
		entity.setProperty("username", "TestUser");
		entity.setProperty("age", 30);
		entity.setProperty("nickname", "testo");
		entity.setAcl(DivrollACL.buildPublicReadWrite());
		entity.create();

		Assert.assertNotNull(entity.getEntityId());

		entity.setProperty("age", 31);

		entity.update();
	}

	@Test
	public void testCreateEntityInvalidACL() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());
		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		DivrollACL acl = DivrollACL.build();
		acl.setAclWrite(Arrays.asList("")); // invalid
		acl.setAclRead(Arrays.asList("")); // invalid
		userProfile.setAcl(acl);
		userProfile.create();
		Assert.assertNotNull(userProfile.getEntityId());
	}

	@Test
	public void testCreateEntityMasterKey() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(null);
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
		Assert.assertEquals(30, userProfile.getProperty("age"));
		Assert.assertNull(userProfile.getAcl());
		Assert.assertNull(userProfile.getAcl());
	}

	@Test(expected = UnauthorizedException.class)
	public void testCreateEntityMasterKeyOnlyShouldThrowException() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(null);
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
		Assert.assertEquals(30, userProfile.getProperty("age"));
		Assert.assertNull(userProfile.getAcl());
		Assert.assertNull(userProfile.getAcl());

		// This wil throw exception since the created Role has
		// Master Key-only access
		userProfile.retrieve();
	}

	@Test
	public void testCreateEntityMasterKeyOnly() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
		Assert.assertTrue(userProfile.getAcl().getAclRead().isEmpty());
		Assert.assertTrue(userProfile.getAcl().getAclWrite().isEmpty());

		userProfile.retrieve();

		assertNotNull(userProfile.getEntityId());
		Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
		Assert.assertTrue(userProfile.getAcl().getAclRead().isEmpty());
		Assert.assertTrue(userProfile.getAcl().getAclWrite().isEmpty());
	}

	@Test(expected = UnauthorizedException.class)
	public void testCreateEntityInvalidAppId() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize("WRONG", application.getApiToken());
		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();
	}

	@Test(expected = UnauthorizedException.class)
	public void testCreateEntityInvalidApiToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), "WRONG");
		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();
	}

	@Test
	public void testCreateUserInvalidMasterKey() {
		// TODO
	}

	@Test(expected = UnauthorizedException.class)
	public void testGetEntityInvalidAppId() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
		Assert.assertTrue(userProfile.getAcl().getAclRead().isEmpty());
		Assert.assertTrue(userProfile.getAcl().getAclWrite().isEmpty());

		Divroll.initialize("WRONG", application.getApiToken());
		userProfile.retrieve();
		Assert.assertNull(userProfile.getEntityId());
		Assert.assertNull(userProfile.getAcl());
	}

	@Test(expected = UnauthorizedException.class)
	public void testGetUserInvalidApiToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
		Assert.assertTrue(userProfile.getAcl().getAclRead().isEmpty());
		Assert.assertTrue(userProfile.getAcl().getAclWrite().isEmpty());

		Divroll.initialize(application.getAppId(), "WRONG");
		userProfile.retrieve();
		Assert.assertNull(userProfile.getEntityId());
		Assert.assertNull(userProfile.getAcl());
	}

	@Test
	public void testGetUserInvalidMasterKey() {
		// TODO
	}

	@Test
	public void testCreateAndGetEntityWithACLUsingAuthToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollUser adminUser = new DivrollUser();
		adminUser.create("admin", "password");

		Assert.assertNotNull(adminUser.getEntityId());

		String userId = adminUser.getEntityId();

		DivrollEntity userProfile = new DivrollEntity("UserProfile");

		DivrollACL divrollACL = DivrollACL.build();
		divrollACL.setPublicRead(true);
		divrollACL.setAclWrite(Arrays.asList(userId));
		userProfile.setAcl(divrollACL);

		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
		Assert.assertTrue(userProfile.getAcl().getPublicRead());

		adminUser.login("admin", "password");
		Assert.assertNotNull(adminUser.getAuthToken());
		Assert.assertNotNull(Divroll.getAuthToken());

		userProfile.retrieve();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
		Assert.assertTrue(userProfile.getAcl().getPublicRead());
	}

	@Test(expected = UnauthorizedException.class)
	public void testCreateAndGetEntityWithACLMissingAuthTokenShouldFail() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollUser divrollAdmin = new DivrollUser();
		divrollAdmin.setAcl(DivrollACL.buildMasterKeyOnly());
		divrollAdmin.create("admin", "password");

		String adminUserId = divrollAdmin.getEntityId();

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		DivrollACL acl = new DivrollACL();
		acl.setAclWrite(Arrays.asList(adminUserId));
		acl.setAclRead(Arrays.asList(adminUserId));
		userProfile.setAcl(acl);
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
		Assert.assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

		userProfile.retrieve();
	}

	@Test
	public void testUpdatePublicEntityMissingAuthToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(DivrollACL.buildPublicReadWrite());
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
		Assert.assertTrue(userProfile.getAcl().getPublicRead());
		Assert.assertTrue(userProfile.getAcl().getPublicWrite());

		userProfile.setProperty("age", 40);
		userProfile.update();

		Assert.assertEquals(40, userProfile.getProperty("age"));
		Assert.assertTrue(userProfile.getAcl().getPublicRead());
		Assert.assertTrue(userProfile.getAcl().getPublicWrite());
	}

	@Test(expected = UnauthorizedException.class)
	public void testUpdateEntityWithACLMissingAuthTokenShouldFail() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollUser divrollUser = new DivrollUser();
		divrollUser.create("admin", "password");

		Assert.assertNotNull(divrollUser.getEntityId());

		String userId = divrollUser.getEntityId();

		DivrollACL divrollACL = DivrollACL.build();
		divrollACL.setAclRead(Arrays.asList(userId));
		divrollACL.setAclWrite(Arrays.asList(userId));

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(divrollACL);
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
		Assert.assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

		userProfile.setProperty("age", 40);
		userProfile.update();
	}

	@Test
	public void testUpdateEntityWithACL() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollUser divrollUser = new DivrollUser();
		divrollUser.create("admin", "password");

		Assert.assertNotNull(divrollUser.getEntityId());

		String userId = divrollUser.getEntityId();

		DivrollACL divrollACL = DivrollACL.build();
		divrollACL.setAclRead(Arrays.asList(userId));
		divrollACL.setAclWrite(Arrays.asList(userId));

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(divrollACL);
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
		Assert.assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

		divrollUser.login("admin", "password");

		userProfile.setProperty("age", 40);
		userProfile.update();
	}

	@Test
	public void testUpdateEntityWithACLUsingMasterKey() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

		DivrollUser divrollUser = new DivrollUser();
		divrollUser.create("admin", "password");

		Assert.assertNotNull(divrollUser.getEntityId());

		String userId = divrollUser.getEntityId();

		DivrollACL divrollACL = DivrollACL.build();
		divrollACL.setAclRead(Arrays.asList(userId));
		divrollACL.setAclWrite(Arrays.asList(userId));

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(divrollACL);
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
		Assert.assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

		userProfile.setProperty("age", 40);
		userProfile.update();
	}

	@Test
	public void testUpdatePublicEntityUsingAuthToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollUser admin = new DivrollUser();
		admin.create("admin", "password");

		Assert.assertNotNull(admin.getEntityId());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(DivrollACL.buildPublicReadWrite());
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getPublicRead());
		Assert.assertTrue(userProfile.getAcl().getPublicWrite());

		admin.login("admin", "password");

		userProfile.setProperty("age", 40);
		userProfile.update();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getPublicRead());
		Assert.assertTrue(userProfile.getAcl().getPublicWrite());
	}

	@Test
	public void testUpdatePublicUserUsingMasterKey() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollUser admin = new DivrollUser();
		admin.create("admin", "password");

		Assert.assertNotNull(admin.getEntityId());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(DivrollACL.buildPublicReadWrite());
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getPublicRead());
		Assert.assertTrue(userProfile.getAcl().getPublicWrite());

		userProfile.setProperty("age", 40);
		userProfile.update();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getPublicRead());
		Assert.assertTrue(userProfile.getAcl().getPublicWrite());
	}

	@Test
	public void testUpdatePublicEntityChangeACLUsingMasterKey() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(DivrollACL.buildPublicReadWrite());
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getPublicRead());
		Assert.assertTrue(userProfile.getAcl().getPublicWrite());

		userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
		userProfile.update();

		Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
		Assert.assertFalse(userProfile.getAcl().getPublicRead());
		Assert.assertFalse(userProfile.getAcl().getPublicWrite());
	}

	@Test
	public void testUpdatePublicEntityChangeACLUsingAuthToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollUser admin = new DivrollUser();
		admin.create("admin", "password");

		Assert.assertNotNull(admin.getEntityId());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setAcl(DivrollACL.buildPublicReadWrite());
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.getAcl().getPublicRead());
		Assert.assertTrue(userProfile.getAcl().getPublicWrite());

		DivrollACL divrollACL = DivrollACL.build();
		divrollACL.setAclRead(Arrays.asList(admin.getEntityId()));
		divrollACL.setAclWrite(Arrays.asList(admin.getEntityId()));
		userProfile.setAcl(divrollACL);

		admin.login("admin", "password");

		assertNotNull(admin.getAuthToken());

		userProfile.setProperty("age", 40);
		userProfile.update();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertNull(userProfile.getAcl().getPublicRead());
		Assert.assertNull(userProfile.getAcl().getPublicWrite());
	}

	@Test
	public void testUpdateEntityUsingAuthTokenWithRole() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollRole role = new DivrollRole("Admin");
		role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
		role.create();

		String adminRoleId = role.getEntityId();

		DivrollUser adminUser = new DivrollUser();
		adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
		adminUser.getRoles().add(role);
		adminUser.create("admin", "password");

		DivrollUser divrollUser = new DivrollUser();
		DivrollACL userACL = new DivrollACL();
		userACL.setPublicRead(true);
		userACL.setPublicWrite(false);
		userACL.setAclWrite(Arrays.asList(adminRoleId));
		divrollUser.setAcl(userACL);
		divrollUser.create("user", "password");

		Assert.assertNotNull(adminUser.getRoles());
		Assert.assertFalse(adminUser.getRoles().isEmpty());

		adminUser.login("admin", "password");
		String authToken = adminUser.getAuthToken();

		Assert.assertNotNull(authToken);
		Assert.assertNotNull(Divroll.getAuthToken());

		divrollUser.update("new_username", "new_password");
		Assert.assertEquals("new_username", divrollUser.getUsername());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);

		DivrollACL divrollACL = DivrollACL.build();
		divrollACL.setAclRead(Arrays.asList(adminUser.getEntityId()));
		divrollACL.setAclWrite(Arrays.asList(adminUser.getEntityId()));
		userProfile.setAcl(divrollACL);

		userProfile.create();

		userProfile.update();
	}

	@Test(expected = NotFoundRequestException.class)
	public void testDeletePublicEntity() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.setAcl(DivrollACL.buildPublicReadWrite());

		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());

		userProfile.delete();
		userProfile.retrieve();
		Assert.assertNull(userProfile.getEntityId());
	}

	@Test(expected = NotFoundRequestException.class)
	public void testDeletePublicEntityWithAuthToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollUser adminUser = new DivrollUser();
		adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
		adminUser.create("admin", "password");

		adminUser.login("admin", "password");

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.setAcl(DivrollACL.buildPublicReadWrite());
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());

		adminUser.login("admin", "password");

		userProfile.delete();

		userProfile.retrieve();

		Assert.assertNull(userProfile.getEntityId());
	}

	@Test
	public void testDeletePublicEntityWithMasterKey() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		userProfile.setAcl(DivrollACL.buildPublicReadWrite());
		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertTrue(userProfile.delete());
	}

	@Test
	public void testDeleteEntityWithACLWithAuthToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollRole adminRole = new DivrollRole("Admin");
		adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
		adminRole.create();

		DivrollUser adminUser = new DivrollUser();
		adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
		adminUser.getRoles().add(adminRole);
		adminUser.create("admin", "password");

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		DivrollACL acl = new DivrollACL();
		acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
		userProfile.setAcl(acl);

		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));

		adminUser.login("admin", "password");
		Assert.assertNotNull(adminUser.getAuthToken());
		Assert.assertNotNull(Divroll.getAuthToken());

		userProfile.delete();
	}

	@Test(expected = UnauthorizedException.class)
	public void testDeleteEntityWithACLWithoutAuthToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollRole adminRole = new DivrollRole("Admin");
		adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
		adminRole.create();

		DivrollUser adminUser = new DivrollUser();
		adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
		adminUser.getRoles().add(adminRole);
		adminUser.create("admin", "password");

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		DivrollACL acl = new DivrollACL();
		acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
		userProfile.setAcl(acl);

		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));

		// adminUser.login("admin", "password");
		// Assert.assertNotNull(adminUser.getAuthToken());
		// Assert.assertNotNull(Divroll.getAuthToken());

		userProfile.delete();
	}

	@Test
	public void testSetEntityBlob() throws UnsupportedEncodingException {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollRole adminRole = new DivrollRole("Admin");
		adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
		adminRole.create();

		DivrollUser adminUser = new DivrollUser();
		adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
		adminUser.getRoles().add(adminRole);
		adminUser.create("admin", "password");

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		DivrollACL acl = new DivrollACL();
		acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
		userProfile.setAcl(acl);

		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));

		adminUser.login("admin", "password");
		Assert.assertNotNull(adminUser.getAuthToken());
		Assert.assertNotNull(Divroll.getAuthToken());

      userProfile.setBlobProperty("picture", "this is a picture".getBytes(StandardCharsets.UTF_8));

      byte[] blob = userProfile.getBlobProperty("picture");

		Assert.assertNotNull(blob);
		Assert.assertEquals("this is a picture", new String(blob, StandardCharsets.UTF_8));

		userProfile.deleteBlobProperty("picture");

		userProfile.delete();
	}

	@Test
	public void testCreateLink() throws UnsupportedEncodingException {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DivrollRole adminRole = new DivrollRole("Admin");
		adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
		adminRole.create();

		DivrollUser adminUser = new DivrollUser();
		adminUser.setAcl(DivrollACL.buildPublicReadWrite());
		adminUser.getRoles().add(adminRole);
		adminUser.create("admin", "password");

		DivrollEntity userProfile = new DivrollEntity("UserProfile");
		userProfile.setProperty("nickname", "Johnny");
		userProfile.setProperty("age", 30);
		DivrollACL acl = new DivrollACL();
		acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
		acl.setAclRead(Arrays.asList(adminRole.getEntityId()));
		userProfile.setAcl(acl);

		userProfile.create();

		Assert.assertNotNull(userProfile.getEntityId());
		Assert.assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));

		adminUser.login("admin", "password");
		Assert.assertNotNull(adminUser.getAuthToken());
		Assert.assertNotNull(Divroll.getAuthToken());

      userProfile.setBlobProperty("picture", "this is a picture".getBytes(StandardCharsets.UTF_8));

      byte[] blob = userProfile.getBlobProperty("picture");

		Assert.assertNotNull(blob);
		Assert.assertEquals("this is a picture", new String(blob, StandardCharsets.UTF_8));

		userProfile.deleteBlobProperty("picture");

		try {
			userProfile.addLink("user", adminUser.getEntityId());
			userProfile.retrieve();
		} catch (Exception e) {
			e.printStackTrace();
		}

		adminUser.setLink("userProfile", userProfile.getEntityId());
		List<DivrollEntity> userProfiles = adminUser.links("userProfile");
		userProfiles.forEach(entity -> {
			System.out.println(entity.getEntityId());
			System.out.println(entity.getProperty("nickname"));
		});
		assertNotNull(userProfiles);
		Assert.assertFalse(userProfiles.isEmpty());

		Assert.assertNotNull(userProfile.getProperty("linkNames"));
		Assert.assertTrue(((List) userProfile.getProperty("linkNames")).contains("user"));

		List<DivrollEntity> entities = userProfile.links("user");
		Assert.assertNotNull(entities);
		Assert.assertFalse(entities.isEmpty());

		for (DivrollEntity entity : entities) {
		}

		userProfile.removeLink("user", adminUser.getEntityId());
		userProfile.retrieve();
		//
		Assert.assertNotNull(userProfile.getProperty("linkNames"));
		Assert.assertFalse(((List) userProfile.getProperty("linkNames")).contains("user"));

		userProfile.delete();
	}

	@Test
	public void testCreateEntityWithJSONObjectProperty() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", "Sample Name");
		jsonObject.put("age", 100L);

		DivrollEntity sampleEntity = new DivrollEntity("Sample");
		sampleEntity.put("person", jsonObject);
		sampleEntity.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
		sampleEntity.create();

		assertNotNull(sampleEntity.getEntityId());

		DivrollEntities sampleEntities = new DivrollEntities("Sample");
		sampleEntities.query();
		List<DivrollEntity> list = sampleEntities.getEntities();
		assertEquals(1, sampleEntities.getEntities().size());
		list.forEach(entity -> {
			System.out.println(entity.toString());
		});
	}

	@Test
	public void testCreateEntityWithJSONArrayProperty() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", "Sample Name");
		jsonObject.put("age", 100L);

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(jsonObject);

		DivrollEntity sampleEntity = new DivrollEntity("Sample");
		sampleEntity.put("persons", jsonArray);
		sampleEntity.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
		sampleEntity.create();

		assertNotNull(sampleEntity.getEntityId());

		DivrollEntities sampleEntities = new DivrollEntities("Sample");
		sampleEntities.query();
		List<DivrollEntity> list = sampleEntities.getEntities();
		assertEquals(1, sampleEntities.getEntities().size());
		list.forEach(entity -> {
			List personList = (LinkedList) entity.getProperty("persons");
			assertEquals(1, personList.size());
			Map<String, Object> personMap = (Map<String, Object>) personList.iterator().next();
			personMap.forEach((key, value) -> {
				System.out.println(key + " " + value);
			});
		});
	}

}
