package com.squorpikkor.app.adjustmentdb;

import com.squorpikkor.app.adjustmentdb.ui.main.MainViewModel;

import java.util.Date;

public class DEvent {
    private Date startDate;
    private Date closeDate;
    private String state;
    private String description;
    private String location;
    private String unit_id;
    private String id;

    public DEvent(Date startDate, String state, String location) {
        this.startDate = startDate;
        this.state = state;
        this.location = location;
    }

    public DEvent() {
    }

    public DEvent(Date startDate, String state, String description, String location, String unit_id, String id) {
        this.startDate = startDate;
        this.state = state;
        this.description = description;
        this.location = location;
        this.unit_id = unit_id;
        this.id = id;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    //Если ивент завершен, то у него появляется дата закрытия
    public boolean isComplete() {
        return this.closeDate!=null;
    }

    /**Сколько дней длилось событие. Если событие не закрыто, то считается количество
     * дней от начала события (startDate) до сегодняшнего дня, если закрыто — до дня закрытия*/
    public int daysPassed() {
        if (this.startDate ==null) return 0;
        Date end = closeDate==null?new Date():closeDate;
        return (int)((end.getTime()-this.startDate.getTime())/(1000*60*60*24));
    }

    /**Закрывает себя в БД*/
    public void updateEvent(MainViewModel model) {
        model.updateEvent(id);
    }

    public void closeEvent() {
        closeDate = new Date();
    }

    public Date getDate() {
        return startDate;
    }

    public void setDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(String unit_id) {
        this.unit_id = unit_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
