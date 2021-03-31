package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squorpikkor.app.adjustmentdb.DState;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.DevType;

import java.util.ArrayList;
import java.util.Arrays;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.DATE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ID;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.PROFILE_LOCATION;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.PROFILE_NAME;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.PROFILE_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.PROF_TYPE_ANY;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.STATE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_ALL_STATES;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TABLE_PROFILES;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.UNIT_ID;

class FireDBHelper {

    private final FirebaseFirestore db;

    public FireDBHelper() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Добавляет документ в БД. Если документ не существует, он будет создан. Если документ существует,
     * его содержимое будет перезаписано вновь предоставленными данными
     */
    void addUnitToDB(DUnit unit, String table, String documentName) {
        //В коллекцию устройств добавляем/обновляем устройство
        db.collection(table)
                .document(documentName)
                .set(unit)
                .addOnSuccessListener(aVoid -> Log.e(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.e(TAG, "Error writing document", e));

    }

    /**
     * Сохранение статуса в таблицу статусов
     */
    void addStateToDB(DState dState, String table) {
        db.collection(table)
                .document()
                .set(dState)
                .addOnSuccessListener(aVoid -> Log.e(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.e(TAG, "Error writing document", e));
    }

    /**
     * Метод загружает элементы из БД (все в текущей таблице). Трюк в том, что метод при получении данных
     * заносит их в коллекцию объектов, которая является MutableLiveData из ViewModel, ссылка на эту коллекцию
     * объект класса FireDBHelper получает в конструкторе. Получается, что приложение, получив данные из БД
     * в облаке сохраняет их в коллекцию, на которую подписан RecyclerView, таким образом изменения в
     * БД автоматом отображаются в списке RecyclerView
     * <p>
     * QuerySnapshot -- это список QueryDocumentSnapshot (список всех "user"-ов в таблице "users" Базы Данных )
     * QueryDocumentSnapshot -- это объект в БД, один "user", у которого можно будет прочитать свойства
     */
    //todo тип второго параметра может быть другим, чтобы не делать перегруженный метод надо сделать
    // универсальный параметр (например MutableLiveData<ArrayList<T>> или аналог)
    void getElementFromDB(String table, MutableLiveData<ArrayList<DUnit>> units) {
        db.collection(table).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot == null || querySnapshot.size() == 0) return;
                units.setValue((ArrayList<DUnit>) querySnapshot.toObjects(DUnit.class));
            } else {
                Log.e(TAG, "Error - " + task.getException());
            }
        });
    }

    /**
     * Слушатель изменений для "user", здесь немного путаница в названиях: здесь объект класса QuerySnapshot (список) называется
     * queryDocumentSnapshots (как будто это объект класса QueryDocumentSnapshot, на самом деле -- это "user", внимательно смотрим), оставил так, потому что так написано
     * в оф. документации
     * Если срабатывает событие, слушатель запускает загрузку ВСЕХ объектов из БД
     * В будущем нужно будет загружать не все элементы, а только новые элементы в БД (или удалять удаленные из БД)
     * иначе, когда элементов будет много, будет долго, да и трафик лишний
     */
    void addDBListener(String table, MutableLiveData<ArrayList<DUnit>> units) {
        db.collection(table).addSnapshotListener((queryDocumentSnapshots, error) -> getElementFromDB(table, units));
    }

    void getDevTypeFromDB(String table, MutableLiveData<ArrayList<DevType>> dev) {
        db.collection(table).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot == null) return;
                dev.setValue((ArrayList<DevType>) querySnapshot.toObjects(DevType.class));
            } else {
                Log.e(TAG, "Error - " + task.getException());
            }
        });
    }

    void getStringArrayFromDB(String table, MutableLiveData<ArrayList<String>> mList, String fieldName) {
        db.collection(table).get().addOnCompleteListener(task -> {
            ArrayList<String> list = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                list.add(document.get(fieldName).toString());
            }
            mList.setValue(list);
        });
    }

    void getStringArrayFromDB(String table, String document, String table2, MutableLiveData<ArrayList<String>> mList, String fieldName) {
        db.collection(table).document(document).collection(table2).get().addOnCompleteListener(task -> {
            ArrayList<String> list = new ArrayList<>();
            for (DocumentSnapshot document1 : task.getResult()) {
                list.add(document1.get(fieldName).toString());
                /////mList.getValue().add(document.get(fieldName).toString());
            }
            mList.setValue(list);
        }).addOnFailureListener(e -> Log.e(TAG, "onFailure: "+e));
    }

    /**Слушатель для новых событий у выбранного юнита. Слушает всю коллекцию статусов и при новом
     * событии загружает те статусы у которых "unit_id" равен id выбранного юнита. Другими словами
     * обновляет события выбранного юнита, если список событий изменился*/
    void addSelectedUnitListener(String unit_id, MutableLiveData<ArrayList<DState>> unitStatesList) {
        db.collection(TABLE_ALL_STATES).addSnapshotListener((queryDocumentSnapshots, error) -> {
            getStatesFromDB(unit_id, unitStatesList);
        });
    }

    /**Загружает список событий по "unit_id"*/
    void getStatesFromDB(String unit_id, MutableLiveData<ArrayList<DState>> states) {
        db.collection(TABLE_ALL_STATES)
                .whereEqualTo(UNIT_ID, unit_id)
                .orderBy(DATE)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot == null) return;
                        ArrayList<DState> newStates = new ArrayList<>();
                        for (QueryDocumentSnapshot q : querySnapshot) {
                            Timestamp timestamp = (Timestamp) q.get(DATE);
                            /*Log.e(TAG, "1: "+q.get("date"));
                            Log.e(TAG, "2: "+timestamp.toDate());
                            Log.e(TAG, "2: "+timestamp.toDate());
                            Log.e(TAG, "2: "+getRightDate(timestamp.getSeconds()));
                            Log.e(TAG, "3: "+q.get("state"));*/
                            newStates.add(new DState(timestamp.toDate(), q.get(STATE).toString()));
                        }
                        states.setValue(newStates);
                    } else {
                        Log.e(TAG, "Error - " + task.getException());
                    }
                });
    }

    void addDevTypeListener(String table, MutableLiveData<ArrayList<DevType>> dev) {
        db.collection(table).addSnapshotListener((queryDocumentSnapshots, error) -> getDevTypeFromDB(table, dev));
    }

    void addStringArrayListener(String table, MutableLiveData<ArrayList<String>> mList, String fieldName) {
        Log.e(TAG, "addStringArrayListener: ");
        db.collection(table).addSnapshotListener((queryDocumentSnapshots, error) -> {
            //getStringFromDB(table, s);
            getStringArrayFromDB(table, mList, fieldName);
        });
    }

    void addStringArrayListener(String table, String document, String table2, MutableLiveData<ArrayList<String>> mList, String fieldName) {
        db.collection(table).document(document).collection(table2).addSnapshotListener((queryDocumentSnapshots, error) -> {
            //getStringFromDB(table, s);
            getStringArrayFromDB(table, document, table2, mList, fieldName);
        });
    }


    void getUnitById(String table, String id, MutableLiveData<ArrayList<DUnit>> selectedUnits) {
        db.collection(table)
                .whereEqualTo(ID, id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot == null || querySnapshot.size() == 0) return;
                        selectedUnits.setValue((ArrayList<DUnit>) querySnapshot.toObjects(DUnit.class));
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }



    /**
     *
     * @param table
     * @param mList
     * @param param1
     * @param value1
     * @param param2
     * @param value2
     * @param fieldName это то поле, значение которого будет считываться в возвращаемый ArrayList<String>
     */
    void getStringArrayByParam(String table, MutableLiveData<ArrayList<String>> mList, String param1, String value1, String param2, String value2, String fieldName) {
        db.collection(table)
                .whereEqualTo(param1, "value1")
                .whereEqualTo(param2, value2)
                .get().addOnCompleteListener(task -> {
            ArrayList<String> list = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                list.add(document.get(fieldName).toString());
            }
            mList.setValue(list);
        });
    }


    /**Загружает список статусов по типу (серия/ремонт) и локации (регулировка/монтаж...). Так как
     * есть статусы, которые одинаковые и для ремонта и для серии (у этих статусов тип "any"), то
     * при выборе статусов ищется тип выбранный в параметре метода (ремонт или серия) ИЛИ тип "any"
     * (т.е. при любом выбранном типе ВСЕГДА будут добавляться в выборку типы "any" в выбранной локации)*/
    void getListOfStates(String location, String type, MutableLiveData<ArrayList<String>> mList) {
        db.collection(TABLE_PROFILES)
                .whereEqualTo(PROFILE_LOCATION, location)
                .whereIn(PROFILE_TYPE, Arrays.asList(PROF_TYPE_ANY, type))
                .get().addOnCompleteListener(task -> {
            ArrayList<String> list = new ArrayList<>();
            for (DocumentSnapshot document : task.getResult()) {
                list.add(document.get(PROFILE_NAME).toString());//todo если буду делать локализацию, то здесь надо будет вставлять что-то типа if(lang.isEng)name = "name_eng". В БД будет дополнительное поле "name_eng", оно будет выбираться вместо "name". И всё, весь остальной код уже будет работать. Это конечно касается только имени статуса, для других сделать аналогично
            }
            mList.setValue(list);
        });
    }

}
