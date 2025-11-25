package com.auro.auro.repository.projection;

import java.math.BigDecimal;

public interface VoucherUsageProjection {
    String getVoucherCode();
    Long getUsageCount();
    BigDecimal getTotalDiscount();
}

