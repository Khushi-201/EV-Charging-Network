package org.example.Entities;

public class Connector {
    private final String id;
    private final ConnectorType type;
    private ConnectorStatus status = ConnectorStatus.AVAILABLE;
    private String activeSessionId;

    public Connector(String id, ConnectorType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() { return id; }
    public ConnectorType getType() { return type; }
    public ConnectorStatus getStatus() { return status; }
    public String getActiveSessionId() { return activeSessionId; }

    public synchronized boolean isAvailable() {
        return status == ConnectorStatus.AVAILABLE;
    }

    public synchronized void occupy(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Session ID cannot be empty");
        }

        if (status != ConnectorStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Connector is not available");
        }

        status = ConnectorStatus.OCCUPIED;
        activeSessionId = sessionId;
    }

    public synchronized void release() {
        if (status == ConnectorStatus.OCCUPIED) {
            status = ConnectorStatus.AVAILABLE;
            activeSessionId = null;
        }
    }

    public synchronized void takeOutOfService() {
        if (status == ConnectorStatus.OCCUPIED) {
            throw new IllegalStateException(
                    "Cannot take an occupied connector out of service");
        }

        status = ConnectorStatus.OUT_OF_SERVICE;
        activeSessionId = null;
    }

    public synchronized void restore() {
        if (status == ConnectorStatus.OUT_OF_SERVICE) {
            status = ConnectorStatus.AVAILABLE;
        }
    }
}