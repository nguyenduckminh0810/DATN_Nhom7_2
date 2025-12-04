package com.auro.auro.controller;

import com.auro.auro.dto.request.DanhGiaDonHangRequest;
import com.auro.auro.dto.request.GuestCheckoutRequest;
import com.auro.auro.dto.request.TaoDonTuGioHangRequest;
import com.auro.auro.dto.response.DonHangChiTietResponse;
import com.auro.auro.dto.response.DonHangPageResponse;
import com.auro.auro.dto.response.DonHangResponse;
import com.auro.auro.model.KhachHang;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.security.CustomUserDetails;
import com.auro.auro.service.DonHangService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/khach-hang/don-hang")
public class DonHangKhachController {
    private static final Logger log = LoggerFactory.getLogger(DonHangKhachController.class);

    @Autowired
    private DonHangService donHangService;

    @Autowired
    private KhachHangRepository khachHangRepository;

    // Track last order timestamp per logged-in customer (across sessions)
    private static final ConcurrentHashMap<Long, Long> lastOrderByKhachHang = new ConcurrentHashMap<>();

    // tạo đơn hàng từ giỏ hàng
    @PreAuthorize("hasAnyRole('CUS', 'STF', 'ADM')")
    @PostMapping("/tao-tu-gio-hang")
    @Transactional
    public ResponseEntity<DonHangResponse> taoDonTuGioHang(@RequestBody TaoDonTuGioHangRequest request,
            HttpServletRequest httpRequest,
            Authentication auth) {
        try {
            Long khachHangId = layKhachHangIdTuAuth(auth);

            if (khachHangId == null) {
                throw new RuntimeException("Không thể xác định khách hàng");
            }

            // Duplicate protection: prefer per-customer blocking (10s) when logged-in,
            // otherwise use session-level blocking (10s)
            try {
                long now = System.currentTimeMillis();
                if (khachHangId != null) {
                    Long last = lastOrderByKhachHang.get(khachHangId);
                    if (last != null && now - last < 10000) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("success", false);
                        error.put("message",
                                "Bạn đã gửi yêu cầu đặt hàng gần đây. Vui lòng chờ vài giây trước khi thử lại.");
                        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(null);
                    }
                    lastOrderByKhachHang.put(khachHangId, now);
                } else {
                    Object lastObj = httpRequest.getSession().getAttribute("lastOrderTs");
                    if (lastObj instanceof Long) {
                        long last = (Long) lastObj;
                        if (now - last < 10000) {
                            Map<String, Object> error = new HashMap<>();
                            error.put("success", false);
                            error.put("message",
                                    "Bạn đã gửi yêu cầu đặt hàng gần đây. Vui lòng chờ vài giây trước khi thử lại.");
                            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(null);
                        }
                    }
                    httpRequest.getSession().setAttribute("lastOrderTs", now);
                }
            } catch (Exception ex) {
                // ignore session/map errors
            }

            DonHangResponse dh = donHangService.taoDonTuGioHang(request, khachHangId);
            return ResponseEntity.ok(dh);
        } catch (Exception e) {
            log.error("Error in taoDonTuGioHang: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // lấy đơn hàng của khách
    @PreAuthorize("hasAnyRole('CUS', 'STF', 'ADM')")
    @GetMapping("/don-hang-cua-toi")
    public ResponseEntity<DonHangPageResponse> layDonHangCuaToi(@RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "10") int kichThuoc,
            @RequestParam(name = "trangThai", required = false) String trangThai,
            @RequestParam(name = "search", required = false) String keyword,
            Authentication auth) {
        try {
            Long khachHangId = layKhachHangIdTuAuth(auth);
            DonHangPageResponse dhs = donHangService.layDonHangCuaKhach(khachHangId, trang, kichThuoc, trangThai,
                    keyword);
            return ResponseEntity.ok(dhs);
        } catch (Exception e) {
            log.error("Error in layDonHangCuaToi: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Hủy đơn hàng
    @PreAuthorize("hasAnyRole('CUS', 'STF', 'ADM')")
    @PutMapping("/{donHangId}/huy")
    public ResponseEntity<?> huyDonHang(
            @PathVariable Long donHangId, 
            @RequestBody(required = false) Map<String, String> request,
            Authentication auth) {
        try {
            Long khachHangId = layKhachHangIdTuAuth(auth);
            String lyDoHuy = request != null ? request.get("reason") : null;
            if (lyDoHuy == null || lyDoHuy.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Vui lòng nhập lý do hủy đơn hàng");
                errorResponse.put("error", "Vui lòng nhập lý do hủy đơn hàng");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Lấy email từ Authentication
            String emailNguoiHuy = layEmailTuAuth(auth);
            
            DonHangResponse dh = donHangService.huyDonHang(donHangId, khachHangId, lyDoHuy, emailNguoiHuy);
            return ResponseEntity.ok(dh);
        } catch (RuntimeException e) {
            // Trả về message lỗi để frontend hiển thị
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            // Xử lý các exception khác
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Lỗi khi hủy đơn hàng: " + e.getMessage());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
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
    @Transactional
    public ResponseEntity<?> guestCheckout(@RequestBody GuestCheckoutRequest request,
            HttpServletRequest httpRequest,
            Authentication auth) {
        try {
            String sessionId = httpRequest.getSession().getId();
            Long khachHangId = layKhachHangIdTuAuth(auth);

            // Simple session-level duplicate protection: block orders from same session
            // within 5 seconds
            try {
                long now = System.currentTimeMillis();
                Object lastObj = httpRequest.getSession().getAttribute("lastOrderTs");
                if (lastObj instanceof Long) {
                    long last = (Long) lastObj;
                    if (now - last < 5000) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("success", false);
                        error.put("message",
                                "Bạn đã gửi yêu cầu đặt hàng gần đây. Vui lòng chờ vài giây trước khi thử lại.");
                        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
                    }
                }
                httpRequest.getSession().setAttribute("lastOrderTs", now);
            } catch (Exception ex) {
                // ignore session errors
            }

            // Tạo đơn và nhận lại thông tin để FE có id/tổng tiền cho VNPay
            com.auro.auro.dto.response.DonHangResponse dh = donHangService.taoDonHangGuest(sessionId, request,
                    khachHangId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đặt hàng thành công! Kiểm tra email xác nhận (nếu có).");
            if (dh != null) {
                result.put("donHangId", dh.getId());
                result.put("tongThanhToan", dh.getTongThanhToan());
                result.put("tamTinh", dh.getTamTinh());
                result.put("giamGiaTong", dh.getGiamGiaTong());
                result.put("phiVanChuyen", dh.getPhiVanChuyen());
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in guest checkout: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Voucher")) {
                error.put("message", errorMessage);
            } else {
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

            // Nếu là admin hoặc staff, tạo/tìm KhachHang record
            String roleMa = taiKhoan.getVaiTro().getMa();
            if ("ADM".equals(roleMa) || "STF".equals(roleMa)) {
                // Tìm KhachHang theo tài khoản
                KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);

                if (khachHang == null) {
                    // Tạo KhachHang mới cho admin/staff
                    khachHang = new KhachHang();
                    khachHang.setTaiKhoan(taiKhoan);
                    khachHang.setHoTen(taiKhoan.getEmail() != null ? taiKhoan.getEmail() : "Admin/Staff");
                    khachHang.setKieu("ONLINE");
                    khachHang.setTaoLuc(java.time.LocalDateTime.now()); // Set thời gian tạo
                    khachHang = khachHangRepository.save(khachHang);
                }

                return khachHang.getId();
            }

            // Nếu là customer, tìm KhachHang bình thường
            KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);
            return (khachHang != null) ? khachHang.getId() : null;
        } catch (Exception e) {
            log.error("Error in layKhachHangIdTuAuth: {}", e.getMessage(), e);
            return null;
        }
    }

    private String layEmailTuAuth(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            TaiKhoan taiKhoan = userDetails.getTaiKhoan();
            return taiKhoan.getEmail();
        } catch (Exception e) {
            log.error("Error in layEmailTuAuth: {}", e.getMessage());
            return null;
        }
    }

}
