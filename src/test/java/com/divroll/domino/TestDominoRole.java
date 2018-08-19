package com.divroll.domino;

import com.divroll.domino.exception.BadRequestException;
import com.divroll.domino.exception.UnauthorizedException;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.UUID;

@RunWith(JUnit4.class)
public class TestDominoRole extends TestCase {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testCreateRolePublic() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());
        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();
        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        role.retrieve();
    }

    @Test(expected = BadRequestException.class)
    public void testCreateRoleInvalidACLShouldThrowException() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        DominoACL acl = DominoACL.build();
        acl.setAclWrite(Arrays.asList("")); // invalid
        acl.setAclRead(Arrays.asList(""));  // invalid
        role.setAcl(acl);
        role.create();

        Assert.assertNotNull(role.getEntityId());
    }

    @Test
    public void testCreateRoleMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(null);
        role.create();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertFalse(role.getAcl().getPublicRead());
        Assert.assertFalse(role.getAcl().getPublicWrite());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateRoleMasterKeyOnlyShouldThrowException() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(null);
        role.create();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertFalse(role.getAcl().getPublicRead());
        Assert.assertFalse(role.getAcl().getPublicWrite());

        // This wil throw exception since the created Role has
        // Master Key-only access
        role.retrieve();
    }

    @Test
    public void testCreateRoleMasterKeyOnly() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildMasterKeyOnly());
        role.create();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertFalse(role.getAcl().getPublicRead());
        Assert.assertFalse(role.getAcl().getPublicWrite());

        role.retrieve();

        assertNotNull(role.getEntityId());
        assertNotNull(role.getName());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateRoleInvalidAppId() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize("WRONG", application.getApiToken());
        DominoRole role = new DominoRole("Admin");
        role.create();
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateRoleInvalidApiToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), "WRONG");
        DominoRole role = new DominoRole("Admin");
        role.create();
    }

    @Test
    public void testCreateRoleInvalidMasterKey() {
        // TODO
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetRoleInvalidAppId() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());
        DominoRole role = new DominoRole("Admin");
        role.create();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertNotNull(role.getAcl());
        Assert.assertNotNull(role.getName());

        Domino.initialize("WRONG", application.getApiToken());
        role.retrieve();
        Assert.assertNull(role.getEntityId());
        Assert.assertNull(role.getAcl());
        Assert.assertNull(role.getName());
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetRoleInvalidApiToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());
        DominoRole role = new DominoRole("Admin");
        role.create();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertNotNull(role.getAcl());
        Assert.assertNotNull(role.getName());

        Domino.initialize(application.getAppId(), "WRONG");
        role.retrieve();
        Assert.assertNull(role.getEntityId());
        Assert.assertNull(role.getAcl());
        Assert.assertNull(role.getName());
    }

    @Test
    public void testGetRoleInvalidMasterKey() {
        // TODO
    }

    @Test
    public void testCreateAndGetRoleWithACLUsingAuthToken() {
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
    public void testCreateAndGetRoleWithACLMissingAuthTokenShouldFail() {
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

        dominoRole.retrieve();

        Assert.assertNotNull(dominoRole.getEntityId());
        Assert.assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(dominoRole.getAcl().getAclRead().contains("0-0"));
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

    @Test(expected = BadRequestException.class)
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
    }

    @Test(expected = BadRequestException.class)
    public void testDeletePublicRoleThenRetrieveShouldFail() {
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
    }

    @Test(expected = BadRequestException.class)
    public void testDeletePublicRoleWithAuthTokenThenRetrieveShouldFail() {
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
    }

    @Test(expected = BadRequestException.class)
    public void testDeletePublicRoleWithMasterKeyTheRetrieveShouldFail() {
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
