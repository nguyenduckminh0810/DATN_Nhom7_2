package com.auro.auro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.auro.auro.model.DanhGiaSanPham;

public interface DanhGiaSanPhamRepository extends JpaRepository<DanhGiaSanPham, Long> {

    Optional<DanhGiaSanPham> findByDonHangChiTiet_Id(Long donHangChiTietId);

    List<DanhGiaSanPham> findBySanPham_IdOrderByTaoLucDesc(Long sanPhamId);

    Page<DanhGiaSanPham> findBySanPham_IdOrderByTaoLucDesc(Long sanPhamId, Pageable pageable);

}

