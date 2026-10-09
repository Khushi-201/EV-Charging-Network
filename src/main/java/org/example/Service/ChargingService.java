package org.example.Service;

import org.example.Entities.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChargingService {

    private final InMemoryStore store;
    private final StationService stationService;
    private final BillingService billingService;

    public ChargingService(
            InMemoryStore store,
            StationService stationService,
            BillingService billingService) {
        this.store = store;
        this.stationService = stationService;
        this.billingService = billingService;
    }

    public synchronized ChargingSession startSession(
            String driverId,
            String vehicleId,
            ConnectorType requestedType,
            double latitude,
            double longitude,
            double radiusKm,
            String promoCode) {

        Driver driver = store.drivers.get(driverId);

        if (driver == null || !driver.isActive()) {
            throw new IllegalArgumentException(
                    "Driver not found or inactive");
        }

        Vehicle vehicle = store.vehicles.get(vehicleId);

        if (vehicle == null || !vehicle.isActive()) {
            throw new IllegalArgumentException(
                    "Vehicle not found or inactive");
        }

        if (!vehicle.getDriverId().equals(driverId)) {
            throw new IllegalArgumentException(
                    "Vehicle does not belong to this driver");
        }

        if (!vehicle.supportsConnectorType(requestedType)) {
            throw new IllegalArgumentException(
                    "Vehicle does not support the requested connector type");
        }

        boolean alreadyActive = store.sessions.values().stream()
                .anyMatch(s ->
                        s.getVehicleId().equals(vehicleId)
                                && s.getStatus() == SessionStatus.ACTIVE);

        if (alreadyActive) {
            throw new IllegalStateException(
                    "Vehicle already has an active session");
        }

        PromoCode acceptedPromo = null;

        if (promoCode != null && !promoCode.isBlank()) {
            String normalizedCode = promoCode.trim().toUpperCase(
                    java.util.Locale.ROOT
            );

            acceptedPromo = store.promoCodes.get(normalizedCode);

            if (acceptedPromo == null
                    || !acceptedPromo.isValidAt(LocalDateTime.now())) {
                throw new IllegalArgumentException(
                        "Invalid or expired promo code");
            }

            // Store the canonical code used by the promo catalogue.
            promoCode = acceptedPromo.getCode();
        } else {
            promoCode = null;
        }

        // Finds requested connector; AC requests can fall back to DC
        ConnectorChoice choice = stationService.findConnector(
                requestedType, latitude, longitude, radiusKm);

        Station station = choice.getStation();
        Connector connector = choice.getConnector();
        if (!vehicle.supportsConnectorType(connector.getType())) {
            throw new IllegalStateException(
                    "No available compatible connector for this vehicle");
        }
        String sessionId = UUID.randomUUID().toString();

        ChargingSession session = new ChargingSession(
                sessionId,
                driverId,
                vehicleId,
                station.getId(),
                connector.getId(),
                requestedType,
                connector.getType(),
                LocalDateTime.now(),
                promoCode
        );

        session.setPromoSnapshot(
                acceptedPromo == null
                        ? null
                        : acceptedPromo.snapshotForSession()
        );

        connector.occupy(sessionId);
        store.sessions.put(sessionId, session);
        driver.getSessionIds().add(sessionId);

        return session;
    }

    public synchronized BillingDetails endSession(
            String sessionId,
            BigDecimal energyKwh) {

        ChargingSession session = getActiveSession(sessionId);

        if (energyKwh == null || energyKwh.signum() < 0) {
            throw new IllegalArgumentException(
                    "Energy delivered cannot be negative or null");
        }

        PromoCode promo = session.getPromoSnapshot();

        // Important: bill using requested type, not actual connector type.
        // Example: AC requested, DC used as fallback -> AC tariff applies.
        BillingDetails bill = billingService.calculate(
                energyKwh,
                session.getRequestedType(),
                session.getStartTime(),
                promo
        );

        session.complete(
                energyKwh,
                bill,
                LocalDateTime.now());

        releaseConnector(session);

        return bill;
    }

    public synchronized void cancelSession(String sessionId) {
        ChargingSession session = getActiveSession(sessionId);

        session.cancel();
        releaseConnector(session);
    }

    public synchronized void markNoShow(String sessionId) {
        ChargingSession session = getActiveSession(sessionId);

        session.markNoShow();
        releaseConnector(session);
    }

    public List<ChargingSession> getDriverHistory(String driverId) {
        if (!store.drivers.containsKey(driverId)) {
            throw new IllegalArgumentException("Driver not found");
        }

        return store.sessions.values().stream()
                .filter(s -> s.getDriverId().equals(driverId))
                .collect(Collectors.toList());
    }

    // Station's complete session history
    public List<ChargingSession> getStationHistory(String stationId) {
        if (!store.stations.containsKey(stationId)) {
            throw new IllegalArgumentException("Station not found");
        }

        return store.sessions.values().stream()
                .filter(s -> s.getStationId().equals(stationId))
                .collect(Collectors.toList());
    }

    public synchronized void addPromoCode(PromoCode promo) {
        String code = promo.getCode().toUpperCase();

        if (store.promoCodes.containsKey(code)) {
            throw new IllegalArgumentException(
                    "Promo code already exists");
        }

        store.promoCodes.put(code, promo);
    }

    public synchronized void deletePromoCode(String code) {
        PromoCode promo = store.promoCodes.get(code.toUpperCase());

        if (promo == null) {
            throw new IllegalArgumentException(
                    "Promo code not found");
        }

        promo.setActive(false);
    }

    public ChargingSession getSession(String sessionId) {
        ChargingSession session = store.sessions.get(sessionId);

        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }

        return session;
    }

    private ChargingSession getActiveSession(String sessionId) {
        ChargingSession session = getSession(sessionId);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Session is not active");
        }

        return session;
    }

    private void releaseConnector(ChargingSession session) {
        Station station = store.stations.get(session.getStationId());

        if (station == null) {
            throw new IllegalStateException(
                    "Session station no longer exists");
        }

        Connector connector =
                station.getConnector(session.getConnectorId());

        if (connector == null) {
            throw new IllegalStateException(
                    "Session connector no longer exists");
        }

        connector.release();
    }
}

