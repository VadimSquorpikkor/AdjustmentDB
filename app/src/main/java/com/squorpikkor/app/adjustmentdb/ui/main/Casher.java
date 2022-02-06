package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;

import com.squorpikkor.app.adjustmentdb.SaveLoad;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Device;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.DeviceSet;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Location;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.State;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.ui.main.FireDBHelper.APP_DB_VERSION;

/**Для уменьшения количества запросов в БД применяется кэширование — сохранение полученных данных из БД и
 использование их при необходимости без повторного обращения к БД. В БД хранится номер её версии, приложение
 при первом подключении к БД загружает данные и сохраняет их. Также приложение сохраняет версию БД. При
 последующих подключениях приложение сверяет свой номер версии с номером версии, хранящимся в БД. Если версия
 совпадает, приложение загружает данные из своей памяти, иначе подключается к БД и загружает их (при этом
 обновляя номер версии в устройстве). Сейчас сохраняются следующие таблицы: devices, locations, device_set.
 Для каждой сущности сохраняются не только данные хранящиеся в таблице, но и связанные с этой таблицей имена
 (которые берутся из таблицы names). Сама таблица names не кэшируется и не будет кэшироваться в будущем, так
 как данные из этой таблицы для devices, locations и device_set уже сохранены при сохранении в кэш этих
 коллекций.

 Такой вариант получения данных (кэширование плюс версия БД) решает проблему, когда нужно ограничить чтение
 БД при этом обеспечить актуальность данных в БД (вариант захардкодить данные плох тем, что при обновлении
 данных в БД нужно будет всем пользователям НЕМЕДЛЕННО обновить версию приложения, читай: собрать у всех
 пользователей смартфоны и самому обновить версию)

 Другими словами: при изменении данных в БД, данные в приложении обновляются: а) удаленно; б) автоматически,
 без участия пользователя; в) без необходимости обновлять приложение*/
class Casher {

    private static final String CUT = "@@@";
    private static final String DEVICES = "save_devices";
    private static final String LOCATIONS = "save_locations";
    private static final String DEVICE_SETS = "save_dev_sets";
    private static final String STATES = "save_states";
//--------------------------------------------------------------------------------------------------
    void saveDevicesToCash(ArrayList<Device> data, int dbVersion) {
        saveDeviceCash(data);//сохраняем в кэш
        SaveLoad.save(APP_DB_VERSION, dbVersion);//обновляем номер версии БД
    }

    ArrayList<Device> getDeviceCash() {
        Log.e("TAG", "...из кэша");
        ArrayList<String> list = SaveLoad.loadStringArray(DEVICES);
        ArrayList<Device> newDevices = new ArrayList<>();
        for (String s:list) newDevices.add(parseDeviceFromString(s));
        return newDevices;
    }

    private void saveDeviceCash(ArrayList<Device> devs) {
        Log.e("TAG", "...сохраняем в кэш");
        ArrayList<String> list = new ArrayList<>();
        for (Device d:devs) list.add(parseStringFromDevice(d));
        SaveLoad.saveArray(DEVICES, list);
    }

    private Device parseDeviceFromString(String s) {
        Log.e("TAG", "parseDeviceFromString: "+s);
        String[] ar = s.split(CUT);
        String id = ar[0];
        String name = ar[1];
        String engName = ar[2];
        String devSetId = ar[3];
        String imgPath = ar[4];
        return new Device(id, name, engName, devSetId, imgPath);
    }

    private String parseStringFromDevice(Device d) {
        return d.getId()+CUT+
               d.getName()+CUT+
               d.getEngName()+CUT+
               d.getDevSetId()+CUT+
               d.getImgPath();
    }
//--------------------------------------------------------------------------------------------------
    void saveLocationsToCash(ArrayList<Location> data, int dbVersion) {
        saveLocationsCash(data);//сохраняем в кэш
        SaveLoad.save(APP_DB_VERSION, dbVersion);//обновляем номер версии БД
    }

    ArrayList<Location> getLocationCash() {
        Log.e("TAG", "...из кэша location");
        ArrayList<String> list = SaveLoad.loadStringArray(LOCATIONS);
        ArrayList<Location> newLocations = new ArrayList<>();
        for (String s:list) newLocations.add(parseLocationFromString(s));
        return newLocations;
    }

    private void saveLocationsCash(ArrayList<Location> data) {
        Log.e("TAG", "...сохраняем в кэш");
        ArrayList<String> list = new ArrayList<>();
        for (Location d:data) list.add(parseStringFromLocation(d));
        SaveLoad.saveArray(LOCATIONS, list);
    }

    private Location parseLocationFromString(String s) {
        Log.e("TAG", "parseLocationFromString: "+s);
        String[] ar = s.split(CUT);
        String id = ar[0];
        String name = ar[1];
        return new Location(id, name);
    }

    private String parseStringFromLocation(Location d) {
        return d.getId()+CUT+
                d.getName();
    }
//--------------------------------------------------------------------------------------------------
    void saveDeviceSetsToCash(ArrayList<DeviceSet> data, int dbVersion) {
        saveDevSetCash(data);//сохраняем в кэш
        SaveLoad.save(APP_DB_VERSION, dbVersion);//обновляем номер версии БД
    }

    ArrayList<DeviceSet> getDevSetCash() {
        Log.e("TAG", "...из кэша DevSet");
        ArrayList<String> list = SaveLoad.loadStringArray(DEVICE_SETS);
        ArrayList<DeviceSet> newDeviceSets = new ArrayList<>();
        for (String s:list) newDeviceSets.add(parseDevSetFromString(s));
        return newDeviceSets;
    }

    private void saveDevSetCash(ArrayList<DeviceSet> data) {
        Log.e("TAG", "...сохраняем в кэш DevSet");
        ArrayList<String> list = new ArrayList<>();
        for (DeviceSet d:data) list.add(parseStringFromDevSet(d));
        SaveLoad.saveArray(DEVICE_SETS, list);
    }

    private DeviceSet parseDevSetFromString(String s) {
        Log.e("TAG", "parseDevSetsFromString: "+s);
        String[] ar = s.split(CUT);
        String id = ar[0];
        String name = ar[1];
        return new DeviceSet(id, name);
    }

    private String parseStringFromDevSet(DeviceSet d) {
        return d.getId()+CUT+
                d.getName();
    }
//--------------------------------------------------------------------------------------------------
    void saveStatesToCash(ArrayList<State> data, int dbVersion) {
        saveStatesCash(data);//сохраняем в кэш
        SaveLoad.save(APP_DB_VERSION, dbVersion);//обновляем номер версии БД
    }

    ArrayList<State> getStateCash() {
        Log.e("TAG", "...из кэша State");
        ArrayList<String> list = SaveLoad.loadStringArray(STATES);
        ArrayList<State> newStates = new ArrayList<>();
        for (String s:list) newStates.add(parseStateFromString(s));
        return newStates;
    }

    private void saveStatesCash(ArrayList<State> data) {
        Log.e("TAG", "...сохраняем в кэш State");
        ArrayList<String> list = new ArrayList<>();
        for (State d:data) list.add(parseStringFromState(d));
        SaveLoad.saveArray(STATES, list);
    }

    private State parseStateFromString(String s) {
        Log.e("TAG", "parseStateFromString: "+s);
        String[] ar = s.split(CUT);
        String id = ar[0];
        String name = ar[1];
        String type = ar[2];
        String location = ar[3];
        return new State(id, name, type, location);
    }

    private String parseStringFromState(State d) {
        return d.getId()+CUT+
                d.getName()+CUT+
                d.getType()+CUT+
                d.getLocation();
    }
}
