package com.squorpikkor.app.adjustmentdb.ui.main;

enum Profile {
    РЕГУЛИРОВКА,
    СБОРКА,
    МОНТАЖ,
    ГРАДУИРОВКА,
    ПРИЁМКА;

    private String name;

    private String documentName;

    public String getDocumentName() {
        return documentName;
    }

    public String getName() {
        return name;
    }

    public void setData(String name, String tableName) {
        this.name = name;
        this.documentName = tableName;
    }
}
