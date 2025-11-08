package com.auro.auro.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.auro.auro.dto.request.GuestCheckoutRequest;
import com.auro.auro.dto.request.TaoDonTuGioHangRequest;
import com.auro.auro.dto.request.GHNShippingFeeRequest;
import com.auro.auro.dto.response.GHNShippingFeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.auro.auro.dto.response.DonHangChiTietResponse;
import com.auro.auro.dto.response.DonHangResponse;
import com.auro.auro.model.DonHang;
import com.auro.auro.model.DonHangChiTiet;
import com.auro.auro.repository.DonHangChiTietRepository;
import com.auro.auro.repository.DonHangRepository;
import com.auro.auro.model.*;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.repository.DiaChiRepository;
import com.auro.auro.repository.VoucherRepository;
import com.auro.auro.repository.BienTheSanPhamRepository;
import org.springframework.data.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.auro.auro.service.VoucherService;
import com.auro.auro.service.VoucherValidationResult;
import com.auro.auro.service.VoucherApplicationResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonHangService {

    private final DonHangRepository donHangRepository;
    private final DonHangChiTietRepository donHangChiTietRepository;
    private final GioHangService gioHangService;
    private final KhachHangRepository khachHangRepository;
    private final DiaChiRepository diaChiRepository;
    private final VoucherRepository voucherRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final EmailService emailService;
    private final com.auro.auro.repository.HinhAnhRepository hinhAnhRepository;
    private final GHNShippingService ghnShippingService;
    private final VoucherService voucherService;

    // Tạo mới đơn hàng
    @Transactional
    public DonHang createDonHang(DonHang donHang, List<DonHangChiTiet> chiTietList) {
        donHang.setSoDonHang("DH-" + UUID.randomUUID().toString());
        donHang.setTaoLuc(LocalDateTime.now());
        donHang.setCapNhatLuc(LocalDateTime.now());

        if (donHang.getTrangThai() == null || donHang.getTrangThai().trim().isEmpty()) {
            donHang.setTrangThai("Chờ xác nhận");
        }

        // Tính toán tổng tiền
        BigDecimal tamTinh = chiTietList.stream()
                .map(ct -> ct.getDonGia().multiply(BigDecimal.valueOf(ct.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        donHang.setTamTinh(tamTinh);

        DonHang savedDonHang = donHangRepository.save(donHang);

        // Lưu chi tiết đơn hàng
        chiTietList.forEach(ct -> {
            ct.setDonHang(savedDonHang);
            ct.setThanhTien(ct.getDonGia().multiply(BigDecimal.valueOf(ct.getSoLuong())));
        });
        donHangChiTietRepository.saveAll(chiTietList);

        return savedDonHang;
    }

    // Lấy toàn bộ đơn hàng
    public List<DonHang> getAllDonHang() {
        return donHangRepository.findAll();
    }

    // Lấy đơn hàng theo ID
    public DonHang getDonHangById(Long id) {
        return donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
    }

    // Lấy chi tiết đơn hàng theo ID đơn hàng
    public List<DonHangChiTiet> getChiTietByDonHangId(Long donHangId) {
        return donHangChiTietRepository.findByDonHang_Id(donHangId);
    }

    // Cập nhật đơn hàng
    @Transactional
    public DonHangResponse updateDonHang(Long id, Map<String, Object> updates) {
        DonHang donHang = getDonHangById(id);

        // Lưu trạng thái cũ để so sánh
        String trangThaiCu = donHang.getTrangThai();

        // Cập nhật các field
        if (updates.containsKey("diaChiGiao")) {
            donHang.setDiaChiGiao((String) updates.get("diaChiGiao"));
        }
        if (updates.containsKey("ghiChu")) {
            donHang.setGhiChu((String) updates.get("ghiChu"));
        }
        if (updates.containsKey("trangThai")) {
            donHang.setTrangThai((String) updates.get("trangThai"));
        }

        if (updates.containsKey("paymentStatus")) {
            donHang.setPaymentStatus((String) updates.get("paymentStatus"));
        }
        if (updates.containsKey("paymentMethod")) {
            donHang.setPaymentMethod((String) updates.get("paymentMethod"));
        }

        //TRỪ TỒN KHO KHI CHUYỂN SANG TRẠNG THÁI "Đang giao"
        String trangThaiMoi = donHang.getTrangThai();
        if (!"Đang giao".equals(trangThaiCu) && "Đang giao".equals(trangThaiMoi)) {
            List<DonHangChiTiet> chiTietList = donHangChiTietRepository.findByDonHang_Id(id);

            for (DonHangChiTiet chiTiet : chiTietList) {
                BienTheSanPham bienThe = chiTiet.getBienThe();
                int soLuongDat = chiTiet.getSoLuong();
                int tonHienTai = bienThe.getSoLuongTon();

                // Kiểm tra tồn kho trước khi trừ
                if (tonHienTai < soLuongDat) {
                    throw new RuntimeException(
                            String.format("Không đủ hàng trong kho! Sản phẩm: %s, Màu: %s, Size: %s. " +
                                    "Tồn kho: %d, Yêu cầu: %d",
                                    bienThe.getSanPham().getTen(),
                                    bienThe.getMauSac() != null ? bienThe.getMauSac().getTen() : "N/A",
                                    bienThe.getKichCo() != null ? bienThe.getKichCo().getTen() : "N/A",
                                    tonHienTai,
                                    soLuongDat));
                }

                // Trừ tồn kho
                bienThe.setSoLuongTon(tonHienTai - soLuongDat);
                bienTheSanPhamRepository.save(bienThe);
            }
        }

        donHang.setCapNhatLuc(LocalDateTime.now());
        DonHang updated = donHangRepository.save(donHang);

        // Convert sang DTO trước khi return
        return convertToDTO(updated);
    }

    // Xóa đơn hàng
    @Transactional
    public void deleteDonHang(Long id) {
        // Kiểm tra đơn hàng tồn tại
        if (!donHangRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy đơn hàng");
        }

        // Lấy danh sách chi tiết theo ID (không qua lazy loading)
        List<DonHangChiTiet> chiTietList = donHangChiTietRepository.findByDonHang_Id(id);

        // Xóa chi tiết trước
        if (!chiTietList.isEmpty()) {
            donHangChiTietRepository.deleteAll(chiTietList);
        }

        // Sau đó xóa đơn hàng
        donHangRepository.deleteById(id);
    }

    // Xóa mềm đơn hàng (chuyển trạng thái sang Đã hủy)
    @Transactional
    public void softDeleteDonHang(Long id) {
        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));

        // Kiểm tra trạng thái có được phép hủy không
        if ("Hoàn tất".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Không thể hủy đơn hàng đã hoàn thành");
        }

        if ("Đã hủy".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Đơn hàng đã bị hủy từ trước");
        }

        // Đổi trạng thái sang Đã hủy
        donHang.setTrangThai("Đã hủy");
        donHang.setCapNhatLuc(LocalDateTime.now());
        donHangRepository.save(donHang);
    }

    // Lấy toàn bộ đơn hàng DTO
    @Transactional
    public List<DonHangResponse> getAllDonHangDTO() {
        List<DonHang> donHangList = donHangRepository.findAllWithChiTiet();
        return donHangList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy toàn bộ đơn hàng có phân trang
    public Page<DonHangResponse> getDonHangPhanTrang(Pageable pageable) {
        Page<DonHang> donHangPage = donHangRepository.findAll(pageable);
        return donHangPage.map(this::convertToDTO);
    }

    // convert từ entity sang dto
    private DonHangResponse convertToDTO(DonHang dh) {
        DonHangResponse dto = new DonHangResponse();
        dto.setId(dh.getId());
        dto.setSoDonHang(dh.getSoDonHang());
        dto.setTamTinh(dh.getTamTinh());
        dto.setPhiVanChuyen(dh.getPhiVanChuyen());
        dto.setGiamGiaTong(dh.getGiamGiaTong());

        // Fix: Nếu tongThanhToan null, tính lại từ tamTinh + phiVanChuyen - giamGiaTong
        BigDecimal tongThanhToan = dh.getTongThanhToan();
        if (tongThanhToan == null) {
            BigDecimal tamTinh = dh.getTamTinh() != null ? dh.getTamTinh() : BigDecimal.ZERO;
            BigDecimal phiVanChuyen = dh.getPhiVanChuyen() != null ? dh.getPhiVanChuyen() : BigDecimal.ZERO;
            BigDecimal giamGiaTong = dh.getGiamGiaTong() != null ? dh.getGiamGiaTong() : BigDecimal.ZERO;
            tongThanhToan = tamTinh.add(phiVanChuyen).subtract(giamGiaTong);
        }
        dto.setTongThanhToan(tongThanhToan);

        dto.setTrangThai(dh.getTrangThai());
        dto.setDiaChiGiaoSnapshot(dh.getDiaChiGiao());
        dto.setGhiChu(dh.getGhiChu());
        dto.setTaoLuc(dh.getTaoLuc());
        dto.setCapNhatLuc(dh.getCapNhatLuc());

        dto.setPaymentStatus(dh.getPaymentStatus() != null ? dh.getPaymentStatus() : "pending");
        dto.setPaymentMethod(dh.getPaymentMethod() != null ? dh.getPaymentMethod() : "COD");

        // Convert chi tiết list (nếu có)
        if (dh.getChiTietList() != null) {
            List<DonHangChiTietResponse> chiTietDTOs = dh.getChiTietList().stream().map(ct -> {
                DonHangChiTietResponse ctDTO = new DonHangChiTietResponse();
                ctDTO.setId(ct.getId());
                ctDTO.setTenSanPham(ct.getTenHienThi());
                ctDTO.setDonGia(ct.getDonGia());
                ctDTO.setSoLuong(ct.getSoLuong());
                ctDTO.setThanhTien(ct.getThanhTien());

                // Lấy hình ảnh từ bienThe đã eager loaded
                try {
                    if (ct.getBienThe() != null && ct.getBienThe().getSanPham() != null) {
                        Long sanPhamId = ct.getBienThe().getSanPham().getId();
                        // Query hình ảnh từ repository
                        List<HinhAnh> hinhAnhs = hinhAnhRepository.findBySanPham_IdOrderByThuTuAscIdAsc(sanPhamId);
                        if (hinhAnhs != null && !hinhAnhs.isEmpty()) {
                            ctDTO.setHinhAnh(hinhAnhs.get(0).getUrl());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Cannot load image: " + e.getMessage());
                }

                return ctDTO;
            }).collect(Collectors.toList());

            dto.setChiTietList(chiTietDTOs);
        }

        return dto;
    }

    // Tạo đơn từ giỏ
    @Transactional
    public DonHangResponse taoDonTuGioHang(TaoDonTuGioHangRequest request, Long khachHangId) {
        // check trống giỏ hàng
        if (gioHangService.gioHangTrong(khachHangId)) {
            throw new RuntimeException("Giỏ hàng trống, không thể tạo đơn hàng");
        }
        // Lấy thông tin khách
        KhachHang khachHang = khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Lấy địa chỉ giao
        DiaChi diaChi = diaChiRepository.findById(request.getDiaChiId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ giao hàng"));

        if (!diaChi.getKhachHang().getId().equals(khachHangId)) {
            throw new RuntimeException("Địa chỉ không thuộc về khách hàng này");
        }

        String diaChiSnapshot = String.format("%s - %s - %s, %s, %s, %s",
                diaChi.getHoTen(),
                diaChi.getSoDienThoai(),
                diaChi.getDiaChi1(),
                diaChi.getPhuongXa(),
                diaChi.getQuanHuyen(),
                diaChi.getTinhThanh());

        // Lấy chi tiết giỏ hàng
        List<GioHangChiTiet> gioHangItems = gioHangService.layGioHangChiTietKhach(khachHangId);

        // check số lượng
        BigDecimal tamTinh = BigDecimal.ZERO;

        for (GioHangChiTiet item : gioHangItems) {
            BienTheSanPham bienThe = item.getBienThe();

            // Kiểm tra tồn kho
            if (bienThe.getSoLuongTon() < item.getSoLuong()) {
                String tenSP = bienThe.getSanPham() != null ? bienThe.getSanPham().getTen() : "Sản phẩm";
                throw new RuntimeException(String.format(
                        "Sản phẩm '%s' chỉ còn %d sản phẩm trong kho, không đủ số lượng yêu cầu (%d)",
                        tenSP, bienThe.getSoLuongTon(), item.getSoLuong()));
            }

            // lấy giá
            BigDecimal gia = item.getGiaTaiThoiDiem();
            if (gia == null) {
                gia = bienThe.getGia();
                if (gia == null && bienThe.getSanPham() != null) {
                    gia = bienThe.getSanPham().getGia();
                }
            }

            if (gia == null) {
                throw new RuntimeException("Không tìm thấy giá cho sản phẩm");
            }

            // Tính tổng
            BigDecimal thanhTien = gia.multiply(BigDecimal.valueOf(item.getSoLuong()));
            tamTinh = tamTinh.add(thanhTien);
        }

        // add voucher
        BigDecimal giamGiaTong = BigDecimal.ZERO;
        Voucher voucherGiamGia = null;
        Voucher voucherFreeShip = null;

        // check guest không thể dùng vc
        if(request.getVoucherId() != null || request.getFreeshipVoucherId() != null) {
            if(khachHangId == null) {
                throw new RuntimeException("Bạn phải đăng nhập để sử dụng voucher");
            }
        }

        // vc giảm giá
        if(request.getVoucherId() != null) {
            voucherGiamGia = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
            VoucherValidationResult validation = voucherService.validateVoucher(
                    voucherGiamGia.getMa(), khachHangId, tamTinh);

            if(!validation.isValid()) {
                throw new RuntimeException(validation.getMessage());
            }
            
            // check loại voucher
            String loai = voucherGiamGia.getLoai();
            if ("FREESHIP".equals(loai)) {
                throw new RuntimeException("Voucher freeship phải được áp dụng riêng");
            }
            if (!"GIAM_PHAN_TRAM".equals(loai) && !"PHAN_TRAM".equals(loai) && 
                !"GIAM_SO_TIEN".equals(loai) && !"SO_TIEN".equals(loai) &&
                !"percent".equals(loai) && !"so_tien".equals(loai)) {
                throw new RuntimeException("Loại voucher không hợp lệ cho giảm giá: " + loai);
            }

            // voucher giảm giá
            VoucherApplicationResult result = voucherService.applyVoucher(
                    voucherGiamGia.getMa(), khachHangId, tamTinh);

            if(!result.isSuccess()) {
                throw new RuntimeException(result.getMessage());
            }
            
            // Lấy giảm giá và voucher từ result
            giamGiaTong = result.getGiamGia();
            voucherGiamGia = result.getVoucher();
        }

        // vc freeship
        if (request.getFreeshipVoucherId() != null) {
            voucherFreeShip = voucherRepository.findById(request.getFreeshipVoucherId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher freeship"));
            VoucherValidationResult validation = voucherService.validateVoucher(
                    voucherFreeShip.getMa(),
                    khachHangId,
                    tamTinh);
            
            if (!validation.isValid()) {
                throw new RuntimeException(validation.getMessage());
            }
            
            // check loại voucher phải là FREESHIP
            if (!"FREESHIP".equals(voucherFreeShip.getLoai())) {
                throw new RuntimeException("Voucher này không phải voucher freeship");
            }

            // freeship voucher (giảm số lượng và lưu VoucherKhach)
            VoucherApplicationResult result = voucherService.applyVoucher(
                    voucherFreeShip.getMa(),
                    khachHangId,
                    tamTinh);
    
            if (!result.isSuccess()) {
                throw new RuntimeException(result.getMessage());
            }
    
            voucherFreeShip = result.getVoucher();
        }

        // Tính phí vận chuyển từ GHN API
        BigDecimal phiVanChuyen;
        try {
            if (request.getDistrictId() != null && request.getWardCode() != null && request.getServiceId() != null) {
                // Tạo request để gọi GHN API
                GHNShippingFeeRequest ghnRequest = new GHNShippingFeeRequest();
                ghnRequest.setToDistrictId(request.getDistrictId());
                ghnRequest.setToWardCode(request.getWardCode());
                ghnRequest.setServiceId(request.getServiceId());

                // Tính tổng khối lượng và số lượng sản phẩm
                int totalWeight = 0;
                for (GioHangChiTiet item : gioHangItems) {
                    totalWeight += item.getSoLuong() * 200; // Giả sử mỗi sản phẩm 200g
                }
                ghnRequest.setWeight(totalWeight);
                ghnRequest.setInsuranceValue(tamTinh.intValue());

                // Gọi GHN API
                GHNShippingFeeResponse ghnResponse = ghnShippingService.calculateShippingFee(ghnRequest);

                if (ghnResponse != null && ghnResponse.getData() != null) {
                    Integer totalFee = ghnResponse.getData().getTotal();
                    phiVanChuyen = BigDecimal.valueOf(totalFee);
                } else {
                    phiVanChuyen = BigDecimal.valueOf(30000);
                }
            } else {
                phiVanChuyen = BigDecimal.valueOf(30000);
            }
        } catch (Exception e) {
            phiVanChuyen = BigDecimal.valueOf(30000);
        }

        // Nếu có voucher freeship thì set phí ship = 0
        if (voucherFreeShip != null) {
            phiVanChuyen = BigDecimal.ZERO;
        }

        // tạo đơn hàng
        DonHang donHang = new DonHang();
        donHang.setSoDonHang("DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        donHang.setKhachHang(khachHang);
        donHang.setTrangThai("Chờ xác nhận");
        donHang.setTamTinh(tamTinh);
        donHang.setGiamGiaTong(giamGiaTong);
        donHang.setPhiVanChuyen(phiVanChuyen);
        donHang.setVoucher(voucherGiamGia);
        donHang.setDiaChiGiao(diaChiSnapshot);
        donHang.setGhiChu(request.getGhiChu());
        donHang.setPaymentMethod(request.getPhuongThucThanhToan());
        donHang.setPaymentStatus("pending");
        donHang.setTaoLuc(LocalDateTime.now());
        donHang.setCapNhatLuc(LocalDateTime.now());
        donHang.setDatLuc(LocalDateTime.now());
        donHang.setTongThanhToan(
                donHang.getTamTinh()
                        .subtract(donHang.getGiamGiaTong() != null ? donHang.getGiamGiaTong() : BigDecimal.ZERO)
                        .add(donHang.getPhiVanChuyen() != null ? donHang.getPhiVanChuyen() : BigDecimal.ZERO));

        DonHang savedDonHang = donHangRepository.save(donHang);

        // tạo chi tiết đơn hàng và trừ số lượng sp
        for (GioHangChiTiet item : gioHangItems) {
            BienTheSanPham bienThe = item.getBienThe();

            BigDecimal donGia = item.getGiaTaiThoiDiem();
            if (donGia == null) {
                donGia = bienThe.getGia();
                if (donGia == null && bienThe.getSanPham() != null) {
                    donGia = bienThe.getSanPham().getGia();
                }
            }

            String tenHienThi = bienThe.getSanPham() != null ? bienThe.getSanPham().getTen() : "Sản phẩm";
            if (donGia == null) {
                throw new RuntimeException("Không tìm thấy giá cho sản phẩm: " + tenHienThi);
            }
            StringBuilder thuocTinh = new StringBuilder();
            if (bienThe.getMauSac() != null) {
                thuocTinh.append("Màu: ").append(bienThe.getMauSac().getTen());
            }
            if (bienThe.getKichCo() != null) {
                if (thuocTinh.length() > 0)
                    thuocTinh.append(", ");
                thuocTinh.append("Size: ").append(bienThe.getKichCo().getTen());
            }
            if (bienThe.getChatLieu() != null) {
                if (thuocTinh.length() > 0)
                    thuocTinh.append(", ");
                thuocTinh.append("Chất liệu: ").append(bienThe.getChatLieu().getTen());
            }

            DonHangChiTiet chiTiet = new DonHangChiTiet();
            chiTiet.setDonHang(savedDonHang);
            chiTiet.setBienThe(bienThe);
            chiTiet.setTenHienThi(tenHienThi);
            chiTiet.setThuocTinh(thuocTinh.toString());
            chiTiet.setSoLuong(item.getSoLuong());
            chiTiet.setDonGia(donGia);
            chiTiet.setThanhTien(donGia.multiply(BigDecimal.valueOf(item.getSoLuong())));

            donHangChiTietRepository.save(chiTiet);

            // ❌ KHÔNG TRỪ SỐ LƯỢNG KHI TẠO ĐƠN HÀNG
            // Số lượng sẽ được trừ khi admin chuyển trạng thái sang "Đang giao"
            // bienThe.setSoLuongTon(bienThe.getSoLuongTon() - item.getSoLuong());
            // bienTheSanPhamRepository.save(bienThe);
        }
        // Xóa giỏ hàng
        gioHangService.xoaGioHang(khachHangId);
        try {
            emailService.guiEmailXacNhanDonHang(savedDonHang);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác nhận đơn hàng {}: {}",
                    savedDonHang.getSoDonHang(), e.getMessage());
        }
        return convertToDTO(savedDonHang);
    }

    // Lấy đơn hàng của khách
    public Page<DonHangResponse> layDonHangCuaKhach(Long khachHangId, int trang, int kichThuoc) {
        // Lấy tất cả đơn hàng với details (eager loaded)
        List<DonHang> allOrders = donHangRepository.findByKhachHang_IdWithDetails(khachHangId);

        // Manual pagination
        int start = trang * kichThuoc;
        int end = Math.min(start + kichThuoc, allOrders.size());
        List<DonHang> paginatedOrders = allOrders.subList(start, end);

        // Convert to DTO
        List<DonHangResponse> responses = paginatedOrders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Create Page object
        Pageable pageable = PageRequest.of(trang, kichThuoc);
        return new org.springframework.data.domain.PageImpl<>(
                responses,
                pageable,
                allOrders.size());
    } // Hủy đơn hàng

    @Transactional
    public DonHangResponse huyDonHang(Long donHangId, Long khachHangId) {
        DonHang donHang = donHangRepository.findByIdAndKhachHang_Id(donHangId, khachHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!"Chờ xác nhận".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Không thể hủy đơn hàng này");
        }

        donHang.setTrangThai("Đã hủy");
        donHang.setCapNhatLuc(LocalDateTime.now());
        DonHang savedDonHang = donHangRepository.save(donHang);

        return convertToDTO(savedDonHang);
    }

    // Lấy chi tiết đơn hàng của khách hàng
    public DonHangResponse layChiTietDonHangKhach(Long donHangId, Long khachHangId) {
        DonHang donHang = donHangRepository.findByIdAndKhachHang_Id(donHangId, khachHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        return convertToDTO(donHang);
    }

    @Transactional
    public DonHangResponse taoDonHangGuest(String sessionId, GuestCheckoutRequest request,
            Long authenticatedKhachHangId) {
        // Xác định KhachHang trước để biết lấy giỏ hàng từ đâu
        KhachHang khachHang;
        GioHang gioHang;

        // Nếu user đã login, dùng KhachHang của họ và lấy giỏ hàng theo khachHangId
        if (authenticatedKhachHangId != null) {
            System.out.println("User authenticated - using existing KhachHang ID: " + authenticatedKhachHangId);
            khachHang = khachHangRepository.findById(authenticatedKhachHangId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            // Lấy giỏ hàng theo khachHangId cho user đã đăng nhập
            gioHang = gioHangService.layGioHangCuaKhach(authenticatedKhachHangId);
        } else {
            // Tạo KhachHang GUEST mới và lấy giỏ hàng theo sessionId
            System.out.println("Guest user - creating new GUEST KhachHang");
            khachHang = new KhachHang();
            khachHang.setTaiKhoan(null);
            khachHang.setHoTen(request.getHoTen());
            khachHang.setEmail(request.getEmail());
            khachHang.setSoDienThoai(request.getSoDienThoai());
            khachHang.setKieu("GUEST");
            khachHang = khachHangRepository.save(khachHang);

            // Lấy giỏ hàng theo sessionId cho guest
            gioHang = gioHangService.layGioHangTheoSession(sessionId);
        }

        // Lấy chi tiết giỏ hàng
        List<GioHangChiTiet> gioHangItems = gioHangService.layChiTietGioHang(gioHang.getId());
        if (gioHangItems == null || gioHangItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        BigDecimal tamTinh = BigDecimal.ZERO;
        for (GioHangChiTiet item : gioHangItems) {
            BienTheSanPham bienThe = item.getBienThe();
            if (bienThe.getSoLuongTon() < item.getSoLuong()) {
                String tenSP = bienThe.getSanPham() != null ? bienThe.getSanPham().getTen() : "Sản phẩm";
                throw new RuntimeException(String.format(
                        "Sản phẩm '%s' chỉ còn %d sản phẩm trong kho, không đủ số lượng yêu cầu (%d)",
                        tenSP, bienThe.getSoLuongTon(), item.getSoLuong()));
            }
            BigDecimal gia = item.getGiaTaiThoiDiem();
            if (gia == null) {
                gia = bienThe.getGia();
                if (gia == null && bienThe.getSanPham() != null) {
                    gia = bienThe.getSanPham().getGia();
                }
            }
            if (gia == null) {
                throw new RuntimeException("Không tìm thấy giá cho sản phẩm");
            }
            tamTinh = tamTinh.add(gia.multiply(BigDecimal.valueOf(item.getSoLuong())));
        }

        // Tính phí vận chuyển từ GHN API
        BigDecimal phiVanChuyen;
        try {
            if (request.getDistrictId() != null && request.getWardCode() != null && request.getServiceId() != null) {
                log.info("🚚 Calculating shipping fee from GHN API...");
                log.info("📍 To: districtId={}, wardCode={}, serviceId={}",
                        request.getDistrictId(), request.getWardCode(), request.getServiceId());

                // Tạo request để gọi GHN API
                GHNShippingFeeRequest ghnRequest = new GHNShippingFeeRequest();
                ghnRequest.setToDistrictId(request.getDistrictId());
                ghnRequest.setToWardCode(request.getWardCode());
                ghnRequest.setServiceId(request.getServiceId());

                // Tính tổng khối lượng và số lượng sản phẩm
                int totalWeight = 0;
                for (GioHangChiTiet item : gioHangItems) {
                    totalWeight += item.getSoLuong() * 200; // Giả sử mỗi sản phẩm 200g
                }
                ghnRequest.setWeight(totalWeight);
                ghnRequest.setInsuranceValue(tamTinh.intValue());

                // Gọi GHN API
                GHNShippingFeeResponse ghnResponse = ghnShippingService.calculateShippingFee(ghnRequest);

                if (ghnResponse != null && ghnResponse.getData() != null) {
                    Integer totalFee = ghnResponse.getData().getTotal();
                    phiVanChuyen = BigDecimal.valueOf(totalFee);
                    log.info("✅ Shipping fee from GHN: {} đ", phiVanChuyen);
                } else {
                    log.warn("⚠️ GHN API returned null, using default shipping fee");
                    phiVanChuyen = BigDecimal.valueOf(30000);
                }
            } else {
                log.warn("⚠️ Missing GHN info (districtId, wardCode, or serviceId), using default shipping fee");
                phiVanChuyen = BigDecimal.valueOf(30000);
            }
        } catch (Exception e) {
            log.error("❌ Error calculating shipping fee from GHN: {}", e.getMessage());
            log.warn("⚠️ Using default shipping fee due to error");
            phiVanChuyen = BigDecimal.valueOf(30000);
        }

        BigDecimal giamGiaTong = BigDecimal.ZERO;

        String diaChiSnapshot = String.format(
                "%s - %s - %s, %s, %s, %s",
                request.getHoTen(),
                request.getSoDienThoai(),
                request.getDiaChi(),
                request.getPhuongXa() != null ? request.getPhuongXa() : "",
                request.getQuanHuyen(),
                request.getTinhThanh());

        // tạo đơn hàng guest
        DonHang donHang = new DonHang();
        donHang.setSoDonHang("DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        donHang.setKhachHang(khachHang);
        donHang.setTrangThai("Chờ xác nhận");
        donHang.setTamTinh(tamTinh);
        donHang.setGiamGiaTong(giamGiaTong);
        donHang.setPhiVanChuyen(phiVanChuyen);
        donHang.setVoucher(null);
        donHang.setDiaChiGiao(diaChiSnapshot);
        donHang.setGhiChu(request.getGhiChu());
        donHang.setPaymentMethod(request.getPhuongThucThanhToan());
        donHang.setPaymentStatus("pending");
        donHang.setTaoLuc(LocalDateTime.now());
        donHang.setCapNhatLuc(LocalDateTime.now());
        donHang.setDatLuc(LocalDateTime.now());
        donHang.setTongThanhToan(
                donHang.getTamTinh()
                        .subtract(donHang.getGiamGiaTong() != null ? donHang.getGiamGiaTong() : BigDecimal.ZERO)
                        .add(donHang.getPhiVanChuyen() != null ? donHang.getPhiVanChuyen() : BigDecimal.ZERO));

        DonHang savedDonHang = donHangRepository.save(donHang);

        for (GioHangChiTiet item : gioHangItems) {
            BienTheSanPham bienThe = item.getBienThe();

            BigDecimal donGia = item.getGiaTaiThoiDiem();
            if (donGia == null) {
                donGia = bienThe.getGia();
                if (donGia == null && bienThe.getSanPham() != null) {
                    donGia = bienThe.getSanPham().getGia();
                }
            }
            String tenHienThi = bienThe.getSanPham() != null ? bienThe.getSanPham().getTen() : "Sản phẩm";
            if (donGia == null) {
                throw new RuntimeException("Không tìm thấy giá cho sản phẩm: " + tenHienThi);
            }

            StringBuilder thuocTinh = new StringBuilder();
            if (bienThe.getMauSac() != null) {
                thuocTinh.append("Màu: ").append(bienThe.getMauSac().getTen());
            }
            if (bienThe.getKichCo() != null) {
                if (thuocTinh.length() > 0)
                    thuocTinh.append(", ");
                thuocTinh.append("Size: ").append(bienThe.getKichCo().getTen());
            }
            if (bienThe.getChatLieu() != null) {
                if (thuocTinh.length() > 0)
                    thuocTinh.append(", ");
                thuocTinh.append("Chất liệu: ").append(bienThe.getChatLieu().getTen());
            }

            DonHangChiTiet ct = new DonHangChiTiet();
            ct.setDonHang(savedDonHang);
            ct.setBienThe(bienThe);
            ct.setTenHienThi(tenHienThi);
            ct.setThuocTinh(thuocTinh.toString());
            ct.setSoLuong(item.getSoLuong());
            ct.setDonGia(donGia);
            ct.setThanhTien(donGia.multiply(BigDecimal.valueOf(item.getSoLuong())));
            donHangChiTietRepository.save(ct);

            // ❌ KHÔNG TRỪ SỐ LƯỢNG KHI TẠO ĐƠN HÀNG
            // Số lượng sẽ được trừ khi admin chuyển trạng thái sang "Đang giao"
            // bienThe.setSoLuongTon(bienThe.getSoLuongTon() - item.getSoLuong());
            // bienTheSanPhamRepository.save(bienThe);
        }

        // Xóa giỏ hàng sau khi tạo đơn thành công
        if (authenticatedKhachHangId != null) {
            // Xóa giỏ hàng của user đã đăng nhập theo khachHangId
            gioHangService.xoaGioHang(authenticatedKhachHangId);
        } else {
            // Xóa giỏ hàng guest theo sessionId
            gioHangService.xoaGioHangTheoSession(sessionId);
        }

        try {
            emailService.guiEmailXacNhanDonHang(savedDonHang);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác nhận đơn hàng {}: {}", savedDonHang.getSoDonHang(), e.getMessage());
        }

        return convertToDTO(savedDonHang);

    }

    /**
     * Fix các đơn hàng cũ có tongThanhToan = null
     * Tính lại tongThanhToan = tamTinh + phiVanChuyen - giamGiaTong
     */
    @Transactional
    public int fixDonHangNullTongThanhToan() {
        List<DonHang> allOrders = donHangRepository.findAll();
        int count = 0;

        for (DonHang dh : allOrders) {
            if (dh.getTongThanhToan() == null) {
                BigDecimal tamTinh = dh.getTamTinh() != null ? dh.getTamTinh() : BigDecimal.ZERO;
                BigDecimal phiVanChuyen = dh.getPhiVanChuyen() != null ? dh.getPhiVanChuyen() : BigDecimal.ZERO;
                BigDecimal giamGiaTong = dh.getGiamGiaTong() != null ? dh.getGiamGiaTong() : BigDecimal.ZERO;

                BigDecimal tongThanhToan = tamTinh.add(phiVanChuyen).subtract(giamGiaTong);
                dh.setTongThanhToan(tongThanhToan);
                donHangRepository.save(dh);
                count++;

                log.info("Fixed order {} - tongThanhToan: {}", dh.getSoDonHang(), tongThanhToan);
            }
        }

        log.info("Fixed {} orders with null tongThanhToan", count);
        return count;
    }

}
