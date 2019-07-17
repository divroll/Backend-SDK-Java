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
public class TestDivrollEntities extends TestCase {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testGetPublicEntities() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DataFactory df = new DataFactory();

		DivrollUser admin = new DivrollUser();
		admin.setAcl(DivrollACL.buildMasterKeyOnly());
		String adminUsername = df.getEmailAddress();
		admin.create(adminUsername, "password");

		for (int i = 0; i < 10; i++) {
			DivrollEntity divrollEntity = new DivrollEntity("UserProfile");
			divrollEntity.setProperty("nickname", df.getFirstName());
			divrollEntity.setProperty("birthdate", df.getBirthDate().getTime());
			divrollEntity.setProperty("email", df.getEmailAddress());
			divrollEntity.setProperty("address", df.getAddress());
			DivrollACL acl = new DivrollACL();
			acl.setPublicWrite(false);
			acl.setPublicRead(true);
			acl.setAclWrite(Arrays.asList(admin.getEntityId()));
			divrollEntity.setAcl(acl);
			divrollEntity.create();
		}

		DivrollEntities entities = new DivrollEntities("UserProfile");
		entities.query();
		Assert.assertEquals(10, entities.getEntities().size());
	}

	@Test
	public void testGetEntitiesWithACLUsingAuthToken() {
		TestApplication application = TestData.getNewApplication();
		Divroll.initialize(application.getAppId(), application.getApiToken());

		DataFactory df = new DataFactory();

		DivrollUser admin = new DivrollUser();
		admin.setAcl(DivrollACL.buildMasterKeyOnly());
		String adminUsername = df.getEmailAddress();
		admin.create(adminUsername, "password");

		for (int i = 0; i < 10; i++) {
			DivrollEntity divrollEntity = new DivrollEntity("UserProfile");
			divrollEntity.setProperty("nickname", df.getFirstName());
			divrollEntity.setProperty("birthdate", df.getBirthDate().getTime());
			divrollEntity.setProperty("email", df.getEmailAddress());
			divrollEntity.setProperty("address", df.getAddress());
			DivrollACL acl = new DivrollACL();
			acl.setPublicWrite(false);
			acl.setPublicRead(true);
			acl.setAclWrite(Arrays.asList(admin.getEntityId()));
			divrollEntity.setAcl(acl);
			divrollEntity.create();
			Assert.assertNotNull(divrollEntity.getEntityId());
		}

		DivrollEntities entities = new DivrollEntities("UserProfile");
		entities.query();
		Assert.assertEquals(10, entities.getEntities().size());

		for (int i = 0; i < 10; i++) {
			DivrollEntity divrollEntity = new DivrollEntity("UserProfile");
			divrollEntity.setProperty("nickname", df.getFirstName());
			divrollEntity.setProperty("birthdate", df.getBirthDate().getTime());
			divrollEntity.setProperty("email", df.getEmailAddress());
			divrollEntity.setProperty("address", df.getAddress());
			DivrollACL acl = new DivrollACL();
			acl.setPublicWrite(false);
			acl.setPublicRead(false);
			divrollEntity.setAcl(acl);
			divrollEntity.create();
			Assert.assertNotNull(divrollEntity.getEntityId());
		}

		entities.query();
		Assert.assertEquals(10, entities.getEntities().size());

		// Assert.assertEquals(10, entities.getEntities().size());
		// entities = new DivrollEntities("UserProfile");

		for (int i = 0; i < 5; i++) {
			DivrollEntity divrollEntity = new DivrollEntity("UserProfile");
			divrollEntity.setProperty("nickname", df.getFirstName());
			divrollEntity.setProperty("birthdate", df.getBirthDate().getTime());
			divrollEntity.setProperty("email", df.getEmailAddress());
			divrollEntity.setProperty("address", df.getAddress());
			DivrollACL acl = new DivrollACL();
			acl.setAclRead(Arrays.asList(admin.getEntityId()));
			divrollEntity.setAcl(acl);
			divrollEntity.create();
			Assert.assertNotNull(divrollEntity.getEntityId());
		}

		for (int i = 0; i < 5; i++) {
			DivrollEntity divrollEntity = new DivrollEntity("UserProfile");
			divrollEntity.setProperty("nickname", df.getFirstName());
			divrollEntity.setProperty("birthdate", df.getBirthDate().getTime());
			divrollEntity.setProperty("email", df.getEmailAddress());
			divrollEntity.setProperty("address", df.getAddress());
			divrollEntity.setAcl(DivrollACL.buildMasterKeyOnly());
			divrollEntity.create();
			Assert.assertNotNull(divrollEntity.getEntityId());
		}

		admin.login(adminUsername, "password");
		entities.query();
		Assert.assertEquals(15, entities.getEntities().size());

		entities.setCount(true);
		entities.query();
		Assert.assertEquals(15, entities.getEntities().size());
		System.out.println("count=" + entities.getResult());

	}
}
