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
public class TestDivrollRole extends TestCase {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testCreateRolePublic() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());
        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
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
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        DivrollACL acl = DivrollACL.build();
        acl.setAclWrite(Arrays.asList("")); // invalid
        acl.setAclRead(Arrays.asList(""));  // invalid
        role.setAcl(acl);
        role.create();

        Assert.assertNotNull(role.getEntityId());
    }

    @Test
    public void testCreateRoleMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
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
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
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
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildMasterKeyOnly());
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
        Divroll.initialize("WRONG", application.getApiToken());
        DivrollRole role = new DivrollRole("Admin");
        role.create();
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateRoleInvalidApiToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), "WRONG");
        DivrollRole role = new DivrollRole("Admin");
        role.create();
    }

    @Test
    public void testCreateRoleInvalidMasterKey() {
        // TODO
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetRoleInvalidAppId() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());
        DivrollRole role = new DivrollRole("Admin");
        role.create();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertNotNull(role.getAcl());
        Assert.assertNotNull(role.getName());

        Divroll.initialize("WRONG", application.getApiToken());
        role.retrieve();
        Assert.assertNull(role.getEntityId());
        Assert.assertNull(role.getAcl());
        Assert.assertNull(role.getName());
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetRoleInvalidApiToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());
        DivrollRole role = new DivrollRole("Admin");
        role.create();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertNotNull(role.getAcl());
        Assert.assertNotNull(role.getName());

        Divroll.initialize(application.getAppId(), "WRONG");
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
//
        Assert.assertNotNull(divrollRole.getEntityId());
        Assert.assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(divrollRole.getAcl().getPublicRead());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateAndGetRoleWithACLMissingAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.create("admin", "password");

        Assert.assertNotNull(divrollUser.getEntityId());

        String userId = divrollUser.getEntityId();

        DivrollRole divrollRole = new DivrollRole();
        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(userId));
        divrollACL.setAclWrite(Arrays.asList(userId));

        divrollRole.setAcl(divrollACL);
        divrollRole.setName("Admin");
        divrollRole.create();

        Assert.assertNotNull(divrollRole.getEntityId());
        Assert.assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(divrollRole.getAcl().getAclRead().contains("0-0"));

        divrollRole.retrieve();

        Assert.assertNotNull(divrollRole.getEntityId());
        Assert.assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(divrollRole.getAcl().getAclRead().contains("0-0"));
    }

    @Test
    public void testUpdatePublicRoleMissingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
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
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.create("admin", "password");

        Assert.assertNotNull(divrollUser.getEntityId());

        String userId = divrollUser.getEntityId();

        DivrollRole divrollRole = new DivrollRole();
        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(userId));
        divrollACL.setAclWrite(Arrays.asList(userId));

        divrollRole.setAcl(divrollACL);
        divrollRole.setName("Admin");
        divrollRole.create();

        Assert.assertNotNull(divrollRole.getEntityId());
        Assert.assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(divrollRole.getAcl().getAclRead().contains("0-0"));

        divrollRole.setName("Super Admin");
        divrollRole.update();

        Assert.assertNotNull(divrollRole.getEntityId());
        Assert.assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(divrollRole.getAcl().getAclRead().contains("0-0"));
    }

    @Test
    public void testUpdatePublicRoleUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.create("admin", "password");
        divrollUser.login("admin", "password");

        role.setName("Super Admin");
        role.update();

        role.retrieve();

        Assert.assertEquals("Super Admin", role.getName());
    }


    @Test
    public void testUpdatePublicRoleUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
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
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        role.setAcl(DivrollACL.buildMasterKeyOnly());
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
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        Assert.assertNotNull(role.getEntityId());
        Assert.assertEquals("Admin", role.getName());
        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        Assert.assertTrue(role.getAcl().getPublicRead());
        Assert.assertTrue(role.getAcl().getPublicWrite());

        role.setAcl(DivrollACL.buildMasterKeyOnly());
        role.update();

        Assert.assertEquals("Admin", role.getName());
        Assert.assertFalse(role.getAcl().getPublicRead());
        Assert.assertFalse(role.getAcl().getPublicWrite());
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

        Assert.assertNotNull(adminUser.getEntityId());

        adminUser.setRoles(Arrays.asList(managerRole));
        adminUser.update();
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

        System.out.println("Updating...");

        divrollUser.update("new_username", "new_password");
        Assert.assertEquals("new_username", divrollUser.getUsername());
    }

    @Test
    public void testDeletePublicRole() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        Assert.assertNotNull(role.getEntityId());

        role.delete();
    }

    @Test(expected = BadRequestException.class)
    public void testDeletePublicRoleThenRetrieveShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        Assert.assertNotNull(role.getEntityId());

        role.delete();

        role.retrieve();

        Assert.assertNull(role.getEntityId());
    }

    @Test
    public void testDeletePublicRoleWithAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());


        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.create("admin", "password");

        adminUser.login("admin", "password");
        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        Assert.assertNotNull(role.getEntityId());

        adminUser.login("admin", "password");

        role.delete();
    }

    @Test(expected = BadRequestException.class)
    public void testDeletePublicRoleWithAuthTokenThenRetrieveShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());


        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.create("admin", "password");

        adminUser.login("admin", "password");
        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
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
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        Assert.assertNotNull(role.getEntityId());

        role.delete();
    }

    @Test(expected = BadRequestException.class)
    public void testDeletePublicRoleWithMasterKeyTheRetrieveShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        Assert.assertNotNull(role.getEntityId());

        role.delete();

        role.retrieve();

        Assert.assertNull(role.getEntityId());
    }

    @Test(expected = UnauthorizedException.class)
    public void testDeleteRoleWithACLWithAuthToken() {
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

        Assert.assertNotNull(adminUser.getRoles());
        Assert.assertFalse(adminUser.getRoles().isEmpty());

        adminUser.login("admin", "password");
        String authToken = adminUser.getAuthToken();

        Assert.assertNotNull(authToken);
        Assert.assertNotNull(Divroll.getAuthToken());

        role.delete();
    }

    @Test(expected = UnauthorizedException.class)
    public void testDeleteRoleWithACLWithoutAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        role.create();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.create("admin", "password");

        role.delete();
    }

}
