package com.squorpikkor.app.adjustmentdb;

public class DUnit {
    String name; //БДКГ-02
    String innerSerial; //№12345
    String serial; //132.002
    int category;

    public static final String НА_СБОРКЕ = "На сборке";
    public static final String НА_РЕГУЛИРОВКЕ = "На регулировке";
    public static final String НА_ЛИНЕЙКЕ = "На линейке";

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public DUnit() {
    }

    public DUnit(String name, String serial) {
        this.name = name;
        this.innerSerial = serial;
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
}
