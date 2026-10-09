package org.example.Service;

import org.example.Entities.Connector;
import org.example.Entities.ConnectorType;
import org.example.Entities.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StationServiceTest {

    private static final double LATITUDE = 28.6139;
    private static final double LONGITUDE = 77.2090;

    private InMemoryStore store;
    private StationService stationService;

    @BeforeEach
    void setUp() {
        store = new InMemoryStore();
        stationService = new StationService(store);
    }

    @Test
    void rejectsStationWithoutConnectors() {
        Station station = new Station(
                "S1", "Empty Station", LATITUDE, LONGITUDE
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> stationService.registerStation(station)
        );

        assertFalse(store.stations.containsKey("S1"));
    }

    @Test
    void unknownConnectorIdThrowsIllegalArgumentException() {
        Station station = new Station(
                "S1", "Test Station", LATITUDE, LONGITUDE
        );
        station.addConnector(
                new Connector("C1", ConnectorType.DC)
        );

        stationService.registerStation(station);

        assertThrows(
                IllegalArgumentException.class,
                () -> stationService.setConnectorOutOfService(
                        "S1", "UNKNOWN"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> stationService.restoreConnector(
                        "S1", "UNKNOWN"
                )
        );
    }

    @Test
    void outOfServiceConnectorIsNeverSelected() {
        Station station = new Station(
                "S1", "Test Station", LATITUDE, LONGITUDE
        );

        Connector connector = new Connector(
                "C1", ConnectorType.DC
        );

        station.addConnector(connector);
        stationService.registerStation(station);

        stationService.setConnectorOutOfService("S1", "C1");

        assertFalse(connector.isAvailable());

        assertThrows(
                IllegalStateException.class,
                () -> stationService.findConnector(
                        ConnectorType.DC,
                        LATITUDE,
                        LONGITUDE,
                        5.0
                )
        );

        // The connector remains out of service until restored.
        assertFalse(connector.isAvailable());
    }

    @Test
    void restoresOutOfServiceConnector() {
        Station station = new Station(
                "S1", "Test Station", LATITUDE, LONGITUDE
        );

        Connector connector = new Connector(
                "C1", ConnectorType.DC
        );

        station.addConnector(connector);
        stationService.registerStation(station);

        stationService.setConnectorOutOfService("S1", "C1");
        stationService.restoreConnector("S1", "C1");

        assertTrue(connector.isAvailable());

        assertEquals(
                "C1",
                stationService.findConnector(
                        ConnectorType.DC,
                        LATITUDE,
                        LONGITUDE,
                        5.0
                ).getConnector().getId()
        );
    }
}