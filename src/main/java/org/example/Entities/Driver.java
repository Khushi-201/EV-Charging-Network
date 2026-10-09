package org.example.Entities;

import java.util.ArrayList;
import java.util.List;

public class Driver {
    private final String id;
    private final String name;
    private final String email;
    private boolean active = true;
    private final List<String> sessionIds = new ArrayList<>();

    public Driver(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    public List<String> getSessionIds() { return sessionIds; }

    public void setActive(boolean active) {
        this.active = active;
    }
}
