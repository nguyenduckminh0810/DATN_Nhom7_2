package com.auro.auro.service;

import com.auro.auro.dto.request.DangKyRequest;
import com.auro.auro.dto.request.DangNhapRequest;
import com.auro.auro.dto.response.JwtResponse;
import com.auro.auro.dto.response.UserInfoResponse;
import com.auro.auro.exception.BadRequestException;
import com.auro.auro.exception.DuplicateResourceException;
import com.auro.auro.exception.ResourceNotFoundException;
import com.auro.auro.exception.UnauthorizedException;
import com.auro.auro.model.KhachHang;
import com.auro.auro.model.NhanVien;  
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.model.VaiTro;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.repository.NhanVienRepository;  
import com.auro.auro.repository.TaiKhoanRepository;
import com.auro.auro.repository.VaiTroRepository;
import com.auro.auro.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;  
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public JwtResponse dangKy(DangKyRequest request) {
        
        //Validate
        // if ((request.getEmail() == null || request.getEmail().trim().isEmpty()) && 
        //     (request.getSoDienThoai() == null || request.getSoDienThoai().trim().isEmpty())) {
        //     throw new BadRequestException("Phải nhập ít nhất email hoặc số điện thoại");
        // }

        // check trùng
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (taiKhoanRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email này đã được đăng ký");
            }
        }

        if (request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()) {
            if (taiKhoanRepository.existsBySoDienThoai(request.getSoDienThoai())) {
                throw new DuplicateResourceException("Số điện thoại này đã được đăng ký");
            }
        }
        //Tìm vai trò
//Tìm vai trò
String maVaiTro = request.getLoaiTaiKhoan() != null ? 
                  request.getLoaiTaiKhoan().toUpperCase() : "CUSTOMER";
System.out.println("DEBUG - maVaiTro: " + maVaiTro);
String role;
switch (maVaiTro) {
    case "CUSTOMER":
    case "CUS":
        role = "CUS";
        break;
    case "GUEST":
    case "GST":
        role = "GST";
        break;
    case "STAFF":
    case "STF":
        role = "STF";
        break;
    case "ADMIN":
    case "ADM":
        role = "ADM";
        break;
    default:
        System.out.println("DEBUG - No case matched for: " + maVaiTro);
        throw new BadRequestException("Loại tài khoản không hợp lệ: " + maVaiTro);
}
System.out.println("DEBUG - Final role: " + role);
        
        VaiTro vaiTro = vaiTroRepository.findByMa(role)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy vai trò: " + role
                ));



        String matKhauHash = passwordEncoder.encode(request.getMatKhau());

        // Tạo TaiKhoan
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setEmail(request.getEmail() != null && !request.getEmail().trim().isEmpty() ? 
                          request.getEmail().trim() : null);
        taiKhoan.setSoDienThoai(request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty() ? 
                                request.getSoDienThoai().trim() : null);
        taiKhoan.setMatKhauHash(matKhauHash);
        taiKhoan.setVaiTro(vaiTro);
        taiKhoan.setTrangThai(true);
        taiKhoan.setTaoLuc(LocalDateTime.now());
        taiKhoan.setCapNhatLuc(LocalDateTime.now());

        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);

        if ("CUS".equals(maVaiTro) || "GST".equals(maVaiTro)) {
            // Tạo KhachHang
            KhachHang khachHang = new KhachHang();
            khachHang.setTaiKhoan(savedTaiKhoan);
            khachHang.setHoTen(request.getHoTen());
            khachHang.setEmail(savedTaiKhoan.getEmail());
            khachHang.setSoDienThoai(savedTaiKhoan.getSoDienThoai());
            khachHang.setKieu(maVaiTro);
            khachHangRepository.save(khachHang);
            
        } else if ("STF".equals(maVaiTro) || "ADM".equals(maVaiTro)) {
            // Tạo NhanVien
            NhanVien nhanVien = new NhanVien();
            nhanVien.setTaiKhoan(savedTaiKhoan);
            nhanVien.setHoTen(request.getHoTen());
            nhanVien.setSoDienThoai(savedTaiKhoan.getSoDienThoai());
            nhanVienRepository.save(nhanVien);
            
        } else {
            throw new BadRequestException("Loại tài khoản không hợp lệ: " + maVaiTro);
        }

        String username = savedTaiKhoan.getEmail() != null ? 
                  savedTaiKhoan.getEmail() : 
                  savedTaiKhoan.getSoDienThoai();

                  log.info("=== DEBUG REGISTRATION ===");
                  log.info("Email: {}", savedTaiKhoan.getEmail());
                  log.info("SoDienThoai: {}", savedTaiKhoan.getSoDienThoai());
                  log.info("Username: {}", username);
                  log.info("Username length: {}", username != null ? username.length() : "NULL");
                  log.info("========================");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedTaiKhoan.getEmail() != null ? 
                              savedTaiKhoan.getEmail() : 
                              savedTaiKhoan.getSoDienThoai())
                .password(savedTaiKhoan.getMatKhauHash())
                .authorities("ROLE_" + vaiTro.getMa())
                .build();

        String accessToken = jwtService.generateToken(userDetails);
        Long expiresIn = 900000L;


        UserInfoResponse userInfo = mapToUserInfoResponse(savedTaiKhoan);


        return JwtResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(userInfo)
                .build();
    }

    @Transactional(readOnly = true)
    public JwtResponse dangNhap(DangNhapRequest request) {
        
        //Tìm TaiKhoan
        TaiKhoan taiKhoan = taiKhoanRepository
                .findByEmailOrSoDienThoaiAndTrangThaiTrue(request.getLogin())
                .orElseThrow(() -> new UnauthorizedException(
                    "Email/số điện thoại hoặc mật khẩu không chính xác"
                ));

        // check trạng thái
        if (!Boolean.TRUE.equals(taiKhoan.getTrangThai())) {
            throw new UnauthorizedException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên");
        }

       //Verify mật khẩu
        if (!passwordEncoder.matches(request.getMatKhau(), taiKhoan.getMatKhauHash())) {
            throw new UnauthorizedException("Email/số điện thoại hoặc mật khẩu không chính xác");
        }


        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getLogin(),
                    request.getMatKhau()
                )
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Xác thực thất bại: " + e.getMessage());
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(taiKhoan.getEmail() != null ? 
                              taiKhoan.getEmail() : 
                              taiKhoan.getSoDienThoai())
                .password(taiKhoan.getMatKhauHash())
                .authorities("ROLE_" + taiKhoan.getVaiTro().getMa())
                .build();

        String accessToken = jwtService.generateToken(userDetails);
        Long expiresIn = 900000L;


        UserInfoResponse userInfo = mapToUserInfoResponse(taiKhoan);


        return JwtResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(userInfo)
                .build();
    }

    private UserInfoResponse mapToUserInfoResponse(TaiKhoan taiKhoan) {
        
        String hoTen = "N/A";
        String kieu = null;
        
        // Tìm thông tin từ KhachHang hoặc NhanVien
        String maVaiTro = taiKhoan.getVaiTro().getMa();
        
        if ("CUS".equals(maVaiTro) || "GST".equals(maVaiTro)) {
            // Tìm KhachHang
            KhachHang khachHang = khachHangRepository.findByTaiKhoan(taiKhoan).orElse(null);
            if (khachHang != null) {
                hoTen = khachHang.getHoTen();
                kieu = khachHang.getKieu();
            }
            
        } else if ("STF".equals(maVaiTro) || "ADM".equals(maVaiTro)) {
            // Tìm NhanVien
            NhanVien nhanVien = nhanVienRepository.findByTaiKhoan(taiKhoan).orElse(null);
            if (nhanVien != null) {
                hoTen = nhanVien.getHoTen();
                kieu = maVaiTro; 
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
}