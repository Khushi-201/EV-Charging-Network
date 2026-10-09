package org.example.Service;

import org.example.Entities.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

public class BillingService {
    private final java.util.Map<ConnectorType, TariffPlan> tariffs =
            new java.util.EnumMap<>(ConnectorType.class);

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal PEAK_MULTIPLIER =
            new BigDecimal("1.25");

    public BillingService() {
        tariffs.put(ConnectorType.AC, new TariffPlan(
                ConnectorType.AC,
                new BigDecimal("30"),
                new BigDecimal("10"),
                new BigDecimal("8"),
                new BigDecimal("25"),
                new BigDecimal("6"),
                new BigDecimal("4")));

        // DC tariff from the assignment.
        tariffs.put(ConnectorType.DC, new TariffPlan(
                ConnectorType.DC,
                new BigDecimal("150"),
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("25"),
                new BigDecimal("14"),
                new BigDecimal("9")));
    }

    public BillingDetails calculate(
            BigDecimal energyKwh,
            ConnectorType billedType,
            java.time.LocalDateTime startTime,
            PromoCode promo) {

        if (energyKwh == null || energyKwh.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Energy cannot be negative");
        }

        TariffPlan tariff = tariffs.get(billedType);
        BigDecimal raw = calculateEnergyCost(energyKwh, tariff);

        BigDecimal subtotal = raw.max(tariff.getMinimumCharge());

        LocalTime time = startTime.toLocalTime();
        boolean peak = !time.isBefore(LocalTime.of(18, 0))
                && time.isBefore(LocalTime.of(21, 0));

        BigDecimal multiplier = peak ? PEAK_MULTIPLIER : ONE;
        BigDecimal afterPeak = subtotal.multiply(multiplier);

        BigDecimal discount = BigDecimal.ZERO;
        if (promo != null && promo.isValidAt(startTime)) {
            discount = afterPeak
                    .multiply(promo.getDiscountPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            if (promo.getMaxDiscount() != null) {
                discount = discount.min(promo.getMaxDiscount());
            }

            discount = discount.min(afterPeak);
        }

        BigDecimal total = afterPeak.subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);

        return new BillingDetails(
                subtotal.setScale(2, RoundingMode.HALF_UP),
                multiplier,
                afterPeak.setScale(2, RoundingMode.HALF_UP),
                discount.setScale(2, RoundingMode.HALF_UP),
                total);
    }

    private BigDecimal calculateEnergyCost(
            BigDecimal energy, TariffPlan tariff) {

        BigDecimal firstLimit = tariff.getFirstSlabLimit();
        BigDecimal secondLimit = tariff.getSecondSlabLimit();

        BigDecimal firstUnits = energy.min(firstLimit);
        BigDecimal cost = firstUnits.multiply(tariff.getFirstSlabRate());

        if (energy.compareTo(firstLimit) > 0) {
            BigDecimal secondUnits = energy.min(secondLimit)
                    .subtract(firstLimit);
            cost = cost.add(
                    secondUnits.multiply(tariff.getSecondSlabRate()));
        }

        if (energy.compareTo(secondLimit) > 0) {
            BigDecimal thirdUnits = energy.subtract(secondLimit);
            cost = cost.add(
                    thirdUnits.multiply(tariff.getThirdSlabRate()));
        }

        return cost;
    }
}
