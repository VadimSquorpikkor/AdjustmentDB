package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squorpikkor.app.adjustmentdb.DUnit;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.MainActivity.TAG;

class FireDBHelper {

    FirebaseFirestore db;
    MutableLiveData<ArrayList<DUnit>> units;

    public FireDBHelper(MutableLiveData<ArrayList<DUnit>> list) {
        db = FirebaseFirestore.getInstance();
        units = list;
    }

    /** Add a new document with a generated ID */
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

    /** Метод загружает элементы из БД (все в текущей таблице). Трюк в том, что метод при получении данных
    * заносит их в коллекцию объектов, которая является Mutable из ViewModel, ссылка на эту коллекцию
    * объект класса FireDBHelper получает в конструкторе. Получается, что приложение, получив данные из БД
    * в облаке сохраняет их в коллекцию, на которую подписан RecyclerView, таким образом изменения в
    * БД автоматом отображаются в списке RecyclerView
    *
    * QuerySnapshot -- это список QueryDocumentSnapshot (список всех "user"-ов в таблице "users" Базы Данных )
    * QueryDocumentSnapshot -- это объект в БД, один "user", у которого можно будет прочитать свойства */
    void getElementFromDB(String table) {
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

    /** Слушатель изменений для "user", здесь немного путаница в названиях: здесь объект класса QuerySnapshot (список) называется
    * queryDocumentSnapshots (как будто это объект класса QueryDocumentSnapshot, на самом деле -- это "user", внимательно смотрим), оставил так, потому что так написано
    * в оф. документации
    * Если срабатывает событие, слушатель запускает загрузку ВСЕХ объектов из БД
    * В будущем нужно будет загружать не все элементы, а только новые элементы в БД (или удалять удаленные из БД)
    * иначе, когда элементов будет много, будет долго, да и трафик лишний */
    void addDBListener(String table) {
        db.collection(table).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                getElementFromDB(table);
            }
        });
    }
}
