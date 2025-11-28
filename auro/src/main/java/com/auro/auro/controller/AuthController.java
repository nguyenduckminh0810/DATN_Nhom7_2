package com.auro.auro.controller;

import com.auro.auro.dto.request.DangKyRequest;
import com.auro.auro.dto.request.DangNhapRequest;
import com.auro.auro.dto.request.ForgotPasswordRequest;
import com.auro.auro.dto.request.ResetPasswordRequest;
import com.auro.auro.dto.response.ApiResponse;
import com.auro.auro.dto.response.JwtResponse;
import com.auro.auro.dto.response.UserInfoResponse;
import com.auro.auro.exception.UnauthorizedException;
import com.auro.auro.model.KhachHang;
import com.auro.auro.model.NhanVien;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.repository.NhanVienRepository;
import com.auro.auro.repository.TaiKhoanRepository;
import com.auro.auro.security.JwtService;
import com.auro.auro.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.util.HashMap;
import java.util.Map;

//import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<JwtResponse>> dangKy(
            @Valid @RequestBody DangKyRequest request) {

        try {
            JwtResponse jwtResponse = authService.dangKy(request);

            ApiResponse<JwtResponse> response = ApiResponse.success(
                    jwtResponse,
                    "Đăng ký thành công. Chào mừng bạn đến với AURO!");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> dangNhap(
            @Valid @RequestBody DangNhapRequest request) {

        try {
            JwtResponse jwtResponse = authService.dangNhap(request);

            ApiResponse<JwtResponse> response = ApiResponse.success(
                    jwtResponse,
                    "Đăng nhập thành công. Chào mừng bạn quay trở lại!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        try {
            // Lấy thông tin user từ SecurityContext (đã được JwtAuthenticationFilter xử lý)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new UnauthorizedException("Chưa đăng nhập");
            }

            String username = authentication.getName();

            TaiKhoan taiKhoan = taiKhoanRepository
                    .findByEmailOrSoDienThoaiAndTrangThaiTrue(username)
                    .orElseThrow(() -> new UnauthorizedException("Không tìm thấy tài khoản"));

            UserInfoResponse userInfo = mapToUserInfoResponse(taiKhoan);

            ApiResponse<UserInfoResponse> response = ApiResponse.success(
                    userInfo,
                    "Token hợp lệ");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> dangXuat() {

        ApiResponse<Object> response = ApiResponse.success("Đăng xuất thành công");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== Upload Avatar Started ===");

            // Lấy thông tin user hiện tại
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                System.out.println("ERROR: Not authenticated");
                throw new UnauthorizedException("Chưa đăng nhập");
            }

            String username = authentication.getName();
            System.out.println("Username: " + username);

            TaiKhoan taiKhoan = taiKhoanRepository
                    .findByEmailOrSoDienThoaiAndTrangThaiTrue(username)
                    .orElseThrow(() -> new UnauthorizedException("Không tìm thấy tài khoản"));

            System.out.println("TaiKhoan found: " + taiKhoan.getId());

            // Kiểm tra file
            if (file.isEmpty()) {
                System.out.println("ERROR: File is empty");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File không được để trống"));
            }

            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());

            // Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                System.out.println("ERROR: Invalid content type");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File phải là ảnh"));
            }

            // Kiểm tra kích thước file (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                System.out.println("ERROR: File too large");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Kích thước file không được vượt quá 5MB"));
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            System.out.println("Generated filename: " + fileName);

            // Lưu file vào thư mục uploads
            Path uploadPath = Paths.get("uploads/avatars");
            if (!Files.exists(uploadPath)) {
                System.out.println("Creating directory: " + uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            System.out.println("Saving file to: " + filePath.toAbsolutePath());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File saved successfully");

            // Cập nhật avatar
            String avatarUrl = "/uploads/avatars/" + fileName;
            String maVaiTro = taiKhoan.getVaiTro().getMa();
            String email = taiKhoan.getEmail();
            System.out.println("User role: " + maVaiTro + ", Email: " + email);

            if ("CUS".equals(maVaiTro) || "GST".equals(maVaiTro)) {
                // Khách hàng
                KhachHang khachHang = khachHangRepository.findByTaiKhoan_Email(email)
                        .orElseThrow(
                                () -> new RuntimeException("Không tìm thấy thông tin khách hàng với email: " + email));
                System.out.println("KhachHang found: " + khachHang.getId());

                khachHang.setAvatar(avatarUrl);
                khachHangRepository.save(khachHang);
                System.out.println("Avatar updated in database: " + avatarUrl);
            } else if ("STF".equals(maVaiTro) || "ADM".equals(maVaiTro)) {
                // Admin hoặc Staff
                NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_Email(email)
                        .orElseThrow(
                                () -> new RuntimeException("Không tìm thấy thông tin nhân viên với email: " + email));
                System.out.println("NhanVien found: " + nhanVien.getId());

                nhanVien.setAvatar(avatarUrl);
                nhanVienRepository.save(nhanVien);
                System.out.println("Avatar updated in database: " + avatarUrl);
            } else {
                System.out.println("ERROR: Unknown user role");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Vai trò không hợp lệ"));
            }

            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);

            System.out.println("=== Upload Avatar Success ===");
            return ResponseEntity.ok(ApiResponse.success(result, "Upload avatar thành công"));

        } catch (IOException e) {
            System.out.println("ERROR: IOException - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lưu file: " + e.getMessage()));
        } catch (Exception e) {
            System.out.println("ERROR: Exception - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private UserInfoResponse mapToUserInfoResponse(TaiKhoan taiKhoan) {

        String hoTen = "N/A";
        String kieu = null;
        String avatar = null;

        String maVaiTro = taiKhoan.getVaiTro().getMa();
        String email = taiKhoan.getEmail();

        if ("CUS".equals(maVaiTro) || "GST".equals(maVaiTro)) {
            // Tìm KhachHang theo email
            KhachHang khachHang = khachHangRepository.findByTaiKhoan_Email(email).orElse(null);
            if (khachHang != null) {
                hoTen = khachHang.getHoTen();
                kieu = khachHang.getKieu();
                avatar = khachHang.getAvatar();
            }

        } else if ("STF".equals(maVaiTro) || "ADM".equals(maVaiTro)) {
            // Tìm NhanVien theo email
            NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_Email(email).orElse(null);
            if (nhanVien != null) {
                hoTen = nhanVien.getHoTen();
                kieu = "STF";
                avatar = nhanVien.getAvatar();
            }
        }

        return UserInfoResponse.builder()
                .id(taiKhoan.getId())
                .email(taiKhoan.getEmail())
                .soDienThoai(taiKhoan.getSoDienThoai())
                .hoTen(hoTen)
                .vaiTro(maVaiTro)
                .trangThai(taiKhoan.getTrangThai())
                .taoLuc(taiKhoan.getTaoLuc())
                .kieu(kieu)
                .avatar(avatar)
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> quenMatKhau(
            @Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.quenMatKhau(request);

            ApiResponse<Object> response = ApiResponse.success(
                    null,
                    "Link đặt lại mật khẩu đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> datLaiMatKhau(
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.datLaiMatKhau(request);

            ApiResponse<Object> response = ApiResponse.success(
                    null,
                    "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/debug")
    public ResponseEntity<ApiResponse<Object>> debugToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(ApiResponse.error("Không có token"));
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("token", token);
            debugInfo.put("username", username);
            debugInfo.put("isValid", "Checking...");

            return ResponseEntity.ok(ApiResponse.success(debugInfo, "Debug info"));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Lỗi debug: " + e.getMessage()));
        }
    }
}