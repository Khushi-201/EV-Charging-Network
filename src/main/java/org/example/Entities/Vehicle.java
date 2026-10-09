package org.example.Entities;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class Vehicle {

    private final String id;
    private final String driverId;
    private final String registrationNumber;
    private final Set<ConnectorType> supportedConnectorTypes;

    private boolean active = true;

    public Vehicle(
            String id,
            String driverId,
            String registrationNumber,
            ConnectorType supportedConnectorType) {

        this(
                id,
                driverId,
                registrationNumber,
                supportedConnectorType == null
                        ? Collections.emptySet()
                        : EnumSet.of(supportedConnectorType)
        );
    }

    public Vehicle(
            String id,
            String driverId,
            String registrationNumber,
            Set<ConnectorType> supportedConnectorTypes) {

        if (supportedConnectorTypes == null
                || supportedConnectorTypes.isEmpty()
                || supportedConnectorTypes.contains(null)) {
            throw new IllegalArgumentException(
                    "At least one valid connector type is required");
        }

        this.id = id;
        this.driverId = driverId;
        this.registrationNumber = registrationNumber;

        this.supportedConnectorTypes = Collections.unmodifiableSet(
                EnumSet.copyOf(supportedConnectorTypes)
        );
    }

    public String getId() {
        return id;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public Set<ConnectorType> getSupportedConnectorTypes() {
        return supportedConnectorTypes;
    }

    public boolean supportsConnectorType(ConnectorType type) {
        return type != null && supportedConnectorTypes.contains(type);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
