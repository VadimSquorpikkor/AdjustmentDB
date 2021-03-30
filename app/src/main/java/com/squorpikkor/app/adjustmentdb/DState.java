package com.squorpikkor.app.adjustmentdb;

import java.util.Date;

public class DState {
    private Date date;
    private String state;
    private String description;
    private String location;
    private String unit_id;

    public DState(Date date, String state) {
        this.date = date;
        this.state = state;
    }

    public DState(Date date, String state, String description, String location, String unit_id) {
        this.date = date;
        this.state = state;
        this.description = description;
        this.location = location;
        this.unit_id = unit_id;
    }

    public DState() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
}
