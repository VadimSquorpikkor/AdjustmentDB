package com.squorpikkor.app.adjustmentdb.ui.main;


import static com.squorpikkor.app.adjustmentdb.Constant.ANY_VALUE;

import androidx.lifecycle.MutableLiveData;

import com.squorpikkor.app.adjustmentdb.DUnit;

import java.util.ArrayList;

/** Перевод nameId -> name при получении данных из БД и перевод name -> nameId при отправке данных
 * в БД. После получении данных приложение работает только со значениями name.
 * Другими словами: приложение ничего не знает о nameId, класс FireBaseHelper ничего не знает о name,
 * общаются друг с другом через Bridge, это что-то типа переводчика*/
public class Bridge {

    private final FireDBHelper dbh;
    private MutableLiveData<ArrayList<DUnit>> foundUnitsList;

    Dictionary dictionary;

    public Bridge() {
        this.dbh = new FireDBHelper();
        dictionary = new Dictionary();
    }

    void getUnitList (MutableLiveData<ArrayList<DUnit>> unitList, String deviceName, String location, String employee, String type, String state, String devSet, String serial) {
        foundUnitsList = new MutableLiveData<>();
        foundUnitsList.observeForever(list -> decodeUnitList(list, unitList));

        if (serial.equals("")) dbh.getUnitList(foundUnitsList,
                dictionary.getDeviceId(deviceName),
                dictionary.getLocationId(location),
                dictionary.getEmployeeId(employee),
                dictionary.getTypeId(type),
                dictionary.getStateId(state),
                dictionary.getDevSetId(devSet), ANY_VALUE);
        else dbh.getUnitList(foundUnitsList, ANY_VALUE, ANY_VALUE, ANY_VALUE, ANY_VALUE, ANY_VALUE, ANY_VALUE, serial);
    }

    private void decodeUnitList(ArrayList<DUnit> list, MutableLiveData<ArrayList<DUnit>> unitList) {
        ArrayList<DUnit> decodedList = new ArrayList<>();
        for (DUnit unit:list) decodedList.add(decodedDUnit(unit));
        unitList.setValue(decodedList);
    }

    /**id -> name*/
    private DUnit decodedDUnit(DUnit unit) {

        return unit;
    }

    /**name -> id*/
    private DUnit encodeDUnit(DUnit unit) {
        return unit;
    }
}
