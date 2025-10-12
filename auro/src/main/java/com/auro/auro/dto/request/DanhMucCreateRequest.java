package com.auro.auro.dto.request;

import jakarta.validation.constraints.NotBlank;

public class DanhMucCreateRequest {
    @NotBlank
    private String ten;

    @NotBlank
    private String slug;

    private Long idCha;        // có thể null
    private Integer thuTu = 0;
    private Boolean hoatDong = true; 
    //getters and setters
    public String getTen() {
        return ten;
    }
    public void setTen(String ten) {
        this.ten = ten;
    }
    public String getSlug() {
        return slug;
    }
    public void setSlug(String slug) {
        this.slug = slug;
    }
    public Long getIdCha() {
        return idCha;
    }
    public void setIdCha(Long idCha) {
        this.idCha = idCha;
    }
    public Integer getThuTu() {
        return thuTu;
    }
    public void setThuTu(Integer thuTu) {
        this.thuTu = thuTu;
    }
    public Boolean getHoatDong() {
        return hoatDong;
    }
    public void setHoatDong(Boolean hoatDong) {
        this.hoatDong = hoatDong;
    }

}
