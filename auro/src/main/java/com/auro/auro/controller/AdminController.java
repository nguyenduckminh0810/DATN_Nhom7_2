package com.auro.auro.controller;

import com.auro.auro.dto.request.DangKyRequest;
import com.auro.auro.dto.response.ApiResponse;
import com.auro.auro.exception.BadRequestException;
import com.auro.auro.exception.DuplicateResourceException;
import com.auro.auro.exception.ResourceNotFoundException;
import com.auro.auro.model.NhanVien;
import com.auro.auro.model.TaiKhoan;
import com.auro.auro.model.VaiTro;
import com.auro.auro.repository.NhanVienRepository;
import com.auro.auro.repository.TaiKhoanRepository;
import com.auro.auro.repository.VaiTroRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Endpoint đăng ký admin đầu tiên - CHỈ hoạt động khi chưa có admin nào trong
     * hệ thống
     * Sau khi có admin đầu tiên, endpoint này sẽ trả về lỗi 403
     */
    @PostMapping("/register-first-admin")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerFirstAdmin(
            @Valid @RequestBody DangKyRequest request) {

        System.out.println("=== Register First Admin Attempt ===");

        // Kiểm tra xem đã có admin nào chưa
        VaiTro adminRole = vaiTroRepository.findByMa("ADM")
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò Admin"));

        long adminCount = taiKhoanRepository.countByVaiTro(adminRole);
        System.out.println("Current admin count: " + adminCount);

        if (adminCount > 0) {
            System.out.println("Admin already exists - registration blocked");
            throw new BadRequestException(
                    "Hệ thống đã có admin. Vui lòng liên hệ admin hiện tại để tạo tài khoản mới.");
        }

        // Validate
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email là bắt buộc cho tài khoản admin");
        }

        if (taiKhoanRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email này đã được đăng ký");
        }

        if (request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()) {
            if (taiKhoanRepository.existsBySoDienThoai(request.getSoDienThoai())) {
                throw new DuplicateResourceException("Số điện thoại này đã được đăng ký");
            }
        }

        System.out.println("Creating first admin account...");

        // Tạo TaiKhoan với vai trò Admin
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setEmail(request.getEmail().trim());
        taiKhoan.setSoDienThoai(
                request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()
                        ? request.getSoDienThoai().trim()
                        : null);
        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.getMatKhau()));
        taiKhoan.setVaiTro(adminRole);
        taiKhoan.setTrangThai(true);
        taiKhoan.setTaoLuc(LocalDateTime.now());
        taiKhoan.setCapNhatLuc(LocalDateTime.now());

        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);
        System.out.println("First admin TaiKhoan created with ID: " + savedTaiKhoan.getId());

        // Tạo NhanVien
        NhanVien nhanVien = new NhanVien();
        nhanVien.setTaiKhoan(savedTaiKhoan);
        nhanVien.setHoTen(request.getHoTen() != null ? request.getHoTen() : "Admin");
        nhanVien.setSoDienThoai(savedTaiKhoan.getSoDienThoai());

        NhanVien savedNhanVien = nhanVienRepository.save(nhanVien);
        System.out.println("First admin NhanVien created with ID: " + savedNhanVien.getId());

        // Tạo response
        Map<String, Object> result = new HashMap<>();
        result.put("id", savedTaiKhoan.getId());
        result.put("email", savedTaiKhoan.getEmail());
        result.put("hoTen", nhanVien.getHoTen());
        result.put("vaiTro", "ADM");
        result.put("message", "Tạo tài khoản admin đầu tiên thành công! Bạn có thể đăng nhập ngay bây giờ.");

        System.out.println("=== First Admin Created Successfully ===");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Tạo tài khoản admin đầu tiên thành công"));
    }

    @PostMapping("/register-staff")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerStaff(
            @Valid @RequestBody DangKyRequest request) {

        System.out.println("=== Admin Register Staff Started ===");
        System.out.println("Request: " + request);

        // Validate loại tài khoản - chỉ cho phép STF hoặc ADM
        String loaiTaiKhoan = request.getLoaiTaiKhoan();
        if (loaiTaiKhoan == null ||
                (!loaiTaiKhoan.equalsIgnoreCase("STF") && !loaiTaiKhoan.equalsIgnoreCase("ADM"))) {
            throw new BadRequestException("Loại tài khoản phải là 'STF' (Staff) hoặc 'ADM' (Admin)");
        }

        String maVaiTro = loaiTaiKhoan.toUpperCase();
        System.out.println("Role type: " + maVaiTro);

        // Kiểm tra trùng email
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (taiKhoanRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email này đã được đăng ký");
            }
        } else {
            throw new BadRequestException("Email là bắt buộc cho tài khoản nhân viên");
        }

        // Kiểm tra trùng số điện thoại (nếu có)
        if (request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()) {
            if (taiKhoanRepository.existsBySoDienThoai(request.getSoDienThoai())) {
                throw new DuplicateResourceException("Số điện thoại này đã được đăng ký");
            }
        }

        // Tìm vai trò
        VaiTro vaiTro = vaiTroRepository.findByMa(maVaiTro)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò: " + maVaiTro));

        System.out.println("Found role: " + vaiTro.getMa());

        // Tạo TaiKhoan
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setEmail(request.getEmail().trim());
        taiKhoan.setSoDienThoai(
                request.getSoDienThoai() != null && !request.getSoDienThoai().trim().isEmpty()
                        ? request.getSoDienThoai().trim()
                        : null);
        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.getMatKhau()));
        taiKhoan.setVaiTro(vaiTro);
        taiKhoan.setTrangThai(true);
        taiKhoan.setTaoLuc(LocalDateTime.now());
        taiKhoan.setCapNhatLuc(LocalDateTime.now());

        TaiKhoan savedTaiKhoan = taiKhoanRepository.save(taiKhoan);
        System.out.println("TaiKhoan saved with ID: " + savedTaiKhoan.getId());

        // Tạo NhanVien
        NhanVien nhanVien = new NhanVien();
        nhanVien.setTaiKhoan(savedTaiKhoan);
        nhanVien.setHoTen(request.getHoTen() != null ? request.getHoTen() : "N/A");
        nhanVien.setSoDienThoai(savedTaiKhoan.getSoDienThoai());

        NhanVien savedNhanVien = nhanVienRepository.save(nhanVien);
        System.out.println("NhanVien saved with ID: " + savedNhanVien.getId());

        // Tạo response
        Map<String, Object> result = new HashMap<>();
        result.put("id", savedTaiKhoan.getId());
        result.put("email", savedTaiKhoan.getEmail());
        result.put("hoTen", nhanVien.getHoTen());
        result.put("vaiTro", maVaiTro);
        result.put("message", "Đăng ký " + (maVaiTro.equals("ADM") ? "admin" : "nhân viên") + " thành công");

        System.out.println("=== Admin Register Staff Completed ===");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Tạo tài khoản nhân viên thành công"));
    }
}
