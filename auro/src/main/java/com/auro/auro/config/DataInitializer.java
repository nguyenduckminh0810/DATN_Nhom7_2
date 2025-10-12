package com.auro.auro.config;

import com.auro.auro.model.VaiTro;
import com.auro.auro.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

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
        // Only seed when table is empty to avoid duplicates
        if (vaiTroRepository.count() == 0) {
            log.info("=== Initializing default roles (via JPA) ===");

            VaiTro customer = new VaiTro(null, "CUSTOMER", "Khách hàng");
            VaiTro guest = new VaiTro(null, "GUEST", "Khách vãng lai");
            VaiTro staff = new VaiTro(null, "STAFF", "Nhân viên");
            VaiTro admin = new VaiTro(null, "ADMIN", "Quản trị viên");

            vaiTroRepository.saveAll(Arrays.asList(customer, guest, staff, admin));

            log.info("=== ✅ 4 roles created successfully ===");
        } else {
            log.info("=== Roles already exist, skipping initialization ===");
        }
    }
}