package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.squorpikkor.app.adjustmentdb.ui.main.entities.Device;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Entity;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.State;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.Constant.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.Constant.ANY_VALUE_TEXT;
import static com.squorpikkor.app.adjustmentdb.Constant.TYPE_ANY;

/**Сказочно удобный в использовании, я — молодец
 * Решает проблему хранения/получения имен/идентификаторов объектов при выборе позиции из спиннеров
 * Нужно, так как БД работает с идентификаторами, а в приложении отображаются реальные имена (могут
 * быть на разных языках)
 * Из списка объектов Entity (и наследников класса) создается спиннер, при этом хранятся как имена,
 * так и идентификаторы имен. Теперь по позиции выбранного элемента спиннера можно получить и имя и
 * его идентификатор (для поиска по БД)
 * Есть фильтр (setDataByTypeAndLocation) для отображения только тех статусов, которые подходят к
 * выбранной локации и/или типу устройства (для разных локаций/типов статусы могут различаться)
 * Можно добавлять первую строку "-любой-" (также со своим вариантом отображения, например
 * "-любая локация-"), нужно для варианта выбора параметра, когда этот параметр не важен (поиск
 * будет для любого значения, т.е. "найти юнит в любой локации"). Либо ничего не добавлять, в
 * спиннере будет отображаться только список имен объектов
 * При вызове spinnerAdapter.getSelectedNameId() сразу же получаем идентификатор выбранного в
 * спиннере имени ("Диагностика" -> "adj_r_diagnostica")
 * Для всего этого (сделать спиннер, заполнить его данными, сохранить id+имена, добавить "любую"
 * строку) достаточно вызвать setData(devices), что-то типа:
 * mViewModel.getDevices().observe(this, deviceSpinnerAdapter::setData);*/
public class SpinnerAdapter {

    Spinner spinner;
    ArrayList<String> ids;
    ArrayList<String> names;
    Context context;

    public SpinnerAdapter(Spinner spinner, Context context) {
        this.spinner = spinner;
        this.context = context;
    }

    /**Заполняет спиннер именами объектов из списка объектов. Сохраняет идентификаторы.
     * Автоматом первой строкой добавляет "-любой-"*/
    public void setData(ArrayList<? extends Entity> list) {
        if (list==null||list.size()==0)return;//Если список спиннера пустой, то добавлять "-любой-" не нужно
        this.ids = getNameIds(list);
        this.names = getNames(list);
        addFirstLine();
        updateSpinner();
    }

    /**То же, что и setData(ArrayList<? extends Entity> list), только добавляет строку "-без комплекта-", т.е чтобы отобразить те устройства, которые не входят ни в один из комплектов*/
    public void setDataWithEmpty(ArrayList<? extends Entity> list) {
        if (list==null||list.size()==0)return;//Если список спиннера пустой, то добавлять "-любой-" не нужно
        this.ids = getNameIds(list);
        this.names = getNames(list);
        addFirstLineEmpty();
        addFirstLine();
        updateSpinner();
    }

    /**Заполняет спиннер именами объектов из списка объектов. Сохраняет идентификаторы.
     * Вариант setData с возможностью задать, как будет называться первая добавленная строка для
     * ANY_VALUE. Если указано null, то добавочная строка создаваться не будет*/
    public void setData(ArrayList<? extends Entity> list, String s) {
        if (list==null||list.size()==0)return;//Если список спиннера пустой, то добавлять "-любой-" не нужно
        this.ids = getNameIds(list);
        this.names = getNames(list);
        if (s!=null) addFirstLine(s);
        updateSpinner();
    }

    /**Метод работает только для типов State
     * Метод делает спинер, но в отличии от setData отбирает статусы по типу и локации*/
    @SuppressWarnings("unused")
    public void setDataByTypeAndLocation(ArrayList<State> list, String typeId, String locationId) {
        if (list==null||list.size()==0)return;//Если список спиннера пустой, то добавлять "-любой-" не нужно
        this.ids = getNameIdsByTypeAndLocation(list, typeId, locationId);
        this.names = getNamesByTypeAndLocation(list, typeId, locationId);
        addFirstLine();
        updateSpinner();
    }

    /**Метод работает только для типов State
     * Метод делает спинер, но в отличии от setData отбирает статусы по типу и локации. Вариант
     * setDataByTypeAndLocation с возможностью задать, как будет называться первая добавленная
     * строка для ANY_VALUE. Если указано null, то добавочная строка создаваться не будет*/
    public void setDataByTypeAndLocation(ArrayList<State> list, String typeId, String locationId, String s) {
        if (list==null||list.size()==0)return;//Если список спиннера пустой, то добавлять "-любой-" не нужно
        this.ids = getNameIdsByTypeAndLocation(list, typeId, locationId);
        this.names = getNamesByTypeAndLocation(list, typeId, locationId);
        if (s!=null) addFirstLine(s);
        updateSpinner();
    }

    /**Метод работает только для типов Device
     * Метод делает спинер, но в отличии от setData отбирает устройства по типу комплекта
     * (только для 1117, например)*/
    public void setDataByDevSet(ArrayList<Device> list, String devSetId) {
        if (list==null||list.size()==0)return;//Если список спиннера пустой, то добавлять "-любой-" не нужно
        this.ids = getNameIdsByDevSet(list, devSetId);
        this.names = getNamesByDevSet(list, devSetId);
        addFirstLine();
        updateSpinner();
    }

    /**Метод работает только для типов Device
     * Метод делает спинер, но в отличии от setData отбирает устройства по типу комплекта
     * (только для 1117, например)
     * Вариант setDataByDevSet с возможностью задать, как будет называться первая добавленная
     * строка для ANY_VALUE. Если указано null, то добавочная строка создаваться не будет*/
    public void setDataByDevSet(ArrayList<Device> list, String devSetId, String s) {
        if (list==null||list.size()==0)return;//Если список спиннера пустой, то добавлять "-любой-" не нужно
        this.ids = getNameIdsByDevSet(list, devSetId);
        this.names = getNamesByDevSet(list, devSetId);
        if (s!=null) addFirstLine(s);
        updateSpinner();
    }

    private void addFirstLine(String s) {
        ids.add(0, ANY_VALUE);
        names.add(0, s);
        updateSpinner();
    }

    public void addFirstLine() {
        ids.add(0, ANY_VALUE);
        names.add(0, ANY_VALUE_TEXT);
        updateSpinner();
    }

    public void addFirstLineEmpty() {
        ids.add(0, "");
        names.add(0, "-без комплекта-");
        updateSpinner();
    }

    /**Главная магия: сразу же получаем идентификатор выбранного в
     * спиннере имени ("Диагностика" -> "adj_r_diagnostica")*/
    public String getSelectedNameId() {
        if (ids==null)return "";
        return this.ids.get(spinner.getSelectedItemPosition());
    }

    private void updateSpinner() {
        ArrayList<String> newList = new ArrayList<>(names);
        //вместо newList.add(0, ANY_VALUE_TEXT);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, newList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private ArrayList<String> getNames(ArrayList<? extends Entity> list) {
        if (list==null||list.size()==0)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            newList.add(list.get(i).getName());
        }
        return newList;
    }

    public ArrayList<String> getNamesByTypeAndLocation(ArrayList<State> states, String typeId, String locationId) {
        if (states==null||states.size()==0||typeId==null)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
            if (locationId.equals(ANY_VALUE)) {
                for (int i = 0; i < states.size(); i++) {
                    if (states.get(i).getType().equals(TYPE_ANY)
                            || states.get(i).getType().equals(typeId)) newList.add(states.get(i).getName());
                }
            } else {
                for (int i = 0; i < states.size(); i++) {
                    if ((states.get(i).getType().equals(TYPE_ANY)
                            || states.get(i).getType().equals(typeId))
                            && states.get(i).getLocation().equals(locationId)) newList.add(states.get(i).getName());
                }
            }
        return newList;
    }

    /**перебор из строки "1117M&6101", это будет значить, что у девайса 2 возможных варианта комплекта (парсить строку в массив имен разбивая по "&": 1117M%6101 -> [1117M, 6101])*/
    private boolean isInArray(String input, String reg) {
        String[] arr = input.split("&");
        for (String str:arr) {
            if (str.equals(reg)) return true;
        }
        return false;
    }

    /**Возвращает name_id по имени комплекта.
     * Принимает не только строку типа "1117", но и строку типа "1117M&6101",
     * т.е. у девайса может быть несколько возможных комплектов*/
    private ArrayList<String> getNameIdsByDevSet(ArrayList<Device> devices, String devSetId) {
        if (devices==null||devices.size()==0||devSetId==null)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        for (int i = 0; i < devices.size(); i++) {
//            if (devSetId.equals(ANY_VALUE) || devices.get(i).getDevSetId().equals(devSetId)) newList.add(devices.get(i).getNameId());
            if (devSetId.equals(ANY_VALUE) || isInArray(devices.get(i).getDevSetId(), devSetId)) newList.add(devices.get(i).getNameId());
        }
        return newList;
    }

    /**Возвращает name по имени комплекта.
     * Принимает не только строку типа "1117", но и строку типа "1117M&6101",
     * т.е. у девайса может быть несколько возможных комплектов*/
    private ArrayList<String> getNamesByDevSet(ArrayList<Device> devices, String devSetId) {
        if (devices==null||devices.size()==0||devSetId==null)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        for (int i = 0; i < devices.size(); i++) {
//            if (devSetId.equals(ANY_VALUE) || devices.get(i).getDevSetId().equals(devSetId)) newList.add(devices.get(i).getName());
//            if (devSetId.equals(ANY_VALUE) || isInArray(devices.get(i).getDevSetId(), devSetId)) newList.add(devices.get(i).getNameId());
            if (devSetId.equals(ANY_VALUE) || isInArray(devices.get(i).getDevSetId(), devSetId)) newList.add(devices.get(i).getName());
        }
        return newList;
    }

    private ArrayList<String> getNameIds(ArrayList<? extends Entity> list) {
        if (list==null||list.size()==0)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            newList.add(list.get(i).getNameId());
        }
        return newList;
    }


    public ArrayList<String> getNameIdsByTypeAndLocation(ArrayList<State> states, String typeId, String locationId) {
        if (states==null||states.size()==0||typeId==null)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        if (locationId.equals(ANY_VALUE)) {
            for (int i = 0; i < states.size(); i++) {
                if (states.get(i).getType().equals(TYPE_ANY)
                        || states.get(i).getType().equals(typeId)) newList.add(states.get(i).getNameId());
            }
        } else {
            for (int i = 0; i < states.size(); i++) {
                if ((states.get(i).getType().equals(TYPE_ANY)
                        || states.get(i).getType().equals(typeId))
                        && states.get(i).getLocation().equals(locationId)) newList.add(states.get(i).getNameId());
            }
        }
        return newList;
    }

}
