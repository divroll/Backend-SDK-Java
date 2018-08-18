package com.divroll.domino;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.UUID;

public class TestDominoEntity extends TestCase {

    public TestDominoEntity(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite( TestDominoEntity.class );
    }

    public void testCreateEntity() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        String random = UUID.randomUUID().toString();

        DominoEntity entity = new DominoEntity("TestEntity");
        entity.setProperty("username", "TestUser");
        entity.setProperty("age", 30);
        entity.setProperty("nickname", "testo");
        entity.create();
    }

}
