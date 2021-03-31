package com.squorpikkor.app.adjustmentdb.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.squorpikkor.app.adjustmentdb.BuildConfig;
import com.squorpikkor.app.adjustmentdb.DState;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.DevType;
import java.util.ArrayList;
import java.util.Date;

public class MainViewModel extends ViewModel {

//------- Коллекции (таблицы) ----------------------------------------------------------------------
    /**Коллекция имен для устройств*/
    public static final String TABLE_DEV_TYPES = "dev_types";
    /**Коллекция профилей: имя статуса, локация, тип (ремонт/серия)*/
    public static final String TABLE_PROFILES = "profiles";
    /**Коллекция ремонтных устройств*/
    public static final String TABLE_REPAIRS = "repairs";
    /**Коллекция серийных устройств*/
    public static final String TABLE_SERIALS = "serials";
    /**Коллекция всех статусов*/
    public static final String TABLE_ALL_STATES = "states";
//--------------------------------------------------------------------------------------------------

    public static final String PROFILE_ADJUSTMENT = "adjustment";
    public static final String PROFILE_ASSEMBLY = "assembly";
    public static final String PROFILE_GRADUATION = "graduation";
    public static final String PROFILE_SOLDERING = "soldering";
    public static final String PROFILE_REPAIR_AREA = "repair_area";
//--------------------------------------------------------------------------------------------------
    public static final String PROFILE_LOCATION = "location";
    public static final String PROFILE_NAME = "name";
    public static final String PROFILE_TYPE = "type";
    public static final String PROF_TYPE_ANY = "any";
    public static final String PROF_TYPE_REPAIR = "repair";
    public static final String PROF_TYPE_SERIAL = "serial";



    public static final String ID = "id";
    public static final String UNIT_ID = "unit_id";
    public static final String DATE = "date";
    public static final String STATE = "state";

    public static final String SERIAL_TYPE = "serial_type";
    public static final String REPAIR_TYPE = "repair_type";
    FireDBHelper dbh;
    private final MutableLiveData<ArrayList<DUnit>> serialUnitsList;
    private final MutableLiveData<ArrayList<DUnit>> repairsUnitsList;
    private final MutableLiveData<ArrayList<DUnit>> selectedUnits;

    private final MutableLiveData<ArrayList<DevType>> devTypeList;

    private final MutableLiveData<ArrayList<String>> serialStatesList;
    private final MutableLiveData<ArrayList<String>> repairStatesList;

    private final MutableLiveData<ArrayList<DState>> unitStatesList;

    private final MutableLiveData<ArrayList<DUnit>> foundUnitsList;

    public MainViewModel() {
        serialUnitsList = new MutableLiveData<>();
        repairsUnitsList = new MutableLiveData<>();
        selectedUnits = new MutableLiveData<>();
        dbh = new FireDBHelper();
        devTypeList = new MutableLiveData<>();
        serialStatesList = new MutableLiveData<>();
        repairStatesList = new MutableLiveData<>();
        unitStatesList = new MutableLiveData<>();
        foundUnitsList = new MutableLiveData<>();
        initProfile();
        setSelectedProfile(Profile.РЕГУЛИРОВКА);//todo пока захардкодил, потом будет выбираться и/или браться из SharedPref
        addDUnitTableListener();
        addRepairUnitTableListener();
        addDevTypeTableListener();
        addSerialStateNamesListener();
        addRepairStateNamesListener();
    }

    void initProfile() {
        Profile.РЕГУЛИРОВКА.setData("Регулировка", PROFILE_ADJUSTMENT);
        Profile.ГРАДУИРОВКА.setData("Градуировка", PROFILE_GRADUATION);
        Profile.СБОРКА.setData("Сборка", PROFILE_ASSEMBLY);
        Profile.МОНТАЖ.setData("Монтаж", PROFILE_SOLDERING);
        Profile.ПРИЁМКА.setData("Приёмка", PROFILE_REPAIR_AREA);
    }

    Profile selectedProfile;

    public Profile getSelectedProfile() {
        return selectedProfile;
    }

    /**Выбрать профиль (сборка, регулировка...). При смене профиля обновляем лисенеры для имен
     * статусов, так как статусы уже другие*/
    public void setSelectedProfile(Profile selectedProfile) {
        this.selectedProfile = selectedProfile;
        addSerialStateNamesListener();
        addRepairStateNamesListener();
    }

    /**
     * Сохраняет DUnit в БД в соответствующую таблицу
     */
    public void saveDUnitToDB(DUnit unit) {
        if (unit.isSerialUnit()) dbh.addUnitToDB(unit, TABLE_SERIALS, unit.getId());
        if (unit.isRepairUnit()) dbh.addUnitToDB(unit, TABLE_REPAIRS, unit.getId());
        // В коллекцию статусов текущего устройства добавляем статус: описание+дата (добавляем
        // коллекцию в коллекцию). Если поля статуса оставить пустым, то статус не будет добавлне
        // (нужно, наример, если необходимо просто добавить серийный номер, никакого статуса в этом
        // случае быть не может)
        if (!unit.getState().equals("")) {
            Date date = new Date();
            String state = unit.getState();
            String description = unit.getDescription();
            String location = getSelectedProfile().getName();
            String unit_id = unit.getId();
            DState dState = new DState(date, state, description, location, unit_id);
            dbh.addStateToDB(dState, TABLE_ALL_STATES);
        }
    }

//    void getDUnitFromBD() {
//        dbh.getElementFromDB(SERIALS_TABLE, serialUnitsList);
//    }


//----- LISTENERS ----------------------------------------------------------------------------------

    /** Слушатель для таблицы серийных приборов*/
    void addDUnitTableListener() {
        dbh.addDBListener(TABLE_SERIALS, serialUnitsList);
    }

    /** Слушатель для таблицы ремонтных приборов*/
    void addRepairUnitTableListener() {
        dbh.addDBListener(TABLE_REPAIRS, repairsUnitsList);
    }

    /** Слушатель для таблицы имен приборов*/
    void addDevTypeTableListener() {
        dbh.addDevTypeListener(TABLE_DEV_TYPES, devTypeList);
    }

    /** Слушатель для таблицы названий статусов серийных приборов. При событии, serialStatesList
     * получает список серийных статусов или статусов общих для обоих типов. В текущей локации*/
    void addSerialStateNamesListener() {
        dbh.getListOfStates(getSelectedProfile().getLocation(), PROF_TYPE_SERIAL, serialStatesList);
    }

    /** Слушатель для таблицы названий статусов ремонтных приборов. При событии, repairStatesList
     * получает список ремонтных статусов или статусов общих для обоих типов. В текущей локации*/
    void addRepairStateNamesListener() {
        dbh.getListOfStates(getSelectedProfile().getLocation(), PROF_TYPE_REPAIR, repairStatesList);
    }

    /** Слушает изменения в коллекции статусов и при новом событии загружает статусы для выбранного
     * юнита (т.е. только те, которые принадлежат этому юниту)*/
    public void addSelectedUnitStatesListListener(DUnit unit) {
        dbh.addSelectedUnitListener(unit.getId(), unitStatesList);
    }

//--------------------------------------------------------------------------------------------------

    /**
     * Список названий статусов серийных приборов
     */
    public MutableLiveData<ArrayList<String>> getSerialStatesList() {
        return serialStatesList;
    }

    /**
     * Список названий статусов ремонтных приборов
     */
    public MutableLiveData<ArrayList<String>> getRepairStatesList() {
        return repairStatesList;
    }

    /**
     * Список имен (названий) приборов
     */
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

    public void updateSelectedUnit(DUnit newUnit) {
        ArrayList<DUnit> units = new ArrayList<>();
        units.add(newUnit);
        selectedUnits.setValue(units);
    }

    /**
     * Получить список серийных юнитов из БД по их типу и внутреннему серийнику. По-сути в БД такое
     * устройство должно быть всегда в одном экземпляре. То, что функция возвращает список,
     * сделано для проверки на дублирование. Пока в таком случае просто пишется в консоль
     * предупреждение
     */
    public void getThisUnitFromDB(DUnit unit) {
        if (unit.isSerialUnit()) dbh.getUnitById(TABLE_SERIALS, unit.getId(), selectedUnits);
        if (unit.isRepairUnit()) dbh.getUnitById(TABLE_REPAIRS, unit.getId(), selectedUnits);
        dbh.getStatesFromDB(unit.getId(), unitStatesList);
    }

    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }
}