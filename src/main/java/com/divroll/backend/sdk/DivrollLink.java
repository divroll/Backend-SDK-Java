package com.divroll.backend.sdk;

import java.util.LinkedList;
import java.util.List;

public class DivrollLink {
    private String linkName;
    private List<DivrollEntityStub> entities;

    private DivrollLink() {}

    public DivrollLink(String linkName, List<DivrollEntityStub> entities) {
        setLinkName(linkName);
        setEntities(entities);
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public List<DivrollEntityStub> getEntities() {
        if(entities == null) {
            entities = new LinkedList<>();
        }
        return entities;
    }

    public void setEntities(List<DivrollEntityStub> entities) {
        this.entities = entities;
    }
}
