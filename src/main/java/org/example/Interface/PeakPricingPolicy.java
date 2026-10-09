package org.example.Interface;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PeakPricingPolicy {
    BigDecimal getMultiplier(LocalDateTime startTime);
}
