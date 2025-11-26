package com.auro.auro.repository;

import com.auro.auro.model.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Long> {
    List<GioHangChiTiet> findByGioHang_Id(Long idGioHang);

    Optional<GioHangChiTiet> findFirstByGioHang_IdAndBienThe_Id(Long idGioHang, Long idBienThe);

    void deleteByGioHang_Id(Long idGioHang);

    // Find cart items by variant id
    java.util.List<GioHangChiTiet> findByBienThe_Id(Long idBienThe);

    // Count cart items by variant id
    long countByBienThe_Id(Long idBienThe);

    // Delete cart items by variant id
    void deleteByBienThe_Id(Long idBienThe);

    @Query("""
        SELECT gct FROM GioHangChiTiet gct
        LEFT JOIN FETCH gct.bienThe bt
        LEFT JOIN FETCH bt.sanPham sp
        LEFT JOIN FETCH bt.mauSac ms
        LEFT JOIN FETCH bt.kichCo kc
        LEFT JOIN FETCH bt.chatLieu cl
        WHERE gct.gioHang.id = :gioHangId
        """)
    List<GioHangChiTiet> findByGioHang_IdWithDetails(@Param("gioHangId") Long gioHangId);
}
