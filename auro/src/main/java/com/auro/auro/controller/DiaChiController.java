package com.auro.auro.controller;

import com.auro.auro.dto.request.DiaChiRequest;
import com.auro.auro.dto.response.ApiResponse;
import com.auro.auro.dto.response.DiaChiResponse;
import com.auro.auro.model.KhachHang;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.security.CustomUserDetails;
import com.auro.auro.service.DiaChiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dia-chi")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class DiaChiController {

        private final DiaChiService diaChiService;
        private final KhachHangRepository khachHangRepository;

        /**
         * Lấy tất cả địa chỉ của khách hàng đang đăng nhập
         */
        @GetMapping
        public ResponseEntity<ApiResponse<List<DiaChiResponse>>> getAllDiaChi(
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                TaiKhoan taiKhoan = userDetails.getTaiKhoan();

                // Debug log
                System.out.println("=== GET ALL DIA CHI ===");
                System.out.println("Tai khoan ID: " + taiKhoan.getId());
                System.out.println("Tai khoan email: " + taiKhoan.getEmail());

                // Kiểm tra xem tài khoản có phải là khách hàng không
                KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);

                System.out.println("Khach hang found: " + (khachHang != null));
                if (khachHang != null) {
                        System.out.println("Khach hang ID: " + khachHang.getId());
                }

                // Nếu không phải khách hàng (ví dụ: Admin/Staff), trả về danh sách rỗng
                if (khachHang == null) {
                        System.out.println("Khach hang is NULL - returning empty list");
                        return ResponseEntity.ok(ApiResponse.<List<DiaChiResponse>>builder()
                                        .success(true)
                                        .message("Tài khoản này không có địa chỉ giao hàng")
                                        .data(List.of())
                                        .build());
                }

                Long khachHangId = khachHang.getId();
                List<DiaChiResponse> diaChiList = diaChiService.getDiaChiByKhachHang(khachHangId);

                System.out.println("Found " + diaChiList.size() + " addresses");

                return ResponseEntity.ok(ApiResponse.<List<DiaChiResponse>>builder()
                                .success(true)
                                .message("Lấy danh sách địa chỉ thành công")
                                .data(diaChiList)
                                .build());
        }

        /**
         * Lấy địa chỉ mặc định
         */
        @GetMapping("/mac-dinh")
        public ResponseEntity<ApiResponse<DiaChiResponse>> getDiaChiMacDinh(
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                TaiKhoan taiKhoan = userDetails.getTaiKhoan();
                // Kiểm tra xem tài khoản có phải là khách hàng không
                KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);

                // Nếu không phải khách hàng, trả về null
                if (khachHang == null) {
                        return ResponseEntity.ok(ApiResponse.<DiaChiResponse>builder()
                                        .success(false)
                                        .message("Tài khoản này không có địa chỉ giao hàng")
                                        .data(null)
                                        .build());
                }

                Long khachHangId = khachHang.getId();
                DiaChiResponse diaChi = diaChiService.getDiaChiMacDinh(khachHangId);

                return ResponseEntity.ok(ApiResponse.<DiaChiResponse>builder()
                                .success(true)
                                .message("Lấy địa chỉ mặc định thành công")
                                .data(diaChi)
                                .build());
        }

        /**
         * Lấy chi tiết một địa chỉ
         */
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<DiaChiResponse>> getDiaChiById(
                        @PathVariable Long id,
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                TaiKhoan taiKhoan = userDetails.getTaiKhoan();
                KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);

                if (khachHang == null) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiResponse.<DiaChiResponse>builder()
                                                        .success(false)
                                                        .message("Tài khoản này không có quyền truy cập địa chỉ")
                                                        .build());
                }

                Long khachHangId = khachHang.getId();
                DiaChiResponse diaChi = diaChiService.getDiaChiById(khachHangId, id);

                return ResponseEntity.ok(ApiResponse.<DiaChiResponse>builder()
                                .success(true)
                                .message("Lấy thông tin địa chỉ thành công")
                                .data(diaChi)
                                .build());
        }

        /**
         * Thêm địa chỉ mới
         */
        @PostMapping
        public ResponseEntity<ApiResponse<DiaChiResponse>> themDiaChi(
                        @Valid @RequestBody DiaChiRequest request,
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                TaiKhoan taiKhoan = userDetails.getTaiKhoan();
                KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);

                if (khachHang == null) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiResponse.<DiaChiResponse>builder()
                                                        .success(false)
                                                        .message("Chỉ khách hàng mới có thể thêm địa chỉ giao hàng")
                                                        .build());
                }

                Long khachHangId = khachHang.getId();
                DiaChiResponse diaChi = diaChiService.themDiaChi(khachHangId, request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<DiaChiResponse>builder()
                                                .success(true)
                                                .message("Thêm địa chỉ thành công")
                                                .data(diaChi)
                                                .build());
        }

        /**
         * Cập nhật địa chỉ
         */
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<DiaChiResponse>> capNhatDiaChi(
                        @PathVariable Long id,
                        @Valid @RequestBody DiaChiRequest request,
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                TaiKhoan taiKhoan = userDetails.getTaiKhoan();
                KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);

                if (khachHang == null) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiResponse.<DiaChiResponse>builder()
                                                        .success(false)
                                                        .message("Tài khoản này không có quyền cập nhật địa chỉ")
                                                        .build());
                }

                Long khachHangId = khachHang.getId();
                DiaChiResponse diaChi = diaChiService.capNhatDiaChi(khachHangId, id, request);

                return ResponseEntity.ok(ApiResponse.<DiaChiResponse>builder()
                                .success(true)
                                .message("Cập nhật địa chỉ thành công")
                                .data(diaChi)
                                .build());
        }

        /**
         * Đặt địa chỉ làm mặc định
         */
        @PatchMapping("/{id}/mac-dinh")
        public ResponseEntity<ApiResponse<DiaChiResponse>> datMacDinh(
                        @PathVariable Long id,
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                TaiKhoan taiKhoan = userDetails.getTaiKhoan();
                KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);

                if (khachHang == null) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiResponse.<DiaChiResponse>builder()
                                                        .success(false)
                                                        .message("Tài khoản này không có quyền thay đổi địa chỉ")
                                                        .build());
                }

                Long khachHangId = khachHang.getId();
                DiaChiResponse diaChi = diaChiService.datMacDinh(khachHangId, id);

                return ResponseEntity.ok(ApiResponse.<DiaChiResponse>builder()
                                .success(true)
                                .message("Đặt địa chỉ mặc định thành công")
                                .data(diaChi)
                                .build());
        }

        /**
         * Xóa địa chỉ
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> xoaDiaChi(
                        @PathVariable Long id,
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                TaiKhoan taiKhoan = userDetails.getTaiKhoan();
                KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);

                if (khachHang == null) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(ApiResponse.<Void>builder()
                                                        .success(false)
                                                        .message("Tài khoản này không có quyền xóa địa chỉ")
                                                        .build());
                }

                Long khachHangId = khachHang.getId();
                diaChiService.xoaDiaChi(khachHangId, id);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Xóa địa chỉ thành công")
                                .build());
        }
}
