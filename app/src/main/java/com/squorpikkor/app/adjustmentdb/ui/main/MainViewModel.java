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
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Device;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Employee;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Entity;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Location;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.State;
import com.squorpikkor.app.adjustmentdb.ui.main.scanner.ScannerDataShow;
import com.squorpikkor.app.adjustmentdb.ui.main.scanner.Scanner;

import java.util.ArrayList;
import java.util.Date;

import io.grpc.android.BuildConfig;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.Utils.getIdByName;
import static com.squorpikkor.app.adjustmentdb.ui.main.scanner.Encrypter.decodeMe;

/**
 * Принцип хранения/загрузки данных
 * 1. Данные загружаются из БД; сущности (локация, статус, сотрудник, устройство) не имеют имен, только идентификаторы имени
 * 2. Сами имена для всех сущностей хранятся в отдельной таблице "names", у каждого имени есть варианты на других языках
 * (исключая имена устройств — для них только варианты на русском и английском)
 * 3. В приложении есть соответствующие массивы объектов для каждого вида сущностей: locations, states, employees, devices.
 * В объекте хранятся и имена, и их идентификаторы (и ещё разные данные)
 * 4. Эти массивы заполняются только при сработке соответствующих лисенеров, каждый из которых отслеживает изменения в
 * соответствующей сущности таблице в БД ("devices", "locations", "employees", "states"). Таким образов данные в массивы
 * загружаются из БД только при изменении данных (лисенер для локаций срабатывает только при изменениях в таблице "locations",
 * на другие не обращает внимания) или при старте приложения — загрузке страницы (срабатывают все лисенеры)
 * 4. Массивы играют роль словарей и источников данных, из них формируются спиннеры, с их помощью переводятся идентификаторы
 * в имена и обратно, это всё происходит БЕЗ обращения в БД
 * 5. Для заполнения спинеров данными, получения идентификаторов по выбранным пунктам и др, осуществляется через SpinnerAdapter
 * 6. В load методах в массивы загружаются объекты с данными из таблицы, в самих методах используется квази JOIN, чтобы
 * после получения идентификаторов имен сразу же получить из таблицы "names" имена на нужном языке
 * 7. При загрузке юнитов и событий JOIN уже не нужен, данные для имен берутся через метод mViewModel.getLocationMameById(id)
 * */


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
public static final String TABLE_NAMES = "names";


    //Новые стринги для новой БД:
    public static final String TABLE_UNITS = "units";
    public static final String UNIT_DATE = "date";
    public static final String UNIT_DESCRIPTION = "description";
    public static final String UNIT_DEVICE = "device_id"; //todo возможно в имени стринга и не нужен "_ID", только в значении
    public static final String UNIT_EMPLOYEE = "employee_id";
    public static final String UNIT_ID = "id";
    public static final String UNIT_EVENT_ID = "event_id";
    public static final String UNIT_INNER_SERIAL = "inner_serial";
    public static final String UNIT_LOCATION = "location_id";
    public static final String UNIT_SERIAL = "serial";
    public static final String UNIT_STATE = "state_id";
    public static final String UNIT_TYPE = "type_id";//todo по-хорошему нужна коллекция тайпов. Пока обойдусь

    public static final String TABLE_STATES = "states"; //в прошлом profile
    public static final String STATE_ID = "id";
    public static final String STATE_NAME_ID = "name_id";
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
    public static final String EMPLOYEE_NAME_ID = "name_id";
    public static final String EMPLOYEE_LOCATION = "location_id";
    public static final String EMPLOYEE_NAME = "name";

    public static final String TABLE_LOCATIONS = "locations";
    public static final String LOCATION_ID = "id";
    public static final String LOCATION_NAME_ID = "name_id";
    public static final String LOCATION_NAME = "name";//deprecated

    public static final String TABLE_DEVICES = "devices";
    public static final String DEVICE_ID = "id";
    public static final String DEVICE_NAME_ID = "name_id";
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
    private final MutableLiveData<DUnit> selectedUnit;

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

//----------------------------------------------------
    //Для новой архитектуры

    private ArrayList<String> getNames(ArrayList<? extends Entity> list) {
        if (list==null||list.size()==0)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            newList.add(list.get(i).getName());
        }
        return newList;
    }

    private String getNameByIdPrivate(ArrayList<? extends Entity> list, String id) {
        if (list==null||list.size()==0||id==null||id.equals("")) return id;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) return list.get(i).getName();
        }
        return id;
    }

    private ArrayList<String> getNameIds(ArrayList<? extends Entity> list) {
        if (list==null||list.size()==0)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            newList.add(list.get(i).getNameId());
        }
        return newList;
    }

    //todo 1. главный вопрос: всё таки грузить отдельно статусы, локации, сотрудники, устройства
    // в каждый отдельный лист или сразу качать весь "names"?

    MutableLiveData<ArrayList<Location>> locations;
    MutableLiveData<ArrayList<Device>> devices;
    MutableLiveData<ArrayList<Employee>> employees;
    MutableLiveData<ArrayList<State>> states;

    MutableLiveData<ArrayList<DUnit>> foundUnitsList;

    public MutableLiveData<ArrayList<Location>> getLocations() {
        return locations;
    }
    public MutableLiveData<ArrayList<Device>> getDevices() {
        return devices;
    }
    public MutableLiveData<ArrayList<Employee>> getEmployees() {
        return employees;
    }
    public MutableLiveData<ArrayList<State>> getStates() {
        return states;
    }


    /**Из списка локаций выбирает список их имен*/
    public ArrayList<String> getLocationNames() {
        return getNames(locations.getValue());
    }
    public ArrayList<String> getDeviceNames() {
        return getNames(devices.getValue());
    }
    public ArrayList<String> getEmployeeNames() {
        return getNames(employees.getValue());
    }
    public ArrayList<String> getStateNames() {
        return getNames(states.getValue());
    }

    //todo вообще можно сделать частью DEvent (event.getName(mViewModel)) надо подумать
    public String getLocationNameById(String id) {
        return getNameByIdPrivate(locations.getValue(), id);
    }
    public String getDeviceNameById(String id) {
        return getNameByIdPrivate(devices.getValue(), id);
    }
    public String getEmployeeNameById(String id) {
        return getNameByIdPrivate(employees.getValue(), id);
    }
    public String getStateNameById(String id) {
        return getNameByIdPrivate(states.getValue(), id);
    }

    public MutableLiveData<ArrayList<DUnit>> getFoundUnitsList() {
        return foundUnitsList;
    }

    void addListeners() {
        dbh.locationListener(locations);
        dbh.deviceListener(devices);
        dbh.employeeListener(employees);
        dbh.stateListener(states);
    }
//----------------------------------------------------

    public MainViewModel() {
        dbh = new FireDBHelper();

        locations = new MutableLiveData<>();
        devices = new MutableLiveData<>();
        employees = new MutableLiveData<>();
        states = new MutableLiveData<>();

        foundUnitsList = new MutableLiveData<>();
        foundUnitsList = new MutableLiveData<>();
        addListeners();
        selectedUnit = new MutableLiveData<>();
        unitStatesList = new MutableLiveData<>();
        scannerFoundUnitsList = new MutableLiveData<>();
        location_id = new MutableLiveData<>();
        locationName = new MutableLiveData<>();
        barcodeText = new MutableLiveData<>();
        email = new MutableLiveData<>();
        userImage = new MutableLiveData<>();
        startExit = new MutableLiveData<>();
        goToSearchTab = new MutableLiveData<>();
        restartScanning = new MutableLiveData<>();
        restartMultiScanning = new MutableLiveData<>();
        lastEvent = new MutableLiveData<>();
    }

    /**
     * Выбрать профиль (сборка, регулировка...). При смене профиля обновляем лисенеры для имен
     * статусов, так как статусы уже другие
     */
    //todo не совсем верно, здесь выбираем не профиль(раньше профиль == локация), а список доступных статусов, или список доступных профилей(в новом понимании, дурацкое название)
    public void setStatesForLocation(String locationId) {
        getLocationNameByLocationId(locationId);
    }

    public void closeEvent(String event_id) {
        dbh.closeEvent(event_id); // если создан новый ивент, то старый закрываем
    }

    public void saveUnitAndEvent(DUnit unit, DEvent event) {
        dbh.addUnitToDB(unit);
        dbh.addEventToDB(event);
    }

    /**
     * Слушает изменения в коллекции статусов и при новом событии загружает статусы для выбранного
     * юнита (т.е. только те, которые принадлежат этому юниту)
     */
    public void addSelectedUnitStatesListListener(String unit_id) {
        Log.e(TAG, "addSelectedUnitStatesListListener: "+unit_id);
        dbh.addSelectedUnitStatesListener(unit_id, unitStatesList);
    }

    //todo переделать для (String unit_id, String event_id), а лучше под (DUnit unit), сделать один метод вместо 2
    public void addSelectedUnitListener(String unit_id) {
        dbh.addSelectedUnitListener(unit_id, selectedUnit);
        DEvent event = new DEvent();
        dbh.getLastEventFromDB(unit_id, event);//todo переделать: надо брать по id ивента (а не юнита), т.е. брать конкретный, а не брать все и перебирать из найденных
        lastEvent.setValue(event);
    }

    //todo переделать для (String unit_id, String event_id), а лучше под (DUnit unit), сделать один метод вместо 2
    public void addSelectedUnitListener(String unit_id, String event_id) {
        dbh.addSelectedUnitListener(unit_id, selectedUnit);
        DEvent event = new DEvent();
        dbh.getLastEventFromDB_new(event_id, event);
        lastEvent.setValue(event);
    }

//--------------------------------------------------------------------------------------------------

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

    public MutableLiveData<DEvent> getLastEvent() {
        return lastEvent;
    }

    //todo переименовать на startSearch
    public void getUnitListFromBD(String deviceNameId, String locationId, String employeeId, String typeId, String stateId, String serial) {
        Log.e(TAG, "♦ deviceName - "+deviceNameId+" location - "+locationId+" employee - "+employeeId+" type - "+typeId);
        //Если поле номера пустое, то ищем по параметрам, если поле содержит значение, то ищем по этому значению, игнорируя
        // все остальные параметры. Т.е. ищем или по параметрам, или по номеру
        if (serial.equals("")) dbh.getUnitList(foundUnitsList, deviceNameId, locationId, employeeId, typeId, stateId, ANY_VALUE);
        else dbh.getUnitList(foundUnitsList, ANY_VALUE, ANY_VALUE, ANY_VALUE, ANY_VALUE, ANY_VALUE, serial);

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

    /***/
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
        Log.e(TAG, "***STRING"+s);
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
//            addSelectedUnitListener(unit.getId());
            addSelectedUnitListener(unit.getId(), unit.getEventId());
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
                return new DUnit(id, "", "", "", REPAIR_TYPE);
            }
            // Если это серия:
            else {
                id = name + "_" + innerSerial;
                return new DUnit(id, name, innerSerial, "", SERIAL_TYPE);
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

