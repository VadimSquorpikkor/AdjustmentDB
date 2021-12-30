package com.squorpikkor.app.adjustmentdb.ui.main.entities;

public class Location extends Entity{

    public Location(String id, String nameId, String name) {
        super(id, nameId, name);
    }

    //todo эксперимент
    public Location(String id, String name) {
        super(id, "", name);
    }
}
