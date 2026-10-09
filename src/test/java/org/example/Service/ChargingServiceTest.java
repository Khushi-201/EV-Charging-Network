package org.example.Service;

import org.example.Entities.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChargingServiceTest {

    @Test
    void acRequestFallsBackToDcButUsesAcTariff() {
        InMemoryStore store = new InMemoryStore();

        StationService stationService = new StationService(store);
        BillingService billingService = new BillingService();

        ChargingService chargingService = new ChargingService(
                store,
                stationService,
                billingService
        );

        Driver driver = new Driver(
                "D1", "Aman", "aman@example.com"
        );
        store.drivers.put(driver.getId(), driver);

        Vehicle vehicle = new Vehicle(
                "V1", "D1", "UP-01-EV-1234", ConnectorType.AC
        );
        store.vehicles.put(vehicle.getId(), vehicle);

        double latitude = 28.6139;
        double longitude = 77.2090;

        Station station = new Station(
                "S1", "Test Station", latitude, longitude
        );

        station.addConnector(
                new Connector("DC1", ConnectorType.DC)
        );

        stationService.registerStation(station);

        ChargingSession session = chargingService.startSession(
                "D1",
                "V1",
                ConnectorType.AC,
                latitude,
                longitude,
                5.0,
                null
        );

        assertEquals(ConnectorType.AC, session.getRequestedType());
        assertEquals(ConnectorType.DC, session.getActualType());

        BillingDetails bill = chargingService.endSession(
                session.getId(),
                new BigDecimal("12")
        );

        assertEquals(
                0,
                new BigDecimal("92").compareTo(bill.getEnergySubtotal()),
                "The AC tariff must apply even when a DC connector is used"
        );
    }
}
