package com.auro.auro.service;

import com.auro.auro.model.Voucher;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;


@Data
@AllArgsConstructor
public class VoucherApplicationResult {
    private final boolean success;
    private final String message;
    private final Voucher voucher;
    private final BigDecimal giamGia;

    public static VoucherApplicationResult success(Voucher voucher, BigDecimal giamGia) {
        return new VoucherApplicationResult(true, "Áp dụng voucher thành công", voucher, giamGia);
    }

    public static VoucherApplicationResult failed(String message) {
        return new VoucherApplicationResult(false, message, null, BigDecimal.ZERO);
    }
}
