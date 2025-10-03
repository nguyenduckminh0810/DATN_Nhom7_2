package com.auro.auro.repository;

import com.auro.auro.model.DonHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DonHangRepository extends JpaRepository<DonHang, Long> {
    Optional<DonHang> findBySoDonHang(String soDonHang);

    Page<DonHang> findByKhachHang_Id(Long idKhachHang, Pageable pageable);

    Page<DonHang> findByTrangThai(String trangThai, Pageable pageable);

    Page<DonHang> findByDatLucBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
