package com.auro.auro.controller;

import com.auro.auro.dto.request.ChangePasswordRequest;
import com.auro.auro.dto.request.ProfileUpdateRequest;
import com.auro.auro.dto.response.ApiResponse;
import com.auro.auro.dto.response.ProfileResponse;
import com.auro.auro.model.KhachHang;
import com.auro.auro.model.NhanVien;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.repository.NhanVienRepository;
import com.auro.auro.repository.TaiKhoanRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        TaiKhoan taiKhoan = getCurrentTaiKhoan();

        ProfileResponse profile = buildProfileResponse(taiKhoan);

        return ResponseEntity.ok(ApiResponse.success(profile, "Lấy thông tin hồ sơ thành công"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {

        TaiKhoan taiKhoan = getCurrentTaiKhoan();
        String roleCode = taiKhoan.getVaiTro() != null ? taiKhoan.getVaiTro().getMa() : null;
        String email = taiKhoan.getEmail();

        // Chỉ cập nhật HỌ TÊN, tuyệt đối không động vào email / số điện thoại
        if ("CUS".equals(roleCode) || "GST".equals(roleCode)) {
            KhachHang khachHang = khachHangRepository.findByTaiKhoan_Email(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));
            khachHang.setHoTen(request.getFullName());
            khachHangRepository.save(khachHang);
        } else if ("STF".equals(roleCode) || "ADM".equals(roleCode)) {
            NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_Email(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên"));
            nhanVien.setHoTen(request.getFullName());
            nhanVienRepository.save(nhanVien);
        }

        ProfileResponse profile = buildProfileResponse(taiKhoan);
        return ResponseEntity.ok(ApiResponse.success(profile, "Cập nhật hồ sơ thành công"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        TaiKhoan taiKhoan = getCurrentTaiKhoan();

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getCurrentPassword(), taiKhoan.getMatKhauHash())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mật khẩu hiện tại không chính xác"));
        }

        // Đổi mật khẩu
        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.getNewPassword()));
        taiKhoanRepository.save(taiKhoan);

        return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công"));
    }

    private TaiKhoan getCurrentTaiKhoan() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String username = authentication.getName();

        return taiKhoanRepository
                .findByEmailOrSoDienThoaiAndTrangThaiTrue(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
    }

    private ProfileResponse buildProfileResponse(TaiKhoan taiKhoan) {
        String hoTen = null;
        String avatar = null;

        String roleCode = taiKhoan.getVaiTro() != null ? taiKhoan.getVaiTro().getMa() : null;
        String email = taiKhoan.getEmail();

        if ("CUS".equals(roleCode) || "GST".equals(roleCode)) {
            KhachHang khachHang = khachHangRepository.findByTaiKhoan_Email(email).orElse(null);
            if (khachHang != null) {
                hoTen = khachHang.getHoTen();
                avatar = khachHang.getAvatar();
            }
        } else if ("STF".equals(roleCode) || "ADM".equals(roleCode)) {
            NhanVien nhanVien = nhanVienRepository.findByTaiKhoan_Email(email).orElse(null);
            if (nhanVien != null) {
                hoTen = nhanVien.getHoTen();
                avatar = nhanVien.getAvatar();
            }
        }

        if (hoTen == null || hoTen.isBlank()) {
            hoTen = taiKhoan.getEmail() != null
                    ? taiKhoan.getEmail().split("@")[0]
                    : taiKhoan.getSoDienThoai();
        }

        return ProfileResponse.builder()
                .id(taiKhoan.getId())
                .fullName(hoTen)
                .email(taiKhoan.getEmail())
                .phone(taiKhoan.getSoDienThoai())
                .avatar(avatar)
                .role(roleCode)
                .build();
    }
}


