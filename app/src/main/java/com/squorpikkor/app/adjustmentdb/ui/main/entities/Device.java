package com.squorpikkor.app.adjustmentdb.ui.main.entities;

public class Device extends Entity{

    //todo надо подумать, что делать с блоками, которые могут принадлежать разным комплектам.
    // Как вариант — хранить в БД два типа устройств с разными именами документа(то же — id),
    // но с одинаковым именами. Чтобы нельзя было записать в блок для 6101 тип от 1117 надо сделать спинер типа устройств неактивным если комплект выбран "любой"

    String engName;
    String devSetId;
    String devSetName;

    public Device(String id, String nameId, String name, String devSetId, String devSetName) {
        super(id, nameId, name);
        this.devSetId = devSetId;
        this.devSetName = devSetName;
    }

    public String getEngName() {
        return engName;
    }

    public void setEngName(String engName) {
        this.engName = engName;
    }

    public String getDevSetId() {
        return devSetId;
    }

    public String getDevSetName() {
        return devSetName;
    }

    public void setDevSetName(String devSetName) {
        this.devSetName = devSetName;
    }
}
