package com.squorpikkor.app.adjustmentdb.ui.main;

import android.app.Activity;
import android.view.SurfaceView;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.squorpikkor.app.adjustmentdb.DEvent;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.ui.main.scanner.ScannerDataShow;
import com.squorpikkor.app.adjustmentdb.ui.main.scanner.Scanner;

import java.util.ArrayList;
import java.util.Date;

import io.grpc.android.BuildConfig;

import static com.squorpikkor.app.adjustmentdb.ui.main.scanner.Encrypter.decodeMe;

/**
     * Локация — это название местонахождения устройства: участок регулировки, сборки и т.д.
     * У каждого участка свой набор возможных статусов: у регулировки есть диагностика, настройка и другие,
     * при этом пользователь не может назначить для устройства статус, которого нет у текущей локации.
     * При этом для каждого из типов (серия или ремонт) может быть свой набор статусов, а может и не быть:
     * так, например, для участка монтажа и для серии, и для ремонта один и тот же доступный статус —
     * монтаж. У участка ремонта же вообще нет типа "серия" (он вообще не занимается серийными приборами)
     *
     * Статус — это как называется то, что могут делать с устройством: Диагностика, Сборка, Монтаж и т.д.
     * Могут быть двух типов: Серия и Ремонт. Также для каждого статуса есть своя локация.
     *
     * Событие (Event) — единица истории устройства. Вся история есть список событий, в каждом из которых
     * хранится
     *
     *
     * */
public class MainViewModel extends ViewModel implements ScannerDataShow {
//--------------------------------------------------------------------------------------------------
    //Новые стринги для новой БД:
    public static final String TABLE_UNITS = "units";
    public static final String UNIT_DESCRIPTION = "description";
    public static final String UNIT_DEVICE = "device_id"; //todo возможно в имени стринга и не нужен "_ID", только в значении
    public static final String UNIT_EMPLOYEE = "employee_id";
    public static final String UNIT_ID = "id";
    public static final String UNIT_INNER_SERIAL = "inner_serial";
    public static final String UNIT_LOCATION = "location_id";
    public static final String UNIT_SERIAL = "serial";
    public static final String UNIT_STATE = "state_id";
    public static final String UNIT_TYPE = "type_id";//todo по-хорошему нужна коллекция тайпов. Пока обойдусь

    public static final String TABLE_STATES = "states"; //в прошлом profile
    public static final String STATE_ID = "id";
    public static final String STATE_LOCATION = "location_id";
    public static final String STATE_NAME = "name";
    public static final String STATE_TYPE = "type_id";

    public static final String TABLE_EVENTS = "events"; //в прошлом states
    public static final String EVENT_DATE = "date";
    public static final String EVENT_DESCRIPTION = "description";
    public static final String EVENT_LOCATION = "location_id";
    public static final String EVENT_STATE = "state_id";
    public static final String EVENT_UNIT = "unit_id";

    public static final String TABLE_EMPLOYEES = "employees"; //в прошлом users
    public static final String EMPLOYEE_EMAIL = "email"; //email нельзя использовать в качестве id, так как у пользователя может поменяться email, и тогда при необходимости выбрать устройства пользователя нужно будет искать и по старому email и по новому
    public static final String EMPLOYEE_ID = "id";
    public static final String EMPLOYEE_LOCATION = "location_id";
    public static final String EMPLOYEE_NAME = "name";

    public static final String TABLE_LOCATIONS = "locations";
    public static final String LOCATION_ID = "id";
    public static final String LOCATION_NAME = "name";

    public static final String TABLE_DEVICES = "devices";
    public static final String DEVICE_ID = "id";
    public static final String DEVICE_NAME = "name";
    public static final String DEVICE_TYPE = "type";

    public static final String TYPE_ANY = "any_type";
    public static final String TYPE_REPAIR = "repair_type";
    public static final String TYPE_SERIAL = "serial_type";



    public static final String EMPTY_LOCATION_ID = "empty_location_id";
    public static final String EMPTY_LOCATION_NAME = "Локация не найдена";
//--------------------------------------------------------------------------------------------------
    public static final String SERIAL_TYPE = "serial_type";
    public static final String REPAIR_TYPE = "repair_type";

    private static final String SPLIT_SYMBOL = " ";
    private static final String REPAIR_UNIT = "Ремонт";

    private final FireDBHelper dbh;
    private final MutableLiveData<ArrayList<DUnit>> serialUnitsList;
    private final MutableLiveData<ArrayList<DUnit>> repairsUnitsList;
    private final MutableLiveData<DUnit> selectedUnit;

    private final MutableLiveData<ArrayList<String>> devicesList;

    private final MutableLiveData<ArrayList<String>> serialStateIdList;
    private final MutableLiveData<ArrayList<String>> repairStateIdList;
    private final MutableLiveData<ArrayList<String>> serialStatesNames;
    private final MutableLiveData<ArrayList<String>> repairStatesNames;

    private final MutableLiveData<ArrayList<DEvent>> unitStatesList;

    private final MutableLiveData<ArrayList<DUnit>> foundUnitsList;

    private final MutableLiveData<String> location_id;
    private final MutableLiveData<String> locationName;

    private final MutableLiveData<String> barcodeText;

    private FirebaseUser user;

    private final ArrayList<DUnit> unitList;
    Scanner singleScanner;
    Scanner multiScanner;

    public MainViewModel() {
        serialUnitsList = new MutableLiveData<>();
        repairsUnitsList = new MutableLiveData<>();
        selectedUnit = new MutableLiveData<>();
        dbh = new FireDBHelper();
        devicesList = new MutableLiveData<>();
        serialStateIdList = new MutableLiveData<>();
        repairStateIdList = new MutableLiveData<>();
        unitStatesList = new MutableLiveData<>();
        foundUnitsList = new MutableLiveData<>();
        location_id = new MutableLiveData<>();
        locationName = new MutableLiveData<>();
        barcodeText = new MutableLiveData<>();
        addDevTypeTableListener();
        unitList = new ArrayList<>();
        serialStatesNames = new MutableLiveData<>();
        repairStatesNames = new MutableLiveData<>();
    }

    /**Выбрать профиль (сборка, регулировка...). При смене профиля обновляем лисенеры для имен
     * статусов, так как статусы уже другие*/
    //todo не совсем верно, здесь выбираем не профиль(раньше профиль == локация), а список доступных статусов, или список доступных профилей(в новом понимании, дурацкое название)
    public void setSelectedProfile(String profName) {
        getLocationNameByLocationId(profName);
        addSerialStateNamesListener();
        addRepairStateNamesListener();
    }

    /** Сохраняет DUnit в БД в соответствующую таблицу*/
    public void saveDUnitToDB(DUnit unit) {
        dbh.addUnitToDB(unit);
        // В коллекцию статусов текущего устройства добавляем статус: описание+дата (добавляем
        // коллекцию в коллекцию). Если поля статуса оставить пустым, то статус не будет добавлне
        // (нужно, наример, если необходимо просто добавить серийный номер, никакого статуса в этом
        // случае быть не может)
        if (!unit.getState().equals("")) {
            Date date = new Date();
            String state = unit.getState();
            String description = unit.getDescription();
            String unit_id = unit.getId();
            String location_id = getLocation_id().getValue();
            dbh.addEventToDB(date, state, description, unit_id, location_id);
        }
    }

//----- LISTENERS ----------------------------------------------------------------------------------

    /** Слушатель для таблицы имен приборов*/
    void addDevTypeTableListener() {
        //dbh.addDevTypeListener(devicesList);
        dbh.addStringArrayListener(TABLE_DEVICES, devicesList, DEVICE_NAME);
    }

    /** Слушатель для таблицы названий статусов серийных приборов. При событии, serialStatesList
     * получает список серийных статусов или статусов общих для обоих типов. В текущей локации*/
    void addSerialStateNamesListener() {
        dbh.getListOfStates(getLocation_id().getValue(), TYPE_SERIAL, serialStateIdList, serialStatesNames);
    }

    /** Слушатель для таблицы названий статусов ремонтных приборов. При событии, repairStatesList
     * получает список ремонтных статусов или статусов общих для обоих типов. В текущей локации*/
    void addRepairStateNamesListener() {
        dbh.getListOfStates(getLocation_id().getValue(), TYPE_REPAIR, repairStateIdList, repairStatesNames);
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
    public MutableLiveData<ArrayList<String>> getSerialStateIdList() {
        return serialStateIdList;
    }

    /**
     * Список названий статусов ремонтных приборов
     */
    public MutableLiveData<ArrayList<String>> getRepairStateIdList() {
        return repairStateIdList;
    }

    public MutableLiveData<ArrayList<String>> getSerialStatesNames() {
        return serialStatesNames;
    }

    public MutableLiveData<ArrayList<String>> getRepairStatesNames() {
        return repairStatesNames;
    }

    /**
     * Список имен (названий) приборов
     */
    public MutableLiveData<ArrayList<String>> getDevicesList() {
        return devicesList;
    }

    public MutableLiveData<ArrayList<DUnit>> getSerialUnitsList() {
        return serialUnitsList;
    }

    public MutableLiveData<ArrayList<DEvent>> getUnitStatesList() {
        return unitStatesList;
    }

    public MutableLiveData<ArrayList<DUnit>> getRepairUnitsList() {
        return repairsUnitsList;
    }

    public MutableLiveData<DUnit> getSelectedUnit() {
        return selectedUnit;
    }

    public MutableLiveData<ArrayList<DUnit>> getFoundUnitsList() {
        return foundUnitsList;
    }

    public void updateSelectedUnit(DUnit newUnit) {
//        selectedUnit.postValue(newUnit);
        selectedUnit.setValue(newUnit);
    }

    /**
     * Получить список серийных юнитов из БД по их типу и внутреннему серийнику. По-сути в БД такое
     * устройство должно быть всегда в одном экземпляре. То, что функция возвращает список,
     * сделано для проверки на дублирование. Пока в таком случае просто пишется в консоль
     * предупреждение
     */
    public void getThisUnitFromDB(DUnit unit) {
        dbh.getUnitById(unit.getId(), selectedUnit);
        dbh.getEventsFromDB(unit.getId(), unitStatesList);
    }

    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public MutableLiveData<String> getLocation_id() {
        return location_id;
    }

    public MutableLiveData<String> getLocationName() {
        return locationName;
    }

    public void getLocationIdByEMail(String email) {
        dbh.getStringValueByParam(TABLE_EMPLOYEES, EMPLOYEE_EMAIL, email, EMPLOYEE_LOCATION, location_id, EMPTY_LOCATION_ID);
    }

    /***/
    private void getLocationNameByLocationId(String location_id) {
        dbh.getStringValueByParam(TABLE_LOCATIONS, LOCATION_ID, location_id, LOCATION_NAME, locationName, EMPTY_LOCATION_NAME);
    }

    public FirebaseUser getFirebaseUser() {
        return user;
    }

    public void setFirebaseUser(FirebaseUser user) {
        this.user = user;
    }

    public MutableLiveData<String> getBarcodeText() {
        return barcodeText;
    }

    public void startSingleScanner(Activity activity, SurfaceView surfaceView) {
        //todo ? if(singleScanner==null) ?
        singleScanner = new Scanner(activity, false, this, surfaceView);
    }

    public void startMultiScanner(Activity activity, SurfaceView surfaceView) {
        multiScanner = new Scanner(activity, true, this, surfaceView);
    }

    @Override
        public void addUnitToCollection(String s) {
        DUnit unit = getDUnitFromString(s);
        if (unit!=null){
            unitList.add(unit);
            getFoundUnitsList().setValue(unitList);
        }
    }

    public Scanner getSingleScanner() {
        return singleScanner;
    }

    public Scanner getMultiScanner() {
        return multiScanner;
    }

    @Override
        public void saveUnit(String s) {
        DUnit unit = getDUnitFromString(s);
        if (unit != null) {
                //Смысл в том, что если отсканированный блок есть в БД, то данные для этого блока
                // беруться из БД (getRepairUnitById), если этого блока в БД нет (новый), то данные для
                // блока берутся из QR-кода
                updateSelectedUnit(unit);
                getThisUnitFromDB(unit);
            }
        }

        @Override
        public DUnit getDUnitFromString(String s) {
            s = decodeMe(s);
            barcodeText.setValue(s);
            /////txtBarcodeValue.setVisibility(View.VISIBLE);
            String[] ar = s.split(SPLIT_SYMBOL);
            if (ar.length == 2) {
                //Для серии: имя+внутренний_серийный (БДКГ-02 1234), id = БДКГ-02_1234
                //Для ремонта: "Ремонт"+id (Ремонт 0001), id = r_0005
                String name = ar[0];
                String innerSerial = ar[1];
                String id;
                String location = getLocation_id().getValue();

                // Если это ремонт:
                if (name.equals(REPAIR_UNIT)){
                    id = "r_"+ar[1];
                    return new DUnit(id, "", "", "", "", "", REPAIR_TYPE, location);
                }
                // Если это серия:
                else{
                    id = name+"_"+innerSerial;
                    return new DUnit(id, name, innerSerial, "", "", "", SERIAL_TYPE, location);
                }

                // Если строка некорректная, возвращаю null
            } else return null;
        }
    }

