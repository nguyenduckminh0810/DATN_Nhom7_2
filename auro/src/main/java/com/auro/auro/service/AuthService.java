package com.auro.auro.service;

import com.auro.auro.dto.request.DangKyRequest;
import com.auro.auro.dto.request.DangNhapRequest;
import com.auro.auro.dto.request.ForgotPasswordRequest;
import com.auro.auro.dto.request.ResetPasswordRequest;
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
import com.auro.auro.model.ResetToken;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.repository.NhanVienRepository;
import com.auro.auro.repository.ResetTokenRepository;
import com.auro.auro.repository.TaiKhoanRepository;
import com.auro.auro.repository.VaiTroRepository;
import com.auro.auro.security.JwtService;
import com.auro.auro.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.jwt.expiration:900000}")
    private Long jwtExpiration;

    @Transactional
    public JwtResponse dangKy(DangKyRequest request) {

        // Validate
        // if ((request.getEmail() == null || request.getEmail().trim().isEmpty()) &&
        // (request.getSoDienThoai() == null ||
        // request.getSoDienThoai().trim().isEmpty())) {
        // throw new BadRequestException("Phải nhập ít nhất email hoặc số điện thoại");
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
        // Tìm vai trò
        // Tìm vai trò
        String maVaiTro = request.getLoaiTaiKhoan() != null ? request.getLoaiTaiKhoan().toUpperCase() : "CUS";
        System.out.println("DEBUG - maVaiTro: " + maVaiTro);
        String role;
        switch (maVaiTro) {
            case "CUS":
                role = "CUS";
                break;
            case "GST":
                role = "GST";
                break;
            case "STF":
                role = "STF";
                break;
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
                        "Không tìm thấy vai trò: " + role));

        String matKhauHash = passwordEncoder.encode(request.getMatKhau());

        // Tạo TaiKhoan
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setEmail(
                request.getEmail() != null && !request.getEmail().trim().isEmpty() ? request.getEmail().trim() : null);
        taiKhoan.setSoDienThoai(request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()
                ? request.getSoDienThoai().trim()
                : null);
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
            khachHang.setTaoLuc(LocalDateTime.now()); // Set thời gian tạo
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

        String username = savedTaiKhoan.getEmail() != null ? savedTaiKhoan.getEmail() : savedTaiKhoan.getSoDienThoai();

        log.info("=== DEBUG REGISTRATION ===");
        log.info("Email: {}", savedTaiKhoan.getEmail());
        log.info("SoDienThoai: {}", savedTaiKhoan.getSoDienThoai());
        log.info("Username: {}", username);
        log.info("Username length: {}", username != null ? username.length() : "NULL");
        log.info("========================");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(
                        savedTaiKhoan.getEmail() != null ? savedTaiKhoan.getEmail() : savedTaiKhoan.getSoDienThoai())
                .password(savedTaiKhoan.getMatKhauHash())
                .authorities("ROLE_" + vaiTro.getMa())
                .build();

        String accessToken = jwtService.generateToken(userDetails);
        Long expiresIn = jwtExpiration;

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

        // Tìm TaiKhoan không phân biệt trạng thái để có thể kiểm tra trạng thái
        TaiKhoan taiKhoan = taiKhoanRepository
                .findByEmailOrSoDienThoaiIgnoreStatus(request.getLogin())
                .orElseThrow(() -> new UnauthorizedException(
                        "Email/số điện thoại hoặc mật khẩu không chính xác"));

        // Check trạng thái tài khoản trước khi verify mật khẩu
        if (!Boolean.TRUE.equals(taiKhoan.getTrangThai())) {
            throw new UnauthorizedException("Tài khoản đã bị khóa hoặc ngừng hoạt động. Vui lòng liên hệ quản trị viên");
        }

        // Verify mật khẩu
        if (!passwordEncoder.matches(request.getMatKhau(), taiKhoan.getMatKhauHash())) {
            throw new UnauthorizedException("Email/số điện thoại hoặc mật khẩu không chính xác");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLogin(),
                            request.getMatKhau()));
        } catch (Exception e) {
            throw new UnauthorizedException("Xác thực thất bại: " + e.getMessage());
        }

        System.out.println("=== DEBUG dangNhap ===");
        System.out.println("TaiKhoan ID: " + taiKhoan.getId());
        System.out.println("Email: " + taiKhoan.getEmail());
        System.out.println("VaiTro object: " + taiKhoan.getVaiTro());
        System.out.println("VaiTro Ma: " + (taiKhoan.getVaiTro() != null ? taiKhoan.getVaiTro().getMa() : "NULL!"));

        if (taiKhoan.getVaiTro() == null) {
            throw new RuntimeException("Tài khoản không có vai trò! ID: " + taiKhoan.getId());
        }

        String roleMa = taiKhoan.getVaiTro().getMa();
        String authority = "ROLE_" + roleMa;

        System.out.println("Authority to be added: " + authority);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(taiKhoan.getEmail() != null ? taiKhoan.getEmail() : taiKhoan.getSoDienThoai())
                .password(taiKhoan.getMatKhauHash())
                .authorities(authority)
                .build();

        System.out.println("UserDetails authorities: " + userDetails.getAuthorities());

        String accessToken = jwtService.generateToken(userDetails);
        Long expiresIn = jwtExpiration;

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

    @Transactional
    public void quenMatKhau(ForgotPasswordRequest request) {
        // Tìm tài khoản theo email
        TaiKhoan taiKhoan = taiKhoanRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email không tồn tại trong hệ thống"));

        // Kiểm tra trạng thái tài khoản
        if (!Boolean.TRUE.equals(taiKhoan.getTrangThai())) {
            throw new BadRequestException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên");
        }

        // Vô hiệu hóa tất cả token cũ của user này
        resetTokenRepository.invalidateAllTokensForUser(taiKhoan);

        // Tạo reset token mới
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime hetHanLuc = LocalDateTime.now().plusHours(24); // Token có hiệu lực 24 giờ

        // Lưu reset token vào database
        ResetToken resetTokenEntity = ResetToken.builder()
                .token(resetToken)
                .taiKhoan(taiKhoan)
                .hetHanLuc(hetHanLuc)
                .daSuDung(false)
                .taoLuc(LocalDateTime.now())
                .build();

        resetTokenRepository.save(resetTokenEntity);

        // Tạo URL reset password
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

        try {
            // Gửi email
            emailService.guiEmailDatLaiMatKhau(request.getEmail(), resetToken, resetUrl);
            log.info("Đã gửi email đặt lại mật khẩu đến {}", request.getEmail());
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đặt lại mật khẩu: {}", e.getMessage());
            // Nếu email chưa được config hoặc có lỗi, vẫn log token ra console cho dev
            log.warn("Email service chưa được cấu hình hoặc có lỗi. Token reset: {}", resetToken);
            // Trong môi trường dev, log token ra console
            System.out.println("=== DEV MODE: Reset Password Token ===");
            System.out.println("Email: " + request.getEmail());
            System.out.println("Reset URL: " + resetUrl);
            System.out.println("Token: " + resetToken);
            System.out.println("Lưu ý: Trong production, token này sẽ được gửi qua email");
            System.out.println("=====================================");
            // Không throw exception, vẫn trả về success để user không biết email chưa config
            // Trong production, nên throw exception để admin biết cần config email
        }
    }

    @Transactional
    public void datLaiMatKhau(ResetPasswordRequest request) {
        // Tìm token hợp lệ (chưa sử dụng và chưa hết hạn)
        ResetToken resetTokenEntity = resetTokenRepository.findValidToken(
                request.getToken(), 
                LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Token không hợp lệ hoặc đã hết hạn. Vui lòng yêu cầu link đặt lại mật khẩu mới."));

        // Kiểm tra token đã được sử dụng chưa
        if (resetTokenEntity.getDaSuDung()) {
            throw new BadRequestException("Token đã được sử dụng. Vui lòng yêu cầu link đặt lại mật khẩu mới.");
        }

        // Kiểm tra token đã hết hạn chưa
        if (resetTokenEntity.isExpired()) {
            throw new BadRequestException("Token đã hết hạn. Vui lòng yêu cầu link đặt lại mật khẩu mới.");
        }

        // Lấy tài khoản từ token
        TaiKhoan taiKhoan = resetTokenEntity.getTaiKhoan();

        // Kiểm tra trạng thái tài khoản
        if (!Boolean.TRUE.equals(taiKhoan.getTrangThai())) {
            throw new BadRequestException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên");
        }

        // Mã hóa mật khẩu mới
        String newPasswordHash = passwordEncoder.encode(request.getPassword());

        // Cập nhật mật khẩu
        taiKhoan.setMatKhauHash(newPasswordHash);
        taiKhoan.setCapNhatLuc(LocalDateTime.now());
        taiKhoanRepository.save(taiKhoan);

        // Đánh dấu token đã được sử dụng
        resetTokenEntity.setDaSuDung(true);
        resetTokenRepository.save(resetTokenEntity);

        // Vô hiệu hóa tất cả token còn lại của user này (bảo mật)
        resetTokenRepository.invalidateAllTokensForUser(taiKhoan);

        log.info("Đã đặt lại mật khẩu cho tài khoản ID: {}", taiKhoan.getId());
    }
}