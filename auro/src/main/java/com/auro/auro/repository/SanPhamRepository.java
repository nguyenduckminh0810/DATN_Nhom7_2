package com.auro.auro.repository;

import com.auro.auro.model.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
    Page<SanPham> findByTenContainingIgnoreCaseOrMoTaContainingIgnoreCase(String ten, String moTa, Pageable pageable);

    Page<SanPham> findByDanhMuc_Id(Long danhMucId, Pageable pageable);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<SanPham> findByDanhMuc_IdAndTenContainingIgnoreCaseOrDanhMuc_IdAndMoTaContainingIgnoreCase(
            Long danhMucId1, String ten, Long danhMucId2, String moTa, Pageable pageable);
}
