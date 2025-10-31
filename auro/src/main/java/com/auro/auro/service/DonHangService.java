package com.auro.auro.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.auro.auro.dto.request.GuestCheckoutRequest;
import com.auro.auro.dto.request.TaoDonTuGioHangRequest;
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
import org.springframework.data.domain.Sort;
import lombok.extern.slf4j.Slf4j;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

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

    // Tạo mới đơn hàng
    @Transactional
    public DonHang createDonHang(DonHang donHang, List<DonHangChiTiet> chiTietList) {
        donHang.setSoDonHang("DH-" + UUID.randomUUID().toString());
        donHang.setTaoLuc(LocalDateTime.now());
        donHang.setCapNhatLuc(LocalDateTime.now());
        // donHang.setKenhBan("online");

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
        dto.setTongThanhToan(dh.getTongThanhToan());
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
        diaChi.getTinhThanh()
    );

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
                tenSP, bienThe.getSoLuongTon(), item.getSoLuong()
            ));
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
    Voucher voucher = null;
    
    if (request.getVoucherId() != null) {
        voucher = voucherRepository.findById(request.getVoucherId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getBatDauLuc()) || now.isAfter(voucher.getKetThucLuc())) {
            throw new RuntimeException("Voucher đã hết hạn hoặc chưa có hiệu lực");
        }
        
        if (voucher.getGioiHanSuDung() != null && 
            voucher.getGioiHanSuDung() != -1 && 
            voucher.getGioiHanSuDung() <= 0) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng");
        }
        
        if (voucher.getDonToiThieu() != null && 
            tamTinh.compareTo(voucher.getDonToiThieu()) < 0) {
            throw new RuntimeException(String.format(
                "Đơn hàng phải tối thiểu %s để áp dụng voucher này",
                voucher.getDonToiThieu()
            ));
        }
        if ("PHAN_TRAM".equals(voucher.getLoai())) {
            giamGiaTong = tamTinh.multiply(voucher.getGiaTri()).divide(BigDecimal.valueOf(100));
            if (voucher.getGiamToiDa() != null && giamGiaTong.compareTo(voucher.getGiamToiDa()) > 0) {
                giamGiaTong = voucher.getGiamToiDa();
            }
        } else if ("SO_TIEN".equals(voucher.getLoai())) {
            giamGiaTong = voucher.getGiaTri();
        }
        
        if (giamGiaTong.compareTo(tamTinh) > 0) {
            giamGiaTong = tamTinh;
        }
        
        voucherRepository.decreaseLimit(voucher.getId());
    }

    // tính phi ship. mặc định 30k
    BigDecimal phiVanChuyen = BigDecimal.valueOf(30000);

    // tạo đơn hàng
    DonHang donHang = new DonHang();
    donHang.setSoDonHang("DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    donHang.setKhachHang(khachHang);
    donHang.setTrangThai("Chờ xác nhận");
    donHang.setTamTinh(tamTinh);
    donHang.setGiamGiaTong(giamGiaTong);
    donHang.setPhiVanChuyen(phiVanChuyen);
    donHang.setVoucher(voucher);
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
        .add(donHang.getPhiVanChuyen() != null ? donHang.getPhiVanChuyen() : BigDecimal.ZERO)
    );
    
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
            if (thuocTinh.length() > 0) thuocTinh.append(", ");
            thuocTinh.append("Size: ").append(bienThe.getKichCo().getTen());
        }
        if (bienThe.getChatLieu() != null) {
            if (thuocTinh.length() > 0) thuocTinh.append(", ");
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
        
        // trừ số lượng sp
        bienThe.setSoLuongTon(bienThe.getSoLuongTon() - item.getSoLuong());
        bienTheSanPhamRepository.save(bienThe);
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
            Pageable pageable = PageRequest.of(trang, kichThuoc, Sort.by("taoLuc").descending());
            Page<DonHang> donHangs = donHangRepository.findByKhachHang_Id(khachHangId, pageable);
            return donHangs.map(this::convertToDTO);
        }

    // Hủy đơn hàng
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
        public void taoDonHangGuest(String sessionId, GuestCheckoutRequest request) {
            // 1) Lấy giỏ hàng theo idPhien
            GioHang gioHang = gioHangService.layGioHangTheoSession(sessionId);
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
                tenSP, bienThe.getSoLuongTon(), item.getSoLuong()
            ));
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

    BigDecimal phiVanChuyen = BigDecimal.valueOf(30000);
    BigDecimal giamGiaTong = BigDecimal.ZERO;

    KhachHang guest = new KhachHang();
    guest.setTaiKhoan(null);
    guest.setHoTen(request.getHoTen());
    guest.setEmail(request.getEmail());
    guest.setSoDienThoai(request.getSoDienThoai());
    guest.setKieu("GUEST");
    guest = khachHangRepository.save(guest);

    String diaChiSnapshot = String.format(
        "%s - %s - %s, %s, %s, %s",
        request.getHoTen(),
        request.getSoDienThoai(),
        request.getDiaChi(),
        request.getPhuongXa() != null ? request.getPhuongXa() : "",
        request.getQuanHuyen(),
        request.getTinhThanh()
    );

    // tạo đơn hàng guest
    DonHang donHang = new DonHang();
    donHang.setSoDonHang("DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    donHang.setKhachHang(guest);
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
               .add(donHang.getPhiVanChuyen() != null ? donHang.getPhiVanChuyen() : BigDecimal.ZERO)
    );

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
            if (thuocTinh.length() > 0) thuocTinh.append(", ");
            thuocTinh.append("Size: ").append(bienThe.getKichCo().getTen());
        }
        if (bienThe.getChatLieu() != null) {
            if (thuocTinh.length() > 0) thuocTinh.append(", ");
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

        bienThe.setSoLuongTon(bienThe.getSoLuongTon() - item.getSoLuong());
        bienTheSanPhamRepository.save(bienThe);
    }

    // xóa giỏ hàng gst
    gioHangService.xoaGioHangTheoSession(sessionId);

    try {
        emailService.guiEmailXacNhanDonHang(savedDonHang);
    } catch (Exception e) {
        log.error("Lỗi khi gửi email xác nhận đơn hàng {}: {}", savedDonHang.getSoDonHang(), e.getMessage());
    }


}
    
}
