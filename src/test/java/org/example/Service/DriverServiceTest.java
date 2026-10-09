package org.example.Service;

import org.example.Entities.Driver;
import org.example.Entities.Vehicle;
import org.example.Entities.ConnectorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DriverServiceTest {

    @Test
    void registersDriverAndVehicle() {
        InMemoryStore store = new InMemoryStore();
        DriverService service = new DriverService(store);

        Driver driver = new Driver(
                "D1", "Aman", "aman@example.com"
        );

        service.registerDriver(driver);

        Vehicle vehicle = new Vehicle(
                "V1", "D1", "UP-01-EV-1234",
                ConnectorType.AC
        );

        service.registerVehicle(vehicle);

        assertSame(driver, store.drivers.get("D1"));
        assertSame(vehicle, store.vehicles.get("V1"));
    }

    @Test
    void rejectsVehicleWhoseDriverDoesNotExist() {
        InMemoryStore store = new InMemoryStore();
        DriverService service = new DriverService(store);

        Vehicle vehicle = new Vehicle(
                "V1", "MISSING", "UP-01-EV-1234",
                ConnectorType.AC
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerVehicle(vehicle)
        );
    }

    @Test
    void rejectsDuplicateDriverId() {
        InMemoryStore store = new InMemoryStore();
        DriverService service = new DriverService(store);

        service.registerDriver(
                new Driver("D1", "Aman", "aman@example.com")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerDriver(
                        new Driver("D1", "Another", "another@example.com")
                )
        );
    }
}
