package com.squorpikkor.app.adjustmentdb.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.squorpikkor.app.adjustmentdb.DUnit;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {

    public static final String DUNIT_TABLE = "units";
    public static final String REPAIRS_TABLE = "repairs";
    public static final String INNER_SERIAL = "innerSerial";
    public static final String SERIAL = "serial";
    public static final String NAME = "name";
    public static final String ID = "id";
    FireDBHelper dbh;
    private final MutableLiveData<ArrayList<DUnit>> unitsList;
    private final MutableLiveData<ArrayList<DUnit>> repairsUnitsList;
    private final MutableLiveData<ArrayList<DUnit>> selectedUnits;
    private final MutableLiveData<ArrayList<DUnit>> selectedRepairUnits;


    private MutableLiveData<Boolean> isRepair;

    public MainViewModel() {
        unitsList = new MutableLiveData<>();
        repairsUnitsList = new MutableLiveData<>();
        selectedUnits = new MutableLiveData<>();
        selectedRepairUnits = new MutableLiveData<>();
        dbh = new FireDBHelper();
        isRepair = new MutableLiveData<>();
        addDUnitTableListener();
    }

    /**Отслеживание: текущий (отсканированный в данный момент) девайс — это ремонт или серия*/
    public MutableLiveData<Boolean> getIsRepair() {
        return isRepair;
    }

    public void setIsRepair(boolean isRepair) {
        this.isRepair.setValue(isRepair);
    }

    /**Сохраняет DUnit в БД в соответствующую таблицу*/
    void saveDUnitToDB(DUnit unit) {
        dbh.addElementToDB(unit, DUNIT_TABLE);
    }

    /**Сохраняет ремонтный DUnit в БД в соответствующую таблицу*/
    void saveRepairUnitToDB(DUnit unit) {
        dbh.addElementToDB(unit, REPAIRS_TABLE);
    }

    void getDUnitFromBD() {
        dbh.getElementFromDB(DUNIT_TABLE, unitsList);
    }

    /**Слушатель для таблицы серийных приборов*/
    void addDUnitTableListener() {
        dbh.addDBListener(DUNIT_TABLE, unitsList);
    }

    /**Слушатель для таблицы ремонтных приборов*/
    void addRepairUnitTableListener() {
        dbh.addDBListener(REPAIRS_TABLE, repairsUnitsList);
    }

    public MutableLiveData<ArrayList<DUnit>> getUnitsList() {
        return unitsList;
    }

    public MutableLiveData<ArrayList<DUnit>> getSelectedUnits() {
//        Log.e(TAG, "getSelectedUnits: SIZE = "+selectedUnits.getValue().size());
        return selectedUnits;
    }

    /**Получить список серийных юнитов из БД по их типу и внутреннему серийнику. По-сути в БД такое
     *  устройство должно быть всегда в одном экземпляре. То, что функция возвращает список,
     *  сделано для проверки на дублирование. Пока в таком случае просто пишется в консоль
     *  предупреждение*/
    void getDUnitByNameAndInnerSerial(String name, String innerSerial) {
        dbh.readFromDBByTwoParameters(DUNIT_TABLE, NAME, name, INNER_SERIAL, innerSerial, selectedUnits);
    }

    /**Получить список ремонтных юнитов из БД по их типу и серийному номеру*/
    void getRepairUnitByNameAndSerial(String name, String serial) {
        dbh.readFromDBByTwoParameters(REPAIRS_TABLE, NAME, name, SERIAL, serial, selectedRepairUnits);
    }

    /**Получить список ремонтных юнитов из БД по их индификационному номеру*/
    void getRepairUnitById(String id) {
//        dbh.readFromDBByParameter(REPAIRS_TABLE, ID, id, selectedRepairUnits);
        dbh.readFromDBByParameter(REPAIRS_TABLE, ID, id, selectedUnits);// пока selectedUnits — всё равно всё выводится в одни и те же поля фрагмента
    }
}