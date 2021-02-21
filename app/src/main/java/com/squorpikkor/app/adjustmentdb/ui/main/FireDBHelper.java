package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squorpikkor.app.adjustmentdb.DUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;


class FireDBHelper {

    FirebaseFirestore db;
    MutableLiveData<ArrayList<DUnit>> units;

    public FireDBHelper(MutableLiveData<ArrayList<DUnit>> list) {
        db = FirebaseFirestore.getInstance();
        units = list;
    }

    // Add a new document with a generated ID
    void addElementToDB(DUnit unit, String table) {
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

    void getElementFromDB_old(String table) {
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot == null) return;
                    //QuerySnapshot -- это список QueryDocumentSnapshot (список всех "user"-ов в таблице "users" Базы Данных )
                    //QueryDocumentSnapshot -- это объект в БД, один "user", у которого можно будет прочитать свойства
                    for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                        Map<String, Object> user = documentSnapshot.getData();
                        Log.e(TAG, "onComplete: first - " + user.get("first").toString() + " middle - " + user.get("middle").toString());
                    }
                } else {
                    Log.e(TAG, "Error - " + task.getException());
                }
            }
        });
    }


    void getElementFromDB(String table) {
        db.collection(table).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot == null) return;
                    //QuerySnapshot -- это список QueryDocumentSnapshot (список всех "user"-ов в таблице "users" Базы Данных )
                    //QueryDocumentSnapshot -- это объект в БД, один "user", у которого можно будет прочитать свойства
                    /*List<DUnit> units*/units.setValue((ArrayList<DUnit>) querySnapshot.toObjects(DUnit.class));
                    for (DUnit unit:units.getValue()) {
                        Log.e(TAG, "onComplete: name - "+unit.getName()+" "+unit.getSerial());
                    }
                } else {
                    Log.e(TAG, "Error - " + task.getException());
                }
            }
        });
    }

    /*void getElementFromDB(String table) {
        db.collection(table).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    List<DUnit> users = queryDocumentSnapshots.toObjects(User.class);
                    adapter.setUsers(users);
                }
            }
        });
    }*/

    //Слушатель изменений для "user", здесь немного путаница в названиях: здесь объект класса QuerySnapshot (список) называется
    // queryDocumentSnapshots (как будто это объект класса QueryDocumentSnapshot, на самом деле -- это "user", внимательно смотрим), оставил так, потому что так написано
    // в оф. документации
    void addDBListener(String table) {
        db.collection(table).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if (queryDocumentSnapshots != null) {
                    List<DUnit> units = queryDocumentSnapshots.toObjects(DUnit.class);
//                    adapter.setUsers(users);
                }
            }
        });
    }
}
