package com.squorpikkor.app.adjustmentdb;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Utils {

    public static final String EMPTY_VALUE = "- - -";

    /**
     * @param s если параметр null или "", то возвращает "- - -"
     */
    public static String insertRightValue(String s) {
        if (isEmptyOrNull(s)) return EMPTY_VALUE;
        else return s;
    }

    /**
     * Если текст пустой, возвращает параметр;
     * если текст не пустой, но пустой параметр, возвращает тект;
     * если текст не пустой и параметр не пустой, возвращает параметр
     * @param s параметр
     * @param text текст
     */
    public static String insertRightValue(String s, String text) {
        boolean paramIsEmpty = isEmptyOrNull(s);
        boolean textIsEmpty = isEmptyOrNull(text);

        if (textIsEmpty) return s;
        else if (paramIsEmpty) return text;
        else return s;
    }

    /**Возвращает true: если null, если "", если "null" */
    public static boolean isEmptyOrNull(String s) {
        return s == null || s.equals("") || s.equals("null");
    }

    /**Заменяет имя на его идентификатор (переводчик)
     * @param name имя ("Диагностика")
     * @param nameList лист имен
     * @param idList лист идентификаторов
     * @return id ("adj_r_diagnostica")
     */
    public static String getIdByName(String name, ArrayList<String> nameList, ArrayList<String> idList) {
        int position = nameList.indexOf(name);
        return idList.get(position);
    }

    /**Заменяет идентификатор на его имя (переводчик). Если id не найден, то возвращает "- - -"
     * @param id имя ("adj_r_diagnostica")
     * @param nameList лист имен
     * @param idList лист идентификаторов
     * @return name ("Диагностика")
     */
    public static String getNameById(String id, ArrayList<String> nameList, ArrayList<String> idList) {
        int position = idList.indexOf(id);
        if (position==-1)return EMPTY_VALUE;
        else return nameList.get(position);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getRightDateAndTime(long time_stamp_server) {
        String DATE_PATTERN = "dd.MM.yyyy HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
        return formatter.format(time_stamp_server);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getRightDate(long time_stamp_server) {
        String DATE_PATTERN = "dd.MM.yyyy";
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
        return formatter.format(time_stamp_server);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getRightTime(long time_stamp_server) {
        String DATE_PATTERN = "HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
        return formatter.format(time_stamp_server);
    }

    public static int daysPassed(Date startDate) {
        return daysPassed(startDate, new Date());

    }

    public static int daysPassed(Date startDate, Date endDate) {
        return ((int)((endDate.getTime()/(24*60*60*1000))
                -(int)(startDate.getTime()/(24*60*60*1000))));
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    public static void insertValueOrGone(String value, TextView view) {
        if (isEmptyOrNull(value)) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(value);
        }
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    public static void insertValueOrGone(String value, TextView view, String format) {
        if (isEmptyOrNull(value)) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(String.format(format, value));
        }
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    public static String rightDayString(int i) {
        switch (i%10) {
            case 1:return "день";
            case 2:
            case 3:
            case 4:return "дня";
            default:return "дней";
        }
    }
}
