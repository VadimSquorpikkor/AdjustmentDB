package com.squorpikkor.app.adjustmentdb.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.squorpikkor.app.adjustmentdb.DUnit;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {

    public static final String DUNIT_TABLE = "units";
    FireDBHelper dbh;
    private final MutableLiveData<ArrayList<DUnit>> unitsList;

    public MainViewModel() {
        unitsList = new MutableLiveData<>();
        dbh = new FireDBHelper(unitsList);
    }

    void saveDUnitToDB(DUnit unit) {
        dbh.addElementToDB(unit, DUNIT_TABLE);
    }

    void addBDListener() {
        dbh.addDBListener(DUNIT_TABLE);
    }
}