package com.auro.auro.controller;

import com.auro.auro.dto.request.DangKyRequest;
import com.auro.auro.dto.request.DangNhapRequest;
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

import java.util.HashMap;
import java.util.Map;

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
                "Đăng ký thành công. Chào mừng bạn đến với AURO!"  
            );
            
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
                "Đăng nhập thành công. Chào mừng bạn quay trở lại!"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Token không hợp lệ");
            }
            
            String token = authHeader.substring(7);
    
           
            String username = jwtService.extractUsername(token);
            if (username == null) {
                throw new UnauthorizedException("Token không hợp lệ");
            }
            
            
            TaiKhoan taiKhoan = taiKhoanRepository
                    .findByEmailOrSoDienThoaiAndTrangThaiTrue(username)
                    .orElseThrow(() -> new UnauthorizedException("Token không hợp lệ"));
            
            // Validate token với user details
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(taiKhoan.getEmail() != null ? taiKhoan.getEmail() : taiKhoan.getSoDienThoai())
                    .password(taiKhoan.getMatKhauHash())
                    .authorities("ROLE_" + taiKhoan.getVaiTro().getMa())
                    .build();
            
            if (!jwtService.isTokenValid(token, userDetails)) {
                throw new UnauthorizedException("Token đã hết hạn hoặc không hợp lệ");
            }
            
           
            UserInfoResponse userInfo = mapToUserInfoResponse(taiKhoan);
            
            ApiResponse<UserInfoResponse> response = ApiResponse.success(
                userInfo,
                "Token hợp lệ"
            );
            
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

    private UserInfoResponse mapToUserInfoResponse(TaiKhoan taiKhoan) {
        
        String hoTen = "N/A";
        String kieu = null;
        
        
        String maVaiTro = taiKhoan.getVaiTro().getMa();
        
        if ("CUSTOMER".equals(maVaiTro) || "GUEST".equals(maVaiTro)) {
            // Tìm KhachHang
            KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);
            if (khachHang != null) {
                hoTen = khachHang.getHoTen();
                kieu = khachHang.getKieu();
            }
            
        } else if ("STAFF".equals(maVaiTro) || "ADMIN".equals(maVaiTro)) {
            
            NhanVien nhanVien = nhanVienRepository.findByTaiKhoan(taiKhoan).orElse(null);
            if (nhanVien != null) {
                hoTen = nhanVien.getHoTen();
                kieu = "STAFF"; 
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
                .build();
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
}}