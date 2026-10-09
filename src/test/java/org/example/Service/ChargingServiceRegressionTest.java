package org.example.Service;

import org.example.Entities.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChargingServiceRegressionTest {

    private static final double LATITUDE = 28.6139;
    private static final double LONGITUDE = 77.2090;

    private static class Fixture {
        final InMemoryStore store;
        final StationService stationService;
        final BillingService billingService;
        final ChargingService chargingService;
        final Connector connector;

        Fixture(
                InMemoryStore store,
                StationService stationService,
                BillingService billingService,
                ChargingService chargingService,
                Connector connector) {
            this.store = store;
            this.stationService = stationService;
            this.billingService = billingService;
            this.chargingService = chargingService;
            this.connector = connector;
        }
    }

    private Fixture createFixture(
            Vehicle vehicle,
            ConnectorType stationConnectorType) {

        InMemoryStore store = new InMemoryStore();

        DriverService driverService = new DriverService(store);
        driverService.registerDriver(
                new Driver("D1", "Aman", "aman@example.com")
        );
        driverService.registerVehicle(vehicle);

        StationService stationService = new StationService(store);
        BillingService billingService = new BillingService();

        ChargingService chargingService = new ChargingService(
                store,
                stationService,
                billingService
        );

        Station station = new Station(
                "S1", "Test Station", LATITUDE, LONGITUDE
        );

        Connector connector = new Connector(
                "C1", stationConnectorType
        );

        station.addConnector(connector);
        stationService.registerStation(station);

        return new Fixture(
                store,
                stationService,
                billingService,
                chargingService,
                connector
        );
    }

    @Test
    void deactivatingPromoAfterSessionStartDoesNotRemoveDiscount() {
        Vehicle vehicle = new Vehicle(
                "V1", "D1", "UP-01-EV-1234",
                ConnectorType.AC
        );

        Fixture fixture = createFixture(vehicle, ConnectorType.AC);

        LocalDateTime now = LocalDateTime.now();

        PromoCode promo = new PromoCode(
                "SAVE10",
                new BigDecimal("10"),
                null,
                now.minusDays(1),
                now.plusDays(1)
        );

        fixture.chargingService.addPromoCode(promo);

        ChargingSession session = fixture.chargingService.startSession(
                "D1",
                "V1",
                ConnectorType.AC,
                LATITUDE,
                LONGITUDE,
                5.0,
                "SAVE10"
        );

        // Deactivate the original promo after the session has started.
        fixture.chargingService.deletePromoCode("SAVE10");

        assertFalse(
                fixture.store.promoCodes.get("SAVE10").isActive(),
                "The original promo should be deactivated"
        );

        BillingDetails bill = fixture.chargingService.endSession(
                session.getId(),
                new BigDecimal("10")
        );

        // The accepted 10% discount should still apply to this session.
        BigDecimal expectedDiscount = bill.getSubtotalAfterPeak()
                .multiply(new BigDecimal("10"))
                .divide(
                        new BigDecimal("100"),
                        2,
                        RoundingMode.HALF_UP
                );

        assertEquals(
                0,
                expectedDiscount.compareTo(bill.getDiscount()),
                "The discount accepted at session start must be preserved"
        );

        assertEquals(
                0,
                bill.getSubtotalAfterPeak()
                        .subtract(bill.getDiscount())
                        .compareTo(bill.getFinalAmount()),
                "Final amount should equal post-peak subtotal minus discount"
        );
    }

    @Test
    void acOnlyVehicleCannotUseDcFallback() {
        Vehicle vehicle = new Vehicle(
                "V1", "D1", "UP-01-EV-1234",
                ConnectorType.AC
        );

        // The station only has a DC connector.
        Fixture fixture = createFixture(vehicle, ConnectorType.DC);

        assertThrows(
                IllegalStateException.class,
                () -> fixture.chargingService.startSession(
                        "D1",
                        "V1",
                        ConnectorType.AC,
                        LATITUDE,
                        LONGITUDE,
                        5.0,
                        null
                )
        );

        // A failed compatibility check must not reserve the connector.
        assertTrue(fixture.connector.isAvailable());
        assertTrue(
                fixture.store.sessions.isEmpty(),
                "An incompatible request must not create a session"
        );
    }

    @Test
    void dualCompatibleVehicleCanUseDcFallbackAndPaysAcTariff() {
        Vehicle vehicle = new Vehicle(
                "V1",
                "D1",
                "UP-01-EV-1234",
                Set.of(ConnectorType.AC, ConnectorType.DC)
        );

        // Only DC is available, but the driver requests AC.
        Fixture fixture = createFixture(vehicle, ConnectorType.DC);

        ChargingSession session = fixture.chargingService.startSession(
                "D1",
                "V1",
                ConnectorType.AC,
                LATITUDE,
                LONGITUDE,
                5.0,
                null
        );

        assertEquals(ConnectorType.AC, session.getRequestedType());
        assertEquals(ConnectorType.DC, session.getActualType());

        BillingDetails bill = fixture.chargingService.endSession(
                session.getId(),
                new BigDecimal("12")
        );

        // Assumed AC tariff: 10 * ₹8 + 2 * ₹6 = ₹92.
        // energySubtotal is checked before peak pricing is applied.
        assertEquals(
                0,
                new BigDecimal("92").compareTo(bill.getEnergySubtotal()),
                "The AC tariff must apply even when the actual connector is DC"
        );

        assertTrue(
                fixture.connector.isAvailable(),
                "The connector should be released after ending the session"
        );
    }
}