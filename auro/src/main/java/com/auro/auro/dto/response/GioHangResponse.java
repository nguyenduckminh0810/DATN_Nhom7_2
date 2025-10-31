package com.auro.auro.dto.response;

import com.auro.auro.model.GioHangChiTiet;

import java.math.BigDecimal;
import java.util.List;

public class GioHangResponse {

    private Long id;
    private List<GioHangItemResponse> chiTietList;
    private BigDecimal tongTien;
    private Integer soSanPham;
    private Integer tongSoLuong;

    public GioHangResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<GioHangItemResponse> getChiTietList() { return chiTietList; }
    public void setChiTietList(List<GioHangItemResponse> chiTietList) { this.chiTietList = chiTietList; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public Integer getSoSanPham() { return soSanPham; }
    public void setSoSanPham(Integer soSanPham) { this.soSanPham = soSanPham; }

    public Integer getTongSoLuong() { return tongSoLuong; }
    public void setTongSoLuong(Integer tongSoLuong) { this.tongSoLuong = tongSoLuong; }
}
