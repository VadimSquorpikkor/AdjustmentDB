package com.squorpikkor.app.adjustmentdb;

public class DUnit {
    String name; //БДКГ-02
    String innerSerial; //№12345
    String serial; //132.002
    String state; //"На линейке"
    String id; //"0001"

    /**Конструктор без параметров нужен для работы с Firebase*/
    public DUnit() {
    }


    public DUnit(String id, String name, String innerSerial, String serial, String state) {
        this.id = id;
        this.name = name;
        this.innerSerial = innerSerial;
        this.serial = serial;
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInnerSerial() {
        return innerSerial;
    }

    public void setInnerSerial(String serial) {
        this.innerSerial = serial;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
