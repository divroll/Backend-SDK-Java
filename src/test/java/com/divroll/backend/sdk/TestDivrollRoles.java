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

import org.fluttercode.datafactory.impl.DataFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import junit.framework.TestCase;

@RunWith(JUnit4.class)
public class TestDivrollRoles extends TestCase {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testListRolesUsingMasterKey() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

		DivrollRole adminRole = new DivrollRole();
		adminRole.setName("Admin");
		adminRole.create();

		DivrollRole userRole = new DivrollRole();
		userRole.setName("User");
		userRole.create();

		DivrollRole managerRole = new DivrollRole();
		managerRole.setName("Manager");
		managerRole.create();

		DivrollRoles divrollRoles = new DivrollRoles();
		divrollRoles.query();

		assertEquals(3, divrollRoles.getRoles().size());
		assertEquals("Admin", divrollRoles.getRoles().get(0).getName());
		assertEquals("User", divrollRoles.getRoles().get(1).getName());
		assertEquals("Manager", divrollRoles.getRoles().get(2).getName());
	}

	@Test
	public void testGetPublicRoles() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DataFactory df = new DataFactory();

		DivrollUser admin = new DivrollUser();
		admin.setAcl(DivrollACL.buildMasterKeyOnly());
		String adminUsername = df.getEmailAddress();
		admin.create(adminUsername, "password");

		int size = 10;
		for (int i = 0; i < size; i++) {
			DivrollRole role = new DivrollRole();
			role.setName(df.getRandomWord());
			DivrollACL acl = new DivrollACL();
			acl.setPublicWrite(false);
			acl.setPublicRead(true);
			acl.setAclWrite(Arrays.asList(admin.getEntityId()));
			role.setAcl(acl);
			role.create();
		}

		DivrollRoles roles = new DivrollRoles();
		roles.query();
		Assert.assertEquals(10, roles.getRoles().size());
	}

	@Test
	public void testGetRolesWithACLUsingAuthToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DataFactory df = new DataFactory();

		DivrollUser admin = new DivrollUser();
		admin.setAcl(DivrollACL.buildMasterKeyOnly());
		String adminUsername = df.getEmailAddress();
		admin.create(adminUsername, "password");

		for (int i = 0; i < 10; i++) {
			DivrollRole role = new DivrollRole();
			role.setName(df.getRandomWord());
			DivrollACL acl = new DivrollACL();
			acl.setPublicWrite(false);
			acl.setPublicRead(true);
			acl.setAclWrite(Arrays.asList(admin.getEntityId()));
			role.setAcl(acl);
			role.create();
		}

		DivrollRoles roles = new DivrollRoles();
		roles.query();
		Assert.assertEquals(10, roles.getRoles().size());

		for (int i = 0; i < 10; i++) {
			DivrollRole role = new DivrollRole();
			role.setName(df.getRandomWord());
			DivrollACL acl = new DivrollACL();
			acl.setPublicWrite(false);
			acl.setPublicRead(false);
			role.setAcl(acl);
			role.create();
		}

		for (int i = 0; i < 10; i++) {
			DivrollRole role = new DivrollRole();
			role.setName(df.getRandomWord());
			DivrollACL acl = new DivrollACL();
			acl.setPublicWrite(false);
			acl.setPublicRead(false);
			acl.setAclRead(Arrays.asList(admin.getEntityId()));
			role.setAcl(acl);
			role.create();
		}

		admin.login(adminUsername, "password");

		roles.query();
		Assert.assertEquals(20, roles.getRoles().size());
	}
}
