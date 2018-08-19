package com.divroll.domino;

import com.divroll.domino.exception.BadRequestException;
import com.divroll.domino.exception.UnauthorizedException;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;

import java.io.InvalidClassException;
import java.util.Arrays;

@RunWith(JUnit4.class)
public class TestDominoUser extends TestCase {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testCreateUserPublic() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser user = new DominoUser();
        user.setAcl(DominoACL.buildPublicReadWrite());
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertEquals("username", user.getUsername());
        Assert.assertNotNull(user.getAuthToken());
        //Assert.assertTrue(user.getPassword() == null);

        user.retrieve();
    }

    @Test//(expected = InvalidEntityException.class)
    public void testCreateUserInvalidACLShouldThrowException() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser user = new DominoUser();
        DominoACL acl = DominoACL.build();
        acl.setAclWrite(Arrays.asList("")); // invalid
        acl.setAclRead(Arrays.asList(""));  // invalid
        user.setAcl(acl);
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
    }

    @Test
    public void testCreateUserMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser user = new DominoUser();
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
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser user = new DominoUser();
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
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());


        DominoUser user = new DominoUser();
        user.setAcl(DominoACL.buildMasterKeyOnly());
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
        Domino.initialize("WRONG", application.getApiToken());
        DominoUser user = new DominoUser();
        user.setAcl(DominoACL.buildMasterKeyOnly());
        user.create("username", "password");
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateUserInvalidApiToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), "WRONG");
        DominoUser user = new DominoUser();
        user.setAcl(DominoACL.buildMasterKeyOnly());
        user.create("username", "password");
    }

    @Test
    public void testCreateUserInvalidMasterKey() {
        // TODO
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetUserInvalidAppId() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser user = new DominoUser();
        user.setAcl(DominoACL.buildMasterKeyOnly());
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertNotNull(user.getAcl());
        Assert.assertNotNull(user.getAuthToken());

        Domino.initialize("WRONG", application.getApiToken());
        user.retrieve();
        Assert.assertNull(user.getEntityId());
        Assert.assertNull(user.getAcl());
        Assert.assertNull(user.getAuthToken());
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetUserInvalidApiToken() {
        TestApplication application = TestData.getNewApplication();

        Domino.initialize(application.getAppId(), application.getApiToken());
        DominoUser user = new DominoUser();
        user.create("username", "password");

        Assert.assertNotNull(user.getEntityId());
        Assert.assertNotNull(user.getAcl());
        Assert.assertNotNull(user.getUsername());
        Assert.assertNotNull(user.getAuthToken());

        Domino.initialize(application.getAppId(), "WRONG");
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
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser dominoUser = new DominoUser();
        dominoUser.create("admin", "password");

        Assert.assertNotNull(dominoUser.getEntityId());

        String userId = dominoUser.getEntityId();

        DominoRole dominoRole = new DominoRole();
        DominoACL dominoACL = DominoACL.build();
        dominoACL.setPublicRead(true);
        dominoACL.setAclWrite(Arrays.asList(userId));

        dominoRole.setAcl(dominoACL);
        dominoRole.setName("Admin");
        dominoRole.create();

        Assert.assertNotNull(dominoRole.getEntityId());
        Assert.assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(dominoRole.getAcl().getPublicRead());

        dominoUser.login("admin", "password");
        Assert.assertNotNull(dominoUser.getAuthToken());
        Assert.assertNotNull(Domino.getAuthToken());

        dominoRole.retrieve();
//
        Assert.assertNotNull(dominoRole.getEntityId());
        Assert.assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(dominoRole.getAcl().getPublicRead());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateAndGetUserWithACLMissingAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser dominoAdmin = new DominoUser();
        dominoAdmin.setAcl(DominoACL.buildMasterKeyOnly());
        dominoAdmin.create("admin", "password");

        String adminUserId = dominoAdmin.getEntityId();

        DominoUser dominoUser = new DominoUser();
        DominoACL acl = new DominoACL();
        acl.setAclWrite(Arrays.asList(adminUserId));
        acl.setAclRead(Arrays.asList(adminUserId));
        dominoUser.setAcl(acl);
        dominoUser.create("user", "password");

        Assert.assertNotNull(dominoUser.getEntityId());
        Assert.assertTrue(dominoUser.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(dominoUser.getAcl().getAclRead().contains("0-0"));

        dominoUser.retrieve();

//        Assert.assertNotNull(dominoUser.getEntityId());
//        Assert.assertTrue(dominoUser.getAcl().getAclWrite().contains("0-0"));
//        Assert.assertTrue(dominoUser.getAcl().getAclRead().contains("0-0"));
    }

    @Test
    public void testUpdatePublicUserMissingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser dominoUser = new DominoUser();
        dominoUser.setAcl(DominoACL.buildPublicReadWrite());
        dominoUser.create("username", "password");

        Assert.assertNotNull(dominoUser.getEntityId());
        Assert.assertEquals("username", dominoUser.getUsername());
        Assert.assertTrue(dominoUser.getAcl().getPublicRead());
        Assert.assertTrue(dominoUser.getAcl().getPublicWrite());

        dominoUser.update("new_username", "new_password");
        Assert.assertEquals("new_username", dominoUser.getUsername());
        Assert.assertTrue(dominoUser.getAcl().getPublicRead());
        Assert.assertTrue(dominoUser.getAcl().getPublicWrite());
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateUserWithACLMissingAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser dominoUser = new DominoUser();
        dominoUser.create("admin", "password");

        Assert.assertNotNull(dominoUser.getEntityId());

        String userId = dominoUser.getEntityId();

        DominoUser user = new DominoUser();
        DominoACL dominoACL = DominoACL.build();
        dominoACL.setAclRead(Arrays.asList(userId));
        dominoACL.setAclWrite(Arrays.asList(userId));
        user.setAcl(dominoACL);
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
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser admin = new DominoUser();
        admin.create("admin", "password");

        Assert.assertNotNull(admin.getEntityId());

        String userId = admin.getEntityId();

        DominoUser user = new DominoUser();
        DominoACL dominoACL = DominoACL.build();
        dominoACL.setAclRead(Arrays.asList(userId));
        dominoACL.setAclWrite(Arrays.asList(userId));
        user.setAcl(dominoACL);
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
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoUser dominoUser = new DominoUser();
        dominoUser.setAcl(DominoACL.buildPublicReadWrite());
        dominoUser.create("username", "password");

        Assert.assertNotNull(dominoUser.getEntityId());
        Assert.assertEquals("username", dominoUser.getUsername());
        Assert.assertTrue(dominoUser.getAcl().getPublicRead());
        Assert.assertTrue(dominoUser.getAcl().getPublicWrite());

        Assert.assertTrue(dominoUser.getAcl().getPublicRead());
        Assert.assertTrue(dominoUser.getAcl().getPublicWrite());

        dominoUser.update("new_username", "new_password");

        Assert.assertNotNull(dominoUser.getEntityId());
        Assert.assertEquals("new_username", dominoUser.getUsername());
        Assert.assertTrue(dominoUser.getAcl().getPublicRead());
        Assert.assertTrue(dominoUser.getAcl().getPublicWrite());

        Assert.assertTrue(dominoUser.getAcl().getPublicRead());
        Assert.assertTrue(dominoUser.getAcl().getPublicWrite());


    }

    @Test
    public void testUpdatePublicUserChangeACLUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoUser dominoUser = new DominoUser();
        dominoUser.setAcl(DominoACL.buildPublicReadWrite());
        dominoUser.create("username", "password");

        Assert.assertEquals("username", dominoUser.getUsername());
        Assert.assertTrue(dominoUser.getAcl().getPublicRead());
        Assert.assertTrue(dominoUser.getAcl().getPublicWrite());

        dominoUser.setAcl(DominoACL.buildMasterKeyOnly());
        dominoUser.update();

        Assert.assertEquals("username", dominoUser.getUsername());
        Assert.assertFalse(dominoUser.getAcl().getPublicRead());
        Assert.assertFalse(dominoUser.getAcl().getPublicWrite());

    }

    @Test
    public void testUpdatePublicRoleChangeACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser admin = new DominoUser();
        admin.create("admin", "password");

        Assert.assertNotNull(admin.getEntityId());

        DominoUser dominoUser = new DominoUser();
        dominoUser.setAcl(DominoACL.buildPublicReadWrite());
        dominoUser.create("username", "password");

        Assert.assertEquals("username", dominoUser.getUsername());
        Assert.assertTrue(dominoUser.getAcl().getPublicRead());
        Assert.assertTrue(dominoUser.getAcl().getPublicWrite());

        DominoACL dominoACL = DominoACL.build();
        dominoACL.setAclRead(Arrays.asList(admin.getEntityId()));
        dominoACL.setAclWrite(Arrays.asList(admin.getEntityId()));
        dominoUser.setAcl(dominoACL);

        admin.login("admin", "password");

        assertNotNull(admin.getAuthToken());

        dominoUser.update();
//
        Assert.assertEquals("username", dominoUser.getUsername());
        Assert.assertFalse(dominoUser.getAcl().getPublicRead());
        Assert.assertFalse(dominoUser.getAcl().getPublicWrite());
    }

    @Test
    public void testCreateUserWithRole() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        role.create();

        String adminRoleId = role.getEntityId();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.create("admin", "password");
    }

    @Test
    public void testCreateUserWithRoles() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        role.create();

        DominoRole managerRole = new DominoRole("Manager");
        managerRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = role.getEntityId();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.getRoles().add(managerRole);
        adminUser.create("admin", "password");
    }

    @Test(expected = BadRequestException.class)
    public void testCreateUserWithRolesThenUpdateRoleWithoutAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole adminRole = new DominoRole("Admin");
        adminRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DominoRole managerRole = new DominoRole("Manager");
        managerRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = adminRole.getEntityId();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
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
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        role.create();

        DominoRole managerRole = new DominoRole("Manager");
        managerRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = role.getEntityId();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
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
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        role.create();

        String adminRoleId = role.getEntityId();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.create("admin", "password");

        DominoUser dominoUser = new DominoUser();
        DominoACL userACL = new DominoACL();
        userACL.setPublicRead(true);
        userACL.setPublicWrite(false);
        userACL.setAclWrite(Arrays.asList(adminRoleId));
        dominoUser.setAcl(userACL);
        dominoUser.create("user", "password");

        Assert.assertNotNull(adminUser.getRoles());
        Assert.assertFalse(adminUser.getRoles().isEmpty());

        adminUser.login("admin", "password");
        String authToken = adminUser.getAuthToken();

        Assert.assertNotNull(authToken);
        Assert.assertNotNull(Domino.getAuthToken());

        System.out.println("Updating...");

        dominoUser.update("new_username", "new_password");
        Assert.assertEquals("new_username", dominoUser.getUsername());
    }

    @Test(expected = BadRequestException.class)
    public void testDeletePublicUser() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser dominoUser = new DominoUser();
        dominoUser.setAcl(DominoACL.buildPublicReadWrite());
        dominoUser.create("username", "password");

        Assert.assertNotNull(dominoUser.getEntityId());

        dominoUser.delete();

        dominoUser.retrieve();

        Assert.assertNull(dominoUser.getEntityId());
    }

    @Test(expected = BadRequestException.class)
    public void testDeletePublicUserWithAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());


        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.create("admin", "password");

        adminUser.login("admin", "password");

        DominoUser dominoUser = new DominoUser();
        dominoUser.setAcl(DominoACL.buildPublicReadWrite());
        dominoUser.create("user", "password");

        Assert.assertNotNull(dominoUser.getEntityId());

        adminUser.login("admin", "password");

        dominoUser.delete();

        dominoUser.retrieve();

        Assert.assertNull(dominoUser.getEntityId());
    }

    @Test
    public void testDeletePublicUserWithMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        Assert.assertNotNull(role.getEntityId());

        DominoUser dominoUser = new DominoUser();
        dominoUser.setAcl(DominoACL.buildPublicReadWrite());
        dominoUser.create("username", "password");

        Assert.assertNotNull(dominoUser.getEntityId());

        dominoUser.delete();

    }

    @Test
    public void testDeleteUserWithACLWithAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole adminRole = new DominoRole("Admin");
        adminRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        Assert.assertNotNull(adminUser.getRoles());
        Assert.assertFalse(adminUser.getRoles().isEmpty());

        DominoUser dominoUser = new DominoUser();
        DominoACL acl = new DominoACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        dominoUser.setAcl(acl);
        dominoUser.create("username", "password");

        assertNotNull(dominoUser.getEntityId());
        assertNotNull(dominoUser.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        adminUser.login("admin", "password");
        assertNotNull(adminUser.getAuthToken());
        Assert.assertNotNull(Domino.getAuthToken());

        dominoUser.delete();
    }

    @Test(expected = UnauthorizedException.class)
    public void testDeleteUserWithACLWithoutAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole adminRole = new DominoRole("Admin");
        adminRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        Assert.assertNotNull(adminUser.getRoles());
        Assert.assertFalse(adminUser.getRoles().isEmpty());

        DominoUser dominoUser = new DominoUser();
        DominoACL acl = new DominoACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        dominoUser.setAcl(acl);
        dominoUser.create("username", "password");

        assertNotNull(dominoUser.getEntityId());
        assertNotNull(dominoUser.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        dominoUser.delete();
    }
}
