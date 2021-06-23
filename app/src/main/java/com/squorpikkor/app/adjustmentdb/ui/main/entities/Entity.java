package com.squorpikkor.app.adjustmentdb.ui.main.entities;

class Entity {
    String id;
    String nameId;
    String nameRu;

    public Entity(String id, String nameId, String nameRu) {
        this.id = id;
        this.nameId = nameId;
        this.nameRu = nameRu;
    }

    public String getId() {
        return id;
    }

    public String getNameId() {
        return nameId;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }
}
