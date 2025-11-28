package com.auro.auro.repository;

import com.auro.auro.model.ResetToken;
import com.auro.auro.model.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {
    Optional<ResetToken> findByToken(String token);

    Optional<ResetToken> findByTokenAndDaSuDungFalse(String token);

    @Query("SELECT rt FROM ResetToken rt WHERE rt.token = :token AND rt.daSuDung = false AND rt.hetHanLuc > :now")
    Optional<ResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM ResetToken rt WHERE rt.hetHanLuc < :now OR rt.daSuDung = true")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE ResetToken rt SET rt.daSuDung = true WHERE rt.taiKhoan = :taiKhoan AND rt.daSuDung = false")
    void invalidateAllTokensForUser(@Param("taiKhoan") TaiKhoan taiKhoan);
}

