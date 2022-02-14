package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squorpikkor.app.adjustmentdb.DEvent;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.SaveLoad;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Device;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.DeviceSet;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Employee;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Location;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.State;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.squorpikkor.app.adjustmentdb.Constant.DEVICE_NAME_EN;
import static com.squorpikkor.app.adjustmentdb.Constant.DEVICE_NAME_RU;
import static com.squorpikkor.app.adjustmentdb.Constant.DEVICE_SET_NAME_RU;
import static com.squorpikkor.app.adjustmentdb.Constant.EMPLOYEE_NAME_RU;
import static com.squorpikkor.app.adjustmentdb.Constant.LOCATION_NAME_RU;
import static com.squorpikkor.app.adjustmentdb.Constant.STATE_NAME_RU;
import static com.squorpikkor.app.adjustmentdb.Constant.TABLE_LOCATIONS;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_LAST_DATE;
import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.Constant.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.Constant.DEVICE_DEV_SET_ID;
import static com.squorpikkor.app.adjustmentdb.Constant.DEVICE_IMG_PATH;
import static com.squorpikkor.app.adjustmentdb.Constant.EMPLOYEE_EMAIL;
import static com.squorpikkor.app.adjustmentdb.Constant.EMPLOYEE_LOCATION;
import static com.squorpikkor.app.adjustmentdb.Constant.EVENT_CLOSE_DATE;
import static com.squorpikkor.app.adjustmentdb.Constant.EVENT_DATE;
import static com.squorpikkor.app.adjustmentdb.Constant.EVENT_DESCRIPTION;
import static com.squorpikkor.app.adjustmentdb.Constant.EVENT_LOCATION;
import static com.squorpikkor.app.adjustmentdb.Constant.EVENT_STATE;
import static com.squorpikkor.app.adjustmentdb.Constant.EVENT_UNIT;
import static com.squorpikkor.app.adjustmentdb.Constant.STATE_LOCATION;
import static com.squorpikkor.app.adjustmentdb.Constant.STATE_TYPE;
import static com.squorpikkor.app.adjustmentdb.Constant.TABLE_DEVICES;
import static com.squorpikkor.app.adjustmentdb.Constant.TABLE_DEVICE_SET;
import static com.squorpikkor.app.adjustmentdb.Constant.TABLE_EMPLOYEES;
import static com.squorpikkor.app.adjustmentdb.Constant.TABLE_STATES;
import static com.squorpikkor.app.adjustmentdb.Constant.TABLE_EVENTS;
import static com.squorpikkor.app.adjustmentdb.Constant.TABLE_UNITS;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_CLOSE_DATE;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_DATE;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_DEVICE;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_DEVICE_SET;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_EMPLOYEE;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_EVENT_ID;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_ID;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_INNER_SERIAL;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_LOCATION;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_SERIAL;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_STATE;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_TRACKID;
import static com.squorpikkor.app.adjustmentdb.Constant.UNIT_TYPE;


/**Новая концепция для работы с БД. Ещё не реализована, буду постепенно делать:
 * 1. Самое главное: FireDBHelper отдает в приложение массивы (мьютабл) ОЮЪЕКТОВ — уже и с id, и с
 * именами
 * 2. Поэтому НИКАКИХ переводчиков, всё берется у объектов классов напрямую геттерами
 * 3. Так как приложение получает массивы объектов, то уже не сильно принципиально, как я буду
 * хранить имена в БД — в отдельной таблице "names" или в каждом документе коллекции, при
 * необходимости перехода с одного варианта на другой, я просто меняю метод getEntity(): либо
 * получаю имя из поля документа (document.get(LOCATION_NAME)), или докачиваю необходимое имя из
 * "names" по его id. При этом само приложение КАК РАБОТАЛО С УЖЕ ГОТОВЫМИ ОБЪЕКТАМИ, так и будет
 * продолжать работать, не подозревая, что что-то вообще изменилось. Поэтому, если вдруг придется
 * денормализировать БД для экономии запросов, в самом приложении нужно будет немного изменить
 * метод, загружающий сущности (убрать аналог JOIN)
 * 4. При переходе, вдруг, на другую БД (SQL), будет минимум гемора, просто сделать метод,
 * загружающий объекты
 *
 * ВАЖНО! Картинки для устройств хранятся (по крайней мере пока — потом посмотрим) на хостинге от
 * AdjustmentWeb, в самой базе хранятся ссылки на эти картинки. Чтобы изменить/добавить картинку
 * нужно в проекте AdjustmentWeb добавить эти картинки в папку /pics и задеплоить изменения; в БД
 * добавить ссылку на изображение (получится что-то типа https://adjustmentdb.web.app/pics/2503.png)
 * */

class FireDBHelper {

    private final FirebaseFirestore db;

    public FireDBHelper() {
        db = FirebaseFirestore.getInstance();
        casher = new Casher();
        getDataVersion();
    }

    private final Casher casher;
    public static final String APP_DB_VERSION = "app_db_version";//todo по-хорошему нужно сделать разные версии для разных таблиц, иначе при изменении данных для одной таблицы будут обновляться данные для всех таблиц. Но так сложнее администрировать, при изменении данных нужно обновлять определенную версию, а не одну общую
    private int dbVersion;

    private void getDataVersion() {
        db.collection("_settings").document("version").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc != null) {
                    Log.e(TAG, "getDataVersion: "+doc.get("value").toString());
                    dbVersion = Integer.parseInt(doc.get("value").toString());
                }
            }
        });
    }

    /**Поиск эл.почты в employees, если пользователя с такой почтой нет, возвращает false
     *
     * Временно(?) работает так:
     * 1. Загрузка location в приложении отключена
     * 2. проверяется email и если такой есть в БД, то
     * 3. добавляем его в employees
     * 3. запускается locationListener (это теперь единственное место в приложении, откуда этот листенер вообще запускается)
     * 4. после того, как загрузится последняя локация, включается canWorks.setValue(true)
     * 5. который уже всё включает (аккаунт в том числе) и загружает остальные лисенеры
     * Жуткий костыль, потом сделаю нормально
     * P.S. А может и нормально работает*/
    void checkUser(String email, MutableLiveData<Boolean> canWorks, MutableLiveData<ArrayList<Location>> locations, MutableLiveData<ArrayList<Employee>> employees) {
        db.collection(TABLE_EMPLOYEES)
                .whereEqualTo(EMPLOYEE_EMAIL, email)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot == null || querySnapshot.isEmpty()) {
                    canWorks.setValue(false);
                    Log.e(TAG, "♦НЕТ ТАКОГО ПОЛЬЗОВАТЕЛЯ!");
                } else {
                    Log.e(TAG, "♦Есть такой ПОЛЬЗОВАТЕЛЬ");
                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                    String id = document.getId();
                    String name = document.get(EMPLOYEE_NAME_RU).toString();
                    String eMail = document.get(EMPLOYEE_EMAIL).toString();
                    String location = document.get(EMPLOYEE_LOCATION).toString();
                    Employee employee = new Employee(id, name, eMail, location);
                    Log.e(TAG, "checkUser: EMPLOYEE- " + employee.getEMail()+" " +employee.getLocation());
                    employees.getValue().add(employee);
                    locationListener(locations, canWorks);

                }
            }
        });
    }

//--------------------------------------------------------------------------------------------------

    void locationListener(MutableLiveData<ArrayList<Location>> data, MutableLiveData<Boolean> canWorks) {
        int appDbVersion = SaveLoad.loadInt(APP_DB_VERSION);
        if (appDbVersion < dbVersion) {
            locationListener_(data, canWorks);
        } else {
            ArrayList<Location> newLocations = casher.getLocationCash();
            if (newLocations.size()!=0) {
                data.setValue(newLocations);//Если из кэша что-то загрузилось, возвращаем такую коллекцию,
                canWorks.setValue(true);
            } else locationListener_(data, canWorks); //иначе — грузим из БД
        }
    }

    private void locationListener_(MutableLiveData<ArrayList<Location>> data, MutableLiveData<Boolean> canWorks) {
        db.collection(TABLE_LOCATIONS).get().addOnCompleteListener(task -> {
            ArrayList<Location> newLocations = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                String id = document.getId();
                String name = document.get(LOCATION_NAME_RU).toString();
                Location location = new Location(id, name);
                newLocations.add(location);
            }
            casher.saveLocationsToCash(newLocations, dbVersion);
            data.setValue(newLocations);
            canWorks.setValue(true);

        });
    }

    /***/
    void deviceListener(MutableLiveData<ArrayList<Device>> data) {
        int appDbVersion = SaveLoad.loadInt(APP_DB_VERSION);
        if (appDbVersion < dbVersion) {
            deviceListener_(data);
        } else {
            ArrayList<Device> newDevices = casher.getDeviceCash();
            if (newDevices.size()!=0) data.setValue(newDevices);//Если из кэша что-то загрузилось, возвращаем такую коллекцию,
            else deviceListener_(data); //иначе — грузим из БД
        }
    }

    private void deviceListener_(MutableLiveData<ArrayList<Device>> data) {
        ArrayList<Device> newDevices = new ArrayList<>();
        db.collection(TABLE_DEVICES).get().addOnCompleteListener(task -> {
            for (DocumentSnapshot document : task.getResult()) {
                String id = document.getId();
                //И русские и английские имена нужны для распознавания наклеек, которые могут быть и русские и английские
                String name = document.get(DEVICE_NAME_RU).toString();
                String nameEn = document.get(DEVICE_NAME_EN).toString();
                String devSetId = document.get(DEVICE_DEV_SET_ID).toString();
                String imgPath = document.get(DEVICE_IMG_PATH).toString();
                Device device = new Device(id, name, nameEn, devSetId, imgPath);
                newDevices.add(device);
            }
            casher.saveDevicesToCash(newDevices, dbVersion);
            data.setValue(newDevices);
        });
    }

    //TODO когда более-менее будет понятно с пользователями нужно будет тоже кэшировать
    void employeeListener(MutableLiveData<ArrayList<Employee>> data) {
        db.collection(TABLE_EMPLOYEES)
                .get().addOnCompleteListener(task -> {
            ArrayList<Employee> newEmployees = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                String id = document.getId();
                String name = document.get(EMPLOYEE_NAME_RU).toString();
                String eMail = document.get(EMPLOYEE_EMAIL).toString();
                String location = document.get(EMPLOYEE_LOCATION).toString();
                Employee employee = new Employee(id, name, eMail, location);
                newEmployees.add(employee);
            }
            data.setValue(newEmployees);
        });
    }

    void stateListener(MutableLiveData<ArrayList<State>> data) {
        int appDbVersion = SaveLoad.loadInt(APP_DB_VERSION);
        if (appDbVersion < dbVersion) {
            stateListener_(data);
        } else {
            ArrayList<State> newStates = casher.getStateCash();
            if (newStates.size()!=0) data.setValue(newStates);//Если из кэша что-то загрузилось, возвращаем такую коллекцию,
            else stateListener_(data); //иначе — грузим из БД
        }
    }

    private void stateListener_(MutableLiveData<ArrayList<State>> data) {
        db.collection(TABLE_STATES).get().addOnCompleteListener(task -> {
            ArrayList<State> newStates = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                String id = document.getId();
                String name = document.get(STATE_NAME_RU).toString();
                String type = document.get(STATE_TYPE).toString();
                String location = document.get(STATE_LOCATION).toString();
                State state = new State(id, name, type, location);
                newStates.add(state);
            }
            casher.saveStatesToCash(newStates, dbVersion);
            data.setValue(newStates);
        });
    }

    void deviceSetListener(MutableLiveData<ArrayList<DeviceSet>> data) {
        int appDbVersion = SaveLoad.loadInt(APP_DB_VERSION);
        if (appDbVersion < dbVersion) {
            deviceSetListener_(data);
        } else {
            ArrayList<DeviceSet> newDeviceSets = casher.getDevSetCash();
            if (newDeviceSets.size()!=0) data.setValue(newDeviceSets);//Если из кэша что-то загрузилось, возвращаем такую коллекцию,
            else deviceSetListener_(data); //иначе — грузим из БД
        }
    }

    private void deviceSetListener_(MutableLiveData<ArrayList<DeviceSet>> data) {
        db.collection(TABLE_DEVICE_SET).get().addOnCompleteListener(task -> {
            ArrayList<DeviceSet> newDevSets = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                String id = document.getId();
                String name = document.get(DEVICE_SET_NAME_RU).toString();
                DeviceSet devSet = new DeviceSet(id, name);
                newDevSets.add(devSet);
            }
            casher.saveDeviceSetsToCash(newDevSets, dbVersion);
            data.setValue(newDevSets);
        });
    }

    /**Упрощенный вариант получения имени на русском языке по snapshot*/
    String getStringFromSnapshot(Task<DocumentSnapshot> task, String defValue) {
        if (task.isSuccessful()) {
            DocumentSnapshot documentSnapshot = task.getResult();
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Object o = documentSnapshot.get("ru");
                if (o != null && !o.toString().equals("")) {
                    return o.toString();
                }
            }
        }
        return defValue;
    }

    /**В принципе этот метод загружает вариант имени в зависимости от локации телефона, это
     * избыточный функционал, приложением и так будут пользоваться только русскоязычные
     * пользователи. Метод заменен на упрощенный вариант, этот оставил на всякий*/
    @SuppressWarnings("unused")
    String getStringFromSnapshotMultiLang(Task<DocumentSnapshot> task, String defValue) {
        if (task.isSuccessful()) {
            DocumentSnapshot documentSnapshot = task.getResult();
            String lang = Locale.getDefault().getLanguage();
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Object o = documentSnapshot.get(lang);
                Object def = documentSnapshot.get("en");
                if (o != null && !o.toString().equals("")) {
                    return o.toString();
                }
                //если нет языка телефона в БД и не равно "", то присваивается английский вариант
                else if (def != null && !def.toString().equals("")) {
                    Log.e(TAG, "нет такого языка!");
                    return def.toString();
                }
                //если и английского нет в БД и не равно "", то оставляется идентификатор. он и будет отображаться в
                else {
                    Log.e(TAG, "и английского нет!");
                    return defValue;
                }
            } else {
                Log.e(TAG, "нет такого id!");
                return defValue;
            }
        }
        return defValue;
    }

//--------------------------------------------------------------------------------------------------

    /**Добавляет документ в БД. Если документ не существует, он будет создан. Если документ существует,
     * его содержимое будет перезаписано вновь предоставленными данными
     *
     * Location и State не являются полями юнита и беруться из ивента. Сохранение этих полей в юнит
     * нужно для поиска юнитов по этим значениям (локация и статус). При загрузке из БД эти поля
     * игнорируются, таким образом эти 2 поля существуют только внутри БД*/
    //todo переделать на update, если документ существует
    void addUnitToDB(DUnit unit) {
        Map<String, Object> data = new HashMap<>();
        data.put(UNIT_DEVICE, unit.getName());
        data.put(UNIT_EMPLOYEE, unit.getEmployee());
        data.put(UNIT_ID, unit.getId());
        data.put(UNIT_EVENT_ID, unit.getEventId());
        data.put(UNIT_INNER_SERIAL, unit.getInnerSerial());
        data.put(UNIT_SERIAL, unit.getSerial());
        data.put(UNIT_TYPE, unit.getType());
        data.put(UNIT_DATE, unit.getDate());
        data.put(UNIT_DEVICE_SET, unit.getDeviceSet());
        if (unit.getCloseDate()!=null) data.put(UNIT_CLOSE_DATE, unit.getCloseDate());
        if (unit.getLastEvent()!=null) data.put(UNIT_LOCATION, unit.getLastEvent().getLocation());
        if (unit.getLastEvent()!=null) data.put(UNIT_STATE, unit.getLastEvent().getState());
        data.put(UNIT_LAST_DATE, unit.getLastDate());
        data.put(UNIT_TRACKID, unit.getTrackId());
        db.collection(TABLE_UNITS)
                .document(unit.getId())
//                .update(data)
                .set(data)
                .addOnSuccessListener(aVoid -> Log.e(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.e(TAG, "Error writing document", e));
    }

    /**Добавление нового события в БД*/
    void addEventToDB(DEvent event) {
        if (event == null) return;//если нового статуса не было и новый ивент == null, то dbh.addEventToDB его проигнорирует и этот ивент добавляться в БД не будет
        Map<String, Object> data = new HashMap<>();
        data.put(EVENT_DATE, new Date());
        if (event.getCloseDate()!=null) data.put(EVENT_CLOSE_DATE, event.getCloseDate());
        data.put(EVENT_STATE, event.getState());
        data.put(EVENT_DESCRIPTION, event.getDescription());
        data.put(EVENT_UNIT, event.getUnit_id());
        data.put(EVENT_LOCATION, event.getLocation());
        db.collection(TABLE_EVENTS)
                .document(event.getId())
                .set(data)
                .addOnSuccessListener(aVoid -> Log.e(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.e(TAG, "Error writing document", e));
    }

    /**Добавляет к ивенту дату закрытия события. Дата закрытия — это сегодняшняя дата*/
    void updateEvent(String event_id) {
        if (event_id==null) return;
        db.collection(TABLE_EVENTS).document(event_id).update(EVENT_CLOSE_DATE, new Date());
    }

    /**Обертка для getUnitListByParam*/
    //todo добавить поиск по комплекту
    void getUnitList(MutableLiveData<ArrayList<DUnit>> unitList, String deviceNameId, String locationId, String employeeId, String typeId, String stateId, String devSet, String serial) {
        getUnitListByParam(unitList,
                UNIT_DEVICE, deviceNameId,
                UNIT_LOCATION, locationId,
                UNIT_EMPLOYEE, employeeId,
                UNIT_TYPE, typeId,
                UNIT_STATE, stateId,
                UNIT_DEVICE_SET, devSet,
                UNIT_SERIAL, serial);
    }

    void getUnitListByParam(MutableLiveData<ArrayList<DUnit>> unitList, String param1, String value1, String param2, String value2, String param3, String value3, String param4, String value4, String param5, String value5, String param6, String value6,  String param7, String value7) {
        Query query = db.collection(TABLE_UNITS);
        if (!value1.equals(ANY_VALUE)) query = query.whereEqualTo(param1, value1);
        if (!value2.equals(ANY_VALUE)) query = query.whereEqualTo(param2, value2);
        if (!value3.equals(ANY_VALUE)) query = query.whereEqualTo(param3, value3);
        if (!value4.equals(ANY_VALUE)) query = query.whereEqualTo(param4, value4);
        if (!value5.equals(ANY_VALUE)) query = query.whereEqualTo(param5, value5);
        if (!value6.equals(ANY_VALUE)) query = query.whereEqualTo(param6, value6);
        if (!value7.equals(ANY_VALUE)) query = query.whereEqualTo(param7, value7);

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot == null) return;
                        ArrayList<DUnit> list = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            DUnit unit = getDUnitFromSnapshot(document);
                            //JOIN------------------------------------------------------------------
                            getLastEventFromDB(unit, unitList);
                            list.add(unit);
                        }
                        unitList.setValue(list);
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    /**Слушатель для новых событий у выбранного юнита. Слушает всю коллекцию событий и при новом
     * событии загружает те, у которых "unit_id" равен id выбранного юнита. Другими словами
     * обновляет события выбранного юнита, если список событий изменился*/
    void addSelectedUnitStatesListener(String unit_id, MutableLiveData<ArrayList<DEvent>> unitStatesList) {
        db.collection(TABLE_EVENTS).addSnapshotListener((queryDocumentSnapshots, error) -> getEventsFromDB(unit_id, unitStatesList));
    }

    private DUnit getDUnitFromSnapshot(DocumentSnapshot documentSnapshots) {
        DUnit unit = new DUnit();
        unit.setName(String.valueOf(documentSnapshots.get(UNIT_DEVICE)));
        unit.setEmployee(String.valueOf(documentSnapshots.get(UNIT_EMPLOYEE)));
        unit.setId(String.valueOf(documentSnapshots.get(UNIT_ID)));
        unit.setEventId(String.valueOf(documentSnapshots.get(UNIT_EVENT_ID)));
        unit.setInnerSerial(String.valueOf(documentSnapshots.get(UNIT_INNER_SERIAL)));
        unit.setSerial(String.valueOf(documentSnapshots.get(UNIT_SERIAL)));
        unit.setType(String.valueOf(documentSnapshots.get(UNIT_TYPE)));
        unit.setDeviceSet(String.valueOf(documentSnapshots.get(UNIT_DEVICE_SET)));
        Timestamp timestamp = (Timestamp) documentSnapshots.get(UNIT_DATE);
        if (timestamp != null) unit.setDate(timestamp.toDate());
        Timestamp closeTime = (Timestamp) documentSnapshots.get(UNIT_CLOSE_DATE);
        if (closeTime != null) unit.setCloseDate(closeTime.toDate());
        unit.setTrackId(String.valueOf(documentSnapshots.get(UNIT_TRACKID)));
        return unit;
    }

    private DEvent getDEventFromSnapshot(DocumentSnapshot documentSnapshots) {
        DEvent event = new DEvent();
        Timestamp timestamp = (Timestamp) documentSnapshots.get(EVENT_DATE);
        event.setDate(timestamp.toDate());
        event.setState(documentSnapshots.get(EVENT_STATE).toString());
        event.setDescription(documentSnapshots.get(EVENT_DESCRIPTION).toString());
        event.setLocation(documentSnapshots.get(EVENT_LOCATION).toString());
        event.setUnit_id(documentSnapshots.get(EVENT_UNIT).toString());
        event.setId(documentSnapshots.getId());
        return event;
    }

    void listenerForUnitWithLastEvent(String unit_id, MutableLiveData<DUnit> mUnit) {
        db.collection(TABLE_UNITS).document(unit_id).addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (queryDocumentSnapshots != null && queryDocumentSnapshots.exists()) { //TODO обязательно везде добавить эту строку, иначе если документа НЕ СУЩЕСТВУЕТ в БД, то всё равно будет создан объект с полями (String)"null" (не null, а именно "null")
                DUnit unit = getDUnitFromSnapshot(queryDocumentSnapshots);
                mUnit.setValue(unit);
                //JOIN------------------------------------------------------------------
                getLastEventFromDB(mUnit);
            } else Log.e(TAG, "☻ ТАКОГО ЮНИТА НЕТ В БД!");
        });
    }

    void getLastEventFromDB(MutableLiveData<DUnit> mUnit) {
        if (mUnit == null) return;//todo mUnit.getValue() == null return ?
        DUnit unit = mUnit.getValue();
        db.collection(TABLE_EVENTS).document(unit.getEventId())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    DEvent event = getDEventFromSnapshot(document);
                    unit.setLastEvent(event);
                    mUnit.setValue(mUnit.getValue());//update
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    void getLastEventFromDB(DUnit unit, MutableLiveData<ArrayList<DUnit>> unitList) {
        if (unitList == null) return;//todo mUnit.getValue() == null return ?
        db.collection(TABLE_EVENTS).document(unit.getEventId())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    DEvent event = getDEventFromSnapshot(document);
                    unit.setLastEvent(event);
                    unitList.setValue(unitList.getValue());//update
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    /**Загружает список событий по "unit_id"*/
    void getEventsFromDB(String unit_id, MutableLiveData<ArrayList<DEvent>> events) {
        db.collection(TABLE_EVENTS)
            .whereEqualTo(EVENT_UNIT, unit_id)
            .orderBy(EVENT_DATE, Query.Direction.DESCENDING)
            .get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot == null) return;
                    ArrayList<DEvent> newEvents = new ArrayList<>();
                    for (QueryDocumentSnapshot q : querySnapshot) {
                        Timestamp timestamp = (Timestamp) q.get(EVENT_DATE);
                        Date date = timestamp.toDate();
                        String state = q.get(EVENT_STATE).toString();
                        String location = q.get(EVENT_LOCATION).toString();
                        Log.e(TAG, "♦getEventsFromDB: " + location);
                        newEvents.add(new DEvent(date, state, location));
                    }
                    events.setValue(newEvents);
                } else {
                    Log.e(TAG, "Error - " + task.getException());
                }
            });
    }

    /**Отслеживает изменения в юнитах из списка уже найденных сканером устройств. При изменении в
     * конкретном юните автоматом подгружает этот конкретный юнит вместе с событием и обновляет этот
     * юнит в списке найденных. Аналог listenerForUnitWithLastEvent для коллекции юнитов
     *
     * Надо понимать, что изменения в ивенте не обновляют список*/
    public void listenerForMultiScanUnitWithLastEvent(MutableLiveData<ArrayList<DUnit>> scannerFoundUnitsList) {
        for (int i = 0; i < scannerFoundUnitsList.getValue().size(); i++) {
            DUnit unit = scannerFoundUnitsList.getValue().get(i);
            int finalI = i;
            db.collection(TABLE_UNITS).document(unit.getId()).addSnapshotListener((queryDocumentSnapshots, error) -> {
                if (queryDocumentSnapshots != null && queryDocumentSnapshots.exists()) {
                    DUnit newUnit = getDUnitFromSnapshot(queryDocumentSnapshots);
                    scannerFoundUnitsList.getValue().set(finalI, newUnit);
                    scannerFoundUnitsList.setValue(scannerFoundUnitsList.getValue());
                    //JOIN------------------------------------------------------------------
                    getLastEventFromDB(newUnit, scannerFoundUnitsList);
                } else Log.e(TAG, "☻ ТАКОГО ЮНИТА НЕТ В БД!");
            });
        }
    }
}
