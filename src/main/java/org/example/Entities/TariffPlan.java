package org.example.Entities;


import java.math.BigDecimal;

public class TariffPlan {
    private final ConnectorType type;
    private final BigDecimal minimumCharge;
    private final BigDecimal firstSlabLimit;
    private final BigDecimal firstSlabRate;
    private final BigDecimal secondSlabLimit;
    private final BigDecimal secondSlabRate;
    private final BigDecimal thirdSlabRate;

    public TariffPlan(
            ConnectorType type,
            BigDecimal minimumCharge,
            BigDecimal firstSlabLimit,
            BigDecimal firstSlabRate,
            BigDecimal secondSlabLimit,
            BigDecimal secondSlabRate,
            BigDecimal thirdSlabRate) {
        this.type = type;
        this.minimumCharge = minimumCharge;
        this.firstSlabLimit = firstSlabLimit;
        this.firstSlabRate = firstSlabRate;
        this.secondSlabLimit = secondSlabLimit;
        this.secondSlabRate = secondSlabRate;
        this.thirdSlabRate = thirdSlabRate;
    }

    public ConnectorType getType() { return type; }
    public BigDecimal getMinimumCharge() { return minimumCharge; }
    public BigDecimal getFirstSlabLimit() { return firstSlabLimit; }
    public BigDecimal getFirstSlabRate() { return firstSlabRate; }
    public BigDecimal getSecondSlabLimit() { return secondSlabLimit; }
    public BigDecimal getSecondSlabRate() { return secondSlabRate; }
    public BigDecimal getThirdSlabRate() { return thirdSlabRate; }
}
