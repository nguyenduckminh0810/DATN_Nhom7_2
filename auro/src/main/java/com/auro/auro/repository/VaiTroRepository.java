package com.auro.auro.repository;

import com.auro.auro.model.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VaiTroRepository extends JpaRepository<VaiTro, Long> {
    Optional<VaiTro> findByMaVaiTro(String maVaiTro); // GUEST, CUSTOMER, STAFF, ADMIN

    boolean existsByMaVaiTro(String maVaiTro);

    Optional<VaiTro> findByTenVaiTro(String tenVaiTro);

    boolean existsByTenVaiTro(String tenVaiTro);
}