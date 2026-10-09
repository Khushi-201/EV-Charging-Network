package org.example.Entities;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PromoCode {
    private final String code;
    private final BigDecimal discountPercent;
    private final BigDecimal maxDiscount;
    private final LocalDateTime validFrom;
    private final LocalDateTime validUntil;
    private boolean active = true;

    public PromoCode(String code,
                     BigDecimal discountPercent,
                     BigDecimal maxDiscount,
                     LocalDateTime validFrom,
                     LocalDateTime validUntil) {
        if (discountPercent.compareTo(BigDecimal.ZERO) <= 0
                || discountPercent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Discount must be > 0 and <= 100");
        }
        if (maxDiscount != null
                && maxDiscount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Max discount cannot be negative");
        }
        this.code = code;
        this.discountPercent = discountPercent;
        this.maxDiscount = maxDiscount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public String getCode() { return code; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public boolean isActive() { return active; }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isValidAt(LocalDateTime time) {
        return active
                && !time.isBefore(validFrom)
                && !time.isAfter(validUntil);
    }

    public PromoCode snapshotForSession(){
        return new PromoCode(
                this.code,
                this.discountPercent,
                this.maxDiscount,
                this.validFrom,
                this.validUntil
        );
    }
}
