package org.example.Entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ChargingSession {
    private final String id;
    private final String driverId;
    private final String vehicleId;
    private final String stationId;
    private final String connectorId;

    private final ConnectorType requestedType;
    private final ConnectorType actualType;

    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private SessionStatus status = SessionStatus.ACTIVE;
    private BigDecimal energyKwh;
    private BillingDetails billingDetails;
    private final String promoCode;

    private PromoCode promoSnapshot;

    public ChargingSession(
            String id,
            String driverId,
            String vehicleId,
            String stationId,
            String connectorId,
            ConnectorType requestedType,
            ConnectorType actualType,
            LocalDateTime startTime,
            String promoCode) {
        this.id = id;
        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.stationId = stationId;
        this.connectorId = connectorId;
        this.requestedType = requestedType;
        this.actualType = actualType;
        this.startTime = startTime;
        this.promoCode = promoCode;
    }

    public String getId() { return id; }
    public String getDriverId() { return driverId; }
    public String getVehicleId() { return vehicleId; }
    public String getStationId() { return stationId; }
    public String getConnectorId() { return connectorId; }
    public ConnectorType getRequestedType() { return requestedType; }
    public ConnectorType getActualType() { return actualType; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public SessionStatus getStatus() { return status; }
    public BigDecimal getEnergyKwh() { return energyKwh; }
    public BillingDetails getBillingDetails() { return billingDetails; }
    public String getPromoCode() { return promoCode; }

    public void complete(BigDecimal energyKwh,
                         BillingDetails billingDetails,
                         LocalDateTime endTime) {
        if (status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }
        if (energyKwh == null || energyKwh.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Energy cannot be negative");
        }
        this.energyKwh = energyKwh;
        this.billingDetails = billingDetails;
        this.endTime = endTime;
        this.status = SessionStatus.COMPLETED;
    }

    public void cancel() {
        if (status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }
        status = SessionStatus.CANCELLED;
        endTime = LocalDateTime.now();
    }

    public void markNoShow() {
        if (status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active");
        }
        status = SessionStatus.NO_SHOW;
        endTime = LocalDateTime.now();
    }

    public PromoCode getPromoSnapshot() {
        return promoSnapshot;
    }

    public void setPromoSnapshot(PromoCode promoSnapshot) {
        this.promoSnapshot = promoSnapshot;
    }
}
