package org.example.Service;

import org.example.Entities.Driver;
import org.example.Entities.Vehicle;


public class DriverService {

    private final InMemoryStore store;

    public DriverService(InMemoryStore store) {
        this.store = store;
    }

    public synchronized Driver registerDriver(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver cannot be null");
        }

        requireText(driver.getId(), "Driver ID");
        requireText(driver.getName(), "Driver name");
        requireText(driver.getEmail(), "Driver email");

        if (store.drivers.containsKey(driver.getId())) {
            throw new IllegalArgumentException("Driver already exists");
        }

        store.drivers.put(driver.getId(), driver);
        return driver;
    }

    public synchronized Vehicle registerVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }

        requireText(vehicle.getId(), "Vehicle ID");
        requireText(vehicle.getDriverId(), "Driver ID");
        requireText(vehicle.getRegistrationNumber(),
                "Vehicle registration number");

        if (store.vehicles.containsKey(vehicle.getId())) {
            throw new IllegalArgumentException("Vehicle already exists");
        }

        Driver driver = store.drivers.get(vehicle.getDriverId());

        if (driver == null || !driver.isActive()) {
            throw new IllegalArgumentException(
                    "Driver does not exist or is inactive");
        }

        boolean duplicateRegistration = store.vehicles.values()
                .stream()
                .anyMatch(existing ->
                        existing.getRegistrationNumber()
                                .equalsIgnoreCase(
                                        vehicle.getRegistrationNumber()));

        if (duplicateRegistration) {
            throw new IllegalArgumentException(
                    "Vehicle registration number already exists");
        }

        store.vehicles.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty");
        }
    }
}

