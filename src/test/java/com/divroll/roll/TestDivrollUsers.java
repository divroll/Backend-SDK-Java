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
public class TestDivrollUsers extends TestCase {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetApplication() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());
        Assert.assertNotNull(application.getApiToken());
        Assert.assertNotNull(application.getAppId());
        Assert.assertNotNull(application.getMasterKey());
    }

    @Test
    public void testGetUsers() {

        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DataFactory df = new DataFactory();
        for(int i=0;i<100;i++) {
            DivrollUser divrollUser = new DivrollUser();
            divrollUser.create(df.getEmailAddress(), "password");
        }

        DivrollUsers users = new DivrollUsers();
        users.query();

        Assert.assertEquals(100, users.getUsers().size());
    }

    @Test
    public void testGetUsersMasterKeyOnly() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();
        for(int i=0;i<100;i++) {
            DivrollUser divrollUser = new DivrollUser();
            divrollUser.setAcl(DivrollACL.buildMasterKeyOnly());
            divrollUser.create(df.getEmailAddress(), "password");
        }

        DivrollUsers users = new DivrollUsers();
        users.query();

        Assert.assertEquals(0, users.getUsers().size());
    }

    @Test
    public void testGetUsersWithACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();

        DivrollUser admin = new DivrollUser();
        admin.setAcl(DivrollACL.buildMasterKeyOnly());
        String adminUsername = df.getEmailAddress();
        admin.create(adminUsername, "password");

        int size = 10;
        for(int i=0;i<size;i++) {
            DivrollUser divrollUser = new DivrollUser();
            DivrollACL acl = new DivrollACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(false);
            acl.setAclRead(Arrays.asList(admin.getEntityId()));
            acl.setAclWrite(Arrays.asList(admin.getEntityId()));
            divrollUser.setAcl(acl);
            divrollUser.create(df.getEmailAddress(), "password");
        }

        DivrollUsers users = new DivrollUsers();
        users.query();

        Assert.assertEquals(0, users.getUsers().size());

        admin.login(adminUsername, "password");

        users = new DivrollUsers();
        users.query();

        Assert.assertEquals(size, users.getUsers().size());

        size = 20;
        for(int i=0;i<size;i++) {
            DivrollUser divrollUser = new DivrollUser();
            DivrollACL acl = new DivrollACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(true);
            divrollUser.setAcl(acl);
            divrollUser.create(df.getEmailAddress(), "password");
        }

        users = new DivrollUsers();
        users.query();

        Assert.assertEquals(30, users.getUsers().size());

        size = 20;
        for(int i=0;i<size;i++) {
            DivrollUser divrollUser = new DivrollUser();
            DivrollACL acl = new DivrollACL();
            acl.setPublicWrite(true);
            acl.setPublicRead(false);
            divrollUser.setAcl(acl);
            divrollUser.create(df.getEmailAddress(), "password");
        }

        users = new DivrollUsers();
        users.query();

        Assert.assertEquals(30, users.getUsers().size());
    }

}
