package com.squorpikkor.app.adjustmentdb;

import java.util.Date;

public class DState {
    Date date;
    String state;

    public DState(Date date, String state) {
        this.date = date;
        this.state = state;
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
}
