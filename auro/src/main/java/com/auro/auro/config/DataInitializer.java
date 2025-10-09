package com.auro.auro.config;

import com.auro.auro.model.VaiTro;
import com.auro.auro.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final VaiTroRepository vaiTroRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        // Chỉ init nếu table vai_tro đang rỗng
        if (vaiTroRepository.count() == 0) {
            log.info("=== Initializing default roles ===");
            
            vaiTroRepository.save(new VaiTro(null, "CUSTOMER", "Khách hàng"));
            vaiTroRepository.save(new VaiTro(null, "GUEST", "Khách vãng lai"));
            vaiTroRepository.save(new VaiTro(null, "STAFF", "Nhân viên"));
            vaiTroRepository.save(new VaiTro(null, "ADMIN", "Quản trị viên"));
            
            log.info("=== ✅ 4 roles created successfully! ===");
        } else {
            log.info("=== Roles already exist, skipping initialization ===");
        }
    }
}