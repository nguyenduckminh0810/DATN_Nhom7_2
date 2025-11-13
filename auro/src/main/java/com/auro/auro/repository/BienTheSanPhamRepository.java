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

    // Find variants for multiple products
    List<BienTheSanPham> findBySanPham_IdIn(Iterable<Long> sanPhamIds);

    long countBySoLuongTonLessThanEqual(Integer threshold);

    // Get variant IDs for multiple products (only IDs, no entity loading)
    @Query("SELECT b.id FROM BienTheSanPham b WHERE b.sanPham.id IN :sanPhamIds")
    List<Long> findIdsBySanPham_IdIn(@Param("sanPhamIds") Iterable<Long> sanPhamIds);

    // Delete variants for a list of products
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
}
