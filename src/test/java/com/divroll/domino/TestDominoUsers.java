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
public class TestDominoUsers extends TestCase {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetApplication() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());
        Assert.assertNotNull(application.getApiToken());
        Assert.assertNotNull(application.getAppId());
        Assert.assertNotNull(application.getMasterKey());
    }

    @Test
    public void testGetUsers() {
        System.out.println("Running testGetUsers");
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DataFactory df = new DataFactory();
        for(int i=0;i<100;i++) {
            DominoUser dominoUser = new DominoUser();
            dominoUser.create(df.getEmailAddress(), "password");
        }

        DominoUsers users = new DominoUsers();
        users.query();

        Assert.assertEquals(100, users.getUsers().size());
    }

    @Test
    public void testGetUsersMasterKeyOnly() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();
        for(int i=0;i<100;i++) {
            DominoUser dominoUser = new DominoUser();
            dominoUser.setAcl(DominoACL.buildMasterKeyOnly());
            dominoUser.create(df.getEmailAddress(), "password");
        }

        DominoUsers users = new DominoUsers();
        users.query();

        Assert.assertEquals(0, users.getUsers().size());
    }

    @Test
    public void testGetUsersWithACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();

        DominoUser admin = new DominoUser();
        admin.setAcl(DominoACL.buildMasterKeyOnly());
        String adminUsername = df.getEmailAddress();
        admin.create(adminUsername, "password");

        int size = 10;
        for(int i=0;i<size;i++) {
            DominoUser dominoUser = new DominoUser();
            DominoACL acl = new DominoACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(false);
            acl.setAclRead(Arrays.asList(admin.getEntityId()));
            acl.setAclWrite(Arrays.asList(admin.getEntityId()));
            dominoUser.setAcl(acl);
            dominoUser.create(df.getEmailAddress(), "password");
        }

        DominoUsers users = new DominoUsers();
        users.query();

        Assert.assertEquals(0, users.getUsers().size());

        admin.login(adminUsername, "password");

        users = new DominoUsers();
        users.query();

        Assert.assertEquals(size, users.getUsers().size());

        size = 20;
        for(int i=0;i<size;i++) {
            DominoUser dominoUser = new DominoUser();
            DominoACL acl = new DominoACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(true);
            dominoUser.setAcl(acl);
            dominoUser.create(df.getEmailAddress(), "password");
        }

        users = new DominoUsers();
        users.query();

        Assert.assertEquals(30, users.getUsers().size());

        size = 20;
        for(int i=0;i<size;i++) {
            DominoUser dominoUser = new DominoUser();
            DominoACL acl = new DominoACL();
            acl.setPublicWrite(true);
            acl.setPublicRead(false);
            dominoUser.setAcl(acl);
            dominoUser.create(df.getEmailAddress(), "password");
        }

        users = new DominoUsers();
        users.query();

        Assert.assertEquals(30, users.getUsers().size());
    }

}
