package com.auro.auro.controller;

import com.auro.auro.dto.request.ThemVaoGioHangRequest;
import com.auro.auro.dto.response.GioHangItemResponse;
import com.auro.auro.dto.response.GioHangResponse;
import com.auro.auro.model.BienTheSanPham;
import com.auro.auro.model.GioHang;
import com.auro.auro.model.GioHangChiTiet;
import com.auro.auro.repository.BienTheSanPhamRepository;
import com.auro.auro.repository.GioHangChiTietRepository;
import com.auro.auro.service.GioHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.auro.auro.model.KhachHang;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.repository.TaiKhoanRepository;
import com.auro.auro.security.CustomUserDetails;

@RestController
@RequestMapping("/api/gio-hang")
@RequiredArgsConstructor
public class GioHangController {

    private final GioHangService gioHangService;
    private final GioHangChiTietRepository gioHangChiTietRepo;
    private final BienTheSanPhamRepository bienTheRepo;
    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    // xem giỏ hàng
    @GetMapping
    public ResponseEntity<GioHangResponse> xemGioHang(Authentication auth, HttpServletRequest request) {
        try {
            Long khachHangId = (auth != null) ? layKhachHangIdTuAuth(auth) : null;
            String sessionId = request.getSession().getId();
    
            GioHang gioHang = gioHangService.layHoacTaoGioHang(sessionId, khachHangId);
            List<GioHangChiTiet> chiTietEntities = gioHangService.layChiTietGioHang(gioHang.getId());
            List<GioHangItemResponse> chiTietList = chiTietEntities.stream().map(item -> {
                GioHangItemResponse dto = new GioHangItemResponse();
                dto.setId(item.getId());
                dto.setBienTheId(item.getBienThe() != null ? item.getBienThe().getId() : null);
    
                String tenSP = (item.getBienThe() != null && item.getBienThe().getSanPham() != null)
                        ? item.getBienThe().getSanPham().getTen() : "Sản phẩm";
                dto.setTenSanPham(tenSP);
    
                StringBuilder tt = new StringBuilder();
                if (item.getBienThe() != null && item.getBienThe().getMauSac() != null) {
                    tt.append("Màu: ").append(item.getBienThe().getMauSac().getTen());
                }
                if (item.getBienThe() != null && item.getBienThe().getKichCo() != null) {
                    if (tt.length() > 0) tt.append(", ");
                    tt.append("Size: ").append(item.getBienThe().getKichCo().getTen());
                }
                if (item.getBienThe() != null && item.getBienThe().getChatLieu() != null) {
                    if (tt.length() > 0) tt.append(", ");
                    tt.append("Chất liệu: ").append(item.getBienThe().getChatLieu().getTen());
                }
                dto.setThuocTinh(tt.toString());
    
                dto.setSoLuong(item.getSoLuong());
    
                java.math.BigDecimal donGia = item.getGiaTaiThoiDiem();
                if (donGia == null && item.getBienThe() != null) {
                    donGia = item.getBienThe().getGia();
                    if (donGia == null && item.getBienThe().getSanPham() != null) {
                        donGia = item.getBienThe().getSanPham().getGia();
                    }
                }
                if (donGia == null) {
                    donGia = java.math.BigDecimal.ZERO;
                }
                dto.setDonGia(donGia);
                dto.setThanhTien(donGia.multiply(java.math.BigDecimal.valueOf(item.getSoLuong())));
    
                return dto;
            }).toList();
    
            java.math.BigDecimal tongTien = (khachHangId != null)
                    ? gioHangService.tinhTongTienGioHang(khachHangId)
                    : gioHangService.tinhTongTienTheoGioHang(gioHang.getId());
    
            GioHangResponse response = new GioHangResponse();
            response.setId(gioHang.getId());
            response.setChiTietList(chiTietList);
            response.setTongTien(tongTien);
            response.setSoSanPham(chiTietList.size());
            response.setTongSoLuong(chiTietList.stream().mapToInt(GioHangItemResponse::getSoLuong).sum());
    
            return ResponseEntity.ok(response);
    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Thêm sản phẩm vào giỏ
    @PostMapping("/them")
    public ResponseEntity<Map<String, Object>> themVaoGioHang(
        @RequestBody ThemVaoGioHangRequest request,
        Authentication auth,
        HttpServletRequest httpRequest) {
    try {
        Long khachHangId = (auth != null) ? layKhachHangIdTuAuth(auth) : null;
        String sessionId = httpRequest.getSession().getId();

        GioHang gioHang = gioHangService.layHoacTaoGioHang(sessionId, khachHangId);

        BienTheSanPham bienThe = bienTheRepo.findById(request.getBienTheId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (bienThe.getSoLuongTon() < request.getSoLuong()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Sản phẩm chỉ còn " + bienThe.getSoLuongTon() + " trong kho");
            return ResponseEntity.badRequest().body(error);
        }

        List<GioHangChiTiet> chiTietList = gioHangChiTietRepo.findByGioHang_Id(gioHang.getId());
        GioHangChiTiet existingItem = chiTietList.stream()
                .filter(ct -> ct.getBienThe().getId().equals(request.getBienTheId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int soLuongMoi = existingItem.getSoLuong() + request.getSoLuong();

            if (bienThe.getSoLuongTon() < soLuongMoi) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Tổng số lượng vượt quá tồn kho");
                return ResponseEntity.badRequest().body(error);
            }

            existingItem.setSoLuong(soLuongMoi);
            existingItem.setCapNhatLuc(LocalDateTime.now());
            gioHangChiTietRepo.save(existingItem);
        } else {
            GioHangChiTiet itemMoi = new GioHangChiTiet();
            itemMoi.setGioHang(gioHang);
            itemMoi.setBienThe(bienThe);
            itemMoi.setSoLuong(request.getSoLuong());

            BigDecimal gia = bienThe.getGia();
            if (gia == null && bienThe.getSanPham() != null) {
                gia = bienThe.getSanPham().getGia();
            }
            itemMoi.setGiaTaiThoiDiem(gia);

            itemMoi.setTaoLuc(LocalDateTime.now());
            itemMoi.setCapNhatLuc(LocalDateTime.now());

            gioHangChiTietRepo.save(itemMoi);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Thêm vào giỏ hàng thành công");

        return ResponseEntity.ok(result);

    } catch (Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Lỗi: " + e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}

    // cập nhật số lượng trong giỏ
    @PutMapping("/chi-tiet/{id}")
    public ResponseEntity<Map<String, Object>> capNhatSoLuong(
            @PathVariable Long id,
            @RequestParam Integer soLuong,
            Authentication auth,
            HttpServletRequest request) {
        try {
            Long khachHangId = (auth != null) ? layKhachHangIdTuAuth(auth) : null;
            String sessionId = request.getSession().getId();
    
            GioHangChiTiet chiTiet = gioHangChiTietRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));
    
            if (khachHangId != null) {
                if (chiTiet.getGioHang().getKhachHang() == null ||
                        !chiTiet.getGioHang().getKhachHang().getId().equals(khachHangId)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Không có quyền");
                    return ResponseEntity.status(403).body(error);
                }
            } else {
                if (chiTiet.getGioHang().getIdPhien() == null ||
                        !chiTiet.getGioHang().getIdPhien().equals(sessionId)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Không có quyền");
                    return ResponseEntity.status(403).body(error);
                }
            }
    
            if (chiTiet.getBienThe().getSoLuongTon() < soLuong) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Số lượng vượt quá tồn kho");
                return ResponseEntity.badRequest().body(error);
            }
    
            chiTiet.setSoLuong(soLuong);
            chiTiet.setCapNhatLuc(LocalDateTime.now());
            gioHangChiTietRepo.save(chiTiet);
    
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Cập nhật thành công");
    
            return ResponseEntity.ok(result);
    
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // xóa sp trong giỏ
    @DeleteMapping("/chi-tiet/{id}")
    public ResponseEntity<Map<String, Object>> xoaKhoiGioHang(
            @PathVariable Long id,
            Authentication auth,
            HttpServletRequest request) {
        try {
            Long khachHangId = (auth != null) ? layKhachHangIdTuAuth(auth) : null;
            String sessionId = request.getSession().getId();
    
            GioHangChiTiet chiTiet = gioHangChiTietRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));
    
            if (khachHangId != null) {
                if (chiTiet.getGioHang().getKhachHang() == null ||
                        !chiTiet.getGioHang().getKhachHang().getId().equals(khachHangId)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Không có quyền");
                    return ResponseEntity.status(403).body(error);
                }
            } else {
                if (chiTiet.getGioHang().getIdPhien() == null ||
                        !chiTiet.getGioHang().getIdPhien().equals(sessionId)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Không có quyền");
                    return ResponseEntity.status(403).body(error);
                }
            }
    
            gioHangChiTietRepo.delete(chiTiet);
    
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
    
            return ResponseEntity.ok(result);
    
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/clear")
public ResponseEntity<Map<String, Object>> xoaToanBoGioHang(Authentication auth, HttpServletRequest request) {
    try {
        Long khachHangId = (auth != null) ? layKhachHangIdTuAuth(auth) : null;
        String sessionId = request.getSession().getId();

        if (khachHangId != null) {
            gioHangService.xoaGioHang(khachHangId);
        } else {
            gioHangService.xoaGioHangTheoSession(sessionId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Đã xóa toàn bộ giỏ hàng");
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Lỗi: " + e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}

private Long layKhachHangIdTuAuth(Authentication auth) {
    if (auth == null || auth.getPrincipal() == null) {
        return null;
    }
    try {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        TaiKhoan taiKhoan = userDetails.getTaiKhoan();
        KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);
        return (khachHang != null) ? khachHang.getId() : null;
    } catch (Exception e) {
        return null;
    }
}

}
