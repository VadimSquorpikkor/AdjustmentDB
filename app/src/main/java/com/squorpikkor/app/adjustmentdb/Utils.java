package com.squorpikkor.app.adjustmentdb;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static final String EMPTY_VALUE = "- - -";

    /**
     * @param s если параметр null или "", то возвращает "- - -"
     */
    public static String getRightValue(String s) {
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
    public static String getRightValue(String s, String text) {
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

    @SuppressLint("SimpleDateFormat")
    public static String getRightDateAndTime(long time_stamp_server) {
        String DATE_PATTERN = "dd.MM.yyyy HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
        return formatter.format(time_stamp_server);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getRightDateAndTime(Date date) {
        if (date==null) return EMPTY_VALUE;
        String DATE_PATTERN = "dd.MM.yyyy HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
        return formatter.format(date.getTime());
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
        if (i==11||i==12||i==13||i==14)return "дней";
        switch (i%10) {
            case 1:return "день";
            case 2:
            case 3:
            case 4:return "дня";
            default:return "дней";
        }
    }
}
