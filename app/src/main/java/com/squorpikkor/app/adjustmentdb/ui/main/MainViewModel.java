package com.squorpikkor.app.adjustmentdb.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.squorpikkor.app.adjustmentdb.DUnit;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {

    public static final String DUNIT_TABLE = "units";
    public static final String INNER_SERIAL = "innerSerial";
    public static final String NAME = "name";
    FireDBHelper dbh;
    private final MutableLiveData<ArrayList<DUnit>> unitsList;
    private final MutableLiveData<ArrayList<DUnit>> selectedUnits;

    public MainViewModel() {
        unitsList = new MutableLiveData<>();
        selectedUnits = new MutableLiveData<>();
        dbh = new FireDBHelper();
        addDUnitTableListener();
    }

    /**Сохраняет DUnit в БД в соответствующую таблицу*/
    void saveDUnitToDB(DUnit unit) {
        dbh.addElementToDB(unit, DUNIT_TABLE);
    }

    void getDUnitFromBD() {
        dbh.getElementFromDB(DUNIT_TABLE, unitsList);
    }

    void addDUnitTableListener() {
        dbh.addDBListener(DUNIT_TABLE, unitsList);
    }

    public MutableLiveData<ArrayList<DUnit>> getUnitsList() {
        return unitsList;
    }

    public MutableLiveData<ArrayList<DUnit>> getSelectedUnits() {
        return selectedUnits;
    }

    void getDUnitByNameAndInnerSerial(String name, String innerSerial) {
        dbh.readFromDBByTwoParameters(DUNIT_TABLE, NAME, name, INNER_SERIAL, innerSerial, selectedUnits);
    }
}