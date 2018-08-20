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
public class TestDominoRoles extends TestCase {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testListRolesUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoRole adminRole = new DominoRole();
        adminRole.setName("Admin");
        adminRole.create();

        DominoRole userRole = new DominoRole();
        userRole.setName("User");
        userRole.create();

        DominoRole managerRole = new DominoRole();
        managerRole.setName("Manager");
        managerRole.create();

        DominoRoles dominoRoles = new DominoRoles();
        dominoRoles.query();

        assertEquals(3, dominoRoles.getRoles().size());
        assertEquals("Admin", dominoRoles.getRoles().get(0).getName());
        assertEquals("User", dominoRoles.getRoles().get(1).getName());
        assertEquals("Manager", dominoRoles.getRoles().get(2).getName());

    }

    @Test
    public void testGetPublicRoles() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();

        DominoUser admin = new DominoUser();
        admin.setAcl(DominoACL.buildMasterKeyOnly());
        String adminUsername = df.getEmailAddress();
        admin.create(adminUsername, "password");

        int size = 10;
        for(int i=0;i<size;i++) {
            DominoRole role = new DominoRole();
            role.setName(df.getRandomWord());
            DominoACL acl = new DominoACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(true);
            acl.setAclWrite(Arrays.asList(admin.getEntityId()));
            role.setAcl(acl);
            role.create();
        }

        DominoRoles roles = new DominoRoles();
        roles.query();
        Assert.assertEquals(10, roles.getRoles().size());
    }

    @Test
    public void testGetRolesWithACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();

        DominoUser admin = new DominoUser();
        admin.setAcl(DominoACL.buildMasterKeyOnly());
        String adminUsername = df.getEmailAddress();
        admin.create(adminUsername, "password");

        for(int i=0;i<10;i++) {
            DominoRole role = new DominoRole();
            role.setName(df.getRandomWord());
            DominoACL acl = new DominoACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(true);
            acl.setAclWrite(Arrays.asList(admin.getEntityId()));
            role.setAcl(acl);
            role.create();
        }

        DominoRoles roles = new DominoRoles();
        roles.query();
        Assert.assertEquals(10, roles.getRoles().size());

        for(int i=0;i<10;i++) {
            DominoRole role = new DominoRole();
            role.setName(df.getRandomWord());
            DominoACL acl = new DominoACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(false);
            role.setAcl(acl);
            role.create();
        }


        for(int i=0;i<10;i++) {
            DominoRole role = new DominoRole();
            role.setName(df.getRandomWord());
            DominoACL acl = new DominoACL();
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
