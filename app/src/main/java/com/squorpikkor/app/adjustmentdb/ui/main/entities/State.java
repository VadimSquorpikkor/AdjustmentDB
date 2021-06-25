package com.squorpikkor.app.adjustmentdb.ui.main.entities;

public class State extends Entity {

    String type;
    String location;

    public State(String id, String nameId, String name, String type, String location) {
        super(id, nameId, name);
        this.type = type;
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }
}
