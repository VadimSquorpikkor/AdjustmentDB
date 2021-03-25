package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.squorpikkor.app.adjustmentdb.BuildConfig;
import com.squorpikkor.app.adjustmentdb.DState;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.DevType;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

public class MainViewModel extends ViewModel {

    public static final String DUNIT_TABLE = "units";
    public static final String REPAIRS_TABLE = "repairs";
    public static final String DEV_TYPES_TABLE = "dev_types";
    public static final String TABLE_INNER_STATES = "states";

    public static final String REPAIR_STATES_TABLE = "repair_states";
    public static final String SERIAL_STATES_TABLE = "serial_states";

    public static final String INNER_SERIAL = "innerSerial";
    public static final String SERIAL = "serial";
    public static final String NAME = "name";
    public static final String ID = "id";

    public static final String SERIAL_TYPE = "serial_type";
    public static final String REPAIR_TYPE = "repair_type";
    FireDBHelper dbh;
    private final MutableLiveData<ArrayList<DUnit>> serialUnitsList;
    private final MutableLiveData<ArrayList<DUnit>> repairsUnitsList;
    private final MutableLiveData<ArrayList<DUnit>> selectedUnits;
    private final MutableLiveData<ArrayList<DUnit>> selectedRepairUnits;

    private final MutableLiveData<ArrayList<DevType>> devTypeList;

    private final MutableLiveData<ArrayList<String>> serialStatesList;
    private final MutableLiveData<ArrayList<String>> repairStatesList;

    private final MutableLiveData<ArrayList<DState>> unitStatesList;

    private final MutableLiveData<ArrayList<DUnit>> foundUnitsList;

    public MainViewModel() {
        serialUnitsList = new MutableLiveData<>();
        repairsUnitsList = new MutableLiveData<>();
        selectedUnits = new MutableLiveData<>();
        selectedRepairUnits = new MutableLiveData<>();
        dbh = new FireDBHelper();
        devTypeList = new MutableLiveData<>();
        serialStatesList = new MutableLiveData<>();
        repairStatesList = new MutableLiveData<>();
        unitStatesList = new MutableLiveData<>();
        foundUnitsList = new MutableLiveData<>();
        addDUnitTableListener();
        addRepairUnitTableListener();
        addDevTypeTableListener();
        addSerialStateTableListener();
        addRepairStateTableListener();
    }

    /**Сохраняет DUnit в БД в соответствующую таблицу*/
    public void saveDUnitToDB(DUnit unit) {
        dbh.addElementToDB(unit, DUNIT_TABLE);
    }

    /**Сохраняет ремонтный DUnit в БД в соответствующую таблицу*/
    public void saveRepairUnitToDB(DUnit unit) {
        dbh.addElementToDB(unit, REPAIRS_TABLE);
    }

    void getDUnitFromBD() {
        dbh.getElementFromDB(DUNIT_TABLE, serialUnitsList);
    }

    /**Слушатель для таблицы серийных приборов*/
    void addDUnitTableListener() {
        dbh.addDBListener(DUNIT_TABLE, serialUnitsList);
    }

    /**Слушатель для таблицы ремонтных приборов*/
    void addRepairUnitTableListener() {
        dbh.addDBListener(REPAIRS_TABLE, repairsUnitsList);
    }

    /**Слушатель для таблицы имен приборов*/
    void addDevTypeTableListener() {
        dbh.addDevTypeListener(DEV_TYPES_TABLE, devTypeList);
    }

    /**Слушатель для таблицы названий статусов серийных приборов
     * db -> serial_states -> <name> -> name:*/
    void addSerialStateTableListener() {
        dbh.addStringArrayListener(SERIAL_STATES_TABLE, serialStatesList, NAME);
    }

    /**Слушатель для таблицы названий статусов ремонтных приборов
     * db -> repair_states -> <name> -> name:*/
    void addRepairStateTableListener() {
        Log.e(TAG, "addRepairStateTableListener: ");
        dbh.addStringArrayListener(REPAIR_STATES_TABLE, repairStatesList, NAME);
    }

    /**Добавить слушателя для списка статусов для выбранного ремонтного устройства
     * db -> repairs -> r_0005 -> states -> <name> -> date:+state: */
    public void addSelectedRepairUnitStatesListListener(String id) {
        String name = "r_"+ id;
        dbh.addSelectedUnitListener(REPAIRS_TABLE, name, TABLE_INNER_STATES, unitStatesList);
    }

    /**Добавить слушателя для списка статусов для выбранного серийного устройства
     * units -> name_1234 -> states -> <name> -> date:+state: */
    public void addSelectedSerialUnitStatesListListener(String name, String innerSerial) {
        String name_db = name+"_"+innerSerial;
        dbh.addSelectedUnitListener(DUNIT_TABLE, name_db, TABLE_INNER_STATES, unitStatesList);
    }

    /**Список названий статусов серийных приборов*/
    public MutableLiveData<ArrayList<String>> getSerialStatesList() {
        return serialStatesList;
    }

    /**Список названий статусов ремонтных приборов*/
    public MutableLiveData<ArrayList<String>> getRepairStatesList() {
        return repairStatesList;
    }

    /**Список имен (названий) приборов*/
    public MutableLiveData<ArrayList<DevType>> getDevTypeList() {
        return devTypeList;
    }

    public MutableLiveData<ArrayList<DUnit>> getSerialUnitsList() {
        return serialUnitsList;
    }

    public MutableLiveData<ArrayList<DState>> getUnitStatesList() {
        return unitStatesList;
    }

    public MutableLiveData<ArrayList<DUnit>> getRepairUnitsList() {
        return repairsUnitsList;
    }

    public MutableLiveData<ArrayList<DUnit>> getSelectedUnits() {
        return selectedUnits;
    }

    public MutableLiveData<ArrayList<DUnit>> getFoundUnitsList() {
        return foundUnitsList;
    }

    //todo переделать в getSelectedUnits.setValue(...) ???
    public void setSelectedUnit(DUnit newUnit) {
        ArrayList<DUnit> units = new ArrayList<>();
        units.add(newUnit);
        selectedUnits.setValue(units);
    }

    /**Получить список серийных юнитов из БД по их типу и внутреннему серийнику. По-сути в БД такое
     *  устройство должно быть всегда в одном экземпляре. То, что функция возвращает список,
     *  сделано для проверки на дублирование. Пока в таком случае просто пишется в консоль
     *  предупреждение*/
    public void getDUnitByNameAndInnerSerial(String name, String innerSerial) {
        dbh.readFromDBByTwoParameters(DUNIT_TABLE, NAME, name, INNER_SERIAL, innerSerial, selectedUnits);
        String name_db = name+"_"+innerSerial;
        dbh.getStatesFromDB(DUNIT_TABLE, name_db, TABLE_INNER_STATES, unitStatesList);
//        selectedUnit.setName(name);
//        selectedUnit.setInnerSerial(innerSerial);
    }

    /**Получить список ремонтных юнитов из БД по их типу и серийному номеру*/
    public void getRepairUnitByNameAndSerial(String name, String serial) {
        dbh.readFromDBByTwoParameters(REPAIRS_TABLE, NAME, name, SERIAL, serial, selectedRepairUnits);
    }

    /**Получить список ремонтных юнитов из БД по их индификационному номеру*/
    public void getRepairUnitById(String id) {
//        dbh.readFromDBByParameter(REPAIRS_TABLE, ID, id, selectedRepairUnits);
        dbh.readFromDBByParameter(REPAIRS_TABLE, ID, id, selectedUnits);// пока selectedUnits — всё равно всё выводится в одни и те же поля фрагмента
        String name = "r_"+ id;
        dbh.getStatesFromDB(REPAIRS_TABLE, name, TABLE_INNER_STATES, unitStatesList);//Список статусов для текущего юнита
//        selectedUnit.setId(id);
    }

    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }
}