package com.squorpikkor.app.adjustmentdb.ui.main;

public enum Profile {
    РЕГУЛИРОВКА,
    СБОРКА,
    МОНТАЖ,
    ГРАДУИРОВКА,
    ПРИЁМКА;

    private String name;

    private String location;

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public void setData(String name, String location) {
        this.name = name;
        this.location = location;
    }
}
