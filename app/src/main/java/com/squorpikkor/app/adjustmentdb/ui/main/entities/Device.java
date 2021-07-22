package com.squorpikkor.app.adjustmentdb.ui.main.entities;

public class Device extends Entity{

    String engName;
    String devSetId;
    String imgPath;

    public Device(String id, String nameId, String name, String devSetId, String imgPath) {
        super(id, nameId, name);
        this.devSetId = devSetId;
        this.imgPath = imgPath;
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

    public String getImgPath() {
        return imgPath;
    }

}
