package com.auro.auro.service;

import com.auro.auro.dto.request.VoucherCreateRequest;
import com.auro.auro.dto.request.VoucherUpdateRequest;
import com.auro.auro.model.Voucher;
import com.auro.auro.model.VoucherKhach;
import com.auro.auro.model.VoucherKhachId;
import com.auro.auro.model.KhachHang;
import com.auro.auro.repository.VoucherRepository;
import com.auro.auro.repository.VoucherKhachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherKhachRepository voucherKhachRepository;

    // GetAll voucher        
    public List<Voucher> getAllVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findAvailable(now, Pageable.unpaged()).getContent();
    }

    // check validate
    public VoucherValidationResult validateVoucher(String maVoucher, Long khachHangId, BigDecimal donHangTong){
        log.info("🔍 validateVoucher: maVoucher={}, khachHangId={}, donHangTong={}", maVoucher, khachHangId, donHangTong);
        // Tìm voucher, thử không phân biệt hoa/thường nếu không tìm thấy
        Optional<Voucher> voucherOpt = voucherRepository.findByMa(maVoucher);
        if(voucherOpt.isEmpty()) {
            log.info("🔍 Voucher not found with original code, trying uppercase: '{}'", maVoucher.toUpperCase());
            voucherOpt = voucherRepository.findByMa(maVoucher.toUpperCase());
        }
        if(voucherOpt.isEmpty()) {
            log.info("🔍 Voucher not found with uppercase, trying lowercase: '{}'", maVoucher.toLowerCase());
            voucherOpt = voucherRepository.findByMa(maVoucher.toLowerCase());
        }
        if(voucherOpt.isEmpty()) {
            log.warn("⚠️ Voucher not found after trying all case variations: {}", maVoucher);
            return VoucherValidationResult.invalid("Mã voucher không tồn tại");
        }

        Voucher voucher = voucherOpt.get();
        LocalDateTime now = LocalDateTime.now();
        log.info("🔍 Found voucher: id={}, ma={}, loai={}, giaTri={}, batDauLuc={}, ketThucLuc={}, gioiHanSuDung={}, donToiThieu={}", 
                voucher.getId(), voucher.getMa(), voucher.getLoai(), voucher.getGiaTri(),
                voucher.getBatDauLuc(), voucher.getKetThucLuc(), voucher.getGioiHanSuDung(), voucher.getDonToiThieu());

        //check time hiệu lực
        if (now.isBefore(voucher.getBatDauLuc())) {
            log.warn("⚠️ Voucher not yet valid: now={}, batDauLuc={}", now, voucher.getBatDauLuc());
            return VoucherValidationResult.invalid("Voucher chưa có hiệu lực");
        }

        if(now.isAfter(voucher.getKetThucLuc())) {
            log.warn("⚠️ Voucher expired: now={}, ketThucLuc={}", now, voucher.getKetThucLuc());
            return VoucherValidationResult.invalid("Voucher đã hết hạn");
        }

        // Check số lượng
        if (voucher.getGioiHanSuDung() != null) {
            Integer limit = voucher.getGioiHanSuDung();
            if (!Integer.valueOf(-1).equals(limit) && limit <= 0) {
                log.warn("⚠️ Voucher out of stock: gioiHanSuDung={}", limit);
                return VoucherValidationResult.invalid("Voucher đã hết số lượng");
            }
        }

        // check điều kiện đơn hàng
        if(voucher.getDonToiThieu() != null && donHangTong.compareTo(voucher.getDonToiThieu()) < 0) {
            log.warn("⚠️ Order total too low: donHangTong={}, donToiThieu={}", donHangTong, voucher.getDonToiThieu());
            return VoucherValidationResult.invalid(String.format("Đơn hàng phải tối thiểu %s VNĐ", voucher.getDonToiThieu()));
        }

        // check khách hàng đã dùng voucher chưa
        if(khachHangId != null) {
            VoucherKhachId id = new VoucherKhachId(voucher.getId(), khachHangId);
            Optional<VoucherKhach> voucherKhachOpt = voucherKhachRepository.findById(id);
            if(voucherKhachOpt.isPresent() && "da_dung".equals(voucherKhachOpt.get().getTrangThai())) {
                log.warn("⚠️ Customer already used voucher: khachHangId={}, voucherId={}", khachHangId, voucher.getId());
                return VoucherValidationResult.invalid("Bạn đã sử dụng voucher này rồi");
            }
        } else {
            log.info("ℹ️ Guest user (khachHangId=null) - skipping usage check");
        }

        log.info("✅ Voucher validation passed");
        return VoucherValidationResult.valid(voucher);
    }


    @Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW,
            noRollbackFor = {IllegalArgumentException.class})
    public VoucherApplicationResult applyVoucher(String maVoucher, Long khachHangId, BigDecimal donHangTong, BigDecimal phiVanChuyen) {
        log.info("🎫 applyVoucher called: maVoucher={}, khachHangId={}, donHangTong={}, phiVanChuyen={}", 
                maVoucher, khachHangId, donHangTong, phiVanChuyen);
        try {
            // validate voucher
            VoucherValidationResult validation = validateVoucher(maVoucher, khachHangId, donHangTong);
            if(!validation.isValid()) {
                log.error("❌ Voucher validation failed: {}", validation.getMessage());
                return VoucherApplicationResult.failed(validation.getMessage());
            }

            Voucher voucher = validation.getVoucher();
            log.info("✅ Voucher validated successfully: id={}, loai={}, giaTri={}", 
                    voucher.getId(), voucher.getLoai(), voucher.getGiaTri());

            // Tính giảm giá
            BigDecimal giamGia;
            try {
                giamGia = tinhGiamGia(voucher, donHangTong, phiVanChuyen);
                log.info("💰 Calculated discount: giamGia={}", giamGia);
            } catch (IllegalArgumentException e) {
                log.error("❌ Error calculating discount: {}", e.getMessage());
                return VoucherApplicationResult.failed(e.getMessage());
            }

            if(voucher.getGioiHanSuDung() != null && voucher.getGioiHanSuDung() > 0) {
                int updated = voucherRepository.decreaseLimit(voucher.getId());
                if(updated == 0) {
                    return VoucherApplicationResult.failed("Voucher đã hết số lượng");
                }
            }

            // Lưu lịch sử sử dụng (chỉ khi có khachHangId; bọc try/catch để không ảnh hưởng giao dịch đặt hàng)
            if (khachHangId != null) {
                try {
                    VoucherKhachId id = new VoucherKhachId(voucher.getId(), khachHangId);
                    Optional<VoucherKhach> existingVoucherKhach = voucherKhachRepository.findById(id);

                    if (existingVoucherKhach.isEmpty()) {
                        VoucherKhach voucherKhach = new VoucherKhach();
                        voucherKhach.setId(id);
                        voucherKhach.setVoucher(voucher);
                        KhachHang khachHangRef = new KhachHang();
                        khachHangRef.setId(khachHangId);
                        voucherKhach.setKhachHang(khachHangRef);
                        voucherKhach.setTrangThai("da_dung");
                        voucherKhachRepository.save(voucherKhach);
                    } else {
                        VoucherKhach voucherKhach = existingVoucherKhach.get();
                        voucherKhach.setVoucher(voucher);
                        if (voucherKhach.getKhachHang() == null || voucherKhach.getKhachHang().getId() == null) {
                            KhachHang khachHangRef = new KhachHang();
                            khachHangRef.setId(khachHangId);
                            voucherKhach.setKhachHang(khachHangRef);
                        }
                        voucherKhach.setTrangThai("da_dung");
                        voucherKhachRepository.save(voucherKhach);
                    }
                } catch (Exception ignore) {
                    // Không để lỗi tracking làm rollback đơn hàng
                }
            }
            return VoucherApplicationResult.success(voucher, giamGia);
        } catch (Exception e) {
            log.error("Lỗi khi áp dụng voucher {}: {}", maVoucher, e.getMessage(), e);
            return VoucherApplicationResult.failed("Lỗi khi áp dụng voucher: " + e.getMessage());
        }
    }

    @Transactional(
            propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW,
            noRollbackFor = {IllegalArgumentException.class})
    public VoucherApplicationResult applyVoucher(String maVoucher, Long khachHangId, BigDecimal donHangTong) {
        return applyVoucher(maVoucher, khachHangId, donHangTong, null);
    }

    // Tính giảm giá
    private BigDecimal tinhGiamGia(Voucher voucher, BigDecimal donHangTong, BigDecimal phiVanChuyen) {
        if (voucher == null) {
            log.warn("⚠️ tinhGiamGia: voucher is null");
            return BigDecimal.ZERO;
        }
        
        String loai = voucher.getLoai() != null ? voucher.getLoai().trim().toUpperCase() : "";
        log.info("💰 tinhGiamGia: loai={}, giaTri={}, donHangTong={}, phiVanChuyen={}",
                loai, voucher.getGiaTri(), donHangTong, phiVanChuyen);
        switch (loai) {
            case "GIAM_PHAN_TRAM":
            case "PHAN_TRAM":
            case "PERCENT":
            case "PERCENTAGE": {
                BigDecimal tyLe = voucher.getGiaTri() != null ? voucher.getGiaTri() : BigDecimal.ZERO;
                BigDecimal giamGia;

                if (tyLe.compareTo(BigDecimal.ZERO) <= 0) {
                    log.warn("⚠️ tinhGiamGia: tyLe <= 0, returning ZERO");
                    return BigDecimal.ZERO;
                }

                if (tyLe.compareTo(BigDecimal.ONE) <= 0 && tyLe.compareTo(BigDecimal.ZERO) > 0) {
                    if (tyLe.compareTo(BigDecimal.ONE) == 0) {
                        giamGia = donHangTong.multiply(tyLe)
                                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                        log.info("💰 tinhGiamGia: tyLe = 1, treating as 1%, giamGia = donHangTong * 1 / 100 = {} * 1 / 100 = {}", donHangTong, giamGia);
                    } else {
                        giamGia = donHangTong.multiply(tyLe);
                        log.info("💰 tinhGiamGia: tyLe < 1 ({}), treating as direct ratio, giamGia = donHangTong * tyLe = {} * {} = {}", tyLe, donHangTong, tyLe, giamGia);
                    }
                } else {
                    giamGia = donHangTong.multiply(tyLe)
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    log.info("💰 tinhGiamGia: tyLe > 1 ({}), treating as percentage, giamGia = donHangTong * tyLe / 100 = {} * {} / 100 = {}", tyLe, donHangTong, tyLe, giamGia);
                }
                if (voucher.getGiamToiDa() != null
                        && voucher.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0
                        && giamGia.compareTo(voucher.getGiamToiDa()) > 0) {
                    log.info("💰 tinhGiamGia: capping giamGia from {} to giamToiDa={}", giamGia, voucher.getGiamToiDa());
                    giamGia = voucher.getGiamToiDa();
                }

                if (giamGia.compareTo(donHangTong) > 0) {
                    log.info("💰 tinhGiamGia: capping giamGia from {} to donHangTong={}", giamGia, donHangTong);
                    giamGia = donHangTong;
                }

                log.info("✅ tinhGiamGia: final giamGia={}", giamGia);
                return giamGia;
            }
            case "GIAM_SO_TIEN":
            case "SO_TIEN":
            case "AMOUNT": {
                BigDecimal giamGia = voucher.getGiaTri() != null ? voucher.getGiaTri() : BigDecimal.ZERO;
                // check không vượt quá tổng tiền
                if (giamGia.compareTo(donHangTong) > 0) {
                    log.info("💰 tinhGiamGia: capping giamGia from {} to donHangTong={}", giamGia, donHangTong);
                    giamGia = donHangTong;
                }

                log.info("✅ tinhGiamGia: final giamGia={}", giamGia);
                return giamGia;
            }
            case "FREESHIP": {
                if (phiVanChuyen == null) {
                    log.warn("⚠️ tinhGiamGia: FREESHIP but phiVanChuyen null, returning ZERO");
                    return BigDecimal.ZERO;
                }
                BigDecimal giamGia = phiVanChuyen.max(BigDecimal.ZERO);

                if (voucher.getGiamToiDa() != null
                        && voucher.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0
                        && giamGia.compareTo(voucher.getGiamToiDa()) > 0) {
                    log.info("💰 tinhGiamGia: capping freeship giamGia from {} to giamToiDa={}", giamGia, voucher.getGiamToiDa());
                    giamGia = voucher.getGiamToiDa();
                }
                log.info("✅ tinhGiamGia FREESHIP: final giamGia={}", giamGia);
                return giamGia;
            }
            default:
                log.warn("⚠️ tinhGiamGia: Loại voucher không hỗ trợ: {}", loai);
                return BigDecimal.ZERO;
        }
    }

    // check và tạo mới
    @Transactional
    public Voucher createVoucher(VoucherCreateRequest request) {
        if(voucherRepository.existsByMa(request.getMa())) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại: " + request.getMa());
        }

        Voucher voucher = new Voucher();
        voucher.setMa(request.getMa());
        voucher.setLoai(request.getLoai());
        voucher.setGiaTri(request.getGiaTri());
        voucher.setGiamToiDa(request.getGiamToiDa());
        voucher.setDonToiThieu(request.getDonToiThieu());
        voucher.setBatDauLuc(request.getBatDauLuc());
        voucher.setKetThucLuc(request.getKetThucLuc());
        // Handle NULL constraint: if null, use -1 to represent unlimited
        voucher.setGioiHanSuDung(request.getGioiHanSuDung() != null ? request.getGioiHanSuDung() : -1);
        voucher.setTaoLuc(LocalDateTime.now());
        voucher.setCapNhatLuc(LocalDateTime.now());

        return voucherRepository.save(voucher);
    }

    // check trùng và update
    @Transactional
    public Voucher updateVoucher(Long id, VoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Voucher " + id +  " không tồn tại"));

        if(!voucher.getMa().equals(request.getMa()) && voucherRepository.existsByMa(request.getMa())) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại: " + request.getMa());
        }

        voucher.setMa(request.getMa());
        voucher.setLoai(request.getLoai());
        voucher.setGiaTri(request.getGiaTri());
        voucher.setGiamToiDa(request.getGiamToiDa());
        voucher.setDonToiThieu(request.getDonToiThieu());
        voucher.setBatDauLuc(request.getBatDauLuc());
        voucher.setKetThucLuc(request.getKetThucLuc());
        // Handle NULL constraint: if null, use -1 to represent unlimited
        voucher.setGioiHanSuDung(request.getGioiHanSuDung() != null ? request.getGioiHanSuDung() : -1);
        voucher.setCapNhatLuc(LocalDateTime.now());

        return voucherRepository.save(voucher);
    }

    // Xóa voucher
    @Transactional
    public void deleteVoucher(Long id) {
        if(!voucherRepository.existsById(id)) {
            throw new IllegalArgumentException("Voucher " + id +  " không tồn tại");
        }
        voucherRepository.deleteById(id);
    }

    // getAll voucher admin
    public List<Voucher> getAllVouchersForAdmin() {
        return voucherRepository.findAll();
    }

    public Voucher getVoucherById(Long id) {
        return voucherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Voucher " + id +  " không tồn tại"));
    }

}
