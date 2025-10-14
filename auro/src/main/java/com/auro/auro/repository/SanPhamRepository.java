package com.auro.auro.repository;

import com.auro.auro.dto.response.SanPhamResponse;
import com.auro.auro.model.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
    Page<SanPham> findByTenContainingIgnoreCaseOrMoTaContainingIgnoreCase(String ten, String moTa, Pageable pageable);

    @Query(value = """
              SELECT new com.auro.auro.dto.response.SanPhamResponse(
                  sp.id, sp.ten, sp.slug, sp.moTa,
                  dm.id, dm.ten, sp.gia, sp.trangThai, sp.taoLuc, sp.capNhatLuc,
                  (SELECT ha.url FROM HinhAnh ha
                   WHERE ha.sanPham.id = sp.id AND ha.laDaiDien = true)
              )
              FROM SanPham sp
              LEFT JOIN sp.danhMuc dm
              WHERE sp.slug LIKE CONCAT(:slug, '%')
            """, countQuery = """
              SELECT COUNT(sp.id)
              FROM SanPham sp
              WHERE sp.slug LIKE CONCAT(:slug, '%')
            """)
    Page<SanPhamResponse> findBySlugStartsWith(@Param("slug") String slug, Pageable pageable);

    Page<SanPham> findByDanhMuc_Slug(String slug, Pageable pageable);

    Page<SanPham> findByDanhMuc_Id(Long danhMucId, Pageable pageable);

    java.util.Optional<SanPham> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<SanPham> findByDanhMuc_IdAndTenContainingIgnoreCaseOrDanhMuc_IdAndMoTaContainingIgnoreCase(
            Long danhMucId1, String ten, Long danhMucId2, String moTa, Pageable pageable);

    // Check existence of products for a category or list of categories
    boolean existsByDanhMuc_Id(Long danhMucId);

    boolean existsByDanhMuc_IdIn(Iterable<Long> danhMucIds);

    // Count and find products by category ids
    long countByDanhMuc_IdIn(Iterable<Long> danhMucIds);

    java.util.List<com.auro.auro.model.SanPham> findByDanhMuc_IdIn(Iterable<Long> danhMucIds);

    // Return only product ids for given category ids to avoid selecting all columns
    @Query("select p.id from SanPham p where p.danhMuc.id in :ids")
    List<Long> findIdsByDanhMucIdIn(@Param("ids") Iterable<Long> ids);

    // Delete products by category ids
    void deleteByDanhMuc_Id(Long danhMucId);

    void deleteByDanhMuc_IdIn(Iterable<Long> danhMucIds);

    // Optionally: delete products by ids - JpaRepository#deleteAllById exists, but
    // we may also declare a convenience method if needed in service/controller.
}
