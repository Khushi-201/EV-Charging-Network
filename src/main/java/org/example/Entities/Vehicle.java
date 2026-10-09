package org.example.Entities;

public class Vehicle {
    private final String id;
    private final String driverId;
    private final String registrationNumber;
    private final ConnectorType supportedConnectorType;
    private boolean active = true;

    public Vehicle(String id, String driverId,
                   String registrationNumber,
                   ConnectorType supportedConnectorType) {
        this.id = id;
        this.driverId = driverId;
        this.registrationNumber = registrationNumber;
        this.supportedConnectorType = supportedConnectorType;
    }

    public String getId() { return id; }
    public String getDriverId() { return driverId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public ConnectorType getSupportedConnectorType() {
        return supportedConnectorType;
    }
    public boolean isActive() { return active; }

    public void setActive(boolean active) {
        this.active = active;
    }
}