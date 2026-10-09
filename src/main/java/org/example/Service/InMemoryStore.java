package org.example.Service;

import org.example.Entities.*;
import java.util.*;
public class InMemoryStore {
    public final Map<String, Driver> drivers = new HashMap<>();
    public final Map<String, Vehicle> vehicles = new HashMap<>();
    public final Map<String, Station> stations = new HashMap<>();
    public final Map<String, ChargingSession> sessions = new HashMap<>();
    public final Map<String, PromoCode> promoCodes = new HashMap<>();
}
