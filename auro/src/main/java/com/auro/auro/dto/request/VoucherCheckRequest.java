package com.auro.auro.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Data
public class VoucherCheckRequest {
    @NotBlank(message = "Mã Voucher không được để trống")
    private String maVoucher;

    private Long khachHangId;

    @NotNull(message = "Tổng đơn hàng không được để trống")
    private BigDecimal donHangTong;
    private BigDecimal phiVanChuyen;
}
