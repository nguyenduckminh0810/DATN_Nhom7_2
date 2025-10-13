package com.auro.auro.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DonHangResponse {
    private Long id;
    private String soDonHang;
    private BigDecimal tamTinh;
    private BigDecimal tongThanhToan;
    private String trangThai;
    private String diaChiGiaoSnapshot;
    private String ghiChu;
    private LocalDateTime taoLuc;
    private LocalDateTime capNhatLuc;

    // Danh sách chi tiết đơn hàng
    private List<DonHangChiTietResponse> chiTietList;

    // getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSoDonHang() {
        return soDonHang;
    }

    public void setSoDonHang(String soDonHang) {
        this.soDonHang = soDonHang;
    }

    public BigDecimal getTamTinh() {
        return tamTinh;
    }

    public void setTamTinh(BigDecimal tamTinh) {
        this.tamTinh = tamTinh;
    }

    public BigDecimal getTongThanhToan() {
        return tongThanhToan;
    }

    public void setTongThanhToan(BigDecimal tongThanhToan) {
        this.tongThanhToan = tongThanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getDiaChiGiaoSnapshot() {
        return diaChiGiaoSnapshot;
    }

    public void setDiaChiGiaoSnapshot(String diaChiGiaoSnapshot) {
        this.diaChiGiaoSnapshot = diaChiGiaoSnapshot;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public LocalDateTime getTaoLuc() {
        return taoLuc;
    }

    public void setTaoLuc(LocalDateTime taoLuc) {
        this.taoLuc = taoLuc;
    }

    public LocalDateTime getCapNhatLuc() {
        return capNhatLuc;
    }

    public void setCapNhatLuc(LocalDateTime capNhatLuc) {
        this.capNhatLuc = capNhatLuc;
    }

    public List<DonHangChiTietResponse> getChiTietList() {
        return chiTietList;
    }

    public void setChiTietList(List<DonHangChiTietResponse> chiTietList) {
        this.chiTietList = chiTietList;
    }

}
