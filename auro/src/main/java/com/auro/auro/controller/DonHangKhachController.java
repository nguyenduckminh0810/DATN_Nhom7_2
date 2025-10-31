package com.auro.auro.controller;

import com.auro.auro.dto.request.TaoDonTuGioHangRequest;
import com.auro.auro.dto.response.DonHangResponse;
import com.auro.auro.service.DonHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.auro.auro.dto.request.GuestCheckoutRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import com.auro.auro.model.KhachHang;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.security.CustomUserDetails;
@RestController
@RequestMapping("/api/khach-hang/don-hang")
public class DonHangKhachController {

    @Autowired
    private DonHangService donHangService;

    @Autowired
    private KhachHangRepository khachHangRepository;

    // tạo đơn hàng từ giỏ hàng
    @PreAuthorize("hasRole('CUS')")
    @PostMapping("/tao-tu-gio-hang")
    public ResponseEntity<DonHangResponse> taoDonTuGioHang( @RequestBody TaoDonTuGioHangRequest request, Authentication auth) {
        try{
            Long khachHangId = layKhachHangIdTuAuth(auth);
            DonHangResponse dh = donHangService.taoDonTuGioHang(request, khachHangId);
            return ResponseEntity.ok(dh);
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // lấy đơn hàng của khách
    @PreAuthorize("hasRole('CUS')")
    @GetMapping("/don-hang-cua-toi")
    public ResponseEntity <Page<DonHangResponse>> layDonHangCuaToi(@RequestParam(defaultValue = "0") int trang, @RequestParam(defaultValue = "10") int kichThuoc, Authentication auth) {
        try{
            Long khachHangId = layKhachHangIdTuAuth(auth);
            Page<DonHangResponse> dhs = donHangService.layDonHangCuaKhach(khachHangId, trang, kichThuoc);
            return ResponseEntity.ok(dhs);
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Hủy đơn hàng
    @PreAuthorize("hasRole('CUS')")
    @PutMapping("/{donHangId}/huy")
    public ResponseEntity<DonHangResponse> huyDonHang(@PathVariable Long donHangId, Authentication auth) {
        try{
            Long khachHangId = layKhachHangIdTuAuth(auth);
            DonHangResponse dh = donHangService.huyDonHang(donHangId,khachHangId);
            return ResponseEntity.ok(dh);
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy chi tiết đơn hàng
    @PreAuthorize("hasRole('CUS')")
    @GetMapping("/{donHangId}")
    public ResponseEntity<DonHangResponse> layChiTietDonHang(@PathVariable Long donHangId, Authentication auth) {
        try{
            Long khachHangId = layKhachHangIdTuAuth(auth);
            DonHangResponse dh = donHangService.layChiTietDonHangKhach(donHangId, khachHangId);
            return ResponseEntity.ok(dh);
        }catch(Exception e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @PostMapping("/guest-checkout")
public ResponseEntity<?> guestCheckout(@RequestBody GuestCheckoutRequest request, HttpServletRequest httpRequest) {
    try {
        String sessionId = httpRequest.getSession().getId();
        donHangService.taoDonHangGuest(sessionId, request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Đặt hàng thành công! Kiểm tra email xác nhận (nếu có).");
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
