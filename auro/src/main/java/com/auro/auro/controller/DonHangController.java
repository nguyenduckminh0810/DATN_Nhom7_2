package com.auro.auro.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auro.auro.dto.request.CreateDonHangRequest;
import com.auro.auro.dto.response.DonHangResponse;
import com.auro.auro.model.DonHang;
import com.auro.auro.model.DonHangChiTiet;
import com.auro.auro.exception.BadRequestException;
import com.auro.auro.service.DonHangService;
import com.auro.auro.security.CustomUserDetails;
import com.auro.auro.model.TaiKhoan;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/don-hang")
@RequiredArgsConstructor

public class DonHangController {

    private final DonHangService donHangService;

    // Tạo đơn hàng
    @PostMapping
    public ResponseEntity<DonHang> createDonHang(@RequestBody CreateDonHangRequest request) {
        return ResponseEntity.ok(donHangService.createDonHang(request.getDonHang(), request.getChiTietList()));
    }

    @GetMapping
    public List<DonHangResponse> getAllDonHang() {
        return donHangService.getAllDonHangDTO();
    }

    // Lấy đơn hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<DonHang> getDonHangById(@PathVariable Long id) {
        return ResponseEntity.ok(donHangService.getDonHangById(id));
    }

    // Xem đơn hàng chi tiết theo ID đơn hàng
    @GetMapping("/{id}/chi-tiet")
    public ResponseEntity<List<DonHangChiTiet>> getChiTietDonHangById(@PathVariable Long id) {
        return ResponseEntity.ok(donHangService.getChiTietByDonHangId(id));
    }

    @GetMapping("/theo-doi")
    public ResponseEntity<?> theoDoiDonHang(
            @RequestParam(value = "maDon", required = false) String maDon,
            @RequestParam(value = "soDienThoai", required = false) String soDienThoai) {

        boolean hasMa = maDon != null && !maDon.trim().isEmpty();
        boolean hasPhone = soDienThoai != null && !soDienThoai.trim().isEmpty();

        if (!hasMa && !hasPhone) {
            throw new BadRequestException("Vui lòng nhập mã đơn hàng hoặc số điện thoại");
        }

        if (hasMa) {
            DonHangResponse donHang = donHangService.traCuuDonHangTheoMa(maDon, soDienThoai);
            return ResponseEntity.ok(donHang);
        }

        return ResponseEntity.ok(donHangService.traCuuDonHangTheoSoDienThoai(soDienThoai));
    }

    // Cập nhật trạng thái đơn hàng
    @PutMapping("/{id}")
    public ResponseEntity<DonHangResponse> updateDonHang(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        System.out.println("=== UPDATE DON HANG ===");
        System.out.println("ID: " + id);
        System.out.println("Request body: " + request);

        try {
            DonHangResponse updated = donHangService.updateDonHang(id, request);
            System.out.println("Updated successfully: " + updated);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("Error updating: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Xóa mềm đơn hàng (chuyển sang trạng thái Đã hủy)
    @PreAuthorize("hasAnyRole('STF', 'ADM')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDonHang(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request,
            Authentication auth) {
        Map<String, String> response = new HashMap<>();

        try {
            System.out.println("=== XÓA MỀM ĐƠN HÀNG (ADMIN) ===");
            System.out.println("Order ID: " + id);
            System.out.println("Authentication: " + (auth != null ? "Có" : "NULL"));
            System.out.println("Principal: " + (auth != null && auth.getPrincipal() != null ? auth.getPrincipal().getClass().getSimpleName() : "NULL"));
            
            String lyDoHuy = request != null ? request.get("lyDoHuy") : null;
            if (lyDoHuy == null || lyDoHuy.trim().isEmpty()) {
                response.put("error", "Vui lòng nhập lý do hủy đơn hàng");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Lấy email từ Authentication
            String emailNguoiHuy = layEmailTuAuth(auth);
            System.out.println("Email người hủy: " + (emailNguoiHuy != null ? emailNguoiHuy : "NULL"));

            donHangService.softDeleteDonHang(id, lyDoHuy, emailNguoiHuy);

            response.put("message", "Đã hủy đơn hàng thành công");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("RuntimeException: " + e.getMessage());
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String layEmailTuAuth(Authentication auth) {
        if (auth == null) {
            System.err.println("❌ Authentication is NULL");
            return null;
        }
        
        if (auth.getPrincipal() == null) {
            System.err.println("❌ Authentication.getPrincipal() is NULL");
            return null;
        }
        
        try {
            System.out.println("Principal type: " + auth.getPrincipal().getClass().getName());
            
            if (!(auth.getPrincipal() instanceof CustomUserDetails)) {
                System.err.println("❌ Principal is not CustomUserDetails, it's: " + auth.getPrincipal().getClass().getName());
                return null;
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            TaiKhoan taiKhoan = userDetails.getTaiKhoan();
            
            if (taiKhoan == null) {
                System.err.println("❌ TaiKhoan is NULL");
                return null;
            }
            
            String email = taiKhoan.getEmail();
            System.out.println("✅ Successfully extracted email: " + email);
            return email;
        } catch (Exception e) {
            System.err.println("❌ Error in layEmailTuAuth: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Phân trang đơn hàng
    @GetMapping("/phan-trang")
    public ResponseEntity<Map<String, Object>> getDonHangPhanTrang(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        Page<DonHangResponse> donHangPage = donHangService.getDonHangPhanTrang(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", donHangPage.getContent());
        response.put("currentPage", donHangPage.getNumber());
        response.put("totalItems", donHangPage.getTotalElements());
        response.put("totalPages", donHangPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * Fix các đơn hàng cũ có tongThanhToan = null
     * Endpoint: POST /api/don-hang/fix-null-total
     */
    @PostMapping("/fix-null-total")
    public ResponseEntity<Map<String, Object>> fixNullTongThanhToan() {
        Map<String, Object> response = new HashMap<>();

        try {
            int fixedCount = donHangService.fixDonHangNullTongThanhToan();
            response.put("success", true);
            response.put("message", "Đã fix " + fixedCount + " đơn hàng");
            response.put("fixedCount", fixedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
