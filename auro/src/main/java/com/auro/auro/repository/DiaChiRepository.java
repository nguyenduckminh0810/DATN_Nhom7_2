package com.auro.auro.repository;

import com.auro.auro.model.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaChiRepository extends JpaRepository<DiaChi, Long> {
    List<DiaChi> findByKhachHang_IdOrderByMacDinhDescIdDesc(Long idKhachHang);

    Optional<DiaChi> findFirstByKhachHang_IdAndMacDinhTrue(Long idKhachHang);
}
