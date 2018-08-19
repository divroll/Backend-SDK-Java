package com.divroll.domino;

import com.divroll.domino.exception.InvalidEntityException;
import com.divroll.domino.exception.UnauthorizedException;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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

//        dominoUser.retrieve();
//
//        Assert.assertNotNull(dominoUser.getEntityId());
//        Assert.assertTrue(dominoUser.getAcl().getAclWrite().contains("0-0"));
//        Assert.assertTrue(dominoUser.getAcl().getAclRead().contains("0-0"));
    }

    @Test
    public void testUpdatePublicRoleMissingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        role.setName("Super Admin");
        role.update();

        role.retrieve();

        Assert.assertEquals("Super Admin", role.getName());
    }

    @Test(expected = UnauthorizedException.class)
    public void testUpdateRoleWithACLMissingAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser dominoUser = new DominoUser();
        dominoUser.create("admin", "password");

        Assert.assertNotNull(dominoUser.getEntityId());

        String userId = dominoUser.getEntityId();

        DominoRole dominoRole = new DominoRole();
        DominoACL dominoACL = DominoACL.build();
        dominoACL.setAclRead(Arrays.asList(userId));
        dominoACL.setAclWrite(Arrays.asList(userId));

        dominoRole.setAcl(dominoACL);
        dominoRole.setName("Admin");
        dominoRole.create();

        Assert.assertNotNull(dominoRole.getEntityId());
        Assert.assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(dominoRole.getAcl().getAclRead().contains("0-0"));

        dominoRole.setName("Super Admin");
        dominoRole.update();

        Assert.assertNotNull(dominoRole.getEntityId());
        Assert.assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(dominoRole.getAcl().getAclRead().contains("0-0"));
    }

    @Test
    public void testUpdatePublicRoleUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        DominoUser dominoUser = new DominoUser();
        dominoUser.create("admin", "password");
        dominoUser.login("admin", "password");

        role.setName("Super Admin");
        role.update();

        role.retrieve();

        Assert.assertEquals("Super Admin", role.getName());
    }


    @Test
    public void testUpdatePublicRoleUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        role.setName("Super Admin");
        role.update();

        role.retrieve();

        Assert.assertEquals("Super Admin", role.getName());
        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

    }

    @Test
    public void testUpdatePublicRoleChangeACLUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        role.setAcl(DominoACL.buildMasterKeyOnly());
        role.update();

        Assert.assertEquals("Admin", role.getName());
        Assert.assertFalse(role.getAcl().getPublicRead());
        Assert.assertFalse(role.getAcl().getPublicWrite());

        role.retrieve();

        Assert.assertEquals("Admin", role.getName());
        Assert.assertFalse(role.getAcl().getPublicRead());
        Assert.assertFalse(role.getAcl().getPublicWrite());
    }

    @Test
    public void testUpdatePublicRoleChangeACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        role.setAcl(DominoACL.buildMasterKeyOnly());
        role.update();

        Assert.assertEquals("Admin", role.getName());
        Assert.assertFalse(role.getAcl().getPublicRead());
        Assert.assertFalse(role.getAcl().getPublicWrite());
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

    @Test(expected = UnauthorizedException.class)
    public void testCreateUserWithRolesThenUpdateRoleWithoutAuthTokenShouldFail() {
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

        Assert.assertNotNull(adminUser.getEntityId());

        adminUser.setRoles(Arrays.asList(managerRole));
        adminUser.update();
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

    @Test
    public void testDeletePublicRole() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        Assert.assertNotNull(role.getEntityId());

        role.delete();

        role.retrieve();

        Assert.assertNull(role.getEntityId());
    }

    @Test
    public void testDeletePublicRoleWithAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());


        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.create("admin", "password");

        adminUser.login("admin", "password");
        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        Assert.assertNotNull(role.getEntityId());

        adminUser.login("admin", "password");

        role.delete();

        role.retrieve();

        Assert.assertNull(role.getEntityId());
    }

    @Test
    public void testDeletePublicRoleWithMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        Assert.assertNotNull(role.getEntityId());

        role.delete();

        role.retrieve();

        Assert.assertNull(role.getEntityId());
    }

    @Test(expected = UnauthorizedException.class)
    public void testDeleteRoleWithACLWithAuthToken() {
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

        Assert.assertNotNull(adminUser.getRoles());
        Assert.assertFalse(adminUser.getRoles().isEmpty());

        adminUser.login("admin", "password");
        String authToken = adminUser.getAuthToken();

        Assert.assertNotNull(authToken);
        Assert.assertNotNull(Domino.getAuthToken());

        role.delete();
    }

    @Test(expected = UnauthorizedException.class)
    public void testDeleteRoleWithACLWithoutAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        role.create();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.create("admin", "password");

        role.delete();
    }
}
