package com.auro.auro.repository;

import com.auro.auro.model.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import com.auro.auro.model.TaiKhoan;

import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    Optional<KhachHang> findByEmail(String email);
    Optional<KhachHang> findByTaiKhoan(TaiKhoan taiKhoan);
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);
}
