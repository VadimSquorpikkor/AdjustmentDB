package com.squorpikkor.app.adjustmentdb.ui.main.entities;

public class Device extends Entity{

    String engName;

    public Device(String id, String nameId, String name) {
        super(id, nameId, name);
    }

    public String getEngName() {
        return engName;
    }

    public void setEngName(String engName) {
        this.engName = engName;
    }
}
