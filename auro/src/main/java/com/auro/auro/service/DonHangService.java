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
                    log.info("Auto-updated payment status to PAID for completed COD order #{}", donHang.getSoDonHang());
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
                log.info("Auto-updated payment status to PAID for completed COD order #{} (overriding pending status)", donHang.getSoDonHang());
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

        // ✅ XÓA MỀM ĐƠN HÀNG (chuyển trạng thái sang Đã hủy)
    // - Cho phép hủy đơn ở trạng thái: PENDING, SHIPPING, DELIVERED
    // - Không cho phép hủy đơn đã HOAN_TAT
    // - Hoàn lại số lượng khi hủy đơn ở trạng thái SHIPPING hoặc DELIVERED
    // - Lưu lý do hủy và email người hủy
    @Transactional
    public void softDeleteDonHang(Long id, String lyDoHuy, String emailNguoiHuy) {
        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));

        String currentStatus = donHang.getTrangThai();
        String normalizedStatus = normalizeTrangThaiKey(currentStatus);
        
        log.info("=== HỦY ĐƠN HÀNG (ADMIN) ===");
        log.info("Order ID: {}, Email người hủy: {}", id, emailNguoiHuy);
        log.info("Order #{} current status: {} (normalized: {})", donHang.getSoDonHang(), currentStatus, normalizedStatus);

        // Kiểm tra trạng thái có được phép hủy không
        if (OrderStatus.HOAN_TAT.equals(donHang.getTrangThai()) || 
            "HOAN_TAT".equals(normalizedStatus) ||
            "HOAN_THANH".equals(normalizedStatus) ||
            "COMPLETED".equals(normalizedStatus)) {
            throw new RuntimeException("Không thể hủy đơn hàng đã hoàn thành");
        }

        if (OrderStatus.DA_HUY.equals(donHang.getTrangThai()) || 
            "DA_HUY".equals(normalizedStatus) ||
            "CANCELLED".equals(normalizedStatus)) {
            throw new RuntimeException("Đơn hàng đã bị hủy từ trước");
        }

        // Kiểm tra xem đơn hàng có đang ở trạng thái "đang giao" hoặc "đã giao" không
        boolean isDangGiao = "DANG_GIAO".equals(normalizedStatus) ||
                             "SHIPPING".equals(normalizedStatus);
        boolean isDaGiao = "DA_GIAO".equals(normalizedStatus) ||
                          "DA_GIAO_HANG".equals(normalizedStatus) ||
                          "DELIVERED".equals(normalizedStatus);
        
        boolean needsStockRestore = isDangGiao || isDaGiao;
        
        log.info("isDangGiao: {}, isDaGiao: {}, needsStockRestore: {}", isDangGiao, isDaGiao, needsStockRestore);

        // ✅ HOÀN LẠI SỐ LƯỢNG KHI HỦY ĐƠN Ở TRẠNG THÁI "ĐANG GIAO" HOẶC "ĐÃ GIAO"
        if (needsStockRestore) {
            log.info(">>> TRIGGERING STOCK RESTORATION (Order being cancelled from SHIPPING/DELIVERED status) <<<");
            List<DonHangChiTiet> chiTietList = donHangChiTietRepository.findByDonHang_Id(id);
            log.info("Found {} order items to restore stock", chiTietList.size());

            // Hoàn lại số lượng cho tất cả sản phẩm
            for (DonHangChiTiet chiTiet : chiTietList) {
                BienTheSanPham bienThe = chiTiet.getBienThe();
                int soLuongDat = chiTiet.getSoLuong();
                int tonHienTai = bienThe.getSoLuongTon();

                // Hoàn lại tồn kho
                int soLuongMoi = tonHienTai + soLuongDat;
                bienThe.setSoLuongTon(soLuongMoi);
                bienTheSanPhamRepository.save(bienThe);

                log.info("Stock restored! Variant ID: {}, Product: {}, Old stock: {}, Restored: {}, New stock: {}",
                        bienThe.getId(),
                        bienThe.getSanPham() != null ? bienThe.getSanPham().getTen() : "N/A",
                        tonHienTai,
                        soLuongDat,
                        soLuongMoi);
            }

            log.info(">>> STOCK RESTORATION COMPLETED - All {} items processed <<<", chiTietList.size());
        } else {
            log.info("Stock restoration NOT triggered (order status: {} - not SHIPPING or DELIVERED)", currentStatus);
        }

        // ✅ KẾT HỢP EMAIL VÀO LÝ DO HỦY
        String lyDoHuyDayDu = lyDoHuy != null ? lyDoHuy.trim() : "";
        if (emailNguoiHuy != null && !emailNguoiHuy.trim().isEmpty()) {
            if (!lyDoHuyDayDu.isEmpty()) {
                lyDoHuyDayDu = String.format("Email người hủy: %s - Lý do: %s", emailNguoiHuy.trim(), lyDoHuyDayDu);
            } else {
                lyDoHuyDayDu = String.format("Email người hủy: %s", emailNguoiHuy.trim());
            }
        }

        // ✅ CHUYỂN TRẠNG THÁI SANG "ĐÃ HỦY" VÀ LƯU LÝ DO HỦY
        donHang.setTrangThai(OrderStatus.DA_HUY);
        donHang.setLyDoHuy(lyDoHuyDayDu);
        donHang.setEmailNguoiHuy(emailNguoiHuy != null ? emailNguoiHuy.trim() : null);
        donHang.setCapNhatLuc(LocalDateTime.now());
        donHangRepository.save(donHang);
        
        if (needsStockRestore) {
            log.info("✅ Order #{} cancelled successfully - Status changed from {} to DA_HUY (Stock restored). Reason: {}", 
                    donHang.getSoDonHang(), currentStatus, lyDoHuyDayDu);
        } else {
            log.info("✅ Order #{} cancelled successfully - Status changed from {} to DA_HUY (NO stock restoration needed). Reason: {}", 
                    donHang.getSoDonHang(), currentStatus, lyDoHuyDayDu);
        }
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

        
        String diaChiSnapshot = buildDiaChiSnapshot(dh);
        dto.setDiaChiGiaoSnapshot(diaChiSnapshot);
        dto.setGhiChu(dh.getGhiChu());
        dto.setTaoLuc(dh.getTaoLuc());
        dto.setCapNhatLuc(dh.getCapNhatLuc());

        dto.setPaymentStatus(dh.getPaymentStatus() != null ? dh.getPaymentStatus() : "pending");
        dto.setPaymentMethod(dh.getPaymentMethod() != null ? dh.getPaymentMethod() : "COD");
        dto.setLyDoHuy(dh.getLyDoHuy());
        dto.setEmailNguoiHuy(dh.getEmailNguoiHuy());

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

    /**
     * Build chuỗi địa chỉ snapshot từ các field snapshot trong DonHang.
     * Format: "Tên - SĐT - Địa chỉ, Phường/Xã, Quận/Huyện, Tỉnh/Thành".
     * Bỏ qua field null/rỗng, không in ra "null".
     */
    private String buildDiaChiSnapshot(DonHang donHang) {
        if (donHang == null) {
            return "";
        }

        String ten = safeTrim(donHang.getTenNguoiNhan());
        String sdt = safeTrim(donHang.getSdtNguoiNhan());
        String diaChi = safeTrim(donHang.getDiaChiChiTiet());
        String phuongXa = safeTrim(donHang.getPhuongXa());
        String quanHuyen = safeTrim(donHang.getQuanHuyen());
        String tinhThanh = safeTrim(donHang.getTinhThanh());

        List<String> headParts = new ArrayList<>();
        if (!ten.isEmpty()) headParts.add(ten);
        if (!sdt.isEmpty()) headParts.add(sdt);
        if (!diaChi.isEmpty()) headParts.add(diaChi);

        List<String> tailParts = new ArrayList<>();
        if (!phuongXa.isEmpty()) tailParts.add(phuongXa);
        if (!quanHuyen.isEmpty()) tailParts.add(quanHuyen);
        if (!tinhThanh.isEmpty()) tailParts.add(tinhThanh);

        String head = String.join(" - ", headParts);
        String tail = String.join(", ", tailParts);

        if (head.isEmpty() && tail.isEmpty()) {
            return "";
        }
        if (head.isEmpty()) {
            return tail;
        }
        if (tail.isEmpty()) {
            return head;
        }
        return head + ", " + tail;
    }

    /**
     * Build chuỗi thuộc tính hiển thị từ snapshot màu/size.
     * Format: "Màu: {mauSac}, Size: {kichCo}".
     */
    private String buildThuocTinhSnapshot(DonHangChiTiet ct) {
        if (ct == null) {
            return "";
        }
        String mau = safeTrim(ct.getMauSac());
        String size = safeTrim(ct.getKichCo());

        StringBuilder sb = new StringBuilder();
        if (!mau.isEmpty()) {
            sb.append("Màu: ").append(mau);
        }
        if (!size.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Size: ").append(size);
        }

        return sb.toString();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private DonHangChiTietResponse mapChiTietToResponse(DonHangChiTiet ct) {
        DonHangChiTietResponse ctDTO = new DonHangChiTietResponse();
        ctDTO.setId(ct.getId());
        ctDTO.setTenSanPham(ct.getTenHienThi());
        ctDTO.setDonGia(ct.getDonGia());
        ctDTO.setSoLuong(ct.getSoLuong());
        ctDTO.setThanhTien(ct.getThanhTien());

        // Thuộc tính hiển thị cho FE (snapshot từ màu/size, fallback về chuỗi cũ nếu cần)
        String thuocTinhSnapshot = buildThuocTinhSnapshot(ct);
        ctDTO.setThuocTinh(thuocTinhSnapshot);

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
                    // Normalize loại voucher thành uppercase để đảm bảo consistency
                    String loai = voucherGiamGia.getLoai() != null ? voucherGiamGia.getLoai().trim().toUpperCase() : "";
                    if ("FREESHIP".equals(loai)) {
                        log.warn("Voucher freeship phải áp dụng riêng - skip");
                        voucherGiamGia = null;
                    } else if (!"GIAM_PHAN_TRAM".equals(loai) && !"PHAN_TRAM".equals(loai) &&
                            !"GIAM_SO_TIEN".equals(loai) && !"SO_TIEN".equals(loai) &&
                            !"FIXED".equals(loai) && !"FIXED_AMOUNT".equals(loai) &&
                            !"AMOUNT".equals(loai) && !"GIAM_TIEN".equals(loai) &&
                            !"PERCENT".equals(loai) && !"SO_TIEN".equals(loai)) {
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
                            log.info("✅ Voucher applied successfully - maVoucher: {}, giamGiaTong: {}, tamTinh: {}", 
                                    voucherGiamGia.getMa(), giamGiaTong, tamTinh);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Lỗi xử lý voucher giảm giá: {}", e.getMessage(), e);
                voucherGiamGia = null;
            }
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

        // Log để debug
        log.info("📦 Creating order - tamTinh: {}, giamGiaTong: {}, phiVanChuyen: {}, voucher: {}", 
                tamTinh, giamGiaTong, phiVanChuyen, voucherGiamGia != null ? voucherGiamGia.getMa() : "null");

        // --- SNAPSHOT địa chỉ & người nhận (User chọn từ sổ địa chỉ) ---
        donHang.setTenNguoiNhan(diaChi.getHoTen());
        donHang.setSdtNguoiNhan(diaChi.getSoDienThoai());
        donHang.setEmailNguoiNhan(khachHang.getEmail());
        donHang.setDiaChiChiTiet(diaChi.getDiaChi1());
        donHang.setPhuongXa(diaChi.getPhuongXa());
        donHang.setQuanHuyen(diaChi.getQuanHuyen());
        donHang.setTinhThanh(diaChi.getTinhThanh());
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
            DonHangChiTiet chiTiet = new DonHangChiTiet();
            chiTiet.setDonHang(savedDonHang);
            chiTiet.setBienThe(bienThe);
            chiTiet.setTenHienThi(tenHienThi);

            // --- SNAPSHOT thuộc tính sản phẩm ---
            if (bienThe.getMauSac() != null) {
                chiTiet.setMauSac(bienThe.getMauSac().getTen());
            }
            if (bienThe.getKichCo() != null) {
                chiTiet.setKichCo(bienThe.getKichCo().getTen());
            }
            if (bienThe.getChatLieu() != null) {
                chiTiet.setChatLieu(bienThe.getChatLieu().getTen());
            }
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
    public DonHangResponse huyDonHang(Long donHangId, Long khachHangId, String lyDoHuy, String emailNguoiHuy) {
        log.info("=== HỦY ĐƠN HÀNG ===");
        log.info("Order ID: {}, Customer ID: {}, Email người hủy: {}", donHangId, khachHangId, emailNguoiHuy);
        
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

        // ✅ KẾT HỢP EMAIL VÀO LÝ DO HỦY
        String lyDoHuyDayDu = lyDoHuy != null ? lyDoHuy.trim() : "";
        if (emailNguoiHuy != null && !emailNguoiHuy.trim().isEmpty()) {
            if (!lyDoHuyDayDu.isEmpty()) {
                lyDoHuyDayDu = String.format("Email người hủy: %s - Lý do: %s", emailNguoiHuy.trim(), lyDoHuyDayDu);
            } else {
                lyDoHuyDayDu = String.format("Email người hủy: %s", emailNguoiHuy.trim());
            }
        }

        // ✅ CHỈ CHUYỂN TRẠNG THÁI SANG "ĐÃ HỦY" VÀ LƯU LÝ DO HỦY - KHÔNG TRỪ TỒN KHO, KHÔNG LÀM GÌ KHÁC
        donHang.setTrangThai(OrderStatus.DA_HUY);
        donHang.setLyDoHuy(lyDoHuyDayDu);
        donHang.setEmailNguoiHuy(emailNguoiHuy != null ? emailNguoiHuy.trim() : null);
        donHang.setCapNhatLuc(LocalDateTime.now());
        DonHang savedDonHang = donHangRepository.save(donHang);

        log.info("✅ Order #{} cancelled successfully - Status changed from {} to DA_HUY (NO stock deduction). Reason: {}", 
                savedDonHang.getSoDonHang(), currentStatus, lyDoHuyDayDu);
        
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

        // Tìm theo địa chỉ snapshot mới
        String diaChiSnapshot = buildDiaChiSnapshot(order);
        if (!diaChiSnapshot.isEmpty() && diaChiSnapshot.toLowerCase(Locale.ROOT).contains(lower)) {
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

                String thuocTinhSnapshot = buildThuocTinhSnapshot(chiTiet);
                if (!thuocTinhSnapshot.isEmpty()
                        && thuocTinhSnapshot.toLowerCase(Locale.ROOT).contains(lower)) {
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
        // Xác định KhachHang trước để biết lấy giỏ hàng từ đâu
        KhachHang khachHang;
        GioHang gioHang;

        // Nếu user đã login, dùng KhachHang của họ và lấy giỏ hàng theo khachHangId
        if (authenticatedKhachHangId != null) {
            khachHang = khachHangRepository.findById(authenticatedKhachHangId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            // Lấy giỏ hàng theo khachHangId cho user đã đăng nhập
            gioHang = gioHangService.layGioHangCuaKhach(authenticatedKhachHangId);
        } else {
            // Tạo KhachHang GUEST mới và lấy giỏ hàng theo sessionId
            khachHang = new KhachHang();
            khachHang.setTaiKhoan(null);
            khachHang.setHoTen(request.getHoTen());
            khachHang.setEmail(request.getEmail());
            khachHang.setSoDienThoai(request.getSoDienThoai());
            khachHang.setKieu("GUEST");
            khachHang.setTaoLuc(java.time.LocalDateTime.now()); // Set thời gian tạo
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
        } else {
            // Fallback: lấy toàn bộ giỏ hàng (tương thích ngược)
            gioHangItems = allItems;
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
                } else {
                    phiVanChuyen = BigDecimal.valueOf(30000);
                }
            } else {
                phiVanChuyen = BigDecimal.valueOf(30000);
            }
        } catch (Exception e) {
            log.warn("Error calculating shipping fee from GHN, using default: {}", e.getMessage());
            phiVanChuyen = BigDecimal.valueOf(30000);
        }

        // Áp dụng voucher nếu có
        Voucher appliedVoucher = null;
        BigDecimal giamGiaTong = BigDecimal.ZERO;
        
        // Kiểm tra maVoucher có giá trị không
        String maVoucher = request.getMaVoucher();
        if (maVoucher != null) {
            maVoucher = maVoucher.trim();
        }
        
        if (maVoucher != null && !maVoucher.isEmpty()) {
            String code = maVoucher;
            try {
                // Tìm voucher theo mã, thử không phân biệt hoa/thường nếu không tìm thấy
                Optional<Voucher> voucherOpt = voucherRepository.findByMa(code);
                if (voucherOpt.isEmpty()) {
                    voucherOpt = voucherRepository.findByMa(code.toUpperCase());
                }
                if (voucherOpt.isEmpty()) {
                    voucherOpt = voucherRepository.findByMa(code.toLowerCase());
                }
                Voucher voucher = voucherOpt.orElse(null);
                if (voucher != null) {
                    // Lấy mã voucher từ DB (đảm bảo đúng case)
                    String voucherMaFromDB = voucher.getMa();
                    // Normalize loại voucher thành uppercase để đảm bảo consistency
                    String loai = voucher.getLoai() != null ? voucher.getLoai().trim().toUpperCase() : "";
                    if ("FREESHIP".equalsIgnoreCase(loai)) {
                        // Freeship: miễn phí ship
                        phiVanChuyen = BigDecimal.ZERO;
                        appliedVoucher = voucher;
                    } else {
                        // Giảm giá: sử dụng service để tính đúng giamGiaTong
                        // QUAN TRỌNG: Truyền mã voucher từ DB (voucherMaFromDB) thay vì mã từ request
                        // để đảm bảo tìm được voucher trong validateVoucher()
                        VoucherApplicationResult result = voucherService.applyVoucher(voucherMaFromDB,
                                authenticatedKhachHangId, tamTinh, phiVanChuyen);
                        if (!result.isSuccess()) {
                            log.warn("Voucher apply failed: {}", result.getMessage());
                            // Không throw exception trong transaction, chỉ log và set giamGiaTong = 0
                            // Voucher không hợp lệ → không áp dụng giảm giá, nhưng vẫn cho phép đặt hàng
                            giamGiaTong = BigDecimal.ZERO;
                            appliedVoucher = null;
                        } else {
                            // Đảm bảo giamGia không null
                            BigDecimal calculatedGiamGia = result.getGiamGia();
                            if (calculatedGiamGia == null) {
                                log.warn("⚠️ Voucher apply success but giamGia is null, setting to ZERO");
                                calculatedGiamGia = BigDecimal.ZERO;
                            }
                            giamGiaTong = calculatedGiamGia;
                            appliedVoucher = result.getVoucher();
                        }
                    }
                }
            } catch (Exception e) {
                // Các exception khác (database error, etc.) → log và bỏ qua
                // Không throw để không làm rollback transaction
                log.error("Error applying voucher: {}", e.getMessage(), e);
                // giamGiaTong đã được set = 0 ở đầu, không cần làm gì thêm
                // Cho phép đặt hàng tiếp tục dù voucher fail
            }
        }

        // tạo đơn hàng guest
        DonHang donHang = new DonHang();
        donHang.setSoDonHang("DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        donHang.setKhachHang(khachHang);
        donHang.setTrangThai("Chờ xác nhận");
        donHang.setTamTinh(tamTinh);
        donHang.setGiamGiaTong(giamGiaTong);
        donHang.setPhiVanChuyen(phiVanChuyen);
        donHang.setVoucher(appliedVoucher);

        donHang.setTenNguoiNhan(request.getHoTen());
        donHang.setSdtNguoiNhan(request.getSoDienThoai());
        donHang.setEmailNguoiNhan(request.getEmail());
        donHang.setDiaChiChiTiet(request.getDiaChi());
        donHang.setPhuongXa(request.getPhuongXa());
        donHang.setQuanHuyen(request.getQuanHuyen());
        donHang.setTinhThanh(request.getTinhThanh());
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

            DonHangChiTiet ct = new DonHangChiTiet();
            ct.setDonHang(savedDonHang);
            ct.setBienThe(bienThe);
            ct.setTenHienThi(tenHienThi);

            // --- SNAPSHOT thuộc tính sản phẩm ---
            if (bienThe.getMauSac() != null) {
                ct.setMauSac(bienThe.getMauSac().getTen());
            }
            if (bienThe.getKichCo() != null) {
                ct.setKichCo(bienThe.getKichCo().getTen());
            }
            if (bienThe.getChatLieu() != null) {
                ct.setChatLieu(bienThe.getChatLieu().getTen());
            }
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

        DonHangResponse response = convertToDTO(savedDonHang);

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
