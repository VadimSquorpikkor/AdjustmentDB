package com.squorpikkor.app.adjustmentdb;

import android.util.Log;

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
    private Date date;
    private Date closeDate;

    private DEvent event;

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

    public void closeUnit() {
        this.closeDate = new Date();
    }

    //Если юнит завершен, то у него появляется дата закрытия
    public boolean isComplete() {
        return this.closeDate!=null;
    }

    /**Сколько дней юнит находится в серии/ремонте. Если юнит не закрыт, то считается количество
     * дней от начала ремонта/серии до сегодняшнего дня, если юнит закрыт — до дня закрытия*/
    public int daysPassed() {
        if (this.date==null) return 0;
        Date end = closeDate==null?new Date():closeDate;
        return (int)((end.getTime()-this.date.getTime())/(1000*60*60*24));
    }

    public void addNewEvent(DEvent newEvent) {
//        String oldEventId = this.eventId;
//        model.closeEvent(oldEventId);


    }

    public Date getCloseDate() {
        return closeDate;
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
        Log.e("TAG", "♦♦♦♦♦ !!! setEmployee: "+employee);
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
