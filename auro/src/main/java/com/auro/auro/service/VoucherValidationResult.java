package com.auro.auro.service;

import com.auro.auro.model.Voucher;
import lombok.Data;

@Data
public class VoucherValidationResult {
    private final boolean valid;
    private final String message;
    private final Voucher voucher;

    private VoucherValidationResult(boolean valid, String message, Voucher voucher){
        this.valid = valid;
        this. message = message;
        this. voucher = voucher;
    }

    public static VoucherValidationResult valid(Voucher voucher) {
        return new VoucherValidationResult(true, null, voucher);
    }

    public static VoucherValidationResult invalid(String message) {
        return new VoucherValidationResult(false, message, null);
    }
}
