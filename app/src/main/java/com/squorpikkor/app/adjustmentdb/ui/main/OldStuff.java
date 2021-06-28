package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squorpikkor.app.adjustmentdb.DUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_DATE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_DESCRIPTION;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_LOCATION;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_STATE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.EVENT_UNIT;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.STATE_ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.STATE_LOCATION;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.STATE_NAME;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.STATE_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_EVENTS;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_STATES;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_UNITS;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TYPE_ANY;
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

class OldStuff {

    private FirebaseFirestore db;

    /**Добавление нового события в БД*/
    void addEventToDB(Date date, String state, String description, String unit_id, String location_id) {
        Map<String, Object> data = new HashMap<>();
        data.put(EVENT_DATE, date);
        data.put(EVENT_STATE, state);
        data.put(EVENT_DESCRIPTION, description);
        data.put(EVENT_UNIT, unit_id);
        data.put(EVENT_LOCATION, location_id);
        db.collection(TABLE_EVENTS)
                .document()
                .set(data)
                .addOnSuccessListener(aVoid -> Log.e(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.e(TAG, "Error writing document", e));
    }


    @SuppressWarnings("SameParameterValue")
    void addStringArrayListener(String table, MutableLiveData<ArrayList<String>> mList, String fieldName) {
        Log.e(TAG, "addStringArrayListener: ");
        db.collection(table).addSnapshotListener((queryDocumentSnapshots, error) -> {
            //getStringFromDB(table, s);
            getStringArrayFromDB(table, mList, fieldName);
        });
    }

    void addStringArrayOrderedListener(String table, MutableLiveData<ArrayList<String>> mList, String fieldName, String orderBy) {
        Log.e(TAG, "addStringArrayListenerOrdered: ");
        db.collection(table).addSnapshotListener((queryDocumentSnapshots, error) -> {
            getStringArrayFromDBOrdered(table, mList, fieldName, orderBy);
        });
    }


    void getUnitById(String id, MutableLiveData<DUnit> selectedUnit) {
        db.collection(TABLE_UNITS)
                .whereEqualTo(UNIT_ID, id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot == null || querySnapshot.size() == 0) return;
//                        selectedUnits.setValue((ArrayList<DUnit>) querySnapshot.toObjects(DUnit.class));
                        DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                        DUnit unit = new DUnit();
//                        unit.setDescription(String.valueOf(documentSnapshot.get(UNIT_DESCRIPTION)));
                        unit.setName(String.valueOf(documentSnapshot.get(UNIT_DEVICE)));
                        unit.setEmployee(String.valueOf(documentSnapshot.get(UNIT_EMPLOYEE)));
                        unit.setId(String.valueOf(documentSnapshot.get(UNIT_ID)));
                        unit.setEventId(String.valueOf(documentSnapshot.get(UNIT_EVENT_ID)));
                        unit.setInnerSerial(String.valueOf(documentSnapshot.get(UNIT_INNER_SERIAL)));
//                        unit.setLocation(String.valueOf(documentSnapshot.get(UNIT_LOCATION)));
                        unit.setSerial(String.valueOf(documentSnapshot.get(UNIT_SERIAL)));
                        unit.setState(String.valueOf(documentSnapshot.get(UNIT_STATE)));
                        unit.setType(String.valueOf(documentSnapshot.get(UNIT_TYPE)));
                        Timestamp timestamp = (Timestamp) documentSnapshot.get(UNIT_DATE);
                        if (timestamp!=null)unit.setDate(timestamp.toDate());
                        selectedUnit.setValue(unit);

                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }


    //todo поменять на этот вариант
    void getUnitByIdAndAddToList_EXP(String id, MutableLiveData<ArrayList<DUnit>> list, int position) {
        db.collection(TABLE_UNITS)
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            MutableLiveData<ArrayList<DUnit>> newList = new MutableLiveData<>();
                            newList.setValue(list.getValue());

                            DUnit unit = newList.getValue().get(position);
//                            unit.setDescription(String.valueOf(documentSnapshot.get(UNIT_DESCRIPTION)));
                            unit.setName(String.valueOf(documentSnapshot.get(UNIT_DEVICE)));
                            unit.setEmployee(String.valueOf(documentSnapshot.get(UNIT_EMPLOYEE)));
                            unit.setId(String.valueOf(documentSnapshot.get(UNIT_ID)));
                            unit.setEventId(String.valueOf(documentSnapshot.get(UNIT_EVENT_ID)));
                            unit.setInnerSerial(String.valueOf(documentSnapshot.get(UNIT_INNER_SERIAL)));
//                            unit.setLocation(String.valueOf(documentSnapshot.get(UNIT_LOCATION)));
                            unit.setSerial(String.valueOf(documentSnapshot.get(UNIT_SERIAL)));
                            unit.setState(String.valueOf(documentSnapshot.get(UNIT_STATE)));
                            unit.setType(String.valueOf(documentSnapshot.get(UNIT_TYPE)));
                            Timestamp timestamp = (Timestamp) documentSnapshot.get(UNIT_DATE);
                            if (timestamp!=null)unit.setDate(timestamp.toDate());

                            list.setValue(newList.getValue());
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private DUnit getUnitFromSnapshot(DocumentSnapshot documentSnapshot) {
        DUnit unit = new DUnit();
//        unit.setDescription(String.valueOf(documentSnapshot.get(UNIT_DESCRIPTION)));
        unit.setName(String.valueOf(documentSnapshot.get(UNIT_DEVICE)));
        unit.setEmployee(String.valueOf(documentSnapshot.get(UNIT_EMPLOYEE)));
        unit.setId(String.valueOf(documentSnapshot.get(UNIT_ID)));
        unit.setEventId(String.valueOf(documentSnapshot.get(UNIT_EVENT_ID)));
        unit.setInnerSerial(String.valueOf(documentSnapshot.get(UNIT_INNER_SERIAL)));
//        unit.setLocation(String.valueOf(documentSnapshot.get(UNIT_LOCATION)));
        unit.setSerial(String.valueOf(documentSnapshot.get(UNIT_SERIAL)));
        unit.setState(String.valueOf(documentSnapshot.get(UNIT_STATE)));
        unit.setType(String.valueOf(documentSnapshot.get(UNIT_TYPE)));
        Timestamp timestamp = (Timestamp) documentSnapshot.get(UNIT_DATE);
        if (timestamp!=null)unit.setDate(timestamp.toDate());
        return unit;
    }

    /**Обертка для getUnitListByParam*/
    void getRepairUnitListBySerial(MutableLiveData<ArrayList<DUnit>> unitList, String serial) {
        getUnitListByParam(unitList, UNIT_DEVICE, ANY_VALUE,
                UNIT_LOCATION, ANY_VALUE,
                UNIT_EMPLOYEE, ANY_VALUE,
                UNIT_TYPE, REPAIR_TYPE,
                UNIT_STATE, ANY_VALUE,
                UNIT_SERIAL, serial);
    }

    private void getUnitListByParam(MutableLiveData<ArrayList<DUnit>> unitList, String unitDevice, String anyValue, String unitLocation, String anyValue1, String unitEmployee, String anyValue2, String unitType, String repairType, String unitState, String anyValue3, String unitSerial, String serial) {
    }


    /**
     * Получаем лист String из БД и помещаем её в MutableLiveDate
     * @param table имя таблицы (коллекции) из которой берем данные
     * @param mList Mutable, в который помещаем найденные стринги
     * @param fieldName поле таблицы, значение которой считываем в лист
     */
    void getStringArrayFromDB(String table, MutableLiveData<ArrayList<String>> mList, String fieldName) {
        db.collection(table).get().addOnCompleteListener(task -> {
            ArrayList<String> list = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                if (document.get(fieldName)!=null) list.add(document.get(fieldName).toString());
            }
            mList.setValue(list);
        });
    }

    void getStringArrayFromDBOrdered(String table, MutableLiveData<ArrayList<String>> mList, String fieldName, String orderBy) {
        db.collection(table).orderBy(orderBy, Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
            ArrayList<String> list = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                if (document.get(fieldName)!=null) list.add(document.get(fieldName).toString());
                Log.e(TAG, "☻☻☻ getStringArrayFromDBOrdered: "+document.get(fieldName).toString());
            }
            mList.setValue(list);
        });
    }

    /**
     * Получение MutableLiveData<ArrayList<String>> из БД по 2-м параметрам
     * @param table коллекция, по которой будет произведен поиск
     * @param mList MutableLiveData в который будет записан найденный лист
     * @param param1 имя параметра 1 (поля), по которому ведется поиск
     * @param value1 значение параметра 1, по которому ведется поиск
     * @param param2 имя параметра 2 (поля), по которому ведется поиск
     * @param value2 значение параметра 2, по которому ведется поиск
     * @param fieldName это то поле, значение которого будет считываться в возвращаемый ArrayList<String>
     */
    void getStringArrayByParam(String table, MutableLiveData<ArrayList<String>> mList, String param1, String value1, String param2, String value2, String fieldName) {
        db.collection(table)
                .whereEqualTo(param1, value1)
                .whereEqualTo(param2, value2)
                .get().addOnCompleteListener(task -> {
            ArrayList<String> list = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                list.add(document.get(fieldName).toString());
            }
            mList.setValue(list);
        });
    }

    void getString(String table, String documentId, String field, /*String changingValue*/MutableLiveData<String> changingValue) {
        db.collection(table)
                .document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            Object o = documentSnapshot.get(field);
                            String value = o==null?"":o.toString();
                            changingValue.setValue(value);
                        } else {
                            Log.e(TAG, "☻ NOT EXISTS");
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    void getListIdsAndListNames(String table, MutableLiveData<ArrayList<String>> idList, String idField, MutableLiveData<ArrayList<String>> nameList, String nameField) {
        db.collection(table).get().addOnCompleteListener(task -> {
            ArrayList<String> list1 = new ArrayList<>();
            ArrayList<String> list2 = new ArrayList<>();
            int count = 0;
            for (DocumentSnapshot document : task.getResult()) {
                String id = document.get(idField).toString();
                String name = document.get(nameField).toString();
                list1.add(id);
                list2.add(name);
                //todo если буду делать локализацию, то здесь надо будет вставлять что-то типа if(lang.isEng)name = "name_eng". В БД будет дополнительное поле "name_eng", оно будет выбираться вместо "name". И всё, весь остальной код уже будет работать. Это конечно касается только имени статуса, для других сделать аналогично
            }
            idList.setValue(list1);
            nameList.setValue(list2);
        });
    }



    /**Загружает список статусов по типу (серия/ремонт) и локации (регулировка/монтаж...). Так как
     * есть статусы, которые одинаковые и для ремонта и для серии (у этих статусов тип "any"), то
     * при выборе статусов ищется тип выбранный в параметре метода (ремонт или серия) ИЛИ тип "any"
     * (т.е. при любом выбранном типе ВСЕГДА будут добавляться в выборку типы "any" в выбранной локации)*/
    void getListOfStates(String location, String type, MutableLiveData<ArrayList<String>> mStateIsList, MutableLiveData<ArrayList<String>> mNameList) {
        Log.e(TAG, "♦♦♦ getListOfStates: "+location);
        db.collection(TABLE_STATES)
                .whereEqualTo(STATE_LOCATION, location)
                .whereIn(STATE_TYPE, Arrays.asList(TYPE_ANY, type))
                .get().addOnCompleteListener(task -> {
            ArrayList<String> list1 = new ArrayList<>();
            ArrayList<String> list2 = new ArrayList<>();
            int count = 0;
            for (DocumentSnapshot document : task.getResult()) {
                Log.e(TAG, "getListOfStates: "+count);
                count++;
                String name = document.get(STATE_NAME).toString();
                String state_id = document.get(STATE_ID).toString();
                list1.add(state_id);
                list2.add(name);
                //todo если буду делать локализацию, то здесь надо будет вставлять что-то типа if(lang.isEng)name = "name_eng". В БД будет дополнительное поле "name_eng", оно будет выбираться вместо "name". И всё, весь остальной код уже будет работать. Это конечно касается только имени статуса, для других сделать аналогично
            }
            mStateIsList.setValue(list1);
            mNameList.setValue(list2);
        });
    }

    /**
     * Поиск по БД по параметру документа и запись результата поиска в MutableLiveData<String>
     * @param table коллекция, по которой будет произведен поиск
     * @param byParamName имя параметра (поля), по которому ведется поиск
     * @param byValueName значение параметра, по которому ведется поиск
     * @param valueToGet имя поля в найденном документе, значение которого будет записано в результат
     * @param outPutString в эту переменную будет записан найденное значение
     * @param stringIfNull если ничего не найдено, это будет записано в outPutString
     */
    void getStringValueByParam(String table, String byParamName, String byValueName, String valueToGet, MutableLiveData<String> outPutString, String stringIfNull) {
        db.collection(table)
                .whereEqualTo(byParamName, byValueName)
                .get().addOnCompleteListener(task -> {
            ArrayList<String> list = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                list.add(document.get(valueToGet).toString());
            }
            if (list.size()==0) outPutString.setValue(stringIfNull);
            else outPutString.setValue(list.get(0));
        });
    }

}
