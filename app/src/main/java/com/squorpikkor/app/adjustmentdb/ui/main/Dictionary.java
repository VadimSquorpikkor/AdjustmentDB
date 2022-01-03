package com.squorpikkor.app.adjustmentdb.ui.main;

import java.util.HashMap;
import java.util.Map;

public class Dictionary {

    private final Map<String, String> devices;
    private final Map<String, String> locations;
    private final Map<String, String> employees;
    private final Map<String, String> types;
    private final Map<String, String> states;
    private final Map<String, String> devSets;

    public Dictionary() {
        this.devices = new HashMap<>();
        this.locations = new HashMap<>();
        this.employees = new HashMap<>();
        this.types = new HashMap<>();
        this.states = new HashMap<>();
        this.devSets = new HashMap<>();
    }

    public Map<String, String> getLocations() {
        return locations;
    }

    void addDevice(String name, String id) {
        devices.put(name, id);
    }

    String getDeviceId(String name) {
        return devices.get(name);
    }

    void addLocation(String name, String id) {
        locations.put(name, id);
    }

    String getLocationId(String name) {
        return locations.get(name);
    }

    void addEmployee(String name, String id) {
        employees.put(name, id);
    }

    String getEmployeeId(String name) {
        return employees.get(name);
    }

    void addType(String name, String id) {
        types.put(name, id);
    }

    String getTypeId(String name) {
        return types.get(name);
    }

    void addState(String name, String id) {
        states.put(name, id);
    }

    String getStateId(String name) {
        return states.get(name);
    }

    void addDevSet(String name, String id) {
        devSets.put(name, id);
    }

    String getDevSetId(String name) {
        return devSets.get(name);
    }

}
