package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squorpikkor.app.adjustmentdb.DState;
import com.squorpikkor.app.adjustmentdb.DUnit;
import com.squorpikkor.app.adjustmentdb.DevType;

import java.util.ArrayList;
import java.util.Date;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIRS_TABLE;

class FireDBHelper {

    FirebaseFirestore db;

    public FireDBHelper() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Добавляет документ в БД. Если документ не существует, он будет создан. Если документ существует,
     * его содержимое будет перезаписано вновь предоставленными данными
     */
    void addElementToDB(DUnit unit, String table) {
        Log.e(TAG, "addElementToDB: "+unit.getState());
        //Если это серийный
        String documentName = "" + unit.getName() + "_" + unit.getInnerSerial();//2140_45665
        //Если это ремонтный
        if (table.equals(REPAIRS_TABLE)) documentName = "r_" + unit.getId();//r_0001
        //В коллекцию устройств добавляем/обновляем устройство
        db.collection(table)
                .document(documentName)
                .set(unit)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error writing document", e);
                    }
                });
        // В коллекцию статусов текущего устройства добавляем статус: описание+дата (добавляем
        // коллекцию в коллекцию). Если поля статуса оставить пустым, то статус не будет добавлне
        // (нужно, наример, если необходимо просто добавить серийный номер, никакого статуса в этом
        // случае быть не может)
        if (!unit.getState().equals("")) {
            db.collection(table)
                    .document(documentName)
                    .collection("states").document().set(new DState(new Date(), unit.getState()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error writing document", e);
                        }
                    });
        }
    }
    /**
     * Add a new document with a generated ID
     */
    void addElementToDB_OLD(DUnit unit, String table) {
        db.collection(table)
                .add(unit)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.e(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                    }
                });
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
        db.collection(table).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot == null) return;
                    units.setValue((ArrayList<DUnit>) querySnapshot.toObjects(DUnit.class));
                } else {
                    Log.e(TAG, "Error - " + task.getException());
                }
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
        db.collection(table).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                getElementFromDB(table, units);
            }
        });
    }

    void getDevTypeFromDB(String table, MutableLiveData<ArrayList<DevType>> dev) {
        db.collection(table).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot == null) return;
                    dev.setValue((ArrayList<DevType>) querySnapshot.toObjects(DevType.class));
                } else {
                    Log.e(TAG, "Error - " + task.getException());
                }
            }
        });
    }

    void getStatesFromDB(String table, String documentName, String table2, MutableLiveData<ArrayList<DState>> states) {
        db.collection(table).document(documentName).collection(table2).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot == null) return;
                    ArrayList<DState> newStates = new ArrayList<>();
                    for (QueryDocumentSnapshot q:querySnapshot) {
                        Timestamp timestamp = (Timestamp)q.get("date");
                        /*Log.e(TAG, "1: "+q.get("date"));
                        Log.e(TAG, "2: "+timestamp.toDate());
                        Log.e(TAG, "2: "+timestamp.toDate());
                        Log.e(TAG, "2: "+getRightDate(timestamp.getSeconds()));
                        Log.e(TAG, "3: "+q.get("state"));*/
                        newStates.add(new DState(timestamp.toDate(), q.get("state").toString()));
                    }
                    states.setValue(newStates);
                } else {
                    Log.e(TAG, "Error - " + task.getException());
                }
            }
        });
    }

    void getStringArrayFromDB(String table, MutableLiveData<ArrayList<String>> mList, String fieldName) {
        Log.e(TAG, "getStringArrayFromDB: ");
        db.collection(table).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> list = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    list.add(document.get(fieldName).toString());
                    /////mList.getValue().add(document.get(fieldName).toString());
                }
                mList.setValue(list);
            }
        });
    }

    void addDevTypeListener(String table, MutableLiveData<ArrayList<DevType>> dev) {
        db.collection(table).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                getDevTypeFromDB(table, dev);
            }
        });
    }

    void addStringArrayListener(String table, MutableLiveData<ArrayList<String>> mList, String fieldName) {
        Log.e(TAG, "addStringArrayListener: ");
        db.collection(table).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                //getStringFromDB(table, s);
                getStringArrayFromDB(table, mList, fieldName);
            }
        });
    }


    //todo paramValue переделать в Object. С другой стороны — это пока не важно, у меня и так пока все параметры String
    void readFromDBByParameter(String table, String paramName, String paramValue, MutableLiveData<ArrayList<DUnit>> selectedUnits) {
        db.collection(table)
                .whereEqualTo(paramName, paramValue)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null) return;
                            selectedUnits.setValue((ArrayList<DUnit>) querySnapshot.toObjects(DUnit.class));
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //todo paramValue переделать в Object. С другой стороны — это пока не важно, у меня и так пока все параметры String
    void readFromDBByTwoParameters(String table, String paramName1, String paramValue1, String paramName2, String paramValue2, MutableLiveData<ArrayList<DUnit>> selectedUnits) {
        db.collection(table)
                .whereEqualTo(paramName1, paramValue1)
                .whereEqualTo(paramName2, paramValue2)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot == null) return;
                            selectedUnits.setValue((ArrayList<DUnit>) querySnapshot.toObjects(DUnit.class));
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
