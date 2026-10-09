package org.example.Entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Station {
    private final String id;
    private final String name;
    private final double latitude;
    private final double longitude;
    private boolean active = true;
    private final Map<String, Connector> connectors = new HashMap<>();

    public Station(String id, String name,
                   double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isActive() { return active; }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addConnector(Connector connector) {
        if (connectors.containsKey(connector.getId())) {
            throw new IllegalArgumentException("Duplicate connector ID");
        }
        connectors.put(connector.getId(), connector);
    }

    public Connector getConnector(String connectorId) {
        return connectors.get(connectorId);
    }

    public Collection<Connector> getConnectors() {
        return connectors.values();
    }
}
