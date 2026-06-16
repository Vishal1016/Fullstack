package com.cinepass.booking_service.model;

public class Screen {
    private String screenId;
    private String name;
    private int capacity;

    public Screen() {
    }

    public Screen(String screenId, String name, int capacity) {
        this.screenId = screenId;
        this.name = name;
        this.capacity = capacity;
    }

    // Getters and Setters
    public String getScreenId() {
        return screenId;
    }

    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
