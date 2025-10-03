package com.auro.auro.repository;

import com.auro.auro.model.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
    Page<SanPham> findByDanhMuc_Id(Long idDanhMuc, Pageable pageable);

    Page<SanPham> findByTenContainingIgnoreCase(String keyword, Pageable pageable);

    Page<SanPham> findByTrangThaiTrue(Pageable pageable);

    long countByDanhMuc_Id(Long idDanhMuc);
    
    // Tìm theo slug
    Optional<SanPham> findBySlug(String slug);
    
    // Tìm theo thương hiệu
    Page<SanPham> findByThuongHieuContainingIgnoreCase(String thuongHieu, Pageable pageable);
    
    // Tìm kiếm nâng cao
    @Query("""
            SELECT s FROM SanPham s 
            WHERE s.trangThai = true 
            AND (:keyword IS NULL OR LOWER(s.ten) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:thuongHieu IS NULL OR LOWER(s.thuongHieu) LIKE LOWER(CONCAT('%', :thuongHieu, '%')))
            AND (:idDanhMuc IS NULL OR s.danhMuc.id = :idDanhMuc)
            """)
    Page<SanPham> findAdvanced(@Param("keyword") String keyword, 
                               @Param("thuongHieu") String thuongHieu, 
                               @Param("idDanhMuc") Long idDanhMuc, 
                               Pageable pageable);
}