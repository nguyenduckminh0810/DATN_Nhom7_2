package com.auro.auro.service;

import com.auro.auro.model.GioHang;
import com.auro.auro.model.GioHangChiTiet;
import com.auro.auro.model.KhachHang;
import com.auro.auro.repository.GioHangChiTietRepository;
import com.auro.auro.repository.GioHangRepository;
import com.auro.auro.repository.KhachHangRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GioHangService {

    private final GioHangRepository gioHangRepo;
    private final GioHangChiTietRepository gioHangChiTietRepo;
    private final KhachHangRepository khRepo;

    // Lấy giỏ hàng của khách
    @Transactional
    public GioHang layGioHangCuaKhach(Long khachHangId) {
        Optional<GioHang> gioHangOpt = gioHangRepo.findFirstByKhachHang_Id(khachHangId);

        if(gioHangOpt.isPresent()) {
            return gioHangOpt.get();
        }

        KhachHang kh = khRepo.findById(khachHangId).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        GioHang newGioHang = new GioHang();
        newGioHang.setKhachHang(kh);

        return gioHangRepo.save(newGioHang);
    }

    // Lấy chi tiết giỏ hàng
    public List<GioHangChiTiet> layChiTietGioHang(Long gioHangId) {
        return gioHangChiTietRepo.findByGioHang_Id(gioHangId);
    }

    // Lấy chi tiết giỏ hàng khách
    public List<GioHangChiTiet> layGioHangChiTietKhach(Long khachHangId) {
        GioHang gh = layGioHangCuaKhach(khachHangId);
        return layChiTietGioHang(gh.getId());
    }

    // Tính tiền giỏ hàng
    public BigDecimal tinhTongTienGioHang(Long khachHangId) {
        List<GioHangChiTiet> chiTietList = layGioHangChiTietKhach(khachHangId);

        return chiTietList.stream().map(item -> {
            BigDecimal gia = item.getGiaTaiThoiDiem();
            if(gia == null && item.getBienThe() != null) {
                gia = item.getBienThe().getGia();
                if(gia == null && item.getBienThe().getSanPham() != null) {
                    gia = item.getBienThe().getSanPham().getGia();
                }
            }
            return gia != null ? gia.multiply(BigDecimal.valueOf(item.getSoLuong())) : BigDecimal.ZERO;
        })
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Xóa all giỏ hàng
    @Transactional
    public void xoaGioHang(Long khachHangId) {
        Optional<GioHang> gioHangOpt = gioHangRepo.findFirstByKhachHang_Id(khachHangId);

        if (gioHangOpt.isPresent()) {
            Long gioHangId = gioHangOpt.get().getId();

            gioHangChiTietRepo.deleteByGioHang_Id(gioHangId);

            gioHangRepo.delete(gioHangOpt.get());
        }
    }

    // check trống giỏ hàng
    public boolean gioHangTrong(Long khachHangId) {
        List<GioHangChiTiet> chiTietList = layGioHangChiTietKhach(khachHangId);
        return chiTietList.isEmpty();
    }

    // lấy giỏ hàng của guest theo idPhien
    @Transactional
    public GioHang layGioHangTheoSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new RuntimeException("Session ID không hợp lệ");
        }
        
        Optional<GioHang> gioHangOpt = gioHangRepo.findFirstByIdPhien(sessionId);
        
        if (gioHangOpt.isPresent()) {
            return gioHangOpt.get();
        }
        
        // Nếu chưa có giỏ hàng thì tạo mới
        GioHang gioHangMoi = new GioHang();
        gioHangMoi.setIdPhien(sessionId);
        gioHangMoi.setKhachHang(null);
        gioHangMoi.setTaoLuc(java.time.LocalDateTime.now());
        gioHangMoi.setCapNhatLuc(java.time.LocalDateTime.now());
        
        return gioHangRepo.save(gioHangMoi);
    }

    //lấy/tạo giỏ hàng cho cus và gst
    @Transactional
    public GioHang layHoacTaoGioHang(String sessionId, Long khachHangId) {
        if (khachHangId != null) {
            return layGioHangCuaKhach(khachHangId);
        }
        
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            return layGioHangTheoSession(sessionId);
        }
        
        throw new RuntimeException("Không thể xác định giỏ hàng - thiếu sessionId hoặc khachHangId");
    }

    // tính tình theo id giỏ hàng
    public BigDecimal tinhTongTienTheoGioHang(Long gioHangId) {
        List<GioHangChiTiet> chiTietList = layChiTietGioHang(gioHangId);
        
        return chiTietList.stream().map(item -> {
            BigDecimal gia = item.getGiaTaiThoiDiem();
            if (gia == null && item.getBienThe() != null) {
                gia = item.getBienThe().getGia();
                if (gia == null && item.getBienThe().getSanPham() != null) {
                    gia = item.getBienThe().getSanPham().getGia();
                }
            }
            return gia != null ? gia.multiply(BigDecimal.valueOf(item.getSoLuong())) : BigDecimal.ZERO;
        })
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // xóa giỏ hàng gst theo idPhien
    @Transactional
    public void xoaGioHangTheoSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }
        
        Optional<GioHang> gioHangOpt = gioHangRepo.findFirstByIdPhien(sessionId);
        
        if (gioHangOpt.isPresent()) {
            Long gioHangId = gioHangOpt.get().getId();
            
            // Xóa chi tiết trước
            gioHangChiTietRepo.deleteByGioHang_Id(gioHangId);
            
            // Xóa giỏ hàng
            gioHangRepo.delete(gioHangOpt.get());
        }
    }
}
