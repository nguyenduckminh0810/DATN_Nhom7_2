package com.auro.auro.controller;

import com.auro.auro.dto.request.DanhGiaDonHangRequest;
import com.auro.auro.dto.request.GuestCheckoutRequest;
import com.auro.auro.dto.request.TaoDonTuGioHangRequest;
import com.auro.auro.dto.response.DonHangChiTietResponse;
import com.auro.auro.dto.response.DonHangResponse;
import com.auro.auro.model.KhachHang;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.security.CustomUserDetails;
import com.auro.auro.service.DonHangService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/khach-hang/don-hang")
public class DonHangKhachController {

    @Autowired
    private DonHangService donHangService;

    @Autowired
    private KhachHangRepository khachHangRepository;

    // tạo đơn hàng từ giỏ hàng
    @PreAuthorize("hasAnyRole('CUS', 'STF', 'ADM')")
    @PostMapping("/tao-tu-gio-hang")
    public ResponseEntity<DonHangResponse> taoDonTuGioHang(@RequestBody TaoDonTuGioHangRequest request,
            Authentication auth) {
        try {
            System.out.println("=== DEBUG ENDPOINT tao-tu-gio-hang ===");
            System.out.println("Authentication: " + auth);
            System.out.println("Authorities: " + (auth != null ? auth.getAuthorities() : "null"));
            System.out.println("Principal: " + (auth != null ? auth.getPrincipal() : "null"));

            Long khachHangId = layKhachHangIdTuAuth(auth);
            System.out.println("KhachHangId: " + khachHangId);
            System.out.println("Request data: " + request);

            if (khachHangId == null) {
                throw new RuntimeException("Không thể xác định khách hàng");
            }

            DonHangResponse dh = donHangService.taoDonTuGioHang(request, khachHangId);
            return ResponseEntity.ok(dh);
        } catch (Exception e) {
            System.out.println("ERROR in taoDonTuGioHang: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // lấy đơn hàng của khách
    @PreAuthorize("hasAnyRole('CUS', 'STF', 'ADM')")
    @GetMapping("/don-hang-cua-toi")
    public ResponseEntity<Page<DonHangResponse>> layDonHangCuaToi(@RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "10") int kichThuoc, Authentication auth) {
        try {
            Long khachHangId = layKhachHangIdTuAuth(auth);
            System.out.println("=== layDonHangCuaToi ===");
            System.out.println("KhachHang ID: " + khachHangId);
            System.out.println("Page: " + trang + ", Size: " + kichThuoc);

            Page<DonHangResponse> dhs = donHangService.layDonHangCuaKhach(khachHangId, trang, kichThuoc);
            System.out.println("Found orders: " + dhs.getTotalElements());

            return ResponseEntity.ok(dhs);
        } catch (Exception e) {
            System.out.println("ERROR in layDonHangCuaToi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Hủy đơn hàng
    @PreAuthorize("hasAnyRole('CUS', 'STF', 'ADM')")
    @PutMapping("/{donHangId}/huy")
    public ResponseEntity<DonHangResponse> huyDonHang(@PathVariable Long donHangId, Authentication auth) {
        try {
            Long khachHangId = layKhachHangIdTuAuth(auth);
            DonHangResponse dh = donHangService.huyDonHang(donHangId, khachHangId);
            return ResponseEntity.ok(dh);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Lấy chi tiết đơn hàng
    @PreAuthorize("hasAnyRole('CUS', 'STF', 'ADM')")
    @GetMapping("/{donHangId}")
    public ResponseEntity<DonHangResponse> layChiTietDonHang(@PathVariable Long donHangId, Authentication auth) {
        try {
            Long khachHangId = layKhachHangIdTuAuth(auth);
            DonHangResponse dh = donHangService.layChiTietDonHangKhach(donHangId, khachHangId);
            return ResponseEntity.ok(dh);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

    }

    @PreAuthorize("hasAnyRole('CUS', 'STF', 'ADM')")
    @PostMapping("/{donHangId}/danh-gia")
    public ResponseEntity<?> danhGiaDonHang(@PathVariable Long donHangId,
            @Valid @RequestBody DanhGiaDonHangRequest request,
            Authentication auth) {

        Long khachHangId = layKhachHangIdTuAuth(auth);
        if (khachHangId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Không xác định được khách hàng để gửi đánh giá.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        try {
            DonHangChiTietResponse response = donHangService.danhGiaDonHang(
                    donHangId,
                    request.getChiTietId(),
                    khachHangId,
                    request.getSoSao(),
                    request.getNoiDung());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đã lưu đánh giá.");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Không thể lưu đánh giá: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/guest-checkout")
    public ResponseEntity<?> guestCheckout(@RequestBody GuestCheckoutRequest request,
            HttpServletRequest httpRequest,
            Authentication auth) {
        try {
            String sessionId = httpRequest.getSession().getId();
            Long khachHangId = layKhachHangIdTuAuth(auth);

            System.out.println("=== Guest Checkout ===");
            System.out.println("Session ID: " + sessionId);
            System.out.println("Authenticated khachHangId: " + khachHangId);

            // Tạo đơn và nhận lại thông tin để FE có id/tổng tiền cho VNPay
            com.auro.auro.dto.response.DonHangResponse dh = donHangService.taoDonHangGuest(sessionId, request,
                    khachHangId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đặt hàng thành công! Kiểm tra email xác nhận (nếu có).");
            if (dh != null) {
                result.put("donHangId", dh.getId());
                result.put("tongThanhToan", dh.getTongThanhToan());
            }
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

            System.out.println("=== layKhachHangIdTuAuth ===");
            System.out.println("TaiKhoan ID: " + taiKhoan.getId());
            System.out.println("VaiTro: " + taiKhoan.getVaiTro().getMa());

            // Nếu là admin hoặc staff, tạo/tìm KhachHang record
            String roleMa = taiKhoan.getVaiTro().getMa();
            if ("ADM".equals(roleMa) || "STF".equals(roleMa)) {
                System.out.println("Admin/Staff detected - finding or creating KhachHang record");

                // Tìm KhachHang theo tài khoản
                KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);

                if (khachHang == null) {
                    // Tạo KhachHang mới cho admin/staff
                    khachHang = new KhachHang();
                    khachHang.setTaiKhoan(taiKhoan);
                    khachHang.setHoTen(taiKhoan.getEmail() != null ? taiKhoan.getEmail() : "Admin/Staff");
                    khachHang.setKieu("ONLINE");
                    khachHang = khachHangRepository.save(khachHang);
                    System.out.println("Created new KhachHang for Admin/Staff with ID: " + khachHang.getId());
                }

                return khachHang.getId();
            }

            // Nếu là customer, tìm KhachHang bình thường
            KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);
            return (khachHang != null) ? khachHang.getId() : null;
        } catch (Exception e) {
            System.out.println("ERROR in layKhachHangIdTuAuth: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
