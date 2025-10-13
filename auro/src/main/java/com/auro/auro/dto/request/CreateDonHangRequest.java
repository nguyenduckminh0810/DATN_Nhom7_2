package com.auro.auro.dto.request;

import java.util.List;

import com.auro.auro.model.DonHang;
import com.auro.auro.model.DonHangChiTiet;

public class CreateDonHangRequest {
    private DonHang donHang;
    private List<DonHangChiTiet> chiTietList;

    public CreateDonHangRequest(DonHang donHang, List<DonHangChiTiet> chiTietList) {
        this.donHang = donHang;
        this.chiTietList = chiTietList;
    }

    public DonHang getDonHang() {
        return donHang;
    }

    public void setDonHang(DonHang donHang) {
        this.donHang = donHang;
    }

    public List<DonHangChiTiet> getChiTietList() {
        return chiTietList;
    }

    public void setChiTietList(List<DonHangChiTiet> chiTietList) {
        this.chiTietList = chiTietList;
    }

}
