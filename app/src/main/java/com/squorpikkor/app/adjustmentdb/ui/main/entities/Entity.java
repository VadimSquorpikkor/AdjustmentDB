package com.squorpikkor.app.adjustmentdb.ui.main.entities;

public class Entity {
    String id;
    String nameId;
    String name;

    public Entity(String id, String nameId, String name) {
        this.id = id;
        this.nameId = nameId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getNameId() {
        return nameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
