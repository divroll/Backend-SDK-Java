package com.divroll.backend.sdk;

public class DivrollEntityStub {

    private String entityType;
    private String entityId;

    private DivrollEntityStub() {}

    public DivrollEntityStub(String entityType, String entityId) {
        setEntityType(entityType);
        setEntityId(entityId);
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

}
