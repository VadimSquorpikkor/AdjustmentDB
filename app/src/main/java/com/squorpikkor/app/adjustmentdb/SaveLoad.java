package com.squorpikkor.app.adjustmentdb;

import android.content.Context;
import android.content.SharedPreferences;

import com.squorpikkor.app.adjustmentdb.app.App;

import java.util.ArrayList;

public class SaveLoad {
    /**Сохранение по ключу*/
    public static void saveParam(String key, int param) {
        SharedPreferences mPreferences = App.getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        mPreferences.edit().putInt(key, param).apply();
    }

    public static void saveParam(String key, String param) {
        SharedPreferences mPreferences = App.getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        mPreferences.edit().putString(key, param).apply();
    }

    /**Загрузка int по ключу*/
    public static int loadIntParam(String key) {
        SharedPreferences mPreferences = App.getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        int value = 0;
        if (mPreferences.contains(key)) {
            value = mPreferences.getInt(key, 0);
        }
        return value;
    }

    public static String loadStringParam(String key) {
        SharedPreferences mPreferences = App.getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        String value = "";
        if (mPreferences.contains(key)) {
            value = mPreferences.getString(key, "");
        }
        return value;
    }

    /**Сохранение массива по ключу*/
    public static void saveArray(String key, ArrayList<String> list) {
        SharedPreferences mPreferences = App.getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();

        //очистить. если не очищать, то в случае, когда размер массива меньше сохраненного ранее, будет оставаться "хвост" предыдущего массива
        int count = 0;
        while (mPreferences.contains(key + count)) {
            editor.remove(key + count);
            count++;
        }

        for (int i = 0; i < list.size(); i++) {
            editor.putString(key + i, list.get(i));
        }
        editor.apply();
    }

    /**Загрузка ArrayList<String> по ключу*/
    public static ArrayList<String> loadStringArray(String key) {
        SharedPreferences mPreferences = App.getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        ArrayList<String> list = new ArrayList<>();
        int count = 0;
        while (mPreferences.contains(key + count)) {
            String s = mPreferences.getString(key + count, "");
            list.add(s);
            count++;
        }
        return list;
    }
}
