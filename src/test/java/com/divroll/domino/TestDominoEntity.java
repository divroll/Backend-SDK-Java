package com.divroll.domino;

import com.divroll.domino.exception.BadRequestException;
import com.divroll.domino.exception.NotFoundRequestException;
import com.divroll.domino.exception.UnauthorizedException;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@RunWith(JUnit4.class)
public class TestDominoEntity extends TestCase {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testCreateEntityUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoEntity entity = new DominoEntity("TestEntity");
        entity.setProperty("username", "TestUser");
        entity.setProperty("age", 30);
        entity.setProperty("nickname", "testo");
        entity.create();

        Assert.assertNotNull(entity.getEntityId());
    }

    @Test
    public void testCreatePublicEntityThenUpdate() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoEntity entity = new DominoEntity("TestEntity");
        entity.setProperty("username", "TestUser");
        entity.setProperty("age", 30);
        entity.setProperty("nickname", "testo");
        entity.setAcl(DominoACL.buildPublicReadWrite());
        entity.create();

        Assert.assertNotNull(entity.getEntityId());

        entity.setProperty("age", 31);

        entity.update();
    }


    @Test
    public void testCreateEntityInvalidACL() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());
        DominoEntity userProfile = new DominoEntity("UserProfile");
        DominoACL acl = DominoACL.build();
        acl.setAclWrite(Arrays.asList("")); // invalid
        acl.setAclRead(Arrays.asList(""));  // invalid
        userProfile.setAcl(acl);
        userProfile.create();
        Assert.assertNotNull(userProfile.getEntityId());
    }

    @Test
    public void testCreateEntityMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(null);
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
        Assert.assertEquals(30, userProfile.getProperty("age"));
        Assert.assertNull(userProfile.getAcl());
        Assert.assertNull(userProfile.getAcl());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateEntityMasterKeyOnlyShouldThrowException() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(null);
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
        Assert.assertEquals(30, userProfile.getProperty("age"));
        Assert.assertNull(userProfile.getAcl());
        Assert.assertNull(userProfile.getAcl());

        // This wil throw exception since the created Role has
        // Master Key-only access
        userProfile.retrieve();
    }

    @Test
    public void testCreateEntityMasterKeyOnly() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(DominoACL.buildMasterKeyOnly());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
        Assert.assertTrue(userProfile.getAcl().getAclRead().isEmpty());
        Assert.assertTrue(userProfile.getAcl().getAclWrite().isEmpty());

        userProfile.retrieve();

        assertNotNull(userProfile.getEntityId());
        Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
        Assert.assertTrue(userProfile.getAcl().getAclRead().isEmpty());
        Assert.assertTrue(userProfile.getAcl().getAclWrite().isEmpty());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateEntityInvalidAppId() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize("WRONG", application.getApiToken());
        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(DominoACL.buildMasterKeyOnly());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateEntityInvalidApiToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), "WRONG");
        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(DominoACL.buildMasterKeyOnly());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();
    }

    @Test
    public void testCreateUserInvalidMasterKey() {
        // TODO
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetEntityInvalidAppId() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(DominoACL.buildMasterKeyOnly());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
        Assert.assertTrue(userProfile.getAcl().getAclRead().isEmpty());
        Assert.assertTrue(userProfile.getAcl().getAclWrite().isEmpty());

        Domino.initialize("WRONG", application.getApiToken());
        userProfile.retrieve();
        Assert.assertNull(userProfile.getEntityId());
        Assert.assertNull(userProfile.getAcl());
    }

    @Test(expected = UnauthorizedException.class)
    public void testGetUserInvalidApiToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(DominoACL.buildMasterKeyOnly());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
        Assert.assertTrue(userProfile.getAcl().getAclRead().isEmpty());
        Assert.assertTrue(userProfile.getAcl().getAclWrite().isEmpty());

        Domino.initialize(application.getAppId(), "WRONG");
        userProfile.retrieve();
        Assert.assertNull(userProfile.getEntityId());
        Assert.assertNull(userProfile.getAcl());
    }

    @Test
    public void testGetUserInvalidMasterKey() {
        // TODO
    }

    @Test
    public void testCreateAndGetEntityWithACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser adminUser = new DominoUser();
        adminUser.create("admin", "password");

        Assert.assertNotNull(adminUser.getEntityId());

        String userId = adminUser.getEntityId();

        DominoEntity userProfile = new DominoEntity("UserProfile");

        DominoACL dominoACL = DominoACL.build();
        dominoACL.setPublicRead(true);
        dominoACL.setAclWrite(Arrays.asList(userId));
        userProfile.setAcl(dominoACL);

        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(userProfile.getAcl().getPublicRead());

        adminUser.login("admin", "password");
        Assert.assertNotNull(adminUser.getAuthToken());
        Assert.assertNotNull(Domino.getAuthToken());

        userProfile.retrieve();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(userProfile.getAcl().getPublicRead());
    }

    @Test(expected = UnauthorizedException.class)
    public void testCreateAndGetEntityWithACLMissingAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser dominoAdmin = new DominoUser();
        dominoAdmin.setAcl(DominoACL.buildMasterKeyOnly());
        dominoAdmin.create("admin", "password");

        String adminUserId = dominoAdmin.getEntityId();

        DominoEntity userProfile = new DominoEntity("UserProfile");
        DominoACL acl = new DominoACL();
        acl.setAclWrite(Arrays.asList(adminUserId));
        acl.setAclRead(Arrays.asList(adminUserId));
        userProfile.setAcl(acl);
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

        userProfile.retrieve();

    }

    @Test
    public void testUpdatePublicEntityMissingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(DominoACL.buildPublicReadWrite());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
        Assert.assertTrue(userProfile.getAcl().getPublicRead());
        Assert.assertTrue(userProfile.getAcl().getPublicWrite());

        userProfile.setProperty("age", 40);
        userProfile.update();

        Assert.assertEquals(40, userProfile.getProperty("age"));
        Assert.assertTrue(userProfile.getAcl().getPublicRead());
        Assert.assertTrue(userProfile.getAcl().getPublicWrite());
    }

    @Test(expected = UnauthorizedException.class)
    public void testUpdateEntityWithACLMissingAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser dominoUser = new DominoUser();
        dominoUser.create("admin", "password");

        Assert.assertNotNull(dominoUser.getEntityId());

        String userId = dominoUser.getEntityId();

        DominoACL dominoACL = DominoACL.build();
        dominoACL.setAclRead(Arrays.asList(userId));
        dominoACL.setAclWrite(Arrays.asList(userId));

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(dominoACL);
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

        userProfile.setProperty("age", 40);
        userProfile.update();
    }

    @Test
    public void testUpdateEntityWithACL() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser dominoUser = new DominoUser();
        dominoUser.create("admin", "password");

        Assert.assertNotNull(dominoUser.getEntityId());

        String userId = dominoUser.getEntityId();

        DominoACL dominoACL = DominoACL.build();
        dominoACL.setAclRead(Arrays.asList(userId));
        dominoACL.setAclWrite(Arrays.asList(userId));

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(dominoACL);
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

        dominoUser.login("admin", "password");

        userProfile.setProperty("age", 40);
        userProfile.update();
    }

    @Test
    public void testUpdateEntityWithACLUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoUser dominoUser = new DominoUser();
        dominoUser.create("admin", "password");

        Assert.assertNotNull(dominoUser.getEntityId());

        String userId = dominoUser.getEntityId();

        DominoACL dominoACL = DominoACL.build();
        dominoACL.setAclRead(Arrays.asList(userId));
        dominoACL.setAclWrite(Arrays.asList(userId));

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(dominoACL);
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
        Assert.assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

        userProfile.setProperty("age", 40);
        userProfile.update();
    }

    @Test
    public void testUpdatePublicEntityUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser admin = new DominoUser();
        admin.create("admin", "password");

        Assert.assertNotNull(admin.getEntityId());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(DominoACL.buildPublicReadWrite());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getPublicRead());
        Assert.assertTrue(userProfile.getAcl().getPublicWrite());

        admin.login("admin", "password");

        userProfile.setProperty("age", 40);
        userProfile.update();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getPublicRead());
        Assert.assertTrue(userProfile.getAcl().getPublicWrite());
    }


    @Test
    public void testUpdatePublicUserUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser admin = new DominoUser();
        admin.create("admin", "password");

        Assert.assertNotNull(admin.getEntityId());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(DominoACL.buildPublicReadWrite());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getPublicRead());
        Assert.assertTrue(userProfile.getAcl().getPublicWrite());

        userProfile.setProperty("age", 40);
        userProfile.update();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getPublicRead());
        Assert.assertTrue(userProfile.getAcl().getPublicWrite());

    }

    @Test
    public void testUpdatePublicEntityChangeACLUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(DominoACL.buildPublicReadWrite());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getPublicRead());
        Assert.assertTrue(userProfile.getAcl().getPublicWrite());

        userProfile.setAcl(DominoACL.buildMasterKeyOnly());
        userProfile.update();

        Assert.assertEquals("Johnny", userProfile.getProperty("nickname"));
        Assert.assertFalse(userProfile.getAcl().getPublicRead());
        Assert.assertFalse(userProfile.getAcl().getPublicWrite());

    }

    @Test
    public void testUpdatePublicEntityChangeACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoUser admin = new DominoUser();
        admin.create("admin", "password");

        Assert.assertNotNull(admin.getEntityId());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setAcl(DominoACL.buildPublicReadWrite());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.getAcl().getPublicRead());
        Assert.assertTrue(userProfile.getAcl().getPublicWrite());

        DominoACL dominoACL = DominoACL.build();
        dominoACL.setAclRead(Arrays.asList(admin.getEntityId()));
        dominoACL.setAclWrite(Arrays.asList(admin.getEntityId()));
        userProfile.setAcl(dominoACL);

        admin.login("admin", "password");

        assertNotNull(admin.getAuthToken());

        userProfile.setProperty("age", 40);
        userProfile.update();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertNull(userProfile.getAcl().getPublicRead());
        Assert.assertNull(userProfile.getAcl().getPublicWrite());
    }

    @Test
    public void testUpdateEntityUsingAuthTokenWithRole() {
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

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);

        DominoACL dominoACL = DominoACL.build();
        dominoACL.setAclRead(Arrays.asList(adminUser.getEntityId()));
        dominoACL.setAclWrite(Arrays.asList(adminUser.getEntityId()));
        userProfile.setAcl(dominoACL);

        userProfile.create();

        userProfile.update();

    }

    @Test(expected = NotFoundRequestException.class)
    public void testDeletePublicEntity() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.setAcl(DominoACL.buildPublicReadWrite());

        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());

        userProfile.delete();
        userProfile.retrieve();
        Assert.assertNull(userProfile.getEntityId());
    }

    @Test(expected = NotFoundRequestException.class)
    public void testDeletePublicEntityWithAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());


        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.create("admin", "password");

        adminUser.login("admin", "password");

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.setAcl(DominoACL.buildPublicReadWrite());
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());

        adminUser.login("admin", "password");

        userProfile.delete();

        userProfile.retrieve();

        Assert.assertNull(userProfile.getEntityId());
    }

    @Test
    public void testDeletePublicEntityWithMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.setAcl(DominoACL.buildPublicReadWrite());
        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertTrue(userProfile.delete());
    }

    @Test
    public void testDeleteEntityWithACLWithAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole adminRole = new DominoRole("Admin");
        adminRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        DominoACL acl = new DominoACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        userProfile.setAcl(acl);

        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        adminUser.login("admin", "password");
        Assert.assertNotNull(adminUser.getAuthToken());
        Assert.assertNotNull(Domino.getAuthToken());

        userProfile.delete();
    }

    @Test(expected = UnauthorizedException.class)
    public void testDeleteEntityWithACLWithoutAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole adminRole = new DominoRole("Admin");
        adminRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        DominoACL acl = new DominoACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        userProfile.setAcl(acl);

        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        //adminUser.login("admin", "password");
//        Assert.assertNotNull(adminUser.getAuthToken());
//        Assert.assertNotNull(Domino.getAuthToken());

        userProfile.delete();
    }

    @Test
    public void testSetEntityBlob() throws UnsupportedEncodingException {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole adminRole = new DominoRole("Admin");
        adminRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        DominoEntity userProfile = new DominoEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        DominoACL acl = new DominoACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        userProfile.setAcl(acl);

        userProfile.create();

        Assert.assertNotNull(userProfile.getEntityId());
        Assert.assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        adminUser.login("admin", "password");
        Assert.assertNotNull(adminUser.getAuthToken());
        Assert.assertNotNull(Domino.getAuthToken());

        try {
            userProfile.setBlobProperty("picture", "this is a picture".getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] blob = userProfile.getBlobProperty("picture");

        Assert.assertNotNull(blob);
        Assert.assertEquals("this is a picture", new String(blob, "utf-8"));

        userProfile.deleteBlobProperty("picture");

        userProfile.delete();
    }


}
