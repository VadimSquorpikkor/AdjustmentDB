package com.squorpikkor.app.adjustmentdb.ui.main;


import androidx.lifecycle.MutableLiveData;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Location;

import java.util.ArrayList;

/** Перевод nameId -> name при получении данных из БД и перевод name -> nameId при отправке данных
 * в БД. После получении данных приложение работает только со значениями name.
 * Другими словами: приложение ничего не знает о nameId, класс FireBaseHelper ничего не знает о name,
 * общаются друг с другом через Bridge, это что-то типа переводчика*/
public class Bridge {

    private final FireDBHelper dbh;
    private MutableLiveData<ArrayList<Location>> encodedLocations;
    private final Dictionary dictionary;
    private Translater translater;

    public Bridge() {
        this.dbh = new FireDBHelper();
        dictionary = new Dictionary();
        translater = new Translater();
    }

    public void getLocations(MutableLiveData<ArrayList<Location>> decodedData, MutableLiveData<Boolean> canWorks) {
        encodedLocations = new MutableLiveData<>();
        encodedLocations.observeForever(list -> translater.decode(encodedLocations, decodedData));

        dbh.getLocations(encodedLocations, canWorks);
    }

    /*Location decode(Location location) {
        return new Location(location.getId(), dictionary.getLocation(location.getName()));
    }*/

}
