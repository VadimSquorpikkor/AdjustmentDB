package com.squorpikkor.app.adjustmentdb.ui.main;

import android.util.Log;

import com.squorpikkor.app.adjustmentdb.SaveLoad;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Device;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.DeviceSet;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Location;

import java.util.ArrayList;

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
//--------------------------------------------------------------------------------------------------
    ArrayList<Device> getDeviceCash() {
        Log.e("TAG", "...из кэша");
        ArrayList<String> list = SaveLoad.loadStringArray(DEVICES);
        ArrayList<Device> newDevices = new ArrayList<>();
        for (String s:list) newDevices.add(parseDeviceFromString(s));
        return newDevices;
    }

    void saveDeviceCash(ArrayList<Device> devs) {
        Log.e("TAG", "...сохраняем в кэш");
        ArrayList<String> list = new ArrayList<>();
        for (Device d:devs) list.add(parseStringFromDevice(d));
        SaveLoad.saveArray(DEVICES, list);
    }

    private Device parseDeviceFromString(String s) {
        Log.e("TAG", "parseDeviceFromString: "+s);
        String[] ar = s.split(CUT);
        String id = ar[0];
//        String nameId = ar[1];
        String name = ar[1];
        String engName = ar[2];
        String devSetId = ar[3];
        String imgPath = ar[4];
        return new Device(id, name, engName, devSetId, imgPath);
    }

    private String parseStringFromDevice(Device d) {
        return d.getId()+CUT+
//               d.getNameId()+CUT+
               d.getName()+CUT+
               d.getEngName()+CUT+
               d.getDevSetId()+CUT+
               d.getImgPath();
    }
//--------------------------------------------------------------------------------------------------
    public ArrayList<Location> getLocationCash() {
        Log.e("TAG", "...из кэша location");
        ArrayList<String> list = SaveLoad.loadStringArray(LOCATIONS);
        ArrayList<Location> newLocations = new ArrayList<>();
        for (String s:list) newLocations.add(parseLocationFromString(s));
        return newLocations;
    }

    public void saveLocationsCash(ArrayList<Location> data) {
        Log.e("TAG", "...сохраняем в кэш");
        ArrayList<String> list = new ArrayList<>();
        for (Location d:data) list.add(parseStringFromLocation(d));
        SaveLoad.saveArray(LOCATIONS, list);
    }

    private Location parseLocationFromString(String s) {
        Log.e("TAG", "parseLocationFromString: "+s);
        String[] ar = s.split(CUT);
        String id = ar[0];
//        String nameId = ar[1];
        String name = ar[1];
        return new Location(id, name);
    }

    private String parseStringFromLocation(Location d) {
        return d.getId()+CUT+
//                d.getNameId()+CUT+
                d.getName();
    }
//--------------------------------------------------------------------------------------------------
    public ArrayList<DeviceSet> getDevSetCash() {
        Log.e("TAG", "...из кэша DevSet");
        ArrayList<String> list = SaveLoad.loadStringArray(DEVICE_SETS);
        ArrayList<DeviceSet> newDeviceSets = new ArrayList<>();
        for (String s:list) newDeviceSets.add(parseDevSetFromString(s));
        return newDeviceSets;
    }

    public void saveDevSetCash(ArrayList<DeviceSet> data) {
        Log.e("TAG", "...сохраняем в кэш DevSet");
        ArrayList<String> list = new ArrayList<>();
        for (DeviceSet d:data) list.add(parseStringFromDevSet(d));
        SaveLoad.saveArray(DEVICE_SETS, list);
    }

    private DeviceSet parseDevSetFromString(String s) {
        Log.e("TAG", "parseDevSetsFromString: "+s);
        String[] ar = s.split(CUT);
        String id = ar[0];
//        String nameId = ar[1];
        String name = ar[1];
        return new DeviceSet(id, name);
    }

    private String parseStringFromDevSet(DeviceSet d) {
        return d.getId()+CUT+
//                d.getNameId()+CUT+
                d.getName();
    }

}
