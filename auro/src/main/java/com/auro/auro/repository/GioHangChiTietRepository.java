package com.auro.auro.repository;

import com.auro.auro.model.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Long> {
    List<GioHangChiTiet> findByGioHang_Id(Long idGioHang);

    Optional<GioHangChiTiet> findFirstByGioHang_IdAndBienThe_Id(Long idGioHang, Long idBienThe);

    void deleteByGioHang_Id(Long idGioHang);
}
