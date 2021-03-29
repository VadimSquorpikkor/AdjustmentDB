package com.squorpikkor.app.adjustmentdb;

import java.util.Date;

public class DState {
    private Date date;
    private String state;
    private String description;

    public DState(Date date, String state) {
        this.date = date;
        this.state = state;
    }

    public DState(Date date, String state, String description) {
        this.date = date;
        this.state = state;
        this.description = description;
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
}
