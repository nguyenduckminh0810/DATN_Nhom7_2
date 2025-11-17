package com.auro.auro.repository;

import com.auro.auro.model.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GioHangRepository extends JpaRepository<GioHang, Long> {
    Optional<GioHang> findFirstByKhachHang_Id(Long idKhachHang); // user đã đăng nhập

    Optional<GioHang> findFirstByIdPhien(String idPhien); // guest dùng session

    @Query("SELECT COUNT(DISTINCT gh.id) FROM GioHang gh WHERE gh.taoLuc BETWEEN :from AND :to")
    long countByTaoLucBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
