package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.squorpikkor.app.adjustmentdb.BuildConfig;
import com.squorpikkor.app.adjustmentdb.DState;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.DevType;
import java.util.ArrayList;
import java.util.Date;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

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
    /**Коллекция пользователей. Email + профиль*/
    public static final String TABLE_USERS = "users";
    /**Коллекция названий локаций: id+name ("adjustment"+"Регулировка")*/
    public static final String TABLE_LOCATIONS = "locations";
//--------------------------------------------------------------------------------------------------
    public static final String PROFILE_LOCATION = "location";
    public static final String PROFILE_NAME = "name";
    public static final String PROFILE_TYPE = "type";
    public static final String PROF_TYPE_ANY = "any";
    public static final String PROF_TYPE_REPAIR = "repair";
    public static final String PROF_TYPE_SERIAL = "serial";

    public static final String EMPTY_PROFILE_NAME = "empty_profile_name";
    public static final String EMPTY_LOCATION_NAME = "empty_location_name";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String UNIT_ID = "unit_id";
    public static final String DATE = "date";
    public static final String STATE = "state";
    public static final String EMAIL = "email";
    public static final String PROFILE = "profile";

    public static final String SERIAL_TYPE = "serial_type";
    public static final String REPAIR_TYPE = "repair_type";

    private final FireDBHelper dbh;
    private final MutableLiveData<ArrayList<DUnit>> serialUnitsList;
    private final MutableLiveData<ArrayList<DUnit>> repairsUnitsList;
    private final MutableLiveData<ArrayList<DUnit>> selectedUnits;

    private final MutableLiveData<ArrayList<DevType>> devTypeList;

    private final MutableLiveData<ArrayList<String>> serialStatesList;
    private final MutableLiveData<ArrayList<String>> repairStatesList;

    private final MutableLiveData<ArrayList<DState>> unitStatesList;

    private final MutableLiveData<ArrayList<DUnit>> foundUnitsList;

    private final MutableLiveData<String> profileName;
    private final MutableLiveData<String> locationName;

    private FirebaseUser user;

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
        profileName = new MutableLiveData<>();
        locationName = new MutableLiveData<>();
        addDUnitTableListener();
        addRepairUnitTableListener();
        addDevTypeTableListener();
    }

    /**Выбрать профиль (сборка, регулировка...). При смене профиля обновляем лисенеры для имен
     * статусов, так как статусы уже другие*/
    public void setSelectedProfile(String profName) {
        getNameFromDB(profName);
        addSerialStateNamesListener();
        addRepairStateNamesListener();
    }

    /** Сохраняет DUnit в БД в соответствующую таблицу*/
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
            String location = getProfileName().getValue();
            String unit_id = unit.getId();
            DState dState = new DState(date, state, description, location, unit_id);
            dbh.addStateToDB(dState, TABLE_ALL_STATES);
        }
    }

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
        dbh.getListOfStates(getProfileName().getValue(), PROF_TYPE_SERIAL, serialStatesList);
    }

    /** Слушатель для таблицы названий статусов ремонтных приборов. При событии, repairStatesList
     * получает список ремонтных статусов или статусов общих для обоих типов. В текущей локации*/
    void addRepairStateNamesListener() {
        dbh.getListOfStates(getProfileName().getValue(), PROF_TYPE_REPAIR, repairStatesList);
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
        if (repairStatesList.getValue()!=null)Log.e(TAG, "♣♣♣ getRepairStatesList: "+repairStatesList.getValue().size());
        else Log.e(TAG, "♣♣♣ NULL!!!!!!!!!! ");
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

    public MutableLiveData<String> getProfileName() {
        return profileName;
    }

    public MutableLiveData<String> getLocationName() {
        return locationName;
    }

    /**По названию почты получаем из БД профиль (если такой есть). Если такого eMail в БД нет, то
     * будет предложено создать нового юзера с текущим eMail и выбранным из списка профилем*/
    public void getProfileByEMail(String email) {
        dbh.getStringValueByParam(TABLE_USERS, EMAIL, email, PROFILE, profileName, EMPTY_PROFILE_NAME);
    }

    private void getNameFromDB(String profileName) {
        dbh.getStringValueByParam(TABLE_LOCATIONS, ID, profileName, NAME, locationName, EMPTY_LOCATION_NAME);
    }

    public FirebaseUser getFirebaseUser() {
        return user;
    }

    public void setFirebaseUser(FirebaseUser user) {
        this.user = user;
    }
}
