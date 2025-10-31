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

    public TaoDonTuGioHangRequest(){}

    public TaoDonTuGioHangRequest(Long diaChiId, String phuongThucThanhToan, String ghiChu, Long voucherId) {
        this.diaChiId = diaChiId;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.ghiChu = ghiChu;
        this.voucherId = voucherId;
    }

    public Long getDiaChiId() { return diaChiId; }
    public void setDiaChiId(Long diaChiId) { this.diaChiId = diaChiId; }
    
    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }
    
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    
    public Long getVoucherId() { return voucherId; }
    public void setVoucherId(Long voucherId) { this.voucherId = voucherId; }
}
