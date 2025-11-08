package com.auro.auro.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auro.auro.model.DanhGiaSanPham;

public interface DanhGiaSanPhamRepository extends JpaRepository<DanhGiaSanPham, Long> {

    Optional<DanhGiaSanPham> findByDonHangChiTiet_Id(Long donHangChiTietId);

}

