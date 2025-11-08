package com.auro.auro.service;

import com.auro.auro.dto.request.VoucherCreateRequest;
import com.auro.auro.dto.request.VoucherUpdateRequest;
import com.auro.auro.model.Voucher;
import com.auro.auro.model.VoucherKhach;
import com.auro.auro.model.VoucherKhachId;
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
        // Tìm voucher
        Optional<Voucher> voucherOpt = voucherRepository.findByMa(maVoucher);
        if(voucherOpt.isEmpty()) {
            return VoucherValidationResult.invalid("Mã voucher không tồn tại");
        }

        Voucher voucher = voucherOpt.get();
        LocalDateTime now = LocalDateTime.now();

        //check time hiệu lực
        if (now.isBefore(voucher.getBatDauLuc())) {
            return VoucherValidationResult.invalid("Voucher chưa có hiệu lực");
        }

        if(now.isAfter(voucher.getKetThucLuc())) {
            return VoucherValidationResult.invalid("Voucher đã hết hạn");
        }

        //check số lượng
        if (voucher.getGioiHanSuDung() != null && voucher.getGioiHanSuDung() <= 0) {
            return VoucherValidationResult.invalid("Voucher đã hết số lượng");
        }

        // check điều kiện đơn hàng
        if(voucher.getDonToiThieu() != null && donHangTong.compareTo(voucher.getDonToiThieu()) < 0) {
            return VoucherValidationResult.invalid(String.format("Đơn hàng phải tối thiểu %s VNĐ", voucher.getDonToiThieu()));
        }

        // check khách hàng đã dùng voucher chưa
        if(khachHangId != null) {
            VoucherKhachId id = new VoucherKhachId(voucher.getId(), khachHangId);
            Optional<VoucherKhach> voucherKhachOpt = voucherKhachRepository.findById(id);
            if(voucherKhachOpt.isPresent() && "da_dung".equals(voucherKhachOpt.get().getTrangThai())) {
                return VoucherValidationResult.invalid("Bạn đã sử dụng voucher này rồi");
            }
        }

        return VoucherValidationResult.valid(voucher);
    }


    @Transactional(noRollbackFor = {IllegalArgumentException.class})
    public VoucherApplicationResult applyVoucher(String maVoucher, Long khachHangId, BigDecimal donHangTong) {
        try {
            // validate voucher
            VoucherValidationResult validation = validateVoucher(maVoucher, khachHangId, donHangTong);
            if(!validation.isValid()) {
                return VoucherApplicationResult.failed(validation.getMessage());
            }

            Voucher voucher = validation.getVoucher();

            // Tính giảm giá
            BigDecimal giamGia;
            try {
                giamGia = tinhGiamGia(voucher, donHangTong);
            } catch (IllegalArgumentException e) {
                return VoucherApplicationResult.failed(e.getMessage());
            }

            if(voucher.getGioiHanSuDung() != null && voucher.getGioiHanSuDung() > 0) {
                int updated = voucherRepository.decreaseLimit(voucher.getId());
                if(updated == 0) {
                    return VoucherApplicationResult.failed("Voucher đã hết số lượng");
                }
            }

            // Lưu lịch sử sử dụng
            if(khachHangId != null) {
                VoucherKhachId id = new VoucherKhachId(voucher.getId(), khachHangId);
                Optional<VoucherKhach> existingVoucherKhach = voucherKhachRepository.findById(id);
                
                if (existingVoucherKhach.isEmpty()) {
                    // Chưa tồn tại, tạo mới
                    VoucherKhach voucherKhach = new VoucherKhach();
                    voucherKhach.setId(id);
                    voucherKhach.setTrangThai("da_dung");
                    voucherKhachRepository.save(voucherKhach);
                } else {
                    // Đã tồn tại, cập nhật trạng thái
                    VoucherKhach voucherKhach = existingVoucherKhach.get();
                    voucherKhach.setTrangThai("da_dung");
                    voucherKhachRepository.save(voucherKhach);
                }
            }
            return VoucherApplicationResult.success(voucher, giamGia);
        } catch (Exception e) {
            log.error("Lỗi khi áp dụng voucher {}: {}", maVoucher, e.getMessage(), e);
            return VoucherApplicationResult.failed("Lỗi khi áp dụng voucher: " + e.getMessage());
        }
    }

    // Tính giảm giá
    private BigDecimal tinhGiamGia(Voucher voucher, BigDecimal donHangTong) {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }
        
        String loai = voucher.getLoai();
        
        // Giảm theo phần trăm
        if ("GIAM_PHAN_TRAM".equals(loai) || "PHAN_TRAM".equals(loai) || "percent".equals(loai)) {
            BigDecimal giamGia = donHangTong.multiply(voucher.getGiaTri())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            
            // check giảm tối đa nếu có
            if (voucher.getGiamToiDa() != null && giamGia.compareTo(voucher.getGiamToiDa()) > 0) {
                giamGia = voucher.getGiamToiDa();
            }
            
            // check không vượt quá tổng tiền
            if (giamGia.compareTo(donHangTong) > 0) {
                giamGia = donHangTong;
            }
            
            return giamGia;
        }
        
        // Giảm số tiền cố định
        if ("GIAM_SO_TIEN".equals(loai) || "SO_TIEN".equals(loai) || "so_tien".equals(loai)) {
            BigDecimal giamGia = voucher.getGiaTri();
            
            // check không vượt quá tổng tiền
            if (giamGia.compareTo(donHangTong) > 0) {
                giamGia = donHangTong;
            }
            
            return giamGia;
        }
        
        // Freeship
        if ("FREESHIP".equals(loai)) {
            return BigDecimal.ZERO;
        }
        
        throw new IllegalArgumentException("Loại voucher không hỗ trợ: " + loai);
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
