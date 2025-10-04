package com.auro.auro.repository;

import com.auro.auro.model.TonKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TonKhoRepository extends JpaRepository<TonKho, Long> {

    // Tìm tồn kho theo biến thể
    Optional<TonKho> findByBienThe_Id(Long idBienThe);

    // Kiểm tra tồn kho
    @Query("SELECT tk FROM TonKho tk where tk.bienThe.id = :idBienThe and (tk.soLuongTon - tk.soLuongGiu) >= :soLuongCanXem")
    Optional<TonKho> checkAvailableStock(@Param("idBienThe") Long idBienThe, @Param("soLuongCan") Integer soLuongCan);

    // Tìm SP sắp hết hàng
    @Query("select tk from TonKho tk where tk.soLuongTon <= tk.tonAnToan")
    List<TonKho> findLowStockItems();

    // Cập nhật SL tồn
    @Modifying
    @Query("update TonKho tk set tk.soLuongTon = tk.soLuongTon - :soLuong where tk.bienThe.id = :idBienThe")
    int decreaseStock(@Param("idBienThe") long idBienThe, @Param("soLuong") Integer soLuong);

    // Cập nhật SL giữ
    @Modifying
    @Query("update TonKho tk set tk.soLuongGiu = tk.soLuongGiu + :soLuong where tk.bienThe.id = :idBienThe")
    int increaseReservedStock(@Param("idBienThe") long idBienThe, @Param("soLuong") Integer soLuong);

    // Giải phóng số lượng giữ
    @Modifying
    @Query("update TonKho tk set tk.soLuongGiu = tk.soLuongGiu - :soLuong where tk.bienThe.id = :idBienThe")
    int releaseReservedStock(@Param("idBienThe") Long idBienThe, @Param("soLuong") Integer soLuong);

}
