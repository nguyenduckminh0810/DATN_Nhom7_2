package com.auro.auro.repository;

import com.auro.auro.model.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    Optional<NhanVien> findByTaiKhoan_Id(Long idTaiKhoan);

    Optional<NhanVien> findBySoDienThoai(String soDienThoai);
}
