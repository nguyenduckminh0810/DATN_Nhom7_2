package com.auro.auro.dto.response;

import java.math.BigDecimal;

public class DonHangChiTietResponse {
    private Long id;
    private String tenSanPham;
    private String hinhAnh;
    private BigDecimal donGia;
    private Integer soLuong;
    private BigDecimal thanhTien;
    private Long bienTheId;
    private Long sanPhamId;
    private Long mauSacId;
    private String mauSacTen;
    private Long kichCoId;
    private String kichCoTen;
    private Boolean daDanhGia;
    private Integer danhGiaSoSao;
    private String danhGiaNoiDung;
    private java.time.LocalDateTime danhGiaTaoLuc;

    // getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public Long getBienTheId() {
        return bienTheId;
    }

    public void setBienTheId(Long bienTheId) {
        this.bienTheId = bienTheId;
    }

    public Long getSanPhamId() {
        return sanPhamId;
    }

    public void setSanPhamId(Long sanPhamId) {
        this.sanPhamId = sanPhamId;
    }

    public Long getMauSacId() {
        return mauSacId;
    }

    public void setMauSacId(Long mauSacId) {
        this.mauSacId = mauSacId;
    }

    public String getMauSacTen() {
        return mauSacTen;
    }

    public void setMauSacTen(String mauSacTen) {
        this.mauSacTen = mauSacTen;
    }

    public Long getKichCoId() {
        return kichCoId;
    }

    public void setKichCoId(Long kichCoId) {
        this.kichCoId = kichCoId;
    }

    public String getKichCoTen() {
        return kichCoTen;
    }

    public void setKichCoTen(String kichCoTen) {
        this.kichCoTen = kichCoTen;
    }

    public Boolean getDaDanhGia() {
        return daDanhGia;
    }

    public void setDaDanhGia(Boolean daDanhGia) {
        this.daDanhGia = daDanhGia;
    }

    public Integer getDanhGiaSoSao() {
        return danhGiaSoSao;
    }

    public void setDanhGiaSoSao(Integer danhGiaSoSao) {
        this.danhGiaSoSao = danhGiaSoSao;
    }

    public String getDanhGiaNoiDung() {
        return danhGiaNoiDung;
    }

    public void setDanhGiaNoiDung(String danhGiaNoiDung) {
        this.danhGiaNoiDung = danhGiaNoiDung;
    }

    public java.time.LocalDateTime getDanhGiaTaoLuc() {
        return danhGiaTaoLuc;
    }

    public void setDanhGiaTaoLuc(java.time.LocalDateTime danhGiaTaoLuc) {
        this.danhGiaTaoLuc = danhGiaTaoLuc;
    }
}
