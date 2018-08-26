package com.divroll.roll;

import junit.framework.TestCase;
import org.fluttercode.datafactory.impl.DataFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

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

        for(int i=0;i<10;i++) {
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

        for(int i=0;i<10;i++) {
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

        for(int i=0;i<10;i++) {
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

//        Assert.assertEquals(10, entities.getEntities().size());
//        entities = new DivrollEntities("UserProfile");

        for(int i=0;i<5;i++) {
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

        for(int i=0;i<5;i++) {
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

    }

}
