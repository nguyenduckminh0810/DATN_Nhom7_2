package com.auro.auro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class TaoDonTuGioHangRequest {
    @NotNull(message = "ID địa chỉ không được để trống")
    private Long diaChiId;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String phuongThucThanhToan;

    private String ghiChu;
    private Long voucherId;
    private Long freeshipVoucherId;

    private Integer districtId;
    private String wardCode;
    private Integer serviceId;

    public TaoDonTuGioHangRequest(){}

    public TaoDonTuGioHangRequest(Long diaChiId, String phuongThucThanhToan, String ghiChu, Long voucherId, Long freeshipVoucherId, Integer districtId, String wardCode, Integer serviceId) {
        this.diaChiId = diaChiId;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.ghiChu = ghiChu;
        this.voucherId = voucherId;
        this.freeshipVoucherId = freeshipVoucherId;
        this.districtId = districtId;
        this.wardCode = wardCode;
        this.serviceId = serviceId;
    }

    public Long getDiaChiId() { return diaChiId; }
    public void setDiaChiId(Long diaChiId) { this.diaChiId = diaChiId; }
    
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    
    public Long getVoucherId() { return voucherId; }
    public void setVoucherId(Long voucherId) { this.voucherId = voucherId; }

    public Long getFreeshipVoucherId() { return freeshipVoucherId; }
    public void setFreeshipVoucherId(Long freeshipVoucherId) { this.freeshipVoucherId = freeshipVoucherId; }

    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }
    
    public String getWardCode() { return wardCode; }
    public void setWardCode(String wardCode) { this.wardCode = wardCode; }
    
    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }
}
