package com.divroll.domino;

import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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


}
