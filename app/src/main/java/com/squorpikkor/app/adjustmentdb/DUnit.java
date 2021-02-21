package com.squorpikkor.app.adjustmentdb;

public class DUnit {
    String name;
    String serial;

    public DUnit() {
    }

    public DUnit(String name, String serial) {
        this.name = name;
        this.serial = serial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }
}
