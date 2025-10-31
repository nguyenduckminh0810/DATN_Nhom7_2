package com.auro.auro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ThemVaoGioHangRequest {

    @NotNull(message = "ID biến thể không được để trống")
    private Long bienTheId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;
    
    public ThemVaoGioHangRequest() {}
    
    public ThemVaoGioHangRequest(Long bienTheId, Integer soLuong) {
        this.bienTheId = bienTheId;
        this.soLuong = soLuong;
    }
    
    public Long getBienTheId() {
        return bienTheId;
    }
    
    public void setBienTheId(Long bienTheId) {
        this.bienTheId = bienTheId;
    }
    
    public Integer getSoLuong() {
        return soLuong;
    }
    
    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }
}
