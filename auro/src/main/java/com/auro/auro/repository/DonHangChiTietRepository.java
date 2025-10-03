package com.auro.auro.repository;

import com.auro.auro.model.DonHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonHangChiTietRepository extends JpaRepository<DonHangChiTiet, Long> {
    List<DonHangChiTiet> findByDonHang_Id(Long idDonHang);
}
