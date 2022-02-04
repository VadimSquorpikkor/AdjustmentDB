package com.squorpikkor.app.adjustmentdb;

public class Constant {

    public static final String TABLE_NAMES = "names";

    //Новые стринги для новой БД:
    public static final String TABLE_UNITS = "units";
    public static final String UNIT_DATE = "date";
    public static final String UNIT_CLOSE_DATE = "close_date";
    public static final String UNIT_DEVICE = "device_id"; //todo возможно в имени стринга и не нужен "_ID", только в значении
    public static final String UNIT_DEVICE_SET = "devset_id";
    public static final String UNIT_EMPLOYEE = "employee_id";
    public static final String UNIT_ID = "id";
    public static final String UNIT_EVENT_ID = "event_id";
    public static final String UNIT_INNER_SERIAL = "inner_serial";
    public static final String UNIT_LOCATION = "location_id";
    public static final String UNIT_SERIAL = "serial";
    public static final String UNIT_STATE = "state_id";
    public static final String UNIT_TYPE = "type_id";//todo по-хорошему нужна коллекция тайпов. Пока обойдусь
    public static final String UNIT_TRACKID = "trackid";

    public static final String TABLE_STATES = "states"; //в прошлом profile
    public static final String STATE_ID = "id";
    public static final String STATE_NAME_ID = "name_id";
    public static final String STATE_LOCATION = "location_id";
    public static final String STATE_NAME = "name";
    public static final String STATE_TYPE = "type_id";

    public static final String TABLE_EVENTS = "events"; //в прошлом states
    public static final String EVENT_DATE = "date";
    public static final String EVENT_CLOSE_DATE = "close_date";
    public static final String EVENT_DESCRIPTION = "description";
    public static final String EVENT_LOCATION = "location_id";
    public static final String EVENT_STATE = "state_id";
    public static final String EVENT_UNIT = "unit_id";

    public static final String TABLE_EMPLOYEES = "employees"; //в прошлом users
    public static final String EMPLOYEE_EMAIL = "email"; //email нельзя использовать в качестве id, так как у пользователя может поменяться email, и тогда при необходимости выбрать устройства пользователя нужно будет искать и по старому email и по новому
    public static final String EMPLOYEE_ID = "id";
    public static final String EMPLOYEE_NAME_ID = "name_id";
    public static final String EMPLOYEE_LOCATION = "location_id";

    public static final String TABLE_LOCATIONS = "locations";
    public static final String LOCATION_ID = "id";
    public static final String LOCATION_NAME_ID = "name_id";

    public static final String TABLE_DEVICES = "devices";
    public static final String DEVICE_ID = "id";
    public static final String DEVICE_NAME_ID = "name_id";
    public static final String DEVICE_DEV_SET_ID = "devset_id";
    public static final String DEVICE_IMG_PATH = "img_path";

    public static final String TABLE_DEVICE_SET = "device_set";
    public static final String DEVICE_SET_ID = "id";
    public static final String DEVICE_SET_NAME_ID = "name_id";

    public static final String EMPTY_LOCATION_ID = "empty_location_id";
    public static final String EMPTY_LOCATION_NAME_2 = "Локация не найдена";
    public static final String EMPTY_LOCATION_NAME = "Пользователь не зарегистрирован";
    public static final String LOCATION_ADJUSTMENT = "adjustment";
    public static final String LOCATION_GR_SERVISA = "repair_area";
    //--------------------------------------------------------------------------------------------------
    public static final String TYPE_ANY = "any_type";
    public static final String SERIAL_TYPE = "serial_type";
    public static final String REPAIR_TYPE = "repair_type";

    public static final String SPLIT_SYMBOL = " ";
    public static final String REPAIR_UNIT = "Ремонт";
    public static final String SERIAL_UNIT = "Серия";
    public static final String ANY_VALUE = "any_value";
    public static final String ANY_VALUE_TEXT = "- любой -";//"- любой -"
    public static final String EMPTY_VALUE_TEXT = "- не выбран -";//"- не выбран -"
    public static final String IS_COMPLETE = "ЗАВЕРШЕНО";
    public static final String LESS_THAN_ONE = " <1";
//--------------------------------------------------------------------------------------------------

    public static final String BACK_PRESS_SEARCH = "back_press_search";
    public static final String BACK_PRESS_SEARCH_WHEN_FOUND = "back_press_search_when_found";
    public static final String BACK_PRESS_SINGLE = "back_press_single";
    public static final String BACK_PRESS_STATES = "back_press_states";
    public static final String BACK_PRESS_MULTI_STATES = "back_press_multi_states";
    public static final String BACK_PRESS_MULTI = "back_press_multi";
    public static final String BACK_PRESS_INFO_FRAGMENT = "back_press_info";

//--------------------------------------------------------------------------------------------------
}
