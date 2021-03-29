package com.squorpikkor.app.adjustmentdb;

import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.SERIAL_TYPE;

public class DUnit {


    private String name; //БДКГ-02
    private String innerSerial; //№12345
    private String serial; //132.002
    private String id; //"0001"
    private String type; //"Ремонтный"
    private String state; //"На линейке"
    private String description;

    /**Конструктор без параметров нужен для работы с Firebase*/
    public DUnit() {
    }


    public DUnit(String id, String name, String innerSerial, String serial, String state, String description, String type) {
        this.id = id;
        this.name = name;
        this.innerSerial = innerSerial;
        this.serial = serial;
        this.type = type;
        this.state = state;
        this.description = description;
    }

    public boolean isRepairUnit() {
        return type.equals(REPAIR_TYPE);
    }

    public boolean isSerialUnit() {
        return type.equals(SERIAL_TYPE);
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
