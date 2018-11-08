package com.divroll.roll;

import com.divroll.roll.exception.BadRequestException;
import com.divroll.roll.exception.UnauthorizedException;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.Arrays;

@RunWith(JUnit4.class)
public class TestDivrollUser extends TestCase {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testCreateUserPublic() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        System.out.println("APP ID:  " + application.getAppId());
        System.out.println("API KEY: " + application.getApiToken());

        DivrollUser user = new DivrollUser();
        user.setAcl(DivrollACL.buildPublicReadWrite());
        user.create("username", "password");

//        Assert.assertNotNull(user.getEntityId());
//        Assert.assertEquals("username", user.getUsername());
//        Assert.assertNotNull(user.getAuthToken());
        //Assert.assertTrue(user.getPassword() == null);

//        user.retrieve();
    }

    @Test(expected =  BadRequestException.class)
    public void testCreateUserInvalidACLShouldThrowException() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser user = new DivrollUser();
        DivrollACL acl = DivrollACL.build();
        acl.setAclWrite(Arrays.asList("")); // invalid
        acl.setAclRead(Arrays.asList(""));  // invalid
        user.setAcl(acl);
        user.create("username", "password");

        //Assert.assertNotNull(user.getEntityId());
    }

    @Test
    public void testCreateUserMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser user = new DivrollUser();
        user.setAcl(null);
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertEquals("username", user.getUsername());
        Assert.assertTrue(user.getAcl().getAclRead().isEmpty());
        Assert.assertTrue(user.getAcl().getAclWrite().isEmpty());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateUserMasterKeyOnlyShouldThrowException() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser user = new DivrollUser();
        user.setAcl(null);
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertEquals("username", user.getUsername());
        Assert.assertTrue(user.getAcl().getAclRead().isEmpty());
        Assert.assertTrue(user.getAcl().getAclWrite().isEmpty());

        // This wil throw exception since the created Role has
        // Master Key-only access
        user.retrieve();
    }

    @Test
    public void testCreateUserMasterKeyOnly() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());


        DivrollUser user = new DivrollUser();
        user.setAcl(DivrollACL.buildMasterKeyOnly());
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertEquals("username", user.getUsername());
        Assert.assertTrue(user.getAcl().getAclRead().isEmpty());
        Assert.assertTrue(user.getAcl().getAclWrite().isEmpty());

        user.retrieve();

        assertNotNull(user.getEntityId());
        assertNotNull(user.getUsername());
        assertNotNull(user.getAcl());
        assertNotNull(user.getAuthToken());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateUserInvalidAppId() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize("WRONG", application.getApiToken());
        DivrollUser user = new DivrollUser();
        user.setAcl(DivrollACL.buildMasterKeyOnly());
        user.create("username", "password");
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateUserInvalidApiToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), "WRONG");
        DivrollUser user = new DivrollUser();
        user.setAcl(DivrollACL.buildMasterKeyOnly());
        user.create("username", "password");
    }

    @Test
    public void testCreateUserInvalidMasterKey() {
        // TODO
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetUserInvalidAppId() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser user = new DivrollUser();
        user.setAcl(DivrollACL.buildMasterKeyOnly());
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertNotNull(user.getAcl());
        Assert.assertNotNull(user.getAuthToken());

        Divroll.initialize("WRONG", application.getApiToken());
        user.retrieve();
        Assert.assertNull(user.getEntityId());
        Assert.assertNull(user.getAcl());
        Assert.assertNull(user.getAuthToken());
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetUserInvalidApiToken() {
        TestApplication application = TestData.getNewApplication();

        Divroll.initialize(application.getAppId(), application.getApiToken());
        DivrollUser user = new DivrollUser();
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertNotNull(user.getAcl());
        Assert.assertNotNull(user.getUsername());
        Assert.assertNotNull(user.getAuthToken());

        Divroll.initialize(application.getAppId(), "WRONG");
        user.retrieve();
        Assert.assertNull(user.getEntityId());
        Assert.assertNull(user.getAcl());
        Assert.assertNull(user.getUsername());
    }

    @Test
    public void testGetUserInvalidMasterKey() {
        // TODO
    }

    @Test
    public void testCreateAndGetUserWithACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.create("admin", "password");

        Assert.assertNotNull(divrollUser.getEntityId());

        String userId = divrollUser.getEntityId();

        DivrollRole divrollRole = new DivrollRole();
        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setPublicRead(true);
        divrollACL.setAclWrite(Arrays.asList(userId));
        divrollRole.setAcl(divrollACL);
        divrollRole.setName("Admin");
        divrollRole.create();

        Assert.assertNotNull(divrollRole.getEntityId());
        Assert.assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(divrollRole.getAcl().getPublicRead());

        divrollUser.login("admin", "password");
        Assert.assertNotNull(divrollUser.getAuthToken());
        Assert.assertNotNull(Divroll.getAuthToken());

        divrollRole.retrieve();

        Assert.assertNotNull(divrollRole.getEntityId());
        Assert.assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(divrollRole.getAcl().getPublicRead());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateAndGetUserWithACLMissingAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser divrollAdmin = new DivrollUser();
        divrollAdmin.setAcl(DivrollACL.buildMasterKeyOnly());
        divrollAdmin.create("admin", "password");

        String adminUserId = divrollAdmin.getEntityId();

        DivrollUser divrollUser = new DivrollUser();
        DivrollACL acl = new DivrollACL();
        acl.setAclWrite(Arrays.asList(adminUserId));
        acl.setAclRead(Arrays.asList(adminUserId));
        divrollUser.setAcl(acl);
        divrollUser.create("user", "password");

        Assert.assertNotNull(divrollUser.getEntityId());
        Assert.assertTrue(divrollUser.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(divrollUser.getAcl().getAclRead().contains("0-0"));

        divrollUser.retrieve();

//        Assert.assertNotNull(divrollUser.getEntityId());
//        Assert.assertTrue(divrollUser.getAcl().getAclWrite().contains("0-0"));
//        Assert.assertTrue(divrollUser.getAcl().getAclRead().contains("0-0"));
    }

    @Test
    public void testUpdatePublicUserMissingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        Assert.assertNotNull(divrollUser.getEntityId());
        Assert.assertEquals("username", divrollUser.getUsername());
        Assert.assertTrue(divrollUser.getAcl().getPublicRead());
        Assert.assertTrue(divrollUser.getAcl().getPublicWrite());

        divrollUser.update("new_username", "new_password");
        Assert.assertEquals("new_username", divrollUser.getUsername());
        Assert.assertTrue(divrollUser.getAcl().getPublicRead());
        Assert.assertTrue(divrollUser.getAcl().getPublicWrite());
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateUserWithACLMissingAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.create("admin", "password");

        Assert.assertNotNull(divrollUser.getEntityId());

        String userId = divrollUser.getEntityId();

        DivrollUser user = new DivrollUser();
        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(userId));
        divrollACL.setAclWrite(Arrays.asList(userId));
        user.setAcl(divrollACL);
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertTrue(user.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(user.getAcl().getAclRead().contains("0-0"));

        user.update("new_username", "new_password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertTrue(user.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(user.getAcl().getAclRead().contains("0-0"));
    }

    @Test
    public void testUpdatePublicUserUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser admin = new DivrollUser();
        admin.create("admin", "password");

        Assert.assertNotNull(admin.getEntityId());

        String userId = admin.getEntityId();

        DivrollUser user = new DivrollUser();
        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(userId));
        divrollACL.setAclWrite(Arrays.asList(userId));
        user.setAcl(divrollACL);
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertTrue(user.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(user.getAcl().getAclRead().contains("0-0"));

        admin.login("admin", "password");

        user.update("new_username", "new_password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertTrue(user.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(user.getAcl().getAclRead().contains("0-0"));
    }


    @Test
    public void testUpdatePublicUserUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        Assert.assertNotNull(divrollUser.getEntityId());
        Assert.assertEquals("username", divrollUser.getUsername());
        Assert.assertTrue(divrollUser.getAcl().getPublicRead());
        Assert.assertTrue(divrollUser.getAcl().getPublicWrite());

        Assert.assertTrue(divrollUser.getAcl().getPublicRead());
        Assert.assertTrue(divrollUser.getAcl().getPublicWrite());

        divrollUser.update("new_username", "new_password");

        Assert.assertNotNull(divrollUser.getEntityId());
        Assert.assertEquals("new_username", divrollUser.getUsername());
        Assert.assertTrue(divrollUser.getAcl().getPublicRead());
        Assert.assertTrue(divrollUser.getAcl().getPublicWrite());

        Assert.assertTrue(divrollUser.getAcl().getPublicRead());
        Assert.assertTrue(divrollUser.getAcl().getPublicWrite());


    }

    @Test
    public void testUpdatePublicUserChangeACLUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        Assert.assertEquals("username", divrollUser.getUsername());
        Assert.assertTrue(divrollUser.getAcl().getPublicRead());
        Assert.assertTrue(divrollUser.getAcl().getPublicWrite());

        divrollUser.setAcl(DivrollACL.buildMasterKeyOnly());
        divrollUser.update();

        Assert.assertEquals("username", divrollUser.getUsername());
        Assert.assertFalse(divrollUser.getAcl().getPublicRead());
        Assert.assertFalse(divrollUser.getAcl().getPublicWrite());

    }

    @Test
    public void testUpdatePublicRoleChangeACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser admin = new DivrollUser();
        admin.create("admin", "password");

        Assert.assertNotNull(admin.getEntityId());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        Assert.assertEquals("username", divrollUser.getUsername());
        Assert.assertTrue(divrollUser.getAcl().getPublicRead());
        Assert.assertTrue(divrollUser.getAcl().getPublicWrite());

        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(admin.getEntityId()));
        divrollACL.setAclWrite(Arrays.asList(admin.getEntityId()));
        divrollUser.setAcl(divrollACL);

        admin.login("admin", "password");

        assertNotNull(admin.getAuthToken());

        divrollUser.update();
//
        Assert.assertEquals("username", divrollUser.getUsername());
        Assert.assertFalse(divrollUser.getAcl().getPublicRead());
        Assert.assertFalse(divrollUser.getAcl().getPublicWrite());
    }

    @Test
    public void testCreateUserWithRole() {
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
    }

    @Test
    public void testCreateUserWithRoles() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        role.create();

        DivrollRole managerRole = new DivrollRole("Manager");
        managerRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = role.getEntityId();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.getRoles().add(managerRole);
        adminUser.create("admin", "password");
    }

    @Test(expected = BadRequestException.class)
    public void testCreateUserWithRolesThenUpdateRoleWithoutAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole adminRole = new DivrollRole("Admin");
        adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DivrollRole managerRole = new DivrollRole("Manager");
        managerRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = adminRole.getEntityId();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.getRoles().add(managerRole);
        adminUser.create("admin", "password");

        Assert.assertNotNull(adminUser.getEntityId());
        Assert.assertEquals(2, adminUser.getRoles().size());

        adminUser.setRoles(Arrays.asList(managerRole)); // change to Manager role only
        adminUser.update();
        Assert.assertEquals(1, adminUser.getRoles().size());

    }


    @Test
    public void testCreateUserWithRolesThenUpdateRole() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        role.create();

        DivrollRole managerRole = new DivrollRole("Manager");
        managerRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = role.getEntityId();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.getRoles().add(managerRole);
        adminUser.create("admin", "password");

        Assert.assertEquals(2, adminUser.getRoles().size());

        adminUser.login("admin", "password");

        Assert.assertNotNull(adminUser.getEntityId());

        adminUser.setRoles(Arrays.asList(managerRole));
        adminUser.update();

        Assert.assertEquals(1, adminUser.getRoles().size());
    }

    @Test
    public void testUpdateUserWithAuthTokenWithRole() {
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
    }

    @Test(expected = BadRequestException.class)
    public void testDeletePublicUser() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        Assert.assertNotNull(divrollUser.getEntityId());

        divrollUser.delete();

        divrollUser.retrieve();

        Assert.assertNull(divrollUser.getEntityId());
    }

    @Test//(expected = BadRequestException.class)
    public void testDeletePublicUserWithAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());


        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.create("admin", "password");

        adminUser.login("admin", "password");

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("user", "password");

        Assert.assertNotNull(divrollUser.getEntityId());

        adminUser.login("admin", "password");

        divrollUser.delete();

        divrollUser.retrieve();

//        Assert.assertNull(divrollUser.getEntityId());
    }

    @Test
    public void testDeletePublicUserWithMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        Assert.assertNotNull(role.getEntityId());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        Assert.assertNotNull(divrollUser.getEntityId());

        divrollUser.delete();

    }

    @Test
    public void testDeleteUserWithACLWithAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole adminRole = new DivrollRole("Admin");
        adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        Assert.assertNotNull(adminUser.getRoles());
        Assert.assertFalse(adminUser.getRoles().isEmpty());

        DivrollUser divrollUser = new DivrollUser();
        DivrollACL acl = new DivrollACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        divrollUser.setAcl(acl);
        divrollUser.create("username", "password");

        assertNotNull(divrollUser.getEntityId());
        assertNotNull(divrollUser.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        adminUser.login("admin", "password");
        assertNotNull(adminUser.getAuthToken());
        Assert.assertNotNull(Divroll.getAuthToken());

        divrollUser.delete();
    }

    @Test(expected = UnauthorizedException.class)
    public void testDeleteUserWithACLWithoutAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole adminRole = new DivrollRole("Admin");
        adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        Assert.assertNotNull(adminUser.getRoles());
        Assert.assertFalse(adminUser.getRoles().isEmpty());

        DivrollUser divrollUser = new DivrollUser();
        DivrollACL acl = new DivrollACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        divrollUser.setAcl(acl);
        divrollUser.create("username", "password");

        assertNotNull(divrollUser.getEntityId());
        assertNotNull(divrollUser.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        divrollUser.delete();
    }
}
