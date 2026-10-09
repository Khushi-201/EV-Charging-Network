package org.example.Entities;

public class ConnectorChoice {
    private final Station station;
    private final Connector connector;
    public final double distanceKm;

    public ConnectorChoice(
            Station station,
            Connector connector,
            double distanceKm) {
        this.station = station;
        this.connector = connector;
        this.distanceKm = distanceKm;
    }

    public Station getStation() {
        return station;
    }

    public Connector getConnector() {
        return connector;
    }

    public double getDistanceKm() {
        return distanceKm;
    }
}

