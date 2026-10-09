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

    public boolean isAvailable() {
        return status == ConnectorStatus.AVAILABLE;
    }

    public void occupy(String sessionId) {
        if (!isAvailable()) {
            throw new IllegalStateException("Connector is not available");
        }
        this.status = ConnectorStatus.OCCUPIED;
        this.activeSessionId = sessionId;
    }

    public void release() {
        if (status == ConnectorStatus.OCCUPIED) {
            status = ConnectorStatus.AVAILABLE;
        }
        activeSessionId = null;
    }

    public void takeOutOfService() {
        if (status == ConnectorStatus.OCCUPIED) {
            throw new IllegalStateException(
                    "Cannot disable an occupied connector"
            );
        }
        status = ConnectorStatus.OUT_OF_SERVICE;
    }

    public void restore() {
        if (status == ConnectorStatus.OUT_OF_SERVICE) {
            status = ConnectorStatus.AVAILABLE;
        }
    }
}