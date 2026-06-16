package com.cinepass.booking_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "theatres")
public class Theatre {
    @Id
    private String id; // maps to theatreId in API representation
    private String name;
    private String location;
    private List<Screen> screens = new ArrayList<>();

    public Theatre() {
    }

    public Theatre(String id, String name, String location, List<Screen> screens) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.screens = screens;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Screen> getScreens() {
        return screens;
    }

    public void setScreens(List<Screen> screens) {
        this.screens = screens;
    }
}
