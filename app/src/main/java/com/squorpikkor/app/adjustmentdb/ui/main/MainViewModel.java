package com.squorpikkor.app.adjustmentdb.ui.main;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
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

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.Utils.getIdByName;
import static com.squorpikkor.app.adjustmentdb.ui.main.scanner.Encrypter.decodeMe;

/**
 * Локация — это название местонахождения устройства: участок регулировки, сборки и т.д.
 * У каждого участка свой набор возможных статусов: у регулировки есть диагностика, настройка и другие,
 * при этом пользователь не может назначить для устройства статус, которого нет у текущей локации.
 * При этом для каждого из типов (серия или ремонт) может быть свой набор статусов, а может и не быть:
 * так, например, для участка монтажа и для серии, и для ремонта один и тот же доступный статус —
 * монтаж. У участка ремонта же вообще нет типа "серия" (он вообще не занимается серийными приборами)
 * <p>
 * Статус — это как называется то, что могут делать с устройством: Диагностика, Сборка, Монтаж и т.д.
 * Могут быть двух типов: Серия и Ремонт. Также для каждого статуса есть своя локация.
 * <p>
 * Событие (Event) — единица истории устройства. Вся история есть список событий, в каждом из которых
 * хранится
 */
public class MainViewModel extends ViewModel implements ScannerDataShow {
//--------------------------------------------------------------------------------------------------
    //Новые стринги для новой БД:
    public static final String TABLE_UNITS = "units";
    public static final String UNIT_DATE = "date";
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
    public static final String EVENT_CLOSE_DATE = "close_date";
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
    public static final String REPAIR_UNIT = "Ремонт";
    public static final String SERIAL_UNIT = "Серия";
    public static final String ANY_VALUE = "any_value";
    public static final String ANY_VALUE_TEXT = "- любой -";//"- любой -"
    public static final String EMPTY_VALUE_TEXT = "- не выбран -";//"- не выбран -"
    public static final String EXTRA_POSITION = "position";
//--------------------------------------------------------------------------------------------------

    public static final String BACK_PRESS_SEARCH = "back_press_search";
    public static final String BACK_PRESS_SINGLE = "back_press_single";
    public static final String BACK_PRESS_STATES = "back_press_states";
    public static final String BACK_PRESS_MULTI_STATES = "back_press_multi_states";
    public static final String BACK_PRESS_MULTI = "back_press_multi";

    private final FireDBHelper dbh;
    private final MutableLiveData<ArrayList<DUnit>> serialUnitsList;
    private final MutableLiveData<DUnit> selectedUnit;

    private final MutableLiveData<ArrayList<String>> deviceNameList;
    private final MutableLiveData<ArrayList<String>> deviceIdList;
    private final MutableLiveData<ArrayList<String>> serialStateIdList;
    private final MutableLiveData<ArrayList<String>> repairStateIdList;
    private final MutableLiveData<ArrayList<String>> serialStatesNames;
    private final MutableLiveData<ArrayList<String>> repairStatesNames;
    private final MutableLiveData<ArrayList<String>> employeeNamesList;
    private final MutableLiveData<ArrayList<String>> employeeIdList;
    private final MutableLiveData<ArrayList<String>> locationNamesList;
    private final MutableLiveData<ArrayList<String>> locationIdList;
    private final MutableLiveData<ArrayList<String>> allStatesIdList;
    private final MutableLiveData<ArrayList<String>> allStatesNameList;

    private final MutableLiveData<ArrayList<DEvent>> unitStatesList;
    private final MutableLiveData<ArrayList<DUnit>> scannerFoundUnitsList;

    private final MutableLiveData<String> location_id;
    private final MutableLiveData<String> locationName;
    private final MutableLiveData<String> email;
    private final MutableLiveData<String> barcodeText;
    private final MutableLiveData<Drawable> userImage;
    private final MutableLiveData<Boolean> startExit;
    private final MutableLiveData<Boolean> goToSearchTab;
    private final MutableLiveData<Boolean> restartScanning;
    private final MutableLiveData<Boolean> restartMultiScanning;
    private final MutableLiveData<DEvent> lastEvent;

    private FirebaseUser user;

    Scanner singleScanner;
    Scanner multiScanner;

    private int position;

    public MainViewModel() {
        serialUnitsList = new MutableLiveData<>();
        selectedUnit = new MutableLiveData<>();
        dbh = new FireDBHelper();
        deviceNameList = new MutableLiveData<>();
        serialStateIdList = new MutableLiveData<>();
        repairStateIdList = new MutableLiveData<>();
        unitStatesList = new MutableLiveData<>();
        scannerFoundUnitsList = new MutableLiveData<>();
        location_id = new MutableLiveData<>();
        locationName = new MutableLiveData<>();
        barcodeText = new MutableLiveData<>();
        addDeviceNameListener();
        serialStatesNames = new MutableLiveData<>();
        repairStatesNames = new MutableLiveData<>();
        email = new MutableLiveData<>();
        userImage = new MutableLiveData<>();
        startExit = new MutableLiveData<>();
        goToSearchTab = new MutableLiveData<>();
        restartScanning = new MutableLiveData<>();
        restartMultiScanning = new MutableLiveData<>();
        employeeNamesList = new MutableLiveData<>();
        addEmployeeNamesListener();
        deviceIdList = new MutableLiveData<>();
        addDeviceIdListener();
        locationNamesList = new MutableLiveData<>();
        locationIdList = new MutableLiveData<>();
        addLocationNamesListener();
        addLocationIdListener();
        employeeIdList = new MutableLiveData<>();
        addEmployeeIdListener();
        allStatesIdList = new MutableLiveData<>();
        allStatesNameList = new MutableLiveData<>();
        addAllStatesListener();
        lastEvent = new MutableLiveData<>();
    }

    /**
     * Выбрать профиль (сборка, регулировка...). При смене профиля обновляем лисенеры для имен
     * статусов, так как статусы уже другие
     */
    //todo не совсем верно, здесь выбираем не профиль(раньше профиль == локация), а список доступных статусов, или список доступных профилей(в новом понимании, дурацкое название)
    public void setStatesForLocation(String locationId) {
        getLocationNameByLocationId(locationId);
        addSerialStateNamesListener();
        addRepairStateNamesListener();
    }

    /**
     * Сохраняет DUnit в БД в соответствующую таблицу
     */
    public void saveDUnitToDB(DUnit unit) {
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
            String lastEventId = lastEvent.getValue().getId();
            dbh.addEventToDB(date, state, description, unit_id, location_id);
            if (lastEventId!=null) dbh.closeEvent(lastEventId);
        }
        //Если есть новое событие, то обновляем дату/время
        if (unit.getDate()==null) unit.setDate(new Date());
        dbh.addUnitToDB(unit);
    }

    /**
     * Если статус не задан, то присваиваем старый статус (который был до этого),
     * при этом новый event не создается;
     * если статус задан, сохраняем по старой схеме (с сохранением event)
     */
    public void saveDUnitToDB(DUnit unit, String oldState) {
        if (unit.getState().equals("")) {
            unit.setState(oldState);
            dbh.addUnitToDB(unit);
        } else {
            saveDUnitToDB(unit);
        }
    }

//----- LISTENERS ----------------------------------------------------------------------------------


    //todo ВСЕ парные лисенеры (Name / Id) переделать в один лисенер, который будет отслеживать одну таблицу, а заполнять изменения в два MutableLiveData.
    // Типа так:
    void addAllStatesListener() {
        dbh.getListIdsAndListNames(TABLE_STATES, allStatesIdList, STATE_ID, allStatesNameList, STATE_NAME);
    }

    /**
     * Слушатель для таблицы имен приборов
     */
    void addDeviceNameListener() {
        dbh.addStringArrayListener(TABLE_DEVICES, deviceNameList, DEVICE_NAME);
    }

    void addDeviceIdListener() {
        dbh.addStringArrayListener(TABLE_DEVICES, deviceIdList, DEVICE_ID);
    }

    void addLocationNamesListener() {
        dbh.addStringArrayListener(TABLE_LOCATIONS, locationNamesList, LOCATION_NAME);
    }

    void addLocationIdListener() {
        dbh.addStringArrayListener(TABLE_LOCATIONS, locationIdList, LOCATION_ID);
    }

    /**
     * Слушатель для таблицы названий статусов серийных приборов. При событии, serialStatesList
     * получает список серийных статусов или статусов общих для обоих типов. В текущей локации
     */
    void addSerialStateNamesListener() {
        dbh.getListOfStates(getLocation_id().getValue(), TYPE_SERIAL, serialStateIdList, serialStatesNames);
    }

    /**
     * Слушатель для таблицы названий статусов ремонтных приборов. При событии, repairStatesList
     * получает список ремонтных статусов или статусов общих для обоих типов. В текущей локации
     */
    void addRepairStateNamesListener() {
        dbh.getListOfStates(getLocation_id().getValue(), TYPE_REPAIR, repairStateIdList, repairStatesNames);
    }

    /**
     * Слушает изменения в коллекции статусов и при новом событии загружает статусы для выбранного
     * юнита (т.е. только те, которые принадлежат этому юниту)
     */
    public void addSelectedUnitStatesListListener(String unit_id) {
        Log.e(TAG, "addSelectedUnitStatesListListener: "+unit_id);
        dbh.addSelectedUnitStatesListener(unit_id, unitStatesList);
    }

    void addEmployeeNamesListener() {
        dbh.getStringArrayFromDB(TABLE_EMPLOYEES, employeeNamesList, EMPLOYEE_NAME);
    }

    void addEmployeeIdListener() {
        dbh.getStringArrayFromDB(TABLE_EMPLOYEES, employeeIdList, EMPLOYEE_ID);
    }

    public void addSelectedUnitListener(String unit_id) {
        dbh.addSelectedUnitListener(unit_id, selectedUnit);
        DEvent event = new DEvent();
        dbh.getLastEventFromDB(unit_id, event);
        lastEvent.setValue(event);
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

    public MutableLiveData<ArrayList<DUnit>> getSerialUnitsList() {
        return serialUnitsList;
    }

    public MutableLiveData<ArrayList<DEvent>> getUnitStatesList() {
        return unitStatesList;
    }

    public MutableLiveData<DUnit> getSelectedUnit() {
        return selectedUnit;
    }

    public MutableLiveData<ArrayList<DUnit>> getScannerFoundUnitsList() {
        return scannerFoundUnitsList;
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }

    public MutableLiveData<Drawable> getUserImage() {
        return userImage;
    }

    public void updateUserImage(Drawable img) {
        userImage.setValue(img);
    }

    public MutableLiveData<Boolean> getStartExit() {
        return startExit;
    }

    public MutableLiveData<Boolean> getGoToSearchTab() {
        return goToSearchTab;
    }

    public MutableLiveData<Boolean> getRestartScanning() {
        return restartScanning;
    }

    public MutableLiveData<Boolean> getRestartMultiScanning() {
        return restartMultiScanning;
    }

    public MutableLiveData<ArrayList<String>> getEmployeeNamesList() {
        return employeeNamesList;
    }

    public MutableLiveData<ArrayList<String>> getDeviceIdList() {
        return deviceIdList;
    }

    public MutableLiveData<ArrayList<String>> getDeviceNameList() {
        return deviceNameList;
    }

    public MutableLiveData<ArrayList<String>> getLocationNamesList() {
        return locationNamesList;
    }

    public MutableLiveData<ArrayList<String>> getLocationIdList() {
        return locationIdList;
    }

    public MutableLiveData<ArrayList<String>> getEmployeeIdList() {
        return employeeIdList;
    }

    public MutableLiveData<ArrayList<String>> getAllStatesIdList() {
        return allStatesIdList;
    }

    public MutableLiveData<ArrayList<String>> getAllStatesNameList() {
        return allStatesNameList;
    }

    public MutableLiveData<DEvent> getLastEvent() {
        return lastEvent;
    }

    /**По выбранным параметрам получает из БД список юнитов*/
    public void getUnitListFromBD(String deviceName, String location, String employee, String type, String state, String serial) {
        Log.e(TAG, "♦ deviceName - "+deviceName+" location - "+location+" employee - "+employee+" type - "+type);
        //Если параметр не "any", то имя параметра переводим в его идентификатор ("Диагностика" -> "adj_r_diagnostica")
        //Если "any", то так и оставляем
        if (!deviceName.equals(ANY_VALUE)) deviceName = getIdByName(deviceName, deviceNameList.getValue(), deviceIdList.getValue());
        if (!location.equals(ANY_VALUE)) location = getIdByName(location, locationNamesList.getValue(), locationIdList.getValue());
        if (!state.equals(ANY_VALUE)) state = getIdByName(state, allStatesNameList.getValue(), allStatesIdList.getValue());
        if (!employee.equals(ANY_VALUE)) employee = getIdByName(employee, employeeNamesList.getValue(), employeeIdList.getValue());

        dbh.getUnitListByParam(serialUnitsList, UNIT_DEVICE, deviceName, UNIT_LOCATION, location, UNIT_EMPLOYEE, employee, UNIT_TYPE, type, UNIT_STATE, state, UNIT_SERIAL, serial);
    }

    public void restartMultiScanning() {
        scannerFoundUnitsList.setValue(new ArrayList<>());
        restartMultiScanning.postValue(true);
        multiScanner.clearFoundedBarcodes();
    }

    private String backPressCommand;

    public void setBackPressCommand(String backPressCommand) {
        this.backPressCommand = backPressCommand;
    }

    public void getBack() {
        if (backPressCommand.equals(BACK_PRESS_SEARCH)) startExit.setValue(true);
        if (backPressCommand.equals(BACK_PRESS_SINGLE)) goToSearchTab.setValue(true);
        if (backPressCommand.equals(BACK_PRESS_MULTI)) goToSearchTab.setValue(true);
        if (backPressCommand.equals(BACK_PRESS_STATES)) restartScanning.setValue(true);
        if (backPressCommand.equals(BACK_PRESS_MULTI_STATES)) restartMultiScanning();
    }

    public void updateSelectedUnit(DUnit newUnit) {
        selectedUnit.setValue(newUnit);
    }

    public void getEventForThisUnit(String unit_id) {
        dbh.getEventsFromDB(unit_id, unitStatesList);
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
        singleScanner = new Scanner(activity, false, this, surfaceView);
    }

    public void startMultiScanner(Activity activity, SurfaceView surfaceView) {
        Log.e(TAG, "******************************startMultiScanner: ");
        /*if (multiScanner==null)*/
        multiScanner = new Scanner(activity, true, this, surfaceView);
    }

    /**
     * Распознанный мультисканером юнит был помещене в коллекцию. Метод получает этот юнит и
     * проверяет наличие в БД. Если такой есть, то обновляет его данные. Из листа берет размер
     * листа -1, т.е. позицию этого элемента (когда он только добавлен, то он последний).
     * В момент когда данные юнита обновлены, он может быть и не последний, но его позиция сохранена
     */
    public void getThisListUnitFromDB(DUnit unit, MutableLiveData<ArrayList<DUnit>> list) {
        dbh.getUnitByIdAndAddToList(unit.getId(), list, list.getValue().size() - 1);
    }

    @Override
    public void addUnitToCollection(String s) {
        DUnit unit = getDUnitFromString(s);
        if (unit != null) {
            if (scannerFoundUnitsList.getValue() == null) scannerFoundUnitsList.setValue(new ArrayList<>());
            scannerFoundUnitsList.getValue().add(unit);
            getThisListUnitFromDB(unit, scannerFoundUnitsList);
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
            addSelectedUnitListener(unit.getId());
            getEventForThisUnit(unit.getId());
        }
    }

    public void selectUnit(String unit_id) {
        addSelectedUnitListener(unit_id);
        getEventForThisUnit(unit_id);
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
            String name = ar[0];//это device_id
            String innerSerial = ar[1];
            String id;
            String location = getLocation_id().getValue();

            // Если это ремонт:
            if (name.equals(REPAIR_UNIT)) {
                id = "r_" + ar[1];
                return new DUnit(id, "", "", "", "", "", REPAIR_TYPE, location);
            }
            // Если это серия:
            else {
                id = name + "_" + innerSerial;
                return new DUnit(id, name, innerSerial, "", "", "", SERIAL_TYPE, location);
            }

            // Если строка некорректная, возвращаю null
        } else return null;
    }

    public void setPosition(int position) {

        this.position = position;
        //selectUnit(serialUnitsList.getValue().get(position));
    }

    public int getPosition() {
        return position;
    }
}

