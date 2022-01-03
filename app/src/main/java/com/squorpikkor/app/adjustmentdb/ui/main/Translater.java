package com.squorpikkor.app.adjustmentdb.ui.main;

import androidx.lifecycle.MutableLiveData;

import com.squorpikkor.app.adjustmentdb.ui.main.entities.Location;

import java.util.ArrayList;

class Translater {



    /**id -> name*/
    void decode(MutableLiveData<ArrayList<Location>> encodedLocations, MutableLiveData<ArrayList<Location>> decodedData) {
        ArrayList<Location> decodedList = new ArrayList<>();
        if (encodedLocations.getValue()==null) decodedData.setValue(decodedList);
        for (Location location:encodedLocations.getValue()) decodedList.add(new Location(location.getId(), getNameById(location.getName())));
        decodedData.setValue(decodedList);
    }

    private String getNameById(String name) {
        return name;
    }
}
