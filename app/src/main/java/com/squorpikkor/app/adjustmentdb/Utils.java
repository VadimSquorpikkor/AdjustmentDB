package com.squorpikkor.app.adjustmentdb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    public static String getRightDate(long time_stamp_server) {
        String DATE_PATTERN = "dd.MM.yyyy HH:mm";
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
}
