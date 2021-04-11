package com.squorpikkor.app.adjustmentdb.ui.main.scanner;

import com.squorpikkor.app.adjustmentdb.DUnit;

public interface ScannerDataShow {
    void addUnitToCollection(String s);
    void saveUnit(String s);
    DUnit getDUnitFromString(String s);
}
