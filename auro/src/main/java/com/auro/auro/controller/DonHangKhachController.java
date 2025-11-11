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
            System.out.println("MaVoucher from request: '" + request.getMaVoucher() + "'");
            System.out.println("Payment method: " + request.getPhuongThucThanhToan());

            // Tạo đơn và nhận lại thông tin để FE có id/tổng tiền cho VNPay
            com.auro.auro.dto.response.DonHangResponse dh = donHangService.taoDonHangGuest(sessionId, request, khachHangId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đặt hàng thành công! Kiểm tra email xác nhận (nếu có).");
            if (dh != null) {
                result.put("donHangId", dh.getId());
                result.put("tongThanhToan", dh.getTongThanhToan());
                result.put("tamTinh", dh.getTamTinh());
                result.put("giamGiaTong", dh.getGiamGiaTong());
                result.put("phiVanChuyen", dh.getPhiVanChuyen());
                System.out.println("=== Guest Checkout Response ===");
                System.out.println("DonHang ID: " + dh.getId());
                System.out.println("TamTinh: " + dh.getTamTinh());
                System.out.println("GiamGiaTong: " + dh.getGiamGiaTong());
                System.out.println("PhiVanChuyen: " + dh.getPhiVanChuyen());
                System.out.println("TongThanhToan: " + dh.getTongThanhToan());
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("ERROR Guest checkout: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            // Lấy message từ exception, nếu có chứa "Voucher" thì giữ nguyên message
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Voucher")) {
                // Message đã rõ ràng (ví dụ: "Voucher không hợp lệ: Bạn đã sử dụng voucher này rồi")
                error.put("message", errorMessage);
            } else {
                // Các lỗi khác
                error.put("message", "Lỗi khi đặt hàng: " + (errorMessage != null ? errorMessage : "Vui lòng thử lại"));
            }
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
