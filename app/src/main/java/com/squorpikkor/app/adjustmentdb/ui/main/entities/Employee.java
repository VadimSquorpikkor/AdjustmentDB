package com.squorpikkor.app.adjustmentdb.ui.main.entities;

public class Employee extends Entity{

    String eMail;
    String location;

    public Employee(String id, String nameId, String name, String eMail, String location) {
        super(id, nameId, name);
        this.eMail = eMail;
        this.location = location;
    }

    public String getEMail() {
        return eMail;
    }

    public String getLocation() {
        return location;
    }
}
