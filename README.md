# EV Charging Backend

A Java 17 application for managing EV drivers, vehicles, charging stations, connectors, charging sessions, and billing. The solution uses an in-memory store and a CLI entry point, with a focus on domain modeling, correctness, and extensible business logic.

## Assumptions & Business Rules

The requirements leave certain behaviors open to interpretation. The following assumptions define the behavior of this implementation.

### 1. Driver, Vehicle & Station Management

* Every driver, vehicle, station, connector, and session has a unique identifier.
* A vehicle belongs to one driver, and only that driver can start a charging session using it.
* Inactive drivers and vehicles cannot initiate sessions.
* A station must be active to be considered for connector selection.
* A station's location is represented by latitude and longitude. The driver's current location and acceptable search radius are supplied when requesting a charging session.
* The solution uses in-memory storage. Data is not persisted across application restarts.

### 2. Connector Selection & Compatibility

* Only connectors that are available and in service are eligible for selection.
* Connectors marked out of service are excluded until explicitly restored.
* Distance is calculated using the Haversine formula, with the Earth's radius approximated as 6,371 km.
* Only stations within the requested radius are considered.
* The nearest available connector of the requested type is selected.
* **AC-to-DC fallback:** If no available AC connector exists within the requested radius, the system searches for a DC connector within the same radius. An available AC connector is preferred even when a DC connector is closer.
* DC-to-AC fallback is not supported.
* Connector compatibility is simplified to AC/DC types. The implementation does not model vehicle-specific charging standards, voltage, current, or power limits.

### 3. Charging Session Lifecycle

* A vehicle can have at most one active charging session at a time.
* Starting a session reserves the selected connector and records the driver, vehicle, station, requested connector type, actual connector type, and start time.
* Charging is not simulated over time. Ending a session accepts the delivered energy in kWh as input.
* Delivered energy must be non-negative.
* Ending a session calculates the bill, records the energy and end time, and releases the connector.
* Cancelling a session or marking it as a no-show releases the reserved connector.
* Session history retains the session records, including active, completed, cancelled, and no-show sessions.
* Session cancellation and no-show are modeled as lifecycle transitions. A configurable fee or booking grace-period policy is not considered complete unless it is explicitly wired into the session flow.

### 4. Tariff & Billing Rules

Tariffs are configurable assumptions for this assignment, not representations of actual charging-provider prices.

| Rule                                  |     AC |      DC |
| ------------------------------------- | -----: | ------: |
| Minimum session charge                |    ₹30 |    ₹150 |
| First 10 kWh                          | ₹8/kWh | ₹20/kWh |
| Next 15 kWh (above 10 through 25 kWh) | ₹6/kWh | ₹14/kWh |
| Above 25 kWh                          | ₹4/kWh |  ₹9/kWh |

* Tariffs use progressive slabs. Each rate applies only to the energy within that slab.
* The minimum charge is applied to the calculated energy subtotal.
* Peak-hour pricing is applied after the minimum charge, followed by any applicable promo discount.
* Final monetary amounts are rounded to two decimal places using `RoundingMode.HALF_UP`.
* An AC request served by a DC connector is billed using the AC tariff. Billing is based on the **requested connector type**, not the physical connector selected.

**Example:** For 30 kWh on DC, the energy subtotal is `(10 × ₹20) + (15 × ₹14) + (5 × ₹9) = ₹455`. The DC minimum charge does not affect this amount because the calculated subtotal already exceeds ₹150.

### 5. Peak-Hour Pricing

* The assumed peak period is 6:00 PM inclusive to 9:00 PM exclusive.
* The peak multiplier is 1.25; the off-peak multiplier is 1.00.
* The session start time determines the applicable multiplier. Charging duration and changes in station load during a session are not simulated.
* Pricing is separated behind a policy interface to enable alternative implementations, such as utilization-based pricing. Such alternatives must be integrated into the billing flow to become active behavior.

### 6. Promo Codes

* A promo code has a unique code, discount percentage, validity period, active status, and optional maximum discount.
* A promo must be active and within its validity period when the session starts.
* The percentage discount is calculated after the minimum charge and peak multiplier.
* An optional maximum discount caps the discount amount.
* Deleting a promo code means deactivating it, rather than physically removing its record.

A known limitation is that a promo's active status may be checked again during billing. If the code is deactivated while a session is active, its discount may no longer apply. A stronger implementation would snapshot the accepted promo terms when the session starts so that later changes do not affect an existing session.

## Design Decisions & Trade-offs

### Domain-driven separation

The implementation separates domain entities from service logic:

* **Entities:** Model drivers, vehicles, stations, connectors, sessions, tariffs, promo codes, and billing details.
* **StationService:** Handles station registration, connector availability operations, and connector selection.
* **ChargingService:** Coordinates session creation, completion, cancellation, history, and connector reservation.
* **BillingService:** Calculates progressive tariffs, minimum charges, peak pricing, and discounts.
* **InMemoryStore:** Maintains application state using in-memory maps.

This keeps session lifecycle management separate from billing calculations and makes the core behavior easier to test.

### Extensibility

Interfaces define extension points where behavior may vary:

* `StationSelectionStrategy` — supports alternative station-selection algorithms, such as nearest, cheapest, or highest-power selection.
* `PeakPricingPolicy` — allows time-based or station-load-based pricing policies.
* `SessionFeePolicy` — defines cancellation and no-show fee behavior.

These interfaces provide the contracts for future implementations. New behavior must still be implemented and injected into the relevant services; defining an interface alone does not activate the feature.

### Concurrency

Synchronized session operations help prevent competing calls through the same service instance from starting conflicting sessions. Connector reservation should also perform its availability check and state transition atomically.

This design is intended for a single-process application. It does not provide distributed locking or cross-process consistency. A production version would use transactional persistence and suitable locking or conditional updates.

### Intentional trade-offs

The solution prioritizes a small, understandable Java application over a REST framework, database, or elaborate dependency-injection architecture. The CLI and in-memory store are sufficient for demonstrating the domain behavior, while persistent storage, distributed coordination, and production observability remain outside the current scope.

## Verification & Tests

Automated billing tests should cover:

* Progressive tariff slabs and slab boundaries.
* Minimum session charges.
* AC and DC tariff separation.
* Promo-code discounts and discount caps.
* AC requests served by DC connectors while retaining the AC tariff.
* Peak and off-peak pricing.

Service tests should also verify connector availability, out-of-service behavior, session lifecycle transitions, invalid inputs, and competing connector reservations.

Run the test suite from the repository root:

```bash
mvn clean test
```

The test suite must pass before submission. Test files alone do not establish correctness; the cases above should be implemented and executed against the final source code.

## What I Would Improve With More Time

1. Complete unit and integration test coverage, especially for concurrency, connector eligibility, promo-code edge cases, and session lifecycle transitions.
2. Wire each strategy and policy interface into its corresponding service and add alternative implementations.
3. Snapshot accepted promo terms at session start.
4. Model vehicle connector compatibility and charging capabilities more explicitly.
5. Introduce a booking model and grace period if no-show fees are required.
6. Replace in-memory collections with transactional database persistence and atomic connector reservation.
7. Add structured logging, stronger input validation, and clearer CLI input handling.
8. Automate builds and tests through continuous integration.

## Use of AI

AI was used as a development assistant to explore the domain model, draft service implementations, identify potential extension points, and generate initial test cases.

The suggestions were reviewed and adapted to the project's actual package structure and method signatures. For example, `ConnectorChoice` was moved from a nested class to a separate entity so that connector-selection results could be shared without coupling consumers to the internal structure of `StationService`. The implementation also uses Core Java and a CLI instead of retaining the initial Spring Boot approach, keeping the solution focused on the required domain behavior and the available development time.

Generated code was treated as a starting point, not as evidence of correctness. The final implementation should be compiled, tested, and reviewed to confirm that it matches the documented behavior. The submitted code and tests should reflect what was actually verified.

## Repository

The repository contains the Java source code, Maven configuration, automated tests, this README, and Git history. The Git history should reflect the actual development process.

The solution is a focused demonstration of object-oriented design, session lifecycle management, connector selection, billing, and extensibility. It is not intended to represent a production-ready charging platform.
