package org.example.Interface;

import java.math.BigDecimal;

public interface SessionFeePolicy {
    BigDecimal getCancellationFee();
    BigDecimal getNoShowFee();
}