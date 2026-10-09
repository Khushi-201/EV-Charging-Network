package org.example.Service;

import org.example.Entities.BillingDetails;
import org.example.Entities.ConnectorType;
import org.example.Entities.PromoCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BillingServiceTest {

    private BillingService billingService;

    // Noon is outside the assumed 6 PM–9 PM peak window.
    private final LocalDateTime offPeak =
            LocalDateTime.of(2026, 10, 9, 12, 0);

    @BeforeEach
    void setUp() {
        billingService = new BillingService();
    }

    private void assertAmount(String expected, BigDecimal actual) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual),
                "Expected amount: " + expected + ", actual: " + actual
        );
    }

    @Test
    void dcMinimumSessionChargeApplies() {
        // 5 kWh * ₹20 = ₹100, but minimum charge is ₹150.
        BillingDetails bill = billingService.calculate(
                new BigDecimal("5"),
                ConnectorType.DC,
                offPeak,
                null
        );

        assertAmount("150.00", bill.getEnergySubtotal());
        assertAmount("150.00", bill.getFinalAmount());
    }

    @Test
    void dcFirstSlabIsCalculatedCorrectly() {
        // 10 kWh * ₹20 = ₹200.
        BillingDetails bill = billingService.calculate(
                new BigDecimal("10"),
                ConnectorType.DC,
                offPeak,
                null
        );

        assertAmount("200.00", bill.getFinalAmount());
    }

    @Test
    void dcSecondSlabIsCalculatedCorrectly() {
        // 10 * ₹20 + 10 * ₹14 = ₹340.
        BillingDetails bill = billingService.calculate(
                new BigDecimal("20"),
                ConnectorType.DC,
                offPeak,
                null
        );

        assertAmount("340.00", bill.getFinalAmount());
    }

    @Test
    void dcThirdSlabIsCalculatedCorrectly() {
        // 10 * ₹20 + 15 * ₹14 + 5 * ₹9 = ₹455.
        BillingDetails bill = billingService.calculate(
                new BigDecimal("30"),
                ConnectorType.DC,
                offPeak,
                null
        );

        assertAmount("455.00", bill.getFinalAmount());
    }

    @Test
    void acUsesAcTariffInsteadOfDcTariff() {
        // Assumed AC tariff: 10 kWh * ₹8 = ₹80.
        BillingDetails bill = billingService.calculate(
                new BigDecimal("10"),
                ConnectorType.AC,
                offPeak,
                null
        );

        assertAmount("80.00", bill.getEnergySubtotal());
        assertAmount("80.00", bill.getFinalAmount());
    }

    @Test
    void validPromoCodeAppliesPercentageDiscount() {
        // DC energy subtotal: ₹340.
        // A 10% discount is ₹34, making the final bill ₹306.
        PromoCode promo = new PromoCode(
                "SAVE10",
                new BigDecimal("10"),
                null,
                offPeak.minusDays(1),
                offPeak.plusDays(1)
        );

        BillingDetails bill = billingService.calculate(
                new BigDecimal("20"),
                ConnectorType.DC,
                offPeak,
                promo
        );

        assertAmount("340.00", bill.getEnergySubtotal());
        assertAmount("34.00", bill.getDiscount());
        assertAmount("306.00", bill.getFinalAmount());
    }
}
