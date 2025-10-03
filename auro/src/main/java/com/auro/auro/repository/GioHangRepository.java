package com.auro.auro.repository;

import com.auro.auro.model.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GioHangRepository extends JpaRepository<GioHang, Long> {
    Optional<GioHang> findFirstByKhachHang_Id(Long idKhachHang); // user đã đăng nhập

    Optional<GioHang> findFirstByIdPhien(String idPhien); // guest dùng session
}
