package org.example.Interface;
import org.example.Entities.*;
import org.example.Service.*;

public interface StationSelectionStrategy {
    ConnectorChoice selectConnector(
            InMemoryStore store,
            ConnectorType requestedType,
            double latitude,
            double longitude,
            double radiusKm);
}
