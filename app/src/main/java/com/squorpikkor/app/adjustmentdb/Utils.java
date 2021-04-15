package com.squorpikkor.app.adjustmentdb;

public class Utils {

    public static final String EMPTY_VALUE = "- - -";

    /**
     * @param s если параметр null или "", то возвращает "- - -"
     */
    public static String insertRightValue(String s) {
        if (s==null||s.equals("")||s.equals("null")) return EMPTY_VALUE;
        else return s;
    }
}
