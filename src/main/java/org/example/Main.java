package org.example;

import org.example.Service.*;
import java.math.BigDecimal;
import org.example.Entities.*;

public class Main {
    public static void main(String[] args) {
        InMemoryStore store = new InMemoryStore();
        DriverService driverService = new DriverService(store);
        StationService stationService = new StationService(store);
        BillingService billingService = new BillingService();

        ChargingService chargingService =
                new ChargingService(store, stationService, billingService);

        Driver driver = new Driver("D1", "Aman", "aman@example.com");
        driverService.registerDriver(driver);

        Vehicle vehicle = new Vehicle(
                "V1", "D1", "UP-01-EV-1234", ConnectorType.AC);
        driverService.registerVehicle(vehicle);
        Station station = new Station(
                "S1", "City Charging Hub", 28.6139, 77.2090);
        station.addConnector(new Connector("C1", ConnectorType.AC));
        station.addConnector(new Connector("C2", ConnectorType.DC));
        stationService.registerStation(station);

        ChargingSession session = chargingService.startSession(
                "D1", "V1", ConnectorType.AC,
                28.6140, 77.2090, 10.0, null);

        BillingDetails bill = chargingService.endSession(
                session.getId(), new BigDecimal("12"));

        System.out.println("Session: " + session.getId());
        System.out.println("Final amount: ₹" + bill.getFinalAmount());
    }
}