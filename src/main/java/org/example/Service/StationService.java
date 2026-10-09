package org.example.Service;


import org.example.Entities.*;
import java.util.*;

public class StationService {
    private final InMemoryStore store;

    public StationService(InMemoryStore store) {
        this.store = store;
    }

    public synchronized void registerStation(Station station) {
        if (store.stations.containsKey(station.getId())) {
            throw new IllegalArgumentException("Station already exists");
        }
        store.stations.put(station.getId(), station);
    }

    public Station getStation(String stationId) {
        Station station = store.stations.get(stationId);
        if (station == null) {
            throw new IllegalArgumentException("Station not found");
        }
        return station;
    }

    public synchronized void setConnectorOutOfService(
            String stationId, String connectorId) {
        getStation(stationId).getConnector(connectorId)
                .takeOutOfService();
    }

    public synchronized void restoreConnector(
            String stationId, String connectorId) {
        getStation(stationId).getConnector(connectorId).restore();
    }

    public synchronized ConnectorChoice findConnector(
            ConnectorType requestedType,
            double latitude,
            double longitude,
            double radiusKm) {

        ConnectorChoice choice = findByType(
                requestedType, latitude, longitude, radiusKm);

        if (choice == null && requestedType == ConnectorType.AC) {
            choice = findByType(
                    ConnectorType.DC, latitude, longitude, radiusKm);
        }

        if (choice == null) {
            throw new IllegalStateException(
                    "No compatible available connector within radius");
        }
        return choice;
    }

    private ConnectorChoice findByType(
            ConnectorType type,
            double latitude,
            double longitude,
            double radiusKm) {

        ConnectorChoice best = null;

        for (Station station : store.stations.values()) {
            if (!station.isActive()) {
                continue;
            }

            double distance = distanceKm(
                    latitude, longitude,
                    station.getLatitude(), station.getLongitude());

            if (distance > radiusKm) {
                continue;
            }

            for (Connector connector : station.getConnectors()) {
                if (connector.getType() != type || !connector.isAvailable()) {
                    continue;
                }

                if (best == null || distance < best.distanceKm) {
                    best = new ConnectorChoice(station, connector, distance);
                }
            }
        }

        return best;
    }

    private double distanceKm(
            double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return earthRadiusKm * 2 * Math.atan2(
                Math.sqrt(a), Math.sqrt(1 - a));
    }
}
