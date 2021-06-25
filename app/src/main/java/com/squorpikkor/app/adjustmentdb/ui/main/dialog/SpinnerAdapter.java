package com.squorpikkor.app.adjustmentdb.ui.main.dialog;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.Entity;
import com.squorpikkor.app.adjustmentdb.ui.main.entities.State;

import java.util.ArrayList;

import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE_TEXT;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.TYPE_ANY;

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
        this.ids = getNameIds(list);
        this.names = getNames(list);
        addFirstLine();
        updateSpinner();
    }

    /**Вариант setData с возможностью задать, как будет называться первая добавленная строка для
     * ANY_VALUE. Если указано null, то добавочная строка создаваться не будет*/
    public void setData(ArrayList<? extends Entity> list, String s) {
        this.ids = getNameIds(list);
        this.names = getNames(list);
        if (s!=null) addFirstLine(s);
        updateSpinner();
    }

    /**Метод работает только для типов State
     * Метод делает спинер, но в отличии от setData отбирает статусы по типу и локации*/
    public void setDataByTypeAndLocation(ArrayList<State> list, String typeId, String locationId) {
        this.ids = getNameIdsByTypeAndLocation(list, typeId, locationId);
        this.names = getNamesByTypeAndLocation(list, typeId, locationId);
        addFirstLine();
        updateSpinner();
    }

    public void setDataByTypeAndLocation(ArrayList<State> list, String typeId, String locationId, String s) {
        this.ids = getNameIdsByTypeAndLocation(list, typeId, locationId);
        this.names = getNamesByTypeAndLocation(list, typeId, locationId);
        if (s!=null) addFirstLine(s);
        updateSpinner();
    }

    public void addFirstLine(String s) {
        //todo можно потом будет сделать добавление извне, сейчас все спиннерам добавляется
        // одинаковая первая строка: "-любой-", через addFirstLine можно будет добавлять
        // индивидуально (пока это не нужно)
        ids.add(0, ANY_VALUE);
        names.add(0, s);
        updateSpinner();
    }

    public void addFirstLine() {
        ids.add(0, ANY_VALUE);
        names.add(0, ANY_VALUE_TEXT);
        updateSpinner();
    }

    /**Главная магия: сразу же получаем идентификатор выбранного в
     * спиннере имени ("Диагностика" -> "adj_r_diagnostica")*/
    public String getSelectedNameId() {
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

    private ArrayList<String> getNamesByTypeAndLocation(ArrayList<State> states, String typeId, String locationId) {
        if (states==null||states.size()==0)return new ArrayList<>();
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

    private ArrayList<String> getNameIds(ArrayList<? extends Entity> list) {
        if (list==null||list.size()==0)return new ArrayList<>();
        ArrayList<String> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            newList.add(list.get(i).getNameId());
        }
        return newList;
    }

    private ArrayList<String> getNameIdsByTypeAndLocation(ArrayList<State> states, String typeId, String locationId) {
        if (states==null||states.size()==0)return new ArrayList<>();
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
