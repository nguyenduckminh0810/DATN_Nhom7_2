package com.auro.auro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ThanhToanOnlRequest {

    @NotNull(message = "ID đơn hàng không được để trống")
    private Long donHangId;
    
    @NotNull(message = "Số tiền không được để trống")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal soTien;
    
    private String moTa;

    public ThanhToanOnlRequest() {}
    
    public ThanhToanOnlRequest(Long donHangId, BigDecimal soTien, String moTa) {
        this.donHangId = donHangId;
        this.soTien = soTien;
        this.moTa = moTa;
    }

    public Long getDonHangId() {
        return donHangId;
    }
    
    public void setDonHangId(Long donHangId) {
        this.donHangId = donHangId;
    }
    
    public BigDecimal getSoTien() {
        return soTien;
    }
    
    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }
    
    public String getMoTa() {
        return moTa;
    }
    
    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
