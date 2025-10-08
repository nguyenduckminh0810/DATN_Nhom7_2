package com.auro.auro.repository;

import com.auro.auro.model.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VaiTroRepository extends JpaRepository<VaiTro, Long> {
    
    Optional<VaiTro> findByMa(String ma);

    boolean existsByMa(String ma);

    Optional<VaiTro> findByTen(String ten);

    boolean existsByTen(String ten);
}