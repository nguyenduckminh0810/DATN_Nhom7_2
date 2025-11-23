package com.auro.auro.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

import com.auro.auro.dto.request.GHNShippingFeeRequest;
import com.auro.auro.dto.request.GuestCheckoutRequest;
import com.auro.auro.dto.request.TaoDonTuGioHangRequest;
import com.auro.auro.dto.response.GHNShippingFeeResponse;
import com.auro.auro.exception.BadRequestException;
import com.auro.auro.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.auro.auro.dto.response.DonHangChiTietResponse;
import com.auro.auro.dto.response.DonHangPageResponse;
import com.auro.auro.dto.response.DonHangResponse;
import com.auro.auro.model.DanhGiaSanPham;
import com.auro.auro.model.DonHang;
import com.auro.auro.model.DonHangChiTiet;
import com.auro.auro.model.GioHang;
import com.auro.auro.model.GioHangChiTiet;
import com.auro.auro.repository.DonHangChiTietRepository;
import com.auro.auro.repository.DonHangRepository;
import com.auro.auro.model.*;
import com.auro.auro.repository.KhachHangRepository;
import com.auro.auro.repository.DiaChiRepository;
import com.auro.auro.repository.VoucherRepository;
import com.auro.auro.repository.BienTheSanPhamRepository;
import com.auro.auro.repository.DanhGiaSanPhamRepository;
import org.springframework.data.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.auro.auro.service.VoucherService;
import com.auro.auro.service.VoucherValidationResult;
import com.auro.auro.service.VoucherApplicationResult;
import com.auro.auro.constants.OrderStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonHangService {

    private static final Set<String> REVIEWABLE_ORDER_STATUSES = Set.of(
            "DELIVERED",
            "COMPLETED",
            "DA_GIAO",
            "DA_GIAO_HANG",
            "HOAN_TAT",
            "HOAN_THANH",
            "DA_HOAN_TAT",
            "DA_HOAN_THANH",
            "DA_NHAN",
            "GIAO_THANH_CONG");

    private final DonHangRepository donHangRepository;
    private final DonHangChiTietRepository donHangChiTietRepository;
    private final GioHangService gioHangService;
    private final KhachHangRepository khachHangRepository;
    private final DiaChiRepository diaChiRepository;
    private final VoucherRepository voucherRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final DanhGiaSanPhamRepository danhGiaSanPhamRepository;
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
            donHang.setTrangThai(OrderStatus.CHO_XAC_NHAN);
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

    // Tra cứu đơn hàng cho khách vãng lai
    @Transactional
    public DonHangResponse traCuuDonHangTheoMa(String orderCode, String soDienThoai) {
        if (orderCode == null || orderCode.trim().isEmpty()) {
            throw new BadRequestException("Mã đơn hàng không được để trống");
        }

        DonHang donHang = donHangRepository.findBySoDonHang(orderCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã đã nhập"));

        if (soDienThoai != null && !soDienThoai.trim().isEmpty()) {
            if (!isPhoneMatch(donHang, soDienThoai)) {
                throw new BadRequestException("Số điện thoại không trùng khớp với đơn hàng");
            }
        }

        return convertToDTO(donHang);
    }

    @Transactional
    public DonHangPageResponse traCuuDonHangTheoSoDienThoai(String soDienThoai) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            throw new BadRequestException("Số điện thoại không được để trống");
        }

        List<String> phoneCandidates = buildPhoneCandidates(soDienThoai.trim());
        List<DonHang> donHangs = donHangRepository
                .findTop5ByKhachHang_SoDienThoaiInOrderByDatLucDesc(phoneCandidates);

        if (donHangs.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng với số điện thoại đã nhập");
        }

        List<DonHangResponse> content = donHangs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return DonHangPageResponse.builder()
                .content(content)
                .currentPage(0)
                .pageSize(content.size())
                .totalElements(content.size())
                .totalPages(1)
                .build();
    }

    // Cập nhật đơn hàng
    @Transactional
    public DonHangResponse updateDonHang(Long id, Map<String, Object> updates) {
        DonHang donHang = getDonHangById(id);

        // Lưu trạng thái cũ để so sánh
        String trangThaiCu = donHang.getTrangThai();
        String trangThaiCuNormalized = normalizeTrangThaiKey(trangThaiCu);

        // Cập nhật các field
        if (updates.containsKey("diaChiGiao")) {
            donHang.setDiaChiGiao((String) updates.get("diaChiGiao"));
        }
        if (updates.containsKey("ghiChu")) {
            donHang.setGhiChu((String) updates.get("ghiChu"));
        }
        if (updates.containsKey("trangThai")) {
            String newTrangThai = (String) updates.get("trangThai");
            donHang.setTrangThai(newTrangThai);

            // ✅ TỰ ĐỘNG CẬP NHẬT TRẠNG THÁI THANH TOÁN KHI ĐƠN HÀNG HOÀN TẤT
            // Nếu đơn hàng chuyển sang "Hoàn tất" và phương thức thanh toán là COD
            // → Tự động đánh dấu đã thanh toán
            String normalizedStatus = normalizeTrangThaiKey(newTrangThai);
            String currentPaymentStatus = donHang.getPaymentStatus() != null ? 
                    donHang.getPaymentStatus().trim().toUpperCase() : "";
            String currentPaymentMethod = donHang.getPaymentMethod() != null ? 
                    donHang.getPaymentMethod().trim().toUpperCase() : "";
            
            log.info("Payment auto-update check - Status: {}, Normalized: {}, PaymentMethod: {}, PaymentStatus: {}",
                    newTrangThai, normalizedStatus, currentPaymentMethod, currentPaymentStatus);

            // Check các trạng thái hoàn tất: HOAN_TAT, HOAN_THANH, COMPLETED
            boolean isCompleted = "HOAN_TAT".equals(normalizedStatus) || 
                                  "HOAN_THANH".equals(normalizedStatus) ||
                                  "COMPLETED".equals(normalizedStatus) ||
                                  "HOANTAT".equals(normalizedStatus) ||
                                  "HOANTHANH".equals(normalizedStatus);
            
            if (isCompleted) {
                // Kiểm tra nếu là COD và chưa thanh toán
                // Payment method có thể là: COD, cod, COD_CASH, v.v.
                boolean isCOD = "COD".equals(currentPaymentMethod) ||
                               currentPaymentMethod.startsWith("COD") ||
                               currentPaymentMethod.contains("COD");
                
                // Payment status có thể là: pending, PENDING, CHO_THANH_TOAN, cho_thanh_toan
                boolean isNotPaid = "PENDING".equals(currentPaymentStatus) ||
                                   "CHO_THANH_TOAN".equals(currentPaymentStatus) ||
                                   currentPaymentStatus.isEmpty() ||
                                   (!"PAID".equals(currentPaymentStatus) && 
                                    !"DA_THANH_TOAN".equals(currentPaymentStatus));
                
                if (isCOD && isNotPaid) {
                    donHang.setPaymentStatus("PAID");
                    log.info("✅ Auto-updated payment status to PAID for completed COD order #{}", donHang.getSoDonHang());
                    System.out.println("✅ Auto-updated payment status to PAID for completed COD order #" + donHang.getSoDonHang());
                }
            }
        }

        if (updates.containsKey("paymentMethod")) {
            donHang.setPaymentMethod((String) updates.get("paymentMethod"));
        }
        
        // Nếu payment status được set thủ công trong updates, cập nhật trước
        if (updates.containsKey("paymentStatus")) {
            donHang.setPaymentStatus((String) updates.get("paymentStatus"));
        }
        
        // ✅ XỬ LÝ PAYMENT STATUS SAU KHI ĐÃ CẬP NHẬT TẤT CẢ
        // Nếu đơn hàng đã hoàn tất và là COD, tự động set payment status = PAID
        // (chạy sau khi tất cả updates đã được xử lý để đảm bảo logic đúng)
        String finalTrangThai = donHang.getTrangThai();
        String finalNormalizedStatus = normalizeTrangThaiKey(finalTrangThai);
        String finalPaymentMethod = donHang.getPaymentMethod() != null ? 
                donHang.getPaymentMethod().trim().toUpperCase() : "";
        String finalPaymentStatus = donHang.getPaymentStatus() != null ? 
                donHang.getPaymentStatus().trim().toUpperCase() : "";
        
        boolean isCompletedFinal = "HOAN_TAT".equals(finalNormalizedStatus) || 
                                   "HOAN_THANH".equals(finalNormalizedStatus) ||
                                   "COMPLETED".equals(finalNormalizedStatus) ||
                                   "HOANTAT".equals(finalNormalizedStatus) ||
                                   "HOANTHANH".equals(finalNormalizedStatus);
        
        if (isCompletedFinal) {
            boolean isCODFinal = "COD".equals(finalPaymentMethod) ||
                               finalPaymentMethod.startsWith("COD") ||
                               finalPaymentMethod.contains("COD");
            
            boolean isNotPaidFinal = "PENDING".equals(finalPaymentStatus) ||
                                   "CHO_THANH_TOAN".equals(finalPaymentStatus) ||
                                   finalPaymentStatus.isEmpty() ||
                                   (!"PAID".equals(finalPaymentStatus) && 
                                    !"DA_THANH_TOAN".equals(finalPaymentStatus));
            
            // ✅ Nếu là COD và chưa thanh toán, tự động set thành PAID
            // Override payment status nếu nó vẫn là pending (kể cả khi admin set thủ công)
            if (isCODFinal && isNotPaidFinal) {
                donHang.setPaymentStatus("PAID");
                log.info("✅ Auto-updated payment status to PAID for completed COD order #{} (overriding pending status)", donHang.getSoDonHang());
                System.out.println("✅ Auto-updated payment status to PAID for completed COD order #" + donHang.getSoDonHang() + " (overriding pending status)");
            }
        }

        // ✅ QUAN TRỌNG: TRỪ TỒN KHO KHI CHUYỂN SANG TRẠNG THÁI "ĐANG GIAO", "ĐÃ GIAO", HOẶC "HOÀN TẤT"
        // Logic hoạt động:
        // 1. Khi khách thêm vào giỏ hàng → KHÔNG trừ tồn kho (chỉ kiểm tra có đủ hàng không)
        // 2. Khi khách tạo đơn hàng → KHÔNG trừ tồn kho (đơn ở trạng thái "Chờ xác nhận")
        // 3. Khi admin chuyển trạng thái sang "Đang giao", "Đã giao", hoặc "Hoàn tất" → TRỪ TỒN KHO
        //    - Nếu từ PENDING → SHIPPING/DELIVERED/COMPLETED: TRỪ TỒN KHO
        //    - Nếu đã ở SHIPPING/DELIVERED/COMPLETED: KHÔNG trừ lại (đã trừ rồi)
        // Lý do: Tránh trường hợp khách thêm vào giỏ nhưng không mua, hoặc đơn bị hủy
        String trangThaiMoi = donHang.getTrangThai();
        String trangThaiMoiNormalized = normalizeTrangThaiKey(trangThaiMoi);

        // Kiểm tra trạng thái cũ và mới
        boolean wasPending = "CHO_XAC_NHAN".equals(trangThaiCuNormalized) || 
                            "PENDING".equals(trangThaiCuNormalized);
        boolean wasDangGiao = "DANG_GIAO".equals(trangThaiCuNormalized) ||
                             "SHIPPING".equals(trangThaiCuNormalized);
        boolean wasDaGiaoOrCompleted = "DA_GIAO".equals(trangThaiCuNormalized) ||
                                      "DA_GIAO_HANG".equals(trangThaiCuNormalized) ||
                                      "DELIVERED".equals(trangThaiCuNormalized) ||
                                      "HOAN_TAT".equals(trangThaiCuNormalized) ||
                                      "HOAN_THANH".equals(trangThaiCuNormalized) ||
                                      "COMPLETED".equals(trangThaiCuNormalized) ||
                                      "HOANTAT".equals(trangThaiCuNormalized) ||
                                      "HOANTHANH".equals(trangThaiCuNormalized);
        
        // Trạng thái cũ chưa từng được xử lý (chưa trừ tồn kho)
        boolean wasNotProcessed = wasPending || 
                                  (!wasDangGiao && !wasDaGiaoOrCompleted);
        
        // ✅ TRẠNG THÁI "ĐÃ HỦY" - KHÔNG BAO GIỜ TRỪ TỒN KHO
        boolean isDaHuy = "DA_HUY".equals(trangThaiMoiNormalized) ||
                          "CANCELLED".equals(trangThaiMoiNormalized);
        
        // Trạng thái mới cần trừ tồn kho (chỉ khi KHÔNG phải "Đã hủy")
        boolean isDangGiao = "DANG_GIAO".equals(trangThaiMoiNormalized) ||
                            "SHIPPING".equals(trangThaiMoiNormalized);
        boolean isDaGiao = "DA_GIAO".equals(trangThaiMoiNormalized) ||
                          "DA_GIAO_HANG".equals(trangThaiMoiNormalized) ||
                          "DELIVERED".equals(trangThaiMoiNormalized);
        boolean isCompleted = "HOAN_TAT".equals(trangThaiMoiNormalized) ||
                             "HOAN_THANH".equals(trangThaiMoiNormalized) ||
                             "COMPLETED".equals(trangThaiMoiNormalized) ||
                             "HOANTAT".equals(trangThaiMoiNormalized) ||
                             "HOANTHANH".equals(trangThaiMoiNormalized);
        
        // ✅ CHỈ TRỪ TỒN KHO KHI: (1) Đơn chưa được xử lý, (2) Chuyển sang Đang giao/Đã giao/Hoàn tất, (3) KHÔNG PHẢI Đã hủy
        boolean needsStockReduction = !isDaHuy && wasNotProcessed && (isDangGiao || isDaGiao || isCompleted);

        log.info("=== UPDATE ORDER STATUS ===");
        log.info("Order ID: {}", id);
        log.info("Old status: {} (normalized: {})", trangThaiCu, trangThaiCuNormalized);
        log.info("New status: {} (normalized: {})", trangThaiMoi, trangThaiMoiNormalized);
        log.info("wasPending: {}, wasDangGiao: {}, wasDaGiaoOrCompleted: {}, wasNotProcessed: {}", 
                wasPending, wasDangGiao, wasDaGiaoOrCompleted, wasNotProcessed);
        log.info("isDangGiao: {}, isDaGiao: {}, isCompleted: {}, needsStockReduction: {}", 
                isDangGiao, isDaGiao, isCompleted, needsStockReduction);

        if (needsStockReduction) {
            // Xác định tên trạng thái để log
            String targetStatusName = isDangGiao ? "SHIPPING (Đang giao)" : 
                                    (isDaGiao ? "DELIVERED (Đã giao)" : "COMPLETED (Hoàn tất)");
            log.info(">>> TRIGGERING STOCK REDUCTION (Order status changed to {}) <<<", targetStatusName);
            List<DonHangChiTiet> chiTietList = donHangChiTietRepository.findByDonHang_Id(id);
            log.info("Found {} order items to process", chiTietList.size());

            // ✅ BƯỚC 1: KIỂM TRA TẤT CẢ SẢN PHẨM TRƯỚC KHI TRỪ TỒN KHO
            List<String> outOfStockItems = new ArrayList<>();

            for (DonHangChiTiet chiTiet : chiTietList) {
                BienTheSanPham bienThe = chiTiet.getBienThe();
                int soLuongDat = chiTiet.getSoLuong();
                int tonHienTai = bienThe.getSoLuongTon();

                log.info("Checking stock - Variant ID: {}, Product: {}, Current stock: {}, Ordered: {}",
                        bienThe.getId(),
                        bienThe.getSanPham() != null ? bienThe.getSanPham().getTen() : "N/A",
                        tonHienTai,
                        soLuongDat);

                // Thu thập danh sách sản phẩm thiếu hàng
                if (tonHienTai < soLuongDat) {
                    String productName = bienThe.getSanPham() != null ? bienThe.getSanPham().getTen() : "N/A";
                    String color = bienThe.getMauSac() != null ? bienThe.getMauSac().getTen() : "N/A";
                    String size = bienThe.getKichCo() != null ? bienThe.getKichCo().getTen() : "N/A";

                    String itemInfo = String.format("  • %s (%s - %s): Cần %d, Còn %d → Thiếu %d",
                            productName, color, size, soLuongDat, tonHienTai, soLuongDat - tonHienTai);

                    outOfStockItems.add(itemInfo);

                    log.error("INSUFFICIENT STOCK! Variant ID: {}, Available: {}, Required: {}",
                            bienThe.getId(), tonHienTai, soLuongDat);
                }
            }

            // Nếu có sản phẩm thiếu hàng, throw exception với danh sách đầy đủ
            if (!outOfStockItems.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("⚠️ HẾT HÀNG - CẦN NHẬP THÊM!\n\n");
                errorMessage.append(String.format("Đơn hàng có %d sản phẩm thiếu hàng:\n\n", outOfStockItems.size()));

                for (String item : outOfStockItems) {
                    errorMessage.append(item).append("\n");
                }

                // Xác định tên trạng thái để hiển thị trong thông báo lỗi
                String statusName = isDangGiao ? "\"Đang giao\"" : 
                                   (isDaGiao ? "\"Đã giao\"" : "\"Hoàn tất\"");
                errorMessage.append("\n→ Vui lòng nhập thêm hàng trước khi chuyển đơn sang trạng thái ").append(statusName).append(".");

                throw new RuntimeException(errorMessage.toString());
            }

            // ✅ BƯỚC 2: NẾU ĐỦ HÀNG, TRỪ TỒN KHO CHO TẤT CẢ SẢN PHẨM
            for (DonHangChiTiet chiTiet : chiTietList) {
                BienTheSanPham bienThe = chiTiet.getBienThe();
                int soLuongDat = chiTiet.getSoLuong();
                int tonHienTai = bienThe.getSoLuongTon();

                // Trừ tồn kho
                int soLuongMoi = tonHienTai - soLuongDat;
                bienThe.setSoLuongTon(soLuongMoi);
                bienTheSanPhamRepository.save(bienThe);

                log.info("Stock reduced! Variant ID: {}, Old stock: {}, New stock: {}",
                        bienThe.getId(), tonHienTai, soLuongMoi);
            }

            log.info(">>> STOCK REDUCTION COMPLETED - All {} items processed <<<", chiTietList.size());
        } else {
            if (wasDangGiao || wasDaGiaoOrCompleted) {
                log.info("Stock reduction NOT triggered (order already processed - was in SHIPPING/DELIVERED/COMPLETED status)");
            } else {
                log.info("Stock reduction NOT triggered (status change not to SHIPPING/DELIVERED/COMPLETED)");
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

    // ✅ XÓA MỀM ĐƠN HÀNG (chuyển trạng thái sang Đã hủy) - CHỈ CHUYỂN TRẠNG THÁI, KHÔNG TRỪ TỒN KHO
    @Transactional
    public void softDeleteDonHang(Long id) {
        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));

        // Kiểm tra trạng thái có được phép hủy không
        if (OrderStatus.HOAN_TAT.equals(donHang.getTrangThai())) {
            throw new RuntimeException("Không thể hủy đơn hàng đã hoàn thành");
        }

        if (OrderStatus.DA_HUY.equals(donHang.getTrangThai())) {
            throw new RuntimeException("Đơn hàng đã bị hủy từ trước");
        }

        // ✅ CHỈ CHUYỂN TRẠNG THÁI SANG "ĐÃ HỦY" - KHÔNG TRỪ TỒN KHO, KHÔNG LÀM GÌ KHÁC
        donHang.setTrangThai(OrderStatus.DA_HUY);
        donHang.setCapNhatLuc(LocalDateTime.now());
        donHangRepository.save(donHang);
        
        log.info("✅ Order #{} soft deleted - Status changed to DA_HUY (NO stock deduction)", donHang.getSoDonHang());
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
            List<DonHangChiTietResponse> chiTietDTOs = dh.getChiTietList().stream()
                    .map(this::mapChiTietToResponse)
                    .collect(Collectors.toList());

            dto.setChiTietList(chiTietDTOs);
        }

        return dto;
    }

    private boolean isPhoneMatch(DonHang donHang, String phoneInput) {
        if (donHang == null || phoneInput == null) {
            return false;
        }

        KhachHang khachHang = donHang.getKhachHang();
        if (khachHang == null || khachHang.getSoDienThoai() == null) {
            return false;
        }

        String normalizedInput = normalizePhone(phoneInput);
        String normalizedStored = normalizePhone(khachHang.getSoDienThoai());

        if (normalizedInput.isEmpty() || normalizedStored.isEmpty()) {
            return khachHang.getSoDienThoai().trim().equalsIgnoreCase(phoneInput.trim());
        }

        return normalizedInput.equals(normalizedStored);
    }

    private List<String> buildPhoneCandidates(String rawPhone) {
        List<String> candidates = new ArrayList<>();
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            return candidates;
        }

        String trimmed = rawPhone.trim();
        String normalized = normalizePhone(trimmed);

        candidates.add(trimmed);
        if (!normalized.isEmpty() && !candidates.contains(normalized)) {
            candidates.add(normalized);
        }

        if (!normalized.isEmpty()) {
            if (normalized.startsWith("0") && normalized.length() > 1) {
                String international = "+84" + normalized.substring(1);
                if (!candidates.contains(international)) {
                    candidates.add(international);
                }
            } else if (normalized.startsWith("84") && normalized.length() > 2) {
                String domestic = "0" + normalized.substring(2);
                if (!candidates.contains(domestic)) {
                    candidates.add(domestic);
                }
            }
        }

        return candidates;
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }

        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.startsWith("84") && digitsOnly.length() > 2) {
            digitsOnly = "0" + digitsOnly.substring(2);
        }
        return digitsOnly;
    }

    private DonHangChiTietResponse mapChiTietToResponse(DonHangChiTiet ct) {
        DonHangChiTietResponse ctDTO = new DonHangChiTietResponse();
        ctDTO.setId(ct.getId());
        ctDTO.setTenSanPham(ct.getTenHienThi());
        ctDTO.setDonGia(ct.getDonGia());
        ctDTO.setSoLuong(ct.getSoLuong());
        ctDTO.setThanhTien(ct.getThanhTien());

        if (ct.getBienThe() != null) {
            ctDTO.setBienTheId(ct.getBienThe().getId());

            if (ct.getBienThe().getSanPham() != null) {
                ctDTO.setSanPhamId(ct.getBienThe().getSanPham().getId());

                try {
                    List<HinhAnh> hinhAnhs = hinhAnhRepository
                            .findBySanPham_IdOrderByThuTuAscIdAsc(ct.getBienThe().getSanPham().getId());
                    if (hinhAnhs != null && !hinhAnhs.isEmpty()) {
                        ctDTO.setHinhAnh(hinhAnhs.get(0).getUrl());
                    }
                } catch (Exception e) {
                    log.warn("Cannot load image for product {}: {}", ct.getBienThe().getSanPham().getId(),
                            e.getMessage());
                }
            }

            if (ct.getBienThe().getMauSac() != null) {
                ctDTO.setMauSacId(ct.getBienThe().getMauSac().getId());
                ctDTO.setMauSacTen(ct.getBienThe().getMauSac().getTen());
            }

            if (ct.getBienThe().getKichCo() != null) {
                ctDTO.setKichCoId(ct.getBienThe().getKichCo().getId());
                ctDTO.setKichCoTen(ct.getBienThe().getKichCo().getTen());
            }
        }

        danhGiaSanPhamRepository.findByDonHangChiTiet_Id(ct.getId()).ifPresentOrElse(danhGia -> {
            ctDTO.setDaDanhGia(true);
            ctDTO.setDanhGiaSoSao(danhGia.getSoSao());
            ctDTO.setDanhGiaNoiDung(danhGia.getNoiDung());
            ctDTO.setDanhGiaTaoLuc(danhGia.getTaoLuc());
        }, () -> {
            ctDTO.setDaDanhGia(false);
        });

        return ctDTO;
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
        if (request.getVoucherId() != null || request.getFreeshipVoucherId() != null) {
            if (khachHangId == null) {
                throw new RuntimeException("Bạn phải đăng nhập để sử dụng voucher");
            }
        }

        // vc giảm giá
        if (request.getVoucherId() != null) {
            try {
                voucherGiamGia = voucherRepository.findById(request.getVoucherId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
                VoucherValidationResult validation = voucherService.validateVoucher(
                        voucherGiamGia.getMa(), khachHangId, tamTinh);

                if (!validation.isValid()) {
                    log.warn("Voucher invalid: {}", validation.getMessage());
                    voucherGiamGia = null;
                } else {
                    String loai = voucherGiamGia.getLoai();
                    if ("FREESHIP".equals(loai)) {
                        log.warn("Voucher freeship phải áp dụng riêng - skip");
                        voucherGiamGia = null;
                    } else if (!"GIAM_PHAN_TRAM".equals(loai) && !"PHAN_TRAM".equals(loai) &&
                            !"GIAM_SO_TIEN".equals(loai) && !"SO_TIEN".equals(loai) &&
                            !"percent".equals(loai) && !"so_tien".equals(loai)) {
                        log.warn("Loại voucher không hợp lệ cho giảm giá: {}", loai);
                        voucherGiamGia = null;
                    } else {
                        VoucherApplicationResult result = voucherService.applyVoucher(
                                voucherGiamGia.getMa(), khachHangId, tamTinh);
                        if (!result.isSuccess()) {
                            log.warn("Apply voucher failed: {}", result.getMessage());
                            voucherGiamGia = null;
                        } else {
                            giamGiaTong = result.getGiamGia();
                            voucherGiamGia = result.getVoucher();
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Lỗi xử lý voucher giảm giá: {}", e.getMessage());
                voucherGiamGia = null;
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

            if (!result.isSuccess()) {
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
        donHang.setTrangThai(OrderStatus.CHO_XAC_NHAN);
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
        // ✅ CHỈ XÓA các chi tiết giỏ hàng đã được đặt hàng, KHÔNG xóa toàn bộ giỏ hàng
        // Giữ lại các sản phẩm chưa được chọn để user có thể đặt hàng sau
        gioHangService.xoaChiTietGioHangDaDat(gioHangItems);

        // Flush để phát hiện lỗi ràng buộc ngay tại đây (thay vì tới lúc commit)
        try {
            donHangRepository.flush();
        } catch (Exception e) {
            log.error("Flush failed in taoDonTuGioHang: {}", e.getMessage(), e);
            throw e;
        }
        try {
            emailService.guiEmailXacNhanDonHang(savedDonHang);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác nhận đơn hàng {}: {}",
                    savedDonHang.getSoDonHang(), e.getMessage());
        }
        return convertToDTO(savedDonHang);
    }

    // Lấy đơn hàng của khách
    public DonHangPageResponse layDonHangCuaKhach(Long khachHangId, int trang, int kichThuoc, String trangThai,
            String keyword) {
        if (kichThuoc <= 0) {
            kichThuoc = 10;
        }

        List<DonHang> allOrders = donHangRepository.findByKhachHang_IdWithDetails(khachHangId);

        List<DonHang> filteredOrders = allOrders.stream()
                .filter(order -> matchesStatus(order, trangThai))
                .filter(order -> matchesKeyword(order, keyword))
                .collect(Collectors.toList());

        Map<String, Long> statusCounts = buildStatusCounts(filteredOrders);

        int totalElements = filteredOrders.size();
        int totalPages = kichThuoc <= 0 ? 0 : (int) Math.ceil((double) totalElements / (double) kichThuoc);

        int safePage = Math.max(trang, 0);
        int start = safePage * kichThuoc;
        if (start >= totalElements) {
            start = Math.max(totalElements - kichThuoc, 0);
        }
        int end = Math.min(start + kichThuoc, totalElements);
        List<DonHang> paginatedOrders = filteredOrders.subList(start, end);

        List<DonHangResponse> responses = paginatedOrders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return DonHangPageResponse.builder()
                .content(responses)
                .currentPage(totalElements == 0 ? 0 : Math.min(safePage + 1, Math.max(totalPages, 1)))
                .pageSize(kichThuoc)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .statusCounts(statusCounts)
                .build();
    } // Hủy đơn hàng

    // ✅ HỦY ĐƠN HÀNG - CHỈ CHUYỂN TRẠNG THÁI, KHÔNG TRỪ TỒN KHO
    @Transactional
    public DonHangResponse huyDonHang(Long donHangId, Long khachHangId) {
        log.info("=== HỦY ĐƠN HÀNG ===");
        log.info("Order ID: {}, Customer ID: {}", donHangId, khachHangId);
        
        DonHang donHang = donHangRepository.findByIdAndKhachHang_Id(donHangId, khachHangId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy đơn hàng ID: {} của khách hàng ID: {}", donHangId, khachHangId);
                    return new RuntimeException("Không tìm thấy đơn hàng");
                });

        String currentStatus = donHang.getTrangThai();
        log.info("Order #{} current status: {}", donHang.getSoDonHang(), currentStatus);
        
        // ✅ Normalize trạng thái để so sánh (hỗ trợ cả tiếng Việt và tiếng Anh)
        String normalizedStatus = normalizeTrangThaiKey(currentStatus);
        log.info("Order #{} normalized status: {}", donHang.getSoDonHang(), normalizedStatus);
        
        // ✅ Kiểm tra trạng thái - cho phép hủy nếu là "PENDING" (tiếng Anh) hoặc "Chờ xác nhận" (tiếng Việt)
        // Sử dụng normalize để hỗ trợ cả hai format
        boolean canCancel = "CHO_XAC_NHAN".equals(normalizedStatus) || 
                           "PENDING".equals(normalizedStatus) ||
                           "CHO_XAC_NHAN".equals(currentStatus) ||
                           "PENDING".equals(currentStatus) ||
                           "Chờ xác nhận".equals(currentStatus) ||
                           (currentStatus != null && currentStatus.trim().equalsIgnoreCase("Chờ xác nhận")) ||
                           (currentStatus != null && currentStatus.trim().equalsIgnoreCase("PENDING"));
        
        if (!canCancel) {
            String errorMsg = String.format("Không thể hủy đơn hàng. Trạng thái hiện tại: %s. Chỉ có thể hủy đơn hàng ở trạng thái 'Chờ xác nhận' (PENDING)", 
                    currentStatus != null ? currentStatus : "null");
            log.error("Cannot cancel order #{}: {} (normalized: {})", donHang.getSoDonHang(), errorMsg, normalizedStatus);
            throw new RuntimeException(errorMsg);
        }

        // ✅ CHỈ CHUYỂN TRẠNG THÁI SANG "ĐÃ HỦY" - KHÔNG TRỪ TỒN KHO, KHÔNG LÀM GÌ KHÁC
        donHang.setTrangThai(OrderStatus.DA_HUY);
        donHang.setCapNhatLuc(LocalDateTime.now());
        DonHang savedDonHang = donHangRepository.save(donHang);

        log.info("✅ Order #{} cancelled successfully - Status changed from {} to DA_HUY (NO stock deduction)", 
                savedDonHang.getSoDonHang(), currentStatus);
        
        return convertToDTO(savedDonHang);
    }

    @Transactional
    public DonHangChiTietResponse danhGiaDonHang(Long donHangId, Long chiTietId, Long khachHangId,
            Integer soSao, String noiDung) {
        DonHang donHang = donHangRepository.findByIdAndKhachHang_Id(donHangId, khachHangId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!coTheDanhGia(donHang.getTrangThai())) {
            throw new RuntimeException("Chỉ có thể đánh giá đơn hàng đã giao");
        }

        DonHangChiTiet chiTiet = donHang.getChiTietList().stream()
                .filter(item -> item.getId().equals(chiTietId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong đơn hàng"));

        DanhGiaSanPham danhGia = danhGiaSanPhamRepository.findByDonHangChiTiet_Id(chiTietId)
                .orElse(new DanhGiaSanPham());

        danhGia.setDonHang(donHang);
        danhGia.setDonHangChiTiet(chiTiet);
        if (chiTiet.getBienThe() != null) {
            danhGia.setSanPham(chiTiet.getBienThe().getSanPham());
        }
        danhGia.setKhachHang(donHang.getKhachHang());
        danhGia.setSoSao(soSao);
        danhGia.setNoiDung(noiDung);
        if (danhGia.getTaoLuc() == null) {
            danhGia.setTaoLuc(LocalDateTime.now());
        }
        danhGia.setCapNhatLuc(LocalDateTime.now());

        danhGiaSanPhamRepository.save(danhGia);

        return mapChiTietToResponse(chiTiet);
    }

    private Map<String, Long> buildStatusCounts(List<DonHang> orders) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("ALL", (long) orders.size());
        for (String code : Arrays.asList("PENDING", "SHIPPING", "DELIVERED", "COMPLETED", "CANCELLED")) {
            counts.putIfAbsent(code, 0L);
        }
        for (DonHang order : orders) {
            String code = normalizeTrangThaiCode(order.getTrangThai());
            counts.merge(code, 1L, Long::sum);
        }
        return counts;
    }

    private boolean matchesStatus(DonHang order, String trangThai) {
        if (trangThai == null || trangThai.trim().isEmpty() || "ALL".equalsIgnoreCase(trangThai.trim())) {
            return true;
        }
        String orderCode = normalizeTrangThaiCode(order.getTrangThai());
        return orderCode.equalsIgnoreCase(trangThai.trim());
    }

    private boolean matchesKeyword(DonHang order, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String lower = keyword.trim().toLowerCase(Locale.ROOT);

        if (order.getSoDonHang() != null && order.getSoDonHang().toLowerCase(Locale.ROOT).contains(lower)) {
            return true;
        }
        if (order.getTrangThai() != null && order.getTrangThai().toLowerCase(Locale.ROOT).contains(lower)) {
            return true;
        }
        if (order.getDiaChiGiao() != null && order.getDiaChiGiao().toLowerCase(Locale.ROOT).contains(lower)) {
            return true;
        }
        if (order.getGhiChu() != null && order.getGhiChu().toLowerCase(Locale.ROOT).contains(lower)) {
            return true;
        }
        if (order.getTaoLuc() != null && order.getTaoLuc().toString().toLowerCase(Locale.ROOT).contains(lower)) {
            return true;
        }
        if (order.getCapNhatLuc() != null
                && order.getCapNhatLuc().toString().toLowerCase(Locale.ROOT).contains(lower)) {
            return true;
        }
        if (order.getChiTietList() != null) {
            for (DonHangChiTiet chiTiet : order.getChiTietList()) {
                if (chiTiet.getTenHienThi() != null
                        && chiTiet.getTenHienThi().toLowerCase(Locale.ROOT).contains(lower)) {
                    return true;
                }
                if (chiTiet.getThuocTinh() != null
                        && chiTiet.getThuocTinh().toLowerCase(Locale.ROOT).contains(lower)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizeTrangThaiCode(String value) {
        String key = normalizeTrangThaiKey(value);
        switch (key) {
            case "CHO_XAC_NHAN":
                return "PENDING";
            case "DANG_GIAO":
                return "SHIPPING";
            case "DA_GIAO":
            case "DA_GIAO_HANG":
                return "DELIVERED";
            case "HOAN_TAT":
            case "HOAN_THANH":
                return "COMPLETED";
            case "DA_HUY":
                return "CANCELLED";
            default:
                return "PENDING";
        }
    }

    private boolean coTheDanhGia(String trangThaiDonHang) {
        String normalized = normalizeTrangThaiKey(trangThaiDonHang);
        if (normalized.isEmpty()) {
            return false;
        }
        return REVIEWABLE_ORDER_STATUSES.contains(normalized);
    }

    private String normalizeTrangThaiKey(String value) {
        if (value == null) {
            return "";
        }
        String upper = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_");
        return upper.replaceAll("^_+|_+$", "");
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
        System.out.println("=== taoDonHangGuest START ===");
        System.out.println("SessionId: " + sessionId);
        System.out.println("AuthenticatedKhachHangId: " + authenticatedKhachHangId);
        System.out.println("Request maVoucher: '" + request.getMaVoucher() + "'");
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

        // ✅ Lấy CHỈ các chi tiết giỏ hàng đã được chọn (nếu có danh sách ID)
        // Nếu không có danh sách ID, lấy toàn bộ giỏ hàng (tương thích ngược)
        List<GioHangChiTiet> allItems = gioHangService.layChiTietGioHang(gioHang.getId());
        List<GioHangChiTiet> gioHangItems;
        
        if (request.getSelectedCartItemIds() != null && !request.getSelectedCartItemIds().isEmpty()) {
            // Chỉ lấy các chi tiết giỏ hàng đã được chọn
            gioHangItems = allItems.stream()
                    .filter(item -> request.getSelectedCartItemIds().contains(item.getId()))
                    .collect(java.util.stream.Collectors.toList());
            System.out.println("✅ [CHECKOUT] Only selected items: " + gioHangItems.size() + " / " + allItems.size());
            log.info("✅ [CHECKOUT] Only selected items: {} / {}", gioHangItems.size(), allItems.size());
        } else {
            // Fallback: lấy toàn bộ giỏ hàng (tương thích ngược)
            gioHangItems = allItems;
            System.out.println("⚠️ [CHECKOUT] No selectedCartItemIds provided, using all cart items");
            log.warn("⚠️ [CHECKOUT] No selectedCartItemIds provided, using all cart items");
        }
        
        if (gioHangItems == null || gioHangItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống hoặc không có sản phẩm được chọn");
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

        // Áp dụng voucher nếu có
        Voucher appliedVoucher = null;
        BigDecimal giamGiaTong = BigDecimal.ZERO;
        System.out.println("=== VOUCHER PROCESSING START ===");
        System.out.println("🎫 Checking voucher - maVoucher from request: '" + request.getMaVoucher() + "'");
        log.info("🎫 Checking voucher - maVoucher from request: '{}'", request.getMaVoucher());
        if (request.getMaVoucher() != null && !request.getMaVoucher().trim().isEmpty()) {
            String code = request.getMaVoucher().trim();
            System.out.println("🎫 Applying voucher: code=" + code + ", khachHangId=" + authenticatedKhachHangId
                    + ", tamTinh=" + tamTinh);
            log.info("🎫 Applying voucher: code={}, khachHangId={}, tamTinh={}", code, authenticatedKhachHangId,
                    tamTinh);
            try {
                // Tìm voucher theo mã, thử không phân biệt hoa/thường nếu không tìm thấy
                System.out.println("🔍 Searching for voucher with code: '" + code + "'");
                log.info("🔍 Searching for voucher with code: '{}'", code);
                Optional<Voucher> voucherOpt = voucherRepository.findByMa(code);
                if (voucherOpt.isEmpty()) {
                    System.out
                            .println("🔍 Not found with original code, trying uppercase: '" + code.toUpperCase() + "'");
                    log.info("🔍 Not found with original code, trying uppercase: '{}'", code.toUpperCase());
                    voucherOpt = voucherRepository.findByMa(code.toUpperCase());
                }
                if (voucherOpt.isEmpty()) {
                    System.out.println("🔍 Not found with uppercase, trying lowercase: '" + code.toLowerCase() + "'");
                    log.info("🔍 Not found with uppercase, trying lowercase: '{}'", code.toLowerCase());
                    voucherOpt = voucherRepository.findByMa(code.toLowerCase());
                }
                if (voucherOpt.isEmpty()) {
                    System.out.println(
                            "⚠️ Voucher code '" + code + "' not found in database after trying all case variations");
                    log.warn("⚠️ Voucher code '{}' not found in database after trying all case variations", code);
                }
                Voucher voucher = voucherOpt.orElse(null);
                if (voucher != null) {
                    // Lấy mã voucher từ DB (đảm bảo đúng case)
                    String voucherMaFromDB = voucher.getMa();
                    String loai = voucher.getLoai();
                    System.out.println("🎫 Found voucher: id=" + voucher.getId() + ", ma=" + voucherMaFromDB +
                            ", loai=" + loai + ", giaTri=" + voucher.getGiaTri() +
                            ", giamToiDa=" + voucher.getGiamToiDa() + ", donToiThieu=" + voucher.getDonToiThieu());
                    log.info("🎫 Found voucher: id={}, ma={}, loai={}, giaTri={}, giamToiDa={}, donToiThieu={}",
                            voucher.getId(), voucherMaFromDB, loai, voucher.getGiaTri(),
                            voucher.getGiamToiDa(), voucher.getDonToiThieu());
                    if ("FREESHIP".equalsIgnoreCase(loai)) {
                        // Freeship: miễn phí ship
                        phiVanChuyen = BigDecimal.ZERO;
                        appliedVoucher = voucher;
                        System.out.println("✅ Applied FREESHIP voucher - shipping fee set to 0");
                        log.info("✅ Applied FREESHIP voucher - shipping fee set to 0");
                    } else {
                        // Giảm giá: sử dụng service để tính đúng giamGiaTong
                        // QUAN TRỌNG: Truyền mã voucher từ DB (voucherMaFromDB) thay vì mã từ request
                        // (code)
                        // để đảm bảo tìm được voucher trong validateVoucher()
                        System.out.println("🎫 Calling applyVoucher with: voucherMaFromDB=" + voucherMaFromDB +
                                ", khachHangId=" + authenticatedKhachHangId + ", tamTinh=" + tamTinh);
                        log.info("🎫 Calling applyVoucher with: voucherMaFromDB={}, khachHangId={}, tamTinh={}",
                                voucherMaFromDB, authenticatedKhachHangId, tamTinh);
                        VoucherApplicationResult result = voucherService.applyVoucher(voucherMaFromDB,
                                authenticatedKhachHangId, tamTinh);
                        System.out.println("🎫 Voucher apply result: success=" + result.isSuccess() +
                                ", message=" + result.getMessage() + ", giamGia=" + result.getGiamGia());
                        log.info("🎫 Voucher apply result: success={}, message={}, giamGia={}",
                                result.isSuccess(), result.getMessage(), result.getGiamGia());
                        if (!result.isSuccess()) {
                            System.out.println("❌ Voucher apply failed: " + result.getMessage());
                            log.error("❌ Voucher apply failed: {}", result.getMessage());
                            // QUAN TRỌNG: Throw exception để ngăn chặn đặt hàng khi voucher không hợp lệ
                            // Đặc biệt là khi voucher đã được sử dụng
                            throw new RuntimeException("Voucher không hợp lệ: " + result.getMessage());
                        } else {
                            giamGiaTong = result.getGiamGia();
                            appliedVoucher = result.getVoucher();
                            System.out.println("✅ Applied discount voucher - giamGiaTong=" + giamGiaTong +
                                    ", voucherId=" + (appliedVoucher != null ? appliedVoucher.getId() : "null"));
                            log.info("✅ Applied discount voucher - giamGiaTong={}, voucherId={}",
                                    giamGiaTong, appliedVoucher != null ? appliedVoucher.getId() : "null");
                        }
                    }
                } else {
                    System.out.println("⚠️ Voucher code '" + code + "' not found in database - skipping voucher");
                    log.warn("⚠️ Voucher code '{}' not found in database - skipping voucher", code);
                    // Voucher không tìm thấy → cho phép đặt hàng tiếp tục (không có giảm giá)
                }
            } catch (RuntimeException e) {
                // Nếu là RuntimeException từ applyVoucher (voucher không hợp lệ, đã sử dụng,
                // etc.)
                // thì throw ra ngoài để ngăn chặn đặt hàng
                String errorMsg = e.getMessage();
                System.out.println("❌ Voucher validation/application failed: " + errorMsg);
                log.error("❌ Voucher validation/application failed: {}", errorMsg);
                throw e; // Re-throw để controller có thể catch và trả về error message cho frontend
            } catch (Exception e) {
                // Các exception khác (database error, etc.) → log và bỏ qua
                System.out.println("❌ Apply voucher error (exception caught): " + e.getMessage());
                e.printStackTrace();
                log.error("❌ Apply voucher error (exception caught): {}", e.getMessage(), e);
                // Không throw để cho phép đặt hàng dù voucher fail, nhưng log rõ ràng
                // giamGiaTong đã được set = 0 ở đầu, không cần làm gì thêm
            }
        } else {
            System.out.println("🎫 No voucher code provided in request");
            log.info("🎫 No voucher code provided in request");
        }
        System.out.println("💰 Final pricing: tamTinh=" + tamTinh + ", giamGiaTong=" + giamGiaTong + ", phiVanChuyen="
                + phiVanChuyen);
        System.out.println("=== VOUCHER PROCESSING END ===");
        log.info("💰 Final pricing: tamTinh={}, giamGiaTong={}, phiVanChuyen={}", tamTinh, giamGiaTong, phiVanChuyen);

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
        donHang.setVoucher(appliedVoucher);
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

        // ✅ CHỈ XÓA các chi tiết giỏ hàng đã được đặt hàng, KHÔNG xóa toàn bộ giỏ hàng
        // Giữ lại các sản phẩm chưa được chọn để user có thể đặt hàng sau
        gioHangService.xoaChiTietGioHangDaDat(gioHangItems);

        // Flush để phát hiện lỗi ràng buộc ngay tại đây (thay vì tới lúc commit)
        try {
            donHangRepository.flush();
        } catch (Exception e) {
            log.error("Flush failed in taoDonHangGuest: {}", e.getMessage(), e);
            throw e;
        }

        try {
            emailService.guiEmailXacNhanDonHang(savedDonHang);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác nhận đơn hàng {}: {}", savedDonHang.getSoDonHang(), e.getMessage());
        }

        // Log trước khi convert để debug
        log.info(
                "📦 taoDonHangGuest completed - DonHang ID: {}, tamTinh: {}, giamGiaTong: {}, phiVanChuyen: {}, tongThanhToan: {}",
                savedDonHang.getId(), savedDonHang.getTamTinh(), savedDonHang.getGiamGiaTong(),
                savedDonHang.getPhiVanChuyen(), savedDonHang.getTongThanhToan());

        DonHangResponse response = convertToDTO(savedDonHang);
        log.info("📦 Response DTO - tongThanhToan: {}, giamGiaTong: {}", response.getTongThanhToan(),
                response.getGiamGiaTong());

        return response;

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
