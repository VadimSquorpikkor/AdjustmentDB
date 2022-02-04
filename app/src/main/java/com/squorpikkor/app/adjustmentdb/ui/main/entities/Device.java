package com.squorpikkor.app.adjustmentdb.ui.main.entities;

public class Device extends Entity{

    String engName;
    String devSetId;
    String imgPath;

    /*public Device(String id, String name, String devSetId, String imgPath) {
        super(id, id, name);
        this.devSetId = devSetId;
        this.imgPath = imgPath;
    }*/

    public Device(String id, String name, String engName, String devSetId, String imgPath) {
        super(id, id, name);
        this.devSetId = devSetId;
        this.imgPath = imgPath;
        this.engName = engName;
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
