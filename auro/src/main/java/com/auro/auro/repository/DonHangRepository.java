package com.auro.auro.repository;

import com.auro.auro.model.DonHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonHangRepository extends JpaRepository<DonHang, Long> {
    Optional<DonHang> findBySoDonHang(String soDonHang);

    Page<DonHang> findByKhachHang_Id(Long idKhachHang, Pageable pageable);

    Page<DonHang> findByTrangThai(String trangThai, Pageable pageable);

    Page<DonHang> findByDatLucBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("SELECT dh FROM DonHang dh LEFT JOIN FETCH dh.chiTietList")
    List<DonHang> findAllWithChiTiet();

    Optional<DonHang> findByIdAndKhachHang_Id(Long id, Long khachHangId);

}
