package com.squorpikkor.app.adjustmentdb.ui.main;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squorpikkor.app.adjustmentdb.DEvent;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.R;
import com.squorpikkor.app.adjustmentdb.SaveLoad;
import com.squorpikkor.app.adjustmentdb.app.App;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Device;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.DeviceSet;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Employee;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Entity;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Location;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.State;
import com.squorpikkor.app.adjustmentdb.ui.main.scanner.ScannerDataShow;
import com.squorpikkor.app.adjustmentdb.ui.main.scanner.Scanner;
import java.util.ArrayList;
import io.grpc.android.BuildConfig;

import static com.squorpikkor.app.adjustmentdb.Constant.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_INFO_FRAGMENT;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_MULTI;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_MULTI_STATES;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_SEARCH;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_SEARCH_WHEN_FOUND;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_SINGLE;
import static com.squorpikkor.app.adjustmentdb.Constant.BACK_PRESS_STATES;
import static com.squorpikkor.app.adjustmentdb.Constant.EMPTY_LOCATION_ID;
import static com.squorpikkor.app.adjustmentdb.Constant.EMPTY_LOCATION_NAME;
import static com.squorpikkor.app.adjustmentdb.Constant.EMPTY_LOCATION_NAME_2;
import static com.squorpikkor.app.adjustmentdb.Constant.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.Constant.REPAIR_UNIT;
import static com.squorpikkor.app.adjustmentdb.Constant.SERIAL_TYPE;
import static com.squorpikkor.app.adjustmentdb.Constant.SPLIT_SYMBOL;
import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
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
 *
 *
 *
 *
 *
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

    private final FireDBHelper dbh;
    ///private Bridge bridge;

    private final MutableLiveData<DUnit>                selectedUnit;//todo из трёх должен остаться только scannerFoundUnitsList
    private final MutableLiveData<ArrayList<DEvent>>    unitStatesList;
    private final MutableLiveData<ArrayList<DUnit>>     scannerFoundUnitsList;

    private final MutableLiveData<String>   location_id;
    private final MutableLiveData<String>   locationName;
    private final MutableLiveData<Drawable> userImage;
    private final MutableLiveData<Boolean>  startExit;
    private final MutableLiveData<Boolean>  goToSearchTab;
    private final MutableLiveData<Boolean>  restartScanning;
    private final MutableLiveData<Boolean>  restartMultiScanning;
    private final MutableLiveData<Boolean>  backToRecycler;
    private final MutableLiveData<Boolean>  isWrongQR;
    private final MutableLiveData<Boolean>  shouldOpenDialog;
    private final MutableLiveData<String>   email;
    private Scanner singleScanner;
    private Scanner multiScanner;
    private MutableLiveData<ArrayList<Location>>  locations;
    private MutableLiveData<ArrayList<Device>>    devices;
    private MutableLiveData<ArrayList<Employee>>  employees;
    private MutableLiveData<ArrayList<State>>     states;
    private MutableLiveData<ArrayList<DeviceSet>> deviceSets;
    /**Юниты, которые были найдены поиском по БД по параметрам*/
    private MutableLiveData<ArrayList<DUnit>> foundUnitsList;
    private MutableLiveData<Boolean> canWork;

    private MutableLiveData<Boolean> showSurface;
//--------------------------------------------------------------------------------------------------
    public MutableLiveData<ArrayList<Location>>     getLocations() {
        return locations;
    }
    public MutableLiveData<ArrayList<Device>>       getDevices() {
        return devices;
    }
    public MutableLiveData<ArrayList<Employee>>     getEmployees() {
        return employees;
    }
    public MutableLiveData<ArrayList<State>>        getStates() {
        return states;
    }
    public MutableLiveData<ArrayList<DeviceSet>>    getDeviceSets() {
        return deviceSets;
    }
    public MutableLiveData<ArrayList<DEvent>>       getUnitStatesList() {
        return unitStatesList;
    }
    public MutableLiveData<DUnit>                   getSelectedUnit() {
        return selectedUnit;
    }
    public MutableLiveData<ArrayList<DUnit>>        getScannerFoundUnitsList() {
        return scannerFoundUnitsList;
    }
    public MutableLiveData<Drawable>                getUserImage() {
        return userImage;
    }
    public MutableLiveData<String>                  getLocation_id() {
        return location_id;
    }
    public MutableLiveData<String>                  getLocationName() {
        return locationName;
    }
    public MutableLiveData<Boolean>                 getIsWrongQR() {
        return isWrongQR;
    }
    public MutableLiveData<Boolean>                 getShouldOpenDialog() {
        return shouldOpenDialog;
    }
    public MutableLiveData<String>                  getEmail() {
        return email;
    }
    public MutableLiveData<ArrayList<DUnit>>        getFoundUnitsList() {
        return foundUnitsList;
    }
    public MutableLiveData<Boolean>                 getCanWork() {
        return canWork;
    }
    public MutableLiveData<Boolean>                 getShowSurface() {
        return showSurface;
    }
//--------------------------------------------------------------------------------------------------
    public MainViewModel() {
        dbh = new FireDBHelper();
        locations = new MutableLiveData<>();
        devices = new MutableLiveData<>();
        employees = new MutableLiveData<>();
        states = new MutableLiveData<>();
        deviceSets = new MutableLiveData<>();
        foundUnitsList = new MutableLiveData<>();
        foundUnitsList = new MutableLiveData<>();
        selectedUnit = new MutableLiveData<>();
        unitStatesList = new MutableLiveData<>();
        scannerFoundUnitsList = new MutableLiveData<>();
        location_id = new MutableLiveData<>();
        locationName = new MutableLiveData<>();
        userImage = new MutableLiveData<>();
        startExit = new MutableLiveData<>();
        goToSearchTab = new MutableLiveData<>();
        restartScanning = new MutableLiveData<>();
        restartMultiScanning = new MutableLiveData<>();
        backToRecycler = new MutableLiveData<>();
        backToRecycler.setValue(false);
        isWrongQR = new MutableLiveData<>();
        shouldOpenDialog = new MutableLiveData<>(false);
        canWork = new MutableLiveData<>();
        canWork.setValue(false);
        email = new MutableLiveData<>();
        canWork.observeForever(this::doListen);
        dbh.employeeListener(employees);
        showSurface = new MutableLiveData<>(true);

        ///bridge = new Bridge();
    }
//--------------------------------------------------------------------------------------------------
    public void removeListeners() {
        locations.setValue(null);
        devices.setValue(null);
        states.setValue(null);
        deviceSets.setValue(null);
    }

    public void addListeners() {
//        dbh.locationListener(locations);
        dbh.deviceListener(devices);
        dbh.stateListener(states);
        dbh.deviceSetListener(deviceSets);
    }
//--------------------------------------------------------------------------------------------------
    public void updateUserImage(Drawable img) {
        userImage.setValue(img);
    }
    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public void startSingleScanner(Activity activity, SurfaceView surfaceView) {
        singleScanner = new Scanner(activity, false, this, surfaceView);
    }

    public void startMultiScanner(Activity activity, SurfaceView surfaceView) {
        multiScanner = new Scanner(activity, true, this, surfaceView);
    }
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

    private String getNameIdByNamePrivate(ArrayList<? extends Entity> list, String name) {
        if (list==null||list.size()==0||name==null||name.equals("")) return name;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(name)) return list.get(i).getNameId();
        }
        return name;
    }

    private ArrayList<String> getNameIds(ArrayList<? extends Entity> list) {
        if (list==null||list.size()==0)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            newList.add(list.get(i).getNameId());
        }
        return newList;
    }


    /**Из списка локаций выбирает список их имен*/
    public ArrayList<String> getLocationNames() {
        return getNames(locations.getValue());
    }
    public ArrayList<String> getDeviceNames() {
        return getNames(devices.getValue());
    }
    public ArrayList<String> getDeviceNamesRuAndEn() {
        if (devices.getValue()==null||devices.getValue().size()==0)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        for (int i = 0; i < devices.getValue().size(); i++) {
            if (devices.getValue().get(i).getName()!=null) newList.add(devices.getValue().get(i).getName());
            if (devices.getValue().get(i).getEngName()!=null) newList.add(devices.getValue().get(i).getEngName());
        }
        return newList;
    }
    public ArrayList<String> getEmployeeNames() {
        return getNames(employees.getValue());
    }
    public ArrayList<String> getStateNames() {
        return getNames(states.getValue());
    }

    //todo вообще можно сделать частью DEvent (event.getName(mViewModel)) надо подумать
    public String getLocationNameById(String id) {
        if (id.equals(EMPTY_LOCATION_ID)) return EMPTY_LOCATION_NAME_2;
        return getNameByIdPrivate(locations.getValue(), id);
    }
    public String getDeviceNameById(String id) {
        return getNameByIdPrivate(devices.getValue(), id);
    }
    public String getDeviceImageByDevId(String id) {//todo походу надо device делать частью unit
        if (devices==null||devices.getValue()==null||devices.getValue().size()==0||id==null||id.equals("")) return null;
        for (int i = 0; i < devices.getValue().size(); i++) {
            if (devices.getValue().get(i).getId().equals(id)) return devices.getValue().get(i).getImgPath();
        }
        return null;
    }
    public String getDeviceNameIdByName(String name) {
        return getNameIdByNamePrivate(devices.getValue(), name);
    }
    public String getDeviceNameId(String name) {
        //todo упростить (devices.getValue()->list)
        ArrayList<Device> list = devices.getValue();
        if (list==null||list.size()==0||name==null||name.equals("")) return name;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(name)
                    ||list.get(i).getEngName().equals(name)) return list.get(i).getNameId();
        }
        return name;
    }

    public String getEmployeeNameById(String id) {
        return getNameByIdPrivate(employees.getValue(), id);
    }
    public String getStateNameById(String id) {
        return getNameByIdPrivate(states.getValue(), id);
    }

    public void setLocationByEmail(String email) {
        Log.e(TAG, "setLocationByEmail: "+email);
        ArrayList<Employee> list = employees.getValue();
        if (!(list == null || list.size() == 0 || email == null || email.equals(""))) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getEMail().equals(email)){
                    String id = list.get(i).getLocation();
                    location_id.setValue(id);
                    locationName.setValue(getLocationNameById(id));
                    return;
                }
            }
        }
        Log.e(TAG, "setLocationByEmail: НЕ НАЙДЕНО!");
        location_id.setValue(EMPTY_LOCATION_ID);
        locationName.setValue(getLocationNameById(EMPTY_LOCATION_NAME));
    }



    public void checkUserEmail(String email) {
        dbh.checkUser(email, canWork, locations);//todo здесь будет переводчик (Bridge)
    }


//----------------------------------------------------



    private void doListen(Boolean aBoolean) {
        Log.e(TAG, "---------doListen: "+aBoolean);
        if (aBoolean) {
            addListeners();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            setLocationByEmail(user.getEmail());
            email.setValue(user.getEmail());
            DrawableTask task = new DrawableTask(this);
            if (user.getPhotoUrl() == null) {
                updateUserImage(ContextCompat.getDrawable(App.getContext(), R.mipmap.logo));
            } else task.execute(user.getPhotoUrl().toString());
        } else {
            removeListeners();
            setLocationByEmail(null);
            updateUserImage(ContextCompat.getDrawable(App.getContext(), R.mipmap.logo));
            email.setValue("- - -");
        }
    }

    public void updateEvent(String event_id) {
        dbh.updateEvent(event_id); // если создан новый ивент, то старый закрываем
    }

    /**Сохраняет выбранный юнит и его последнее событие*/
    public void saveUnitAndEvent(DUnit unit) {
        Log.e(TAG, "saveUnitAndEvent: СОХРАНЕНИЕ");
        dbh.addUnitToDB(unit);
        dbh.addEventToDB(unit.getLastEvent());
    }

    /**
     * Слушает изменения в коллекции статусов и при новом событии загружает статусы для выбранного
     * юнита (т.е. только те, которые принадлежат этому юниту)*/
    public void addSelectedUnitStatesListListener(String unit_id) {
        dbh.addSelectedUnitStatesListener(unit_id, unitStatesList);
    }

    /**Отслеживает изменения в юните найденном синглсканером. При изменении в юните автоматом
     * подгружает этот юнит вместе с событием и обновляет этот юнит в selectedUnit.*/
    public void addSelectedUnitListener(String unit_id) {
        dbh.listenerForUnitWithLastEvent(unit_id, selectedUnit);
    }

    /**Отслеживает изменения в юнитах из списка уже найденных мультисканером устройств. При
     * изменении в конкретном юните автоматом подгружает этот конкретный юнит вместе с событием и
     * обновляет этот юнит в списке найденных scannerFoundUnitsList.*/
    public void addMultiScanUnitListener() {
        dbh.listenerForMultiScanUnitWithLastEvent(scannerFoundUnitsList);
    }



    //--------------- BACK PRESS -----------------------------------------------------------------------

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

    private String backPressCommand;

    public void setBackPressCommand(String backPressCommand) {
        this.backPressCommand = backPressCommand;
    }

    public String getBackPressCommand() {
        return backPressCommand;
    }

    public void getBack() {
        if (backPressCommand==null) return;

        startExit.setValue(false);
        goToSearchTab.setValue(false);
        restartScanning.setValue(false);
        backToRecycler.setValue(false);

        switch (backPressCommand) {
            case BACK_PRESS_SEARCH_WHEN_FOUND: if (getFoundUnitsList().getValue().size()!=0) getFoundUnitsList().setValue(new ArrayList<>()); backPressCommand = BACK_PRESS_SEARCH; break;
            case BACK_PRESS_SEARCH:
            case BACK_PRESS_MULTI:  startExit.setValue(true); break;
            case BACK_PRESS_SINGLE: goToSearchTab.setValue(true); break;
            case BACK_PRESS_STATES: restartScanning.setValue(true); break;
            case BACK_PRESS_MULTI_STATES: restartMultiScanning(); break;
            case BACK_PRESS_INFO_FRAGMENT: backToRecycler.setValue(true); break;
        }
    }
//--------------------------------------------------------------------------------------------------

    public void startSearch(String deviceNameId, String locationId, String employeeId, String typeId, String stateId, String devSet, String serial) {
        Log.e(TAG, "♦ deviceName - "+deviceNameId+" location - "+locationId+" employee - "+employeeId+" type - "+typeId);
        //Если поле серийного номера пустое, то ищем по параметрам; если поле содержит значение, то ищем по этому значению, игнорируя
        // все остальные параметры. Т.е. ищем или по параметрам, или по серийному номеру
        if (serial.equals("")) dbh.getUnitList(foundUnitsList, deviceNameId, locationId, employeeId, typeId, stateId, devSet, ANY_VALUE);
        else dbh.getUnitList(foundUnitsList, ANY_VALUE, ANY_VALUE, ANY_VALUE, ANY_VALUE, ANY_VALUE, ANY_VALUE, serial);
    }

    public void restartMultiScanning() {
        scannerFoundUnitsList.setValue(new ArrayList<>());
        restartMultiScanning.postValue(true);
        multiScanner.clearFoundedBarcodes();
    }

    public void restartSingleScanning() {
        showSurface.setValue(true);
        singleScanner.initialiseDetectorsAndSources();
        singleScanner.clearFoundedBarcodes();
        setBackPressCommand(BACK_PRESS_SINGLE);
        selectedUnit.setValue(null);
        shouldOpenDialog.setValue(false);
    }

    /**Получить список всех событий для выбранного юнита*/
    public void getEventsForThisUnit(String unit_id) {
        dbh.getEventsFromDB(unit_id, unitStatesList);
    }

    /**Результат распознования QR-кода (Стринг) для МУЛЬТИСКАНА передается в этот метод. Из строки получает юнит и добавляет его в scannerFoundUnitsList*/
    @Override
    public void addUnitToCollection(String s) {
        DUnit unit = getDUnitFromString(s);
        if (unit != null) {
            if (scannerFoundUnitsList.getValue() == null) scannerFoundUnitsList.setValue(new ArrayList<>());
            scannerFoundUnitsList.getValue().add(unit);
            scannerFoundUnitsList.setValue(scannerFoundUnitsList.getValue());//update
            addMultiScanUnitListener();
        } else sayWrongQr();
    }

    public Scanner getSingleScanner() {
        return singleScanner;
    }

    public Scanner getMultiScanner() {
        return multiScanner;
    }

    /**Результат распознования QR-кода (Стринг) для СИНГЛСКАНА передается в этот метод. Из строки получает юнит*/
    @Override
    public void saveUnit(String s) {
        DUnit unit = getDUnitFromString(s);
        if (unit != null) {
            //Смысл в том, что если отсканированный блок есть в БД, то данные для этого блока
            // беруться из БД (getRepairUnitById), если этого блока в БД нет (новый), то данные для
            // блока берутся из QR-кода
            selectedUnit.setValue(unit);
            addSelectedUnitListener(unit.getId());
            getEventsForThisUnit(unit.getId());
            showStateDialog();
        } else sayWrongQr();

    }

    /**Открытие диалога статусов сразу после успешного распознания QR-кода (и вставки данных в диалог)
     * Если пользователь установил такое правило в настройках*/
    private void showStateDialog() {
        boolean savedValue = SaveLoad.getPrefBoolean(R.string.auto_start_dialog);
        if (savedValue) shouldOpenDialog.setValue(true);
    }

    private void sayWrongQr() {
        isWrongQR.setValue(true);
    }

    @Override
    public DUnit getDUnitFromString(String s) {
        Log.e(TAG, "до ******** "+s);
        s = decodeMe(s);
        Log.e(TAG, "после ***** "+s);
        String[] ar = s.split(SPLIT_SYMBOL);
        if (ar.length == 2) {
            //Для серии: имя+внутренний_серийный (БДКГ-02 1234), id = БДКГ-02_1234
            //Для ремонта: "Ремонт"+id (Ремонт 0001), id = r_0005
            String name = ar[0];//это device_id
            String innerSerial = ar[1];
            String id;

            // Если это ремонт:
            if (name.equals(REPAIR_UNIT)) {
                id = "r_" + ar[1];
                return new DUnit(id, "", "", "", REPAIR_TYPE);
            }
            // Если это серия:
            else {
                if (devices.getValue()!=null && idInList(name, devices.getValue())) {
                    id = name + "_" + innerSerial;
                    return new DUnit(id, name, innerSerial, "", SERIAL_TYPE);
                } else return null;
            }

            // Если строка некорректная, возвращаю null
        } else return null;
    }

    private boolean idInList(String name, ArrayList<? extends Entity> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getNameId().equals(name)) return true;
        }
        return false;
    }
}

