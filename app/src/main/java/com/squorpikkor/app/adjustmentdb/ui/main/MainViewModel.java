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

    public static final String SERIALS_TABLE = "serials";
    public static final String REPAIRS_TABLE = "repairs";
    public static final String TABLE_PROFILES = "profiles";
    public static final String TABLE_ALL_STATES = "states";
    public static final String DEV_TYPES_TABLE = "dev_types";

    public static final String REPAIR_STATES_TABLE = "repair_states";
    public static final String SERIAL_STATES_TABLE = "serial_states";

    public static final String ID = "id";
    public static final String NAME = "name";
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
        Profile.РЕГУЛИРОВКА.setData("Регулировка", "adjustment_states");
        Profile.ГРАДУИРОВКА.setData("Градуировка", "graduation_states");
        Profile.СБОРКА.setData("Сборка", "assembly_states");
        Profile.МОНТАЖ.setData("Монтаж", "soldering_states");
        Profile.ПРИЁМКА.setData("Приёмка", "take_to_repair_states");
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
        if (unit.isSerialUnit()) dbh.addUnitToDB(unit, SERIALS_TABLE, unit.getId());
        if (unit.isRepairUnit()) dbh.addUnitToDB(unit, REPAIRS_TABLE, unit.getId());
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

    /**
     * Слушатель для таблицы серийных приборов
     */
    void addDUnitTableListener() {
        dbh.addDBListener(SERIALS_TABLE, serialUnitsList);
    }

    /**
     * Слушатель для таблицы ремонтных приборов
     */
    void addRepairUnitTableListener() {
        dbh.addDBListener(REPAIRS_TABLE, repairsUnitsList);
    }

    /**
     * Слушатель для таблицы имен приборов
     */
    void addDevTypeTableListener() {
        dbh.addDevTypeListener(DEV_TYPES_TABLE, devTypeList);
    }

    /**
     * Слушатель для таблицы названий статусов серийных приборов
     * db -> serial_states -> <name> -> name:
     * Имя таблицы из которой будет браться список профилей выбирается в зависимости от выбранного профиля
     *
     * Если выбран профиль МОНТАЖ или СБОРКА или ГРАДУИРОВКА, то в serialStatesList загружаем данные из таблицы REPAIR_STATES_TABLE (а не SERIAL_STATES_TABLE)
     * Для монтажа для серии нет отдельной таблицы (и серия и ремонт — одинаковые статусы), поэтому нет смысла хранить в БД два одинаковых списка,
     * хранится один и загружается один и тот же в оба списка
     */
    void addSerialStateNamesListener() {
        if (    getSelectedProfile()==Profile.МОНТАЖ ||
                getSelectedProfile()==Profile.ГРАДУИРОВКА ||
                getSelectedProfile()==Profile.СБОРКА) dbh.addStringArrayListener(TABLE_PROFILES, getSelectedProfile().getDocumentName(), REPAIR_STATES_TABLE, serialStatesList, NAME);
        dbh.addStringArrayListener(TABLE_PROFILES, getSelectedProfile().getDocumentName(), SERIAL_STATES_TABLE, serialStatesList, NAME);
    }

    /**
     * Слушатель для таблицы названий статусов ремонтных приборов
     * db -> repair_states -> <name> -> name:
     */
    void addRepairStateNamesListener() {
        dbh.addStringArrayListener(TABLE_PROFILES, getSelectedProfile().getDocumentName(), REPAIR_STATES_TABLE, repairStatesList, NAME);
    }

    /**Слушает изменения в коллекции статусов и при новом событии загружает статусы для выбранного
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
        if (unit.isSerialUnit()) dbh.getUnitById(SERIALS_TABLE, unit.getId(), selectedUnits);
        if (unit.isRepairUnit()) dbh.getUnitById(REPAIRS_TABLE, unit.getId(), selectedUnits);
        dbh.getStatesFromDB(unit.getId(), unitStatesList);
    }

    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }
}