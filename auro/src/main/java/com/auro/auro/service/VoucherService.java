package com.auro.auro.service;

import com.auro.auro.dto.request.VoucherCreateRequest;
import com.auro.auro.dto.request.VoucherUpdateRequest;
import com.auro.auro.model.Voucher;
import com.auro.auro.model.VoucherKhach;
import com.auro.auro.model.KhachHang;
import com.auro.auro.repository.VoucherRepository;
import com.auro.auro.repository.VoucherKhachRepository;
import com.auro.auro.repository.DonHangRepository;
import com.auro.auro.repository.projection.VoucherUsageProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherKhachRepository voucherKhachRepository;
    private final DonHangRepository donHangRepository;

    private static final List<String> COMPLETED_ORDER_STATUSES = List.of(
            "COMPLETED", "DELIVERED", "Hoàn tất", "Đã giao"
    );

    // GetAll voucher        
    public List<Voucher> getAllVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findAvailable(now, Pageable.unpaged()).getContent();
    }

    // check validate
    public VoucherValidationResult validateVoucher(String maVoucher, Long khachHangId, BigDecimal donHangTong){
        // Tìm voucher, thử không phân biệt hoa/thường nếu không tìm thấy
        Optional<Voucher> voucherOpt = voucherRepository.findByMa(maVoucher);
        if(voucherOpt.isEmpty()) {
            voucherOpt = voucherRepository.findByMa(maVoucher.toUpperCase());
        }
        if(voucherOpt.isEmpty()) {
            voucherOpt = voucherRepository.findByMa(maVoucher.toLowerCase());
        }
        if(voucherOpt.isEmpty()) {
            return VoucherValidationResult.invalid("Mã voucher không tồn tại");
        }

        Voucher voucher = voucherOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // Check trạng thái voucher (1 = Active, các trạng thái khác = không hoạt động)
        if (voucher.getTrangThai() == null || voucher.getTrangThai() != 1) {
            return VoucherValidationResult.invalid("Voucher đã kết thúc hoặc không hoạt động");
        }

        //check time hiệu lực
        // Chỉ check thời gian nếu voucher có thời gian hợp lệ
        if (voucher.getBatDauLuc() != null && now.isBefore(voucher.getBatDauLuc())) {
            return VoucherValidationResult.invalid("Voucher chưa có hiệu lực");
        }

        // Nếu voucher có trangThai = 1 (Active), cho phép sử dụng ngay cả khi hết hạn
        // (để admin có thể override hoặc test)
        if (voucher.getKetThucLuc() != null && now.isAfter(voucher.getKetThucLuc())) {
            // Nếu trangThai = 0 (Đã hủy), không cho phép sử dụng
            if (voucher.getTrangThai() == null || voucher.getTrangThai() == 0) {
                return VoucherValidationResult.invalid("Voucher đã hết hạn và bị hủy");
            }
        }

        // check điều kiện đơn hàng
        if(voucher.getDonToiThieu() != null && donHangTong.compareTo(voucher.getDonToiThieu()) < 0) {
            return VoucherValidationResult.invalid(String.format("Đơn hàng phải tối thiểu %s VNĐ", voucher.getDonToiThieu()));
        }

        return VoucherValidationResult.valid(voucher);
    }


    public VoucherApplicationResult applyVoucher(String maVoucher, Long khachHangId, BigDecimal donHangTong, BigDecimal phiVanChuyen) {
        // Validate voucher trước (không cần transaction)
        VoucherValidationResult validation = validateVoucher(maVoucher, khachHangId, donHangTong);
        if(!validation.isValid()) {
            return VoucherApplicationResult.failed(validation.getMessage());
        }

        Voucher voucher = validation.getVoucher();

        // Tính giảm giá
        BigDecimal giamGia;
        try {
            giamGia = tinhGiamGia(voucher, donHangTong, phiVanChuyen);
            
            // Log để debug
            log.info("Voucher apply - maVoucher: {}, loai: {}, giaTri: {}, donHangTong: {}, giamGia: {}", 
                    voucher.getMa(), voucher.getLoai(), voucher.getGiaTri(), donHangTong, giamGia);
            
            // Validate: giamGia phải > 0
            if (giamGia == null || giamGia.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Voucher apply failed - giamGia is null or <= 0: {}", giamGia);
                return VoucherApplicationResult.failed("Không thể tính giảm giá từ voucher này");
            }
        } catch (IllegalArgumentException e) {
            log.error("Voucher apply failed - IllegalArgumentException: {}", e.getMessage(), e);
            return VoucherApplicationResult.failed(e.getMessage());
        } catch (Exception e) {
            log.error("Voucher apply failed - Unexpected error: {}", e.getMessage(), e);
            return VoucherApplicationResult.failed("Lỗi khi tính giảm giá: " + e.getMessage());
        }

        // Lưu lịch sử sử dụng (chỉ khi có khachHangId) - không critical, nên bọc try-catch
        if (khachHangId != null) {
            try {
                VoucherKhach voucherKhach = new VoucherKhach();
                voucherKhach.setVoucher(voucher);

                KhachHang khachHangRef = new KhachHang();
                khachHangRef.setId(khachHangId);
                voucherKhach.setKhachHang(khachHangRef);

                voucherKhach.setTrangThai("da_dung");
                voucherKhachRepository.save(voucherKhach);
            } catch (Exception e) {
                // Không để lỗi tracking làm rollback transaction cha
                log.warn("Error saving voucher usage history (non-critical): {}", e.getMessage());
            }
        }
        
        return VoucherApplicationResult.success(voucher, giamGia);
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
            return BigDecimal.ZERO;
        }
        
        String loai = voucher.getLoai() != null ? voucher.getLoai().trim().toUpperCase() : "";
        switch (loai) {
            case "GIAM_PHAN_TRAM":
            case "PHAN_TRAM":
            case "PERCENT":
            case "PERCENTAGE": {
                BigDecimal tyLe = voucher.getGiaTri() != null ? voucher.getGiaTri() : BigDecimal.ZERO;
                BigDecimal giamGia;

                if (tyLe.compareTo(BigDecimal.ZERO) <= 0) {
                    return BigDecimal.ZERO;
                }

                // Logic tính toán giảm giá phần trăm:
                // - Nếu tyLe >= 1: giá trị là phần trăm (ví dụ: 10 = 10%, 1 = 1%), cần chia cho 100
                // - Nếu tyLe < 1: giá trị đã là decimal (ví dụ: 0.1 = 10%), nhân trực tiếp
                // 
                // Lý do: Trong database, voucher giảm % thường được lưu dưới dạng:
                // - 10, 20, 30... = 10%, 20%, 30% (phần trăm, cần chia 100)
                // - 0.1, 0.2, 0.3... = 10%, 20%, 30% (decimal, nhân trực tiếp)
                
                if (tyLe.compareTo(BigDecimal.ONE) >= 0) {
                    // tyLe >= 1: giá trị là phần trăm (ví dụ: 10 = 10%, 1 = 1%)
                    giamGia = donHangTong.multiply(tyLe)
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                } else {
                    // tyLe < 1: giá trị đã là decimal (ví dụ: 0.1 = 10%)
                    giamGia = donHangTong.multiply(tyLe);
                }
                
                // Áp dụng giảm tối đa nếu có
                if (voucher.getGiamToiDa() != null
                        && voucher.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0
                        && giamGia.compareTo(voucher.getGiamToiDa()) > 0) {
                    giamGia = voucher.getGiamToiDa();
                }

                // Đảm bảo giảm giá không vượt quá tổng tiền đơn hàng
                if (giamGia.compareTo(donHangTong) > 0) {
                    giamGia = donHangTong;
                }

                return giamGia;
            }
            case "GIAM_SO_TIEN":
            case "SO_TIEN":
            case "AMOUNT":
            case "FIXED":
            case "FIXED_AMOUNT":
            case "GIAM_TIEN": {
                // Áp dụng logic tương tự voucher giảm %: lấy giá trị từ giaTri
                BigDecimal giamGia = voucher.getGiaTri() != null ? voucher.getGiaTri() : BigDecimal.ZERO;
                
                // Validate: giá trị giảm phải > 0
                if (giamGia.compareTo(BigDecimal.ZERO) <= 0) {
                    return BigDecimal.ZERO;
                }
                
                // Check giảm tối đa (nếu có) - tương tự voucher giảm %
                if (voucher.getGiamToiDa() != null
                        && voucher.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0
                        && giamGia.compareTo(voucher.getGiamToiDa()) > 0) {
                    giamGia = voucher.getGiamToiDa();
                }
                
                // Check không vượt quá tổng tiền đơn hàng - tương tự voucher giảm %
                if (giamGia.compareTo(donHangTong) > 0) {
                    giamGia = donHangTong;
                }

                return giamGia;
            }
            case "FREESHIP": {
                if (phiVanChuyen == null) {
                    return BigDecimal.ZERO;
                }
                BigDecimal giamGia = phiVanChuyen.max(BigDecimal.ZERO);

                if (voucher.getGiamToiDa() != null
                        && voucher.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0
                        && giamGia.compareTo(voucher.getGiamToiDa()) > 0) {
                    giamGia = voucher.getGiamToiDa();
                }
                return giamGia;
            }
            default:
                log.warn("Loại voucher không hỗ trợ: {}", loai);
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
        // Normalize loại voucher thành uppercase để đảm bảo consistency
        String loai = request.getLoai() != null ? request.getLoai().trim().toUpperCase() : null;
        voucher.setLoai(loai);
        voucher.setGiaTri(request.getGiaTri());
        voucher.setGiamToiDa(request.getGiamToiDa());
        voucher.setDonToiThieu(request.getDonToiThieu());
        voucher.setBatDauLuc(request.getBatDauLuc());
        voucher.setKetThucLuc(request.getKetThucLuc());
        // Giới hạn số lượng không còn bắt buộc - giữ nguyên giá trị nếu admin cung cấp
        voucher.setGioiHanSuDung(request.getGioiHanSuDung());
        voucher.setTrangThai(1); // Mặc định Active khi tạo mới
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
        // Normalize loại voucher thành uppercase để đảm bảo consistency
        String loai = request.getLoai() != null ? request.getLoai().trim().toUpperCase() : null;
        voucher.setLoai(loai);
        voucher.setGiaTri(request.getGiaTri());
        voucher.setGiamToiDa(request.getGiamToiDa());
        voucher.setDonToiThieu(request.getDonToiThieu());
        voucher.setBatDauLuc(request.getBatDauLuc());
        voucher.setKetThucLuc(request.getKetThucLuc());
        voucher.setGioiHanSuDung(request.getGioiHanSuDung());
        // Khi admin cập nhật từ màn hình quản lý, mặc định đưa voucher về trạng thái Active
        voucher.setTrangThai(1);
        voucher.setCapNhatLuc(LocalDateTime.now());

        return voucherRepository.save(voucher);
    }

    // Xóa voucher
    @Transactional
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher " + id + " không tồn tại"));

        // Soft delete: đánh dấu voucher đã hủy, không xóa vật lý để tránh lỗi FK với đơn hàng
        voucher.setTrangThai(0); // 0 = Đã hủy
        voucher.setCapNhatLuc(LocalDateTime.now());
        voucherRepository.save(voucher);

        log.info("Soft deleted voucher id={}, ma={}", voucher.getId(), voucher.getMa());
    }

    // getAll voucher admin - lấy tất cả, không filter theo ngày
    public List<Voucher> getAllVouchersForAdmin() {
        return voucherRepository.findAllOrderByTaoLucDesc();
    }

    public Map<String, VoucherUsageStats> getVoucherUsageStatsForAdmin() {
        List<VoucherUsageProjection> rows = donHangRepository.aggregateVoucherUsage(COMPLETED_ORDER_STATUSES);
        return rows.stream().collect(Collectors.toMap(
                VoucherUsageProjection::getVoucherCode,
                row -> new VoucherUsageStats(
                        row.getUsageCount() != null ? row.getUsageCount() : 0L,
                        row.getTotalDiscount() != null ? row.getTotalDiscount() : BigDecimal.ZERO
                )));
    }

    public Voucher getVoucherById(Long id) {
        return voucherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Voucher " + id +  " không tồn tại"));
    }

    // Tái kích hoạt voucher
    @Transactional
    public Voucher reactivateVoucher(Long id, Integer soNgayGiaHan) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher " + id + " không tồn tại"));
        
        LocalDateTime now = LocalDateTime.now();
        
        // Set trạng thái = 1 (Active)
        voucher.setTrangThai(1);
        
        // Cập nhật thời gian: bắt đầu từ bây giờ
        voucher.setBatDauLuc(now);
        
        // Gia hạn: mặc định 7 ngày nếu không có input, hoặc dùng duration cũ
        if (soNgayGiaHan != null && soNgayGiaHan > 0) {
            voucher.setKetThucLuc(now.plusDays(soNgayGiaHan));
        } else {
            // Nếu không có input, tính duration cũ và gia hạn thêm 7 ngày
            if (voucher.getKetThucLuc() != null && voucher.getBatDauLuc() != null) {
                long daysBetween = java.time.Duration.between(voucher.getBatDauLuc(), voucher.getKetThucLuc()).toDays();
                voucher.setKetThucLuc(now.plusDays(Math.max(daysBetween, 7))); // Tối thiểu 7 ngày
            } else {
                voucher.setKetThucLuc(now.plusDays(7)); // Mặc định 7 ngày
            }
        }
        
        voucher.setCapNhatLuc(now);
        
        return voucherRepository.save(voucher);
    }

    @Transactional
    public Voucher deactivateVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher " + id + " không tồn tại"));

        voucher.setTrangThai(2); // 2 = Ngừng hoạt động thủ công
        voucher.setCapNhatLuc(LocalDateTime.now());

        return voucherRepository.save(voucher);
    }

    public record VoucherUsageStats(long usedCount, BigDecimal totalDiscount) { }

    // Helper method: Tính trạng thái hiển thị dựa trên status và thời gian
    public String getTrangThaiHienThi(Voucher voucher) {
        if (voucher == null) {
            return "Không xác định";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Nếu status == 0: Đã hủy
        if (voucher.getTrangThai() != null && voucher.getTrangThai() == 0) {
            return "Đã hủy";
        }
        
        // Nếu status == 1: Kiểm tra thời gian
        if (voucher.getTrangThai() != null && voucher.getTrangThai() == 1) {
            if (voucher.getKetThucLuc() != null && now.isAfter(voucher.getKetThucLuc())) {
                return "Hết hạn";
            }
            if (voucher.getKetThucLuc() != null && (now.isBefore(voucher.getKetThucLuc()) || now.isEqual(voucher.getKetThucLuc()))) {
                return "Đang diễn ra";
            }
        }
        
        return "Không xác định";
    }

}
