package com.auro.auro.repository;

import com.auro.auro.model.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
  Page<SanPham> findByTenContainingIgnoreCaseOrMoTaContainingIgnoreCase(String ten, String moTa, Pageable pageable);

  Page<SanPham> findByDanhMuc_IdIn(List<Long> danhMucIds, Pageable pageable);

  // Lấy sản phẩm bán chạy nhất dựa trên số lượng đã bán
  @Query(value = """
      WITH BestSelling AS (
          SELECT
              bt.id_san_pham,
              COALESCE(SUM(dhct.so_luong), 0) as total_sold
          FROM bien_the_san_pham bt
          LEFT JOIN don_hang_chi_tiet dhct ON dhct.id_bien_the = bt.id
          GROUP BY bt.id_san_pham
      )
      SELECT sp.*
      FROM san_pham sp
      LEFT JOIN BestSelling bs ON bs.id_san_pham = sp.id
      WHERE sp.trang_thai = 'active'
      ORDER BY COALESCE(bs.total_sold, 0) DESC
      """, countQuery = """
      SELECT COUNT(*)
      FROM san_pham sp
      WHERE sp.trang_thai = 'active'
      """, nativeQuery = true)
  Page<SanPham> findBestSellers(Pageable pageable);

  @Query(value = """
        SELECT sp
        FROM SanPham sp
        LEFT JOIN FETCH sp.danhMuc dm
        WHERE sp.slug LIKE CONCAT(:slug, '%')
      """, countQuery = """
        SELECT COUNT(sp.id)
        FROM SanPham sp
        WHERE sp.slug LIKE CONCAT(:slug, '%')
      """)
  Page<SanPham> findBySlugStartsWith(@Param("slug") String slug, Pageable pageable);

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

  // Delete product by id using JPQL to avoid loading entity
  @Modifying
  @Query("DELETE FROM SanPham sp WHERE sp.id = :id")
  void deleteProductById(@Param("id") Long id);

  // Optionally: delete products by ids - JpaRepository#deleteAllById exists, but
  // we may also declare a convenience method if needed in service/controller.
}
