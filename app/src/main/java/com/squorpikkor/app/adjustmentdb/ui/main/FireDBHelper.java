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
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Device;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.DeviceSet;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Employee;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Entity;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Location;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.State;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.DEVICE_DEV_SET_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.DEVICE_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.DEVICE_NAME_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.DEVICE_SET_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.DEVICE_SET_NAME_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPLOYEE_EMAIL;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPLOYEE_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPLOYEE_LOCATION;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EMPLOYEE_NAME_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_CLOSE_DATE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_DATE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_DESCRIPTION;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_LOCATION;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_STATE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_UNIT;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.LOCATION_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.LOCATION_NAME_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.STATE_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.STATE_LOCATION;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.STATE_NAME_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.STATE_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_DEVICES;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_DEVICE_SET;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_EMPLOYEES;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_LOCATIONS;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_NAMES;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_STATES;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_EVENTS;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_UNITS;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_CLOSE_DATE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_DATE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_DEVICE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_EMPLOYEE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_EVENT_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_INNER_SERIAL;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_LOCATION;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_SERIAL;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_STATE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_TYPE;


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
 * загружающий объекты*/

class FireDBHelper {

//--------------------------------------------------------------------------------------------------
//todo ВСЕ ЛИСЕНЕРЫ НУЖНО ОБЪЕДИНИТЬ В ОДИН (У ВСЕХ СУЩНОСТЕЙ ВЕДЬ ОДИН РОДИТЕЛЬ)
    void joinName(String id, Entity e, MutableLiveData<ArrayList<? extends Entity>> data) {
        db.collection(TABLE_NAMES).document(id).get()
                .addOnCompleteListener(task1 -> {
                    e.setName(getStringFromSnapshot(task1, id));
//                    updateLiveData(data);
                    data.setValue(data.getValue());
                });
    }

    private final FirebaseFirestore db;

    void locationListener(MutableLiveData<ArrayList<Location>> data) {
        db.collection(TABLE_LOCATIONS)
                .get().addOnCompleteListener(task -> {
            ArrayList<Location> newLocations = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                String id = document.get(LOCATION_ID).toString();
                String nameId = document.get(LOCATION_NAME_ID).toString();
                String name = document.get(LOCATION_NAME_ID).toString();
                Location location = new Location(id, nameId, name);

//                joinName(id, location, data);

                //JOIN------------------------------------------------------------------
                db.collection(TABLE_NAMES).document(nameId).get()
                        .addOnCompleteListener(task1 -> {
                            location.setName(getStringFromSnapshot(task1, nameId));
                            data.setValue(data.getValue());//update Mutable
                        });

                newLocations.add(location);

            }
            data.setValue(newLocations);

        });
    }

    void deviceListener(MutableLiveData<ArrayList<Device>> data) {
        db.collection(TABLE_DEVICES)
                .get().addOnCompleteListener(task -> {
            ArrayList<Device> newDevices = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                String id = document.get(DEVICE_ID).toString();
                String nameId = document.get(DEVICE_NAME_ID).toString();
                String name = document.get(DEVICE_NAME_ID).toString();
                String devSetId = document.get(DEVICE_DEV_SET_ID).toString();
                String devSetName = document.get(DEVICE_DEV_SET_ID).toString();

                Device device = new Device(id, nameId, name, devSetId, devSetName);

                //JOIN------------------------------------------------------------------
                db.collection(TABLE_NAMES).document(nameId).get()
                        .addOnCompleteListener(task1 -> {
                            //И русские и английские имена нужны для распознавания наклеек, которые могут быть и русские и английские
                            String ru = "";
                            String en = "";
                            if (task1.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task1.getResult();
                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    ru = documentSnapshot.get("ru").toString();
                                    en = documentSnapshot.get("en").toString();
                                    Log.e(TAG, "deviceListener: "+ru+" "+en);
                                }
                            }

                            if (!ru.equals("")) device.setName(ru);
                            if (!en.equals("")) device.setEngName(en);
                            data.setValue(data.getValue());
                        });

                //JOIN------------------------------------------------------------------
                //  \\\\\\ //  пока не буду делать отдельную таблицу и отдельно русские имена.
//                db.collection(TABLE_NAMES).document(devSetId).get()
//                        .addOnCompleteListener(task1 -> {
//                            device.setDevSetName(getStringFromSnapshot(task1, nameId));
//                            data.setValue(data.getValue());//update Mutable
//                        });

                newDevices.add(device);
            }
            data.setValue(newDevices);
        });
    }

    void employeeListener(MutableLiveData<ArrayList<Employee>> data) {
        db.collection(TABLE_EMPLOYEES)
                .get().addOnCompleteListener(task -> {
            ArrayList<Employee> newEmployees = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                //Внимание! Для employee name_id — это id, а name — это name_id
                //Так сделано, потому что для сохранения сотрудника в юните нужен id сотрудника
                // (поэтому name_id — это id), а для отображения имени в спиннере достаточно
                // name_id без подгрузки имени из таблицы имен (name — это name_id)
                String id = document.get(EMPLOYEE_ID).toString();
                String nameId = document.get(EMPLOYEE_ID).toString();
                String name = document.get(EMPLOYEE_NAME_ID).toString();
                String eMail = document.get(EMPLOYEE_EMAIL).toString();
                String location = document.get(EMPLOYEE_LOCATION).toString();
                Employee employee = new Employee(id, nameId, name, eMail, location);

                //JOIN------------------------------------------------------------------
                // Закоментировано, так как всё равно для русского варианта имен (а employees
                // используется только в AdjustmentDB, а значит ТОЛЬКО в русском варианте слов)
                // name_id ВСЕГДА == name, поэтому JOIN делает только лишнюю работу
//                db.collection(TABLE_NAMES).document(nameId).get()
//                        .addOnCompleteListener(task1 -> {
//                            employee.setName(getStringFromSnapshot(task1, nameId));
//                            data.setValue(data.getValue());
//                        });

                newEmployees.add(employee);
            }
            data.setValue(newEmployees);
        });
    }

    void stateListener(MutableLiveData<ArrayList<State>> data) {
        db.collection(TABLE_STATES)
                .get().addOnCompleteListener(task -> {
            ArrayList<State> newStates = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                String id = document.get(STATE_ID).toString();
                String nameId = document.get(STATE_NAME_ID).toString();
                String name = document.get(STATE_NAME_ID).toString();
                String type = document.get(STATE_TYPE).toString();
                String location = document.get(STATE_LOCATION).toString();
                State state = new State(id, nameId, name, type, location);

                //JOIN------------------------------------------------------------------
                db.collection(TABLE_NAMES).document(nameId).get()
                        .addOnCompleteListener(task1 -> {
                            state.setName(getStringFromSnapshot(task1, nameId));
                            data.setValue(data.getValue());
                        });
                newStates.add(state);
            }
            data.setValue(newStates);
        });
    }

    void deviceSetListener(MutableLiveData<ArrayList<DeviceSet>> data) {
        db.collection(TABLE_DEVICE_SET)
                .get().addOnCompleteListener(task -> {
            ArrayList<DeviceSet> newStates = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                String id = document.get(DEVICE_SET_ID).toString();
                String nameId = document.get(DEVICE_SET_NAME_ID).toString();
                String name = document.get(DEVICE_SET_NAME_ID).toString();
                DeviceSet devSet = new DeviceSet(id, nameId, name);

                //JOIN------------------------------------------------------------------
                db.collection(TABLE_NAMES).document(nameId).get()
                        .addOnCompleteListener(task1 -> {
                            devSet.setName(getStringFromSnapshot(task1, nameId));
                            data.setValue(data.getValue());
                        });
                newStates.add(devSet);
            }
            data.setValue(newStates);
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

    public FireDBHelper() {
        db = FirebaseFirestore.getInstance();
    }

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
        if (unit.getCloseDate()!=null) data.put(UNIT_CLOSE_DATE, unit.getCloseDate());
        data.put(UNIT_LOCATION, unit.getLastEvent().getLocation());
        data.put(UNIT_STATE, unit.getLastEvent().getState());
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
    void closeEvent(String event_id) {
        if (event_id==null) return;
        db.collection(TABLE_EVENTS).document(event_id).update(EVENT_CLOSE_DATE, new Date());
    }

    /**Обертка для getUnitListByParam*/
    void getUnitList(MutableLiveData<ArrayList<DUnit>> unitList, String deviceNameId, String locationId, String employeeId, String typeId, String stateId, String serial) {
        getUnitListByParam(unitList,
                UNIT_DEVICE, deviceNameId,
                UNIT_LOCATION, locationId,
                UNIT_EMPLOYEE, employeeId,
                UNIT_TYPE, typeId,
                UNIT_STATE, stateId,
                UNIT_SERIAL, serial);
    }

    void getUnitListByParam(MutableLiveData<ArrayList<DUnit>> unitList, String param1, String value1, String param2, String value2, String param3, String value3, String param4, String value4, String param5, String value5, String param6, String value6) {
        Query query = db.collection(TABLE_UNITS);
        if (!value1.equals(ANY_VALUE)) query = query.whereEqualTo(param1, value1);
        if (!value2.equals(ANY_VALUE)) query = query.whereEqualTo(param2, value2);
        if (!value3.equals(ANY_VALUE)) query = query.whereEqualTo(param3, value3);
        if (!value4.equals(ANY_VALUE)) query = query.whereEqualTo(param4, value4);
        if (!value5.equals(ANY_VALUE)) query = query.whereEqualTo(param5, value5);
        if (!value6.equals(ANY_VALUE)) query = query.whereEqualTo(param6, value6);

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
        Timestamp timestamp = (Timestamp) documentSnapshots.get(UNIT_DATE);
        if (timestamp != null) unit.setDate(timestamp.toDate());
        Timestamp closeTime = (Timestamp) documentSnapshots.get(UNIT_CLOSE_DATE);
        if (closeTime != null) unit.setCloseDate(closeTime.toDate());
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
