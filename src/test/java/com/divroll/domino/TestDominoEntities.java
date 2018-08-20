package com.divroll.domino;

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
public class TestDominoEntities extends TestCase {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetPublicEntities() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();

        DominoUser admin = new DominoUser();
        admin.setAcl(DominoACL.buildMasterKeyOnly());
        String adminUsername = df.getEmailAddress();
        admin.create(adminUsername, "password");

        for(int i=0;i<10;i++) {
            DominoEntity dominoEntity = new DominoEntity("UserProfile");
            dominoEntity.setProperty("nickname", df.getFirstName());
            dominoEntity.setProperty("birthdate", df.getBirthDate().getTime());
            dominoEntity.setProperty("email", df.getEmailAddress());
            dominoEntity.setProperty("address", df.getAddress());
            DominoACL acl = new DominoACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(true);
            acl.setAclWrite(Arrays.asList(admin.getEntityId()));
            dominoEntity.setAcl(acl);
            dominoEntity.create();
        }

        DominoEntities entities = new DominoEntities("UserProfile");
        entities.query();
        Assert.assertEquals(10, entities.getEntities().size());
    }

    @Test
    public void testGetEntitiesWithACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();

        DominoUser admin = new DominoUser();
        admin.setAcl(DominoACL.buildMasterKeyOnly());
        String adminUsername = df.getEmailAddress();
        admin.create(adminUsername, "password");

        for(int i=0;i<10;i++) {
            DominoEntity dominoEntity = new DominoEntity("UserProfile");
            dominoEntity.setProperty("nickname", df.getFirstName());
            dominoEntity.setProperty("birthdate", df.getBirthDate().getTime());
            dominoEntity.setProperty("email", df.getEmailAddress());
            dominoEntity.setProperty("address", df.getAddress());
            DominoACL acl = new DominoACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(true);
            acl.setAclWrite(Arrays.asList(admin.getEntityId()));
            dominoEntity.setAcl(acl);
            dominoEntity.create();
            Assert.assertNotNull(dominoEntity.getEntityId());
        }

        DominoEntities entities = new DominoEntities("UserProfile");
        entities.query();
        Assert.assertEquals(10, entities.getEntities().size());

        for(int i=0;i<10;i++) {
            DominoEntity dominoEntity = new DominoEntity("UserProfile");
            dominoEntity.setProperty("nickname", df.getFirstName());
            dominoEntity.setProperty("birthdate", df.getBirthDate().getTime());
            dominoEntity.setProperty("email", df.getEmailAddress());
            dominoEntity.setProperty("address", df.getAddress());
            DominoACL acl = new DominoACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(false);
            dominoEntity.setAcl(acl);
            dominoEntity.create();
            Assert.assertNotNull(dominoEntity.getEntityId());
        }

        entities.query();
        Assert.assertEquals(10, entities.getEntities().size());

//        Assert.assertEquals(10, entities.getEntities().size());
//        entities = new DominoEntities("UserProfile");

        for(int i=0;i<5;i++) {
            DominoEntity dominoEntity = new DominoEntity("UserProfile");
            dominoEntity.setProperty("nickname", df.getFirstName());
            dominoEntity.setProperty("birthdate", df.getBirthDate().getTime());
            dominoEntity.setProperty("email", df.getEmailAddress());
            dominoEntity.setProperty("address", df.getAddress());
            DominoACL acl = new DominoACL();
            acl.setAclRead(Arrays.asList(admin.getEntityId()));
            dominoEntity.setAcl(acl);
            dominoEntity.create();
            Assert.assertNotNull(dominoEntity.getEntityId());
        }

        for(int i=0;i<5;i++) {
            DominoEntity dominoEntity = new DominoEntity("UserProfile");
            dominoEntity.setProperty("nickname", df.getFirstName());
            dominoEntity.setProperty("birthdate", df.getBirthDate().getTime());
            dominoEntity.setProperty("email", df.getEmailAddress());
            dominoEntity.setProperty("address", df.getAddress());
            dominoEntity.setAcl(DominoACL.buildMasterKeyOnly());
            dominoEntity.create();
            Assert.assertNotNull(dominoEntity.getEntityId());
        }

        admin.login(adminUsername, "password");
        entities.query();
        Assert.assertEquals(15, entities.getEntities().size());

    }

}
