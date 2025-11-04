package com.auro.auro.dto.response;

import java.math.BigDecimal;

public class GioHangItemResponse {
    private Long id; // id chi tiết giỏ
    private Long bienTheId;
    private String tenSanPham;
    private String thuocTinh;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String image; // Ảnh sản phẩm
    private Integer tonKho; // Tồn kho của biến thể

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBienTheId() {
        return bienTheId;
    }

    public void setBienTheId(Long bienTheId) {
        this.bienTheId = bienTheId;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getThuocTinh() {
        return thuocTinh;
    }

    public void setThuocTinh(String thuocTinh) {
        this.thuocTinh = thuocTinh;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getTonKho() {
        return tonKho;
    }

    public void setTonKho(Integer tonKho) {
        this.tonKho = tonKho;
    }
}
