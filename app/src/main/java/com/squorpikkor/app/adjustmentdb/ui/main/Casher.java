package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;

import com.squorpikkor.app.adjustmentdb.SaveLoad;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Device;

import java.util.ArrayList;

class Casher {

    private static final String CUT = "@@@";
    private static final String DEVICES = "save_devices";

    ArrayList<Device> getDeviceCash() {
        Log.e("TAG", "...из кэша");
        return loadDevices();
    }

    void saveDeviceCash(ArrayList<Device> devs) {
        Log.e("TAG", "...сохраняем в кэш");
        ArrayList<String> list = new ArrayList<>();
        for (Device d:devs) list.add(parseStringFromDevice(d));
        SaveLoad.saveArray(DEVICES, list);
    }

    ArrayList<Device> loadDevices() {
        ArrayList<String> list = SaveLoad.loadStringArray(DEVICES);
        ArrayList<Device> newDevices = new ArrayList<>();
        for (String s:list) newDevices.add(parseDeviceFromString(s));
        return newDevices;
    }

    Device parseDeviceFromString(String s) {
        String[] ar = s.split(CUT);
        String id = ar[0];
        String nameId = ar[1];
        String name = ar[2];
        String engName = ar[3];
        String devSetId = ar[4];
        String imgPath = ar[5];
        return new Device(id, nameId, name, engName, devSetId, imgPath);
    }

    String parseStringFromDevice(Device d) {
        return d.getId()+CUT+
               d.getNameId()+CUT+
               d.getName()+CUT+
               d.getEngName()+CUT+
               d.getDevSetId()+CUT+
               d.getImgPath();
    }


}
