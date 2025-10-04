package com.auro.auro.repository;

import com.auro.auro.model.Gia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GiaRepository extends JpaRepository<Gia, Long> {
    
    // Tìm giá hiện tại của biến thể sản phẩm
    @Query("""
            SELECT g FROM Gia g 
            WHERE g.bienThe.id = :idBienThe 
            AND (:now BETWEEN g.batDauLuc AND g.ketThucLuc OR g.batDauLuc IS NULL)
            ORDER BY g.batDauLuc DESC
            """)
    Optional<Gia> findCurrentPrice(@Param("idBienThe") Long idBienThe, 
                                   @Param("now") LocalDateTime now);
    
    // Tìm tất cả giá của một biến thể
    List<Gia> findByBienThe_IdOrderByBatDauLucDesc(Long idBienThe);
    
    // Tìm giá khuyến mãi đang hoạt động
    @Query("""
            SELECT g FROM Gia g 
            WHERE g.bienThe.id = :idBienThe 
            AND g.giaKhuyenMai IS NOT NULL
            AND (:now BETWEEN g.batDauLuc AND g.ketThucLuc)
            """)
    List<Gia> findActivePromotionPrices(@Param("idBienThe") Long idBienThe, 
                                        @Param("now") LocalDateTime now);
    
    // Tìm giá thấp nhất hiện tại
    @Query("""
            SELECT g FROM Gia g 
            WHERE g.bienThe.id = :idBienThe 
            AND (:now BETWEEN g.batDauLuc AND g.ketThucLuc OR g.batDauLuc IS NULL)
            ORDER BY COALESCE(g.giaKhuyenMai, g.giaNiemYet) ASC
            """)
    Optional<Gia> findLowestCurrentPrice(@Param("idBienThe") Long idBienThe, 
                                         @Param("now") LocalDateTime now);
}