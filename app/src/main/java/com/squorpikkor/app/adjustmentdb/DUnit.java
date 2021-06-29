package com.squorpikkor.app.adjustmentdb;

import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;

import java.util.Date;

import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.SERIAL_TYPE;

public class DUnit {

    private String id; //"0001"
    private String name; //БДКГ-02
    private String innerSerial; //№12345
    private String serial; //132.002
    private String type; //"Ремонтный"
    private String state; //"На линейке"
    private String employee; //Фамилия ответственного
    private String eventId;
    private Date date; //todo сейчас это дата последнего ивента, надо переделать на дату начала ремонта/серии (т.е. при создании юнита сохраняется дата его создания)

//    private DEvent event;

    public DUnit() {
    }

    public DUnit(String id, String name, String innerSerial, String serial, String type) {
        this.id = id;
        this.name = name;
        this.innerSerial = innerSerial;
        this.serial = serial;
        this.type = type;
    }

    public DUnit(String id, String name, String innerSerial, String serial, String type, Date date) {
        this.id = id;
        this.name = name;
        this.innerSerial = innerSerial;
        this.serial = serial;
        this.type = type;
        this.date = date;
    }

    public void addNewEvent(DEvent newEvent, MainViewModel model) {
//        String oldEventId = this.eventId;
//        model.
    }

    /**Возвращает true, если это ремонтное устройство*/
    public boolean isRepairUnit() {
        return type.equals(REPAIR_TYPE);
    }

    /**Возвращает true, если это серийное устройство*/
    public boolean isSerialUnit() {
        return type.equals(SERIAL_TYPE);
    }

    /**Имя последнего статуса устройства*/
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**Имя устройства (БДКГ-02)*/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**Внутренний номер устройства*/
    public String getInnerSerial() {
        return innerSerial;
    }

    public void setInnerSerial(String serial) {
        this.innerSerial = serial;
    }

    /**Серийный номер устройства*/
    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    /**Идентификатор устройства (для серии - AT6130_123, для ремонтных - r_0005)*/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**Тип устройства: серийный или ремонтный*/
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**Фамилия ответственного*/
    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
