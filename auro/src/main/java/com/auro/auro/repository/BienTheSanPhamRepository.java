package com.auro.auro.repository;

import com.auro.auro.model.BienTheSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Long> {
    List<BienTheSanPham> findBySanPham_Id(Long idSanPham);

    List<BienTheSanPham> findBySanPham_IdIn(Iterable<Long> sanPhamIds);

    long countBySoLuongTonLessThanEqual(Integer threshold);

    // Đếm biến thể sắp hết
    // Xử lý cả trường hợp soLuongTon có thể null
    @Query("SELECT COUNT(b) FROM BienTheSanPham b WHERE b.soLuongTon IS NOT NULL AND b.soLuongTon > 0 AND b.soLuongTon <= :threshold")
    long countLowStockVariants(@Param("threshold") Integer threshold);

    // Đếm số sản phẩm có ít nhất 1 biến thể sắp hết hoặc hết hàng
    // Sử dụng native query để đảm bảo chính xác
    @Query(value = """
            SELECT COUNT(DISTINCT id_san_pham)
            FROM bien_the_san_pham
            WHERE so_luong_ton IS NOT NULL AND so_luong_ton <= :threshold
            """, nativeQuery = true)
    long countProductsWithLowStock(@Param("threshold") Integer threshold);

    @Query(value = """
            SELECT DISTINCT id_san_pham
            FROM bien_the_san_pham
            WHERE so_luong_ton IS NOT NULL AND so_luong_ton <= :threshold
            """, nativeQuery = true)
    List<Long> findProductIdsWithLowStock(@Param("threshold") Integer threshold);

    @Query("SELECT b.id FROM BienTheSanPham b WHERE b.sanPham.id IN :sanPhamIds")
    List<Long> findIdsBySanPham_IdIn(@Param("sanPhamIds") Iterable<Long> sanPhamIds);

    @Modifying
    @Query("DELETE FROM BienTheSanPham b WHERE b.sanPham.id IN :sanPhamIds")
    void deleteBySanPham_IdIn(@Param("sanPhamIds") Iterable<Long> sanPhamIds);

    // Delete variants by product id
    @Modifying
    @Query("DELETE FROM BienTheSanPham b WHERE b.sanPham.id = :sanPhamId")
    void deleteBySanPham_Id(@Param("sanPhamId") Long idSanPham);

    boolean existsBySku(String sku);

    Optional<BienTheSanPham> findBySanPham_IdAndMauSac_IdAndKichCo_IdAndChatLieu_Id(
            Long idSanPham, Long idMauSac, Long idKichCo, Long idChatLieu);

    long countByChatLieu_Id(Long idChatLieu);

    @Query(value = """
            SELECT COALESCE(SUM(so_luong_ton), 0)
            FROM bien_the_san_pham
            """, nativeQuery = true)
    long findTotalInventory();

    @Query("SELECT b.id, b.soLuongTon FROM BienTheSanPham b ORDER BY b.soLuongTon")
    List<Object[]> findAllWithStock();
}
