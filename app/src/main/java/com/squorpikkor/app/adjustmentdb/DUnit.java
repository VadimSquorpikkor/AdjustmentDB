package com.squorpikkor.app.adjustmentdb;

import android.util.Log;
import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;
import java.util.Date;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.ANY_VALUE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.REPAIR_TYPE;
import static com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel.SERIAL_TYPE;

public class DUnit {

    private String id; //"0001"
    private String name; //БДКГ-02
    private String innerSerial; //№12345
    private String serial; //132.002
    private String type; //"Ремонтный"
    private String employee; //Фамилия ответственного
    private String eventId; //todo event_id есть в event, но с другой стороны id нужно хранить в БД. Надо подумать (ведь можно использовать unit.getEvent().getId() )
    private Date date;
    private Date closeDate;
    private DEvent lastEvent;
    private String deviceSet;
    private String trackId;

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

    public DEvent getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(DEvent lastEvent) {
        if (lastEvent!=null) Log.e("TAG", "setLastEvent: "+lastEvent.getId());
        else Log.e("TAG", "setLastEvent: NULL");
        this.lastEvent = lastEvent;
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

    public void addNewEvent(MainViewModel model, String state, String description, String location) {
        Log.e("TAG", "addNewEvent: lastEvent = "+lastEvent);

        DEvent newEvent = getNewEvent(state, description, location);
        if (newEvent==null||newEvent.getId()==null||newEvent.getId().equals("")) return;

        if (lastEvent!=null) lastEvent.closeEvent(model);//если это первое событие, то lastEvent будет равен null
        eventId = newEvent.getId();
        lastEvent = newEvent;
        if (lastEvent.getState().equals("rep_r_otpravleno") || lastEvent.getState().equals("rep_r_vydano")) {//TODO сделать через константы
            closeUnit();
//            lastEvent.closeEvent(model);//впринципе, если не закрывать, то ивент будет показывать, сколько времени прошло со дня закрытия ремонта/серии
        }

        //todo убрать if (eventId!=null&&!eventId.equals("")) unit.setEventId(eventId); из selectState
        //model.saveUnitAndEvent(unit, newEvent);//todo переделать под model.saveUnit(unit) ведь event есть в самом юните, зачем передавать отдельно?
    }

    private DEvent getNewEvent(String stateId, String description, String location) {
        //Если в спиннере статуса стоит "-не выбрано-", то значит нового события не будет, тогда возвращаем null
        String eventId = this.id+"_"+new Date().getTime();

        if (stateId.equals(ANY_VALUE)) return null;
        else return new DEvent(new Date(), stateId, description, location, this.id, eventId);
    }


    public String getDeviceSet() {
        return deviceSet;
    }

    public void setDeviceSet(String deviceSet) {
        this.deviceSet = deviceSet;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    /**Возвращает true, если это ремонтное устройство*/
    public boolean isRepairUnit() {
        return type.equals(REPAIR_TYPE);
    }

    /**Возвращает true, если это серийное устройство*/
    public boolean isSerialUnit() {
        return type.equals(SERIAL_TYPE);
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

    /**ТрекID для поиска ремонтного устройства. Если в ремонт приходят несколько устройств комплектом, то у них всех будет одинаковый ТрекID*/
    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }
}
