package com.squorpikkor.app.adjustmentdb;

public class DevType {
    String name;//AT3509
    String type;//Индивидуальный

    /**Конструктор без параметров нужен для работы с Firebase*/
    public DevType() {
    }

    public DevType(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
