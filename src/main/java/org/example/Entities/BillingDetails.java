package org.example.Entities;

import java.math.BigDecimal;

public class BillingDetails {
    private final BigDecimal energySubtotal;
    private final BigDecimal peakMultiplier;
    private final BigDecimal subtotalAfterPeak;
    private final BigDecimal discount;
    private final BigDecimal finalAmount;

    public BillingDetails(BigDecimal energySubtotal,
                          BigDecimal peakMultiplier,
                          BigDecimal subtotalAfterPeak,
                          BigDecimal discount,
                          BigDecimal finalAmount) {
        this.energySubtotal = energySubtotal;
        this.peakMultiplier = peakMultiplier;
        this.subtotalAfterPeak = subtotalAfterPeak;
        this.discount = discount;
        this.finalAmount = finalAmount;
    }

    public BigDecimal getEnergySubtotal() { return energySubtotal; }
    public BigDecimal getPeakMultiplier() { return peakMultiplier; }
    public BigDecimal getSubtotalAfterPeak() { return subtotalAfterPeak; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getFinalAmount() { return finalAmount; }
}
