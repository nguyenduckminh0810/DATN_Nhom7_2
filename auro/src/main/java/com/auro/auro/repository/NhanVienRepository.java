package com.auro.auro.repository;

import com.auro.auro.model.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import com.auro.auro.model.TaiKhoan;

import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    Optional<NhanVien> findByTaiKhoan_Id(Long idTaiKhoan);

    Optional<NhanVien> findByTaiKhoan(TaiKhoan taiKhoan);

    Optional<NhanVien> findByTaiKhoan_Email(String email);

    Optional<NhanVien> findBySoDienThoai(String soDienThoai);
}
