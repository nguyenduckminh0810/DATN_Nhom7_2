package com.auro.auro.service;

import com.auro.auro.dto.request.VariantUpsertRequest;
import com.auro.auro.dto.response.VariantResponse;
import com.auro.auro.model.*;
import com.auro.auro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BienTheService {

    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final SanPhamRepository sanPhamRepository;
    private final MauSacRepository mauSacRepository;
    private final KichCoRepository kichCoRepository;
    private final ChatLieuRepository chatLieuRepository;
    private final HinhAnhRepository hinhAnhRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final DonHangChiTietRepository donHangChiTietRepository;

    /**
     * Lấy danh sách biến thể của sản phẩm
     */
    public List<VariantResponse> getVariantsBySanPhamId(Long sanPhamId) {
        List<BienTheSanPham> variants = bienTheSanPhamRepository.findBySanPham_Id(sanPhamId);
        return variants.stream()
                .map(this::toVariantResponse)
                .toList();
    }

    /**
     * Lấy chi tiết một biến thể
     */
    public VariantResponse getVariantById(Long id) {
        BienTheSanPham variant = bienTheSanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với id: " + id));
        return toVariantResponse(variant);
    }

    /**
     * Tạo hoặc cập nhật nhiều biến thể cho sản phẩm
     */
    @Transactional
    public List<VariantResponse> upsertVariants(Long sanPhamId, VariantUpsertRequest request) {
        // Kiểm tra sản phẩm có tồn tại không
        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + sanPhamId));

        // Lấy danh sách các biến thể hiện tại
        List<BienTheSanPham> existingVariants = bienTheSanPhamRepository.findBySanPham_Id(sanPhamId);

        // ❌ KHÔNG XÓA BIẾN THỂ CŨ - VÌ CÓ THỂ ĐANG ĐƯỢC THAM CHIẾU BỞI GIỎ HÀNG
        // Thay vào đó, sẽ cập nhật biến thể hiện có hoặc tạo mới nếu chưa có

        List<BienTheSanPham> savedVariants = new ArrayList<>();
        List<Long> processedVariantIds = new ArrayList<>();

        // Lấy hoặc tạo chất liệu nếu có
        ChatLieu chatLieu = null;
        if (request.getMaterial() != null && !request.getMaterial().isEmpty()) {
            chatLieu = chatLieuRepository.findByTen(request.getMaterial())
                    .orElseGet(() -> {
                        ChatLieu newChatLieu = new ChatLieu();
                        newChatLieu.setTen(request.getMaterial());
                        return chatLieuRepository.save(newChatLieu);
                    });
            chatLieuRepository.flush();
        }

        // Tạo hoặc cập nhật các biến thể
        for (VariantUpsertRequest.VariantItem item : request.getVariants()) {
            // Bỏ qua nếu stock = 0 hoặc null
            if (item.getStock() == null || item.getStock() <= 0) {
                continue;
            }

            // Xử lý màu sắc
            MauSac mauSac = null;
            if (item.getColor() != null && !item.getColor().isEmpty()) {
                mauSac = mauSacRepository.findByTen(item.getColor())
                        .orElseGet(() -> {
                            MauSac newMauSac = new MauSac();
                            newMauSac.setTen(item.getColor());
                            newMauSac.setMa(item.getColorHex());
                            MauSac saved = mauSacRepository.save(newMauSac);
                            mauSacRepository.flush();
                            return saved;
                        });
            }

            // Xử lý kích cỡ
            KichCo kichCo = null;
            if (item.getSize() != null && !item.getSize().isEmpty()) {
                kichCo = kichCoRepository.findByTen(item.getSize())
                        .orElseGet(() -> {
                            KichCo newKichCo = new KichCo();
                            newKichCo.setTen(item.getSize());
                            KichCo saved = kichCoRepository.save(newKichCo);
                            kichCoRepository.flush();
                            return saved;
                        });
            }

            // ✅ TÌM BIẾN THỂ HIỆN CÓ THEO: sản phẩm, màu, size, chất liệu
            Long mauSacId = (mauSac != null) ? mauSac.getId() : null;
            Long kichCoId = (kichCo != null) ? kichCo.getId() : null;
            Long chatLieuId = (chatLieu != null) ? chatLieu.getId() : null;
            
            BienTheSanPham variant = existingVariants.stream()
                    .filter(v -> {
                        Long vMauSacId = (v.getMauSac() != null) ? v.getMauSac().getId() : null;
                        Long vKichCoId = (v.getKichCo() != null) ? v.getKichCo().getId() : null;
                        Long vChatLieuId = (v.getChatLieu() != null) ? v.getChatLieu().getId() : null;
                        
                        return java.util.Objects.equals(vMauSacId, mauSacId) &&
                               java.util.Objects.equals(vKichCoId, kichCoId) &&
                               java.util.Objects.equals(vChatLieuId, chatLieuId);
                    })
                    .findFirst()
                    .orElse(null);

            boolean isNewVariant = (variant == null);
            
            if (isNewVariant) {
                // Tạo mới biến thể
                variant = new BienTheSanPham();
                variant.setSanPham(sanPham);
                
                // Generate unique SKU
                String sku = generateUniqueSku(sanPham.getId(), item.getSize(), item.getColor(), mauSac, kichCo);
                variant.setSku(sku);
            }
            
            // Cập nhật các thuộc tính
            variant.setMauSac(mauSac);
            variant.setKichCo(kichCo);
            variant.setChatLieu(chatLieu);
            variant.setSoLuongTon(item.getStock());
            
            // Giá của biến thể (có thể null, sẽ dùng giá của sản phẩm)
            if (item.getPrice() != null) {
                variant.setGia(item.getPrice());
            } else {
                variant.setGia(null);
            }

            // Sử dụng saveAndFlush để đảm bảo cập nhật được commit ngay
            // Cho phép cập nhật tồn kho ngay cả khi biến thể đang có trong giỏ hàng
            BienTheSanPham saved = bienTheSanPhamRepository.saveAndFlush(variant);
            
            processedVariantIds.add(saved.getId());

            // Xử lý hình ảnh nếu có imageUrl
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                // Xóa hình ảnh cũ của biến thể này
                hinhAnhRepository.deleteByBienThe_Id(saved.getId());
                hinhAnhRepository.flush();
                
                // Tạo hình ảnh mới
                HinhAnh hinhAnh = new HinhAnh();
                hinhAnh.setBienThe(saved);
                hinhAnh.setUrl(item.getImageUrl());
                hinhAnh.setLaDaiDien(true);
                hinhAnh.setTaoLuc(java.time.LocalDateTime.now());
                hinhAnhRepository.save(hinhAnh);
            }

            savedVariants.add(saved);
        }
        
        // ✅ XÓA CÁC BIẾN THỂ KHÔNG CÒN TRONG DANH SÁCH MỚI
        // (Chỉ xóa nếu không có trong giỏ hàng hoặc đơn hàng)
        // ⚠️ QUAN TRỌNG: Không xóa biến thể đang có trong giỏ hàng để tránh lỗi foreign key
        for (BienTheSanPham existingVariant : existingVariants) {
            if (!processedVariantIds.contains(existingVariant.getId())) {
                try {
                    // Kiểm tra xem biến thể có đang được sử dụng trong giỏ hàng hoặc đơn hàng không
                    long cartCount = gioHangChiTietRepository.countByBienThe_Id(existingVariant.getId());
                    long orderCount = donHangChiTietRepository.countByBienThe_Id(existingVariant.getId());
                    
                    if (cartCount > 0 || orderCount > 0) {
                        // Nếu có tham chiếu, chỉ cập nhật tồn kho về 0 thay vì xóa
                        existingVariant.setSoLuongTon(0);
                        bienTheSanPhamRepository.saveAndFlush(existingVariant);
                        System.out.println("⚠️ Variant ID " + existingVariant.getId() + 
                                " is in cart/order (cart: " + cartCount + ", order: " + orderCount + 
                                "), set stock to 0 instead of deleting");
                    } else {
                        // Nếu không có tham chiếu, xóa bình thường
                        // Xóa hình ảnh trước
                        hinhAnhRepository.deleteByBienThe_Id(existingVariant.getId());
                        hinhAnhRepository.flush();
                        
                        // Xóa biến thể
                        bienTheSanPhamRepository.deleteById(existingVariant.getId());
                    }
                } catch (Exception e) {
                    // Nếu không xóa được (do có tham chiếu), chỉ cập nhật tồn kho về 0
                    try {
                        existingVariant.setSoLuongTon(0);
                        bienTheSanPhamRepository.saveAndFlush(existingVariant);
                        System.out.println("⚠️ Cannot delete variant ID " + existingVariant.getId() + 
                                " (may be referenced by cart or order), set stock to 0 instead: " + e.getMessage());
                    } catch (Exception e2) {
                        System.out.println("⚠️ Cannot update variant ID " + existingVariant.getId() + 
                                ": " + e2.getMessage());
                    }
                }
            }
        }

        hinhAnhRepository.flush();

        return savedVariants.stream()
                .map(this::toVariantResponse)
                .toList();
    }

    /**
     * Cập nhật tồn kho của một biến thể
     * ✅ Cho phép cập nhật ngay cả khi biến thể đang có trong giỏ hàng
     */
    @Transactional
    public VariantResponse updateStock(Long id, Integer stock) {
        // Sử dụng findById với flush để đảm bảo lấy dữ liệu mới nhất
        BienTheSanPham variant = bienTheSanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với id: " + id));

        // Cập nhật số lượng tồn kho
        variant.setSoLuongTon(stock);
        
        // Lưu và flush ngay để đảm bảo cập nhật được commit
        BienTheSanPham updated = bienTheSanPhamRepository.saveAndFlush(variant);

        return toVariantResponse(updated);
    }

    /**
     * Xóa một biến thể
     */
    @Transactional
    public void deleteVariant(Long id) {
        if (!bienTheSanPhamRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy biến thể với id: " + id);
        }
        bienTheSanPhamRepository.deleteById(id);
    }

    /**
     * Xóa tất cả biến thể của sản phẩm
     */
    @Transactional
    public void deleteAllVariantsBySanPham(Long sanPhamId) {
        bienTheSanPhamRepository.deleteBySanPham_Id(sanPhamId);
    }

    /**
     * Chuyển đổi entity sang response DTO
     */
    private VariantResponse toVariantResponse(BienTheSanPham variant) {
        VariantResponse response = new VariantResponse();
        response.setId(variant.getId());
        response.setSku(variant.getSku());
        response.setStock(variant.getSoLuongTon());
        response.setPrice(variant.getGia());
        response.setAvailable(variant.getSoLuongTon() != null && variant.getSoLuongTon() > 0);

        if (variant.getKichCo() != null) {
            response.setSize(variant.getKichCo().getTen());
        }

        if (variant.getMauSac() != null) {
            response.setColor(variant.getMauSac().getTen());
            response.setColorHex(variant.getMauSac().getMa());
        }

        if (variant.getChatLieu() != null) {
            response.setMaterial(variant.getChatLieu().getTen());
        }

        // Load image URL from HinhAnh table
        try {
            var hinhAnh = hinhAnhRepository.findFirstByBienThe_IdOrderByThuTuAscIdAsc(variant.getId());
            if (hinhAnh.isPresent()) {
                response.setImageUrl(hinhAnh.get().getUrl());
            }
        } catch (Exception e) {
            // Ignore error, imageUrl will be null
        }

        return response;
    }

    /**
     * Sinh SKU unique với UUID ngắn để tránh trùng lặp hoàn toàn
     */
    private String generateUniqueSku(Long productId, String size, String color, MauSac mauSac, KichCo kichCo) {
        // Lấy code màu (3 ký tự đầu của tên màu)
        String colorCode = color != null && color.length() >= 3
                ? color.substring(0, 3).toUpperCase()
                : "XXX";

        // Generate random part (8 ký tự từ UUID)
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        // SKU format: SP{productId}-{size}-{colorCode}-{random}
        // VD: SP123-S-TRA-A1B2C3D4
        String sku = String.format("SP%d-%s-%s-%s", productId, size, colorCode, randomPart);

        // Đảm bảo unique (tuy UUID gần như không trùng, nhưng vẫn check)
        while (bienTheSanPhamRepository.existsBySku(sku)) {
            randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            sku = String.format("SP%d-%s-%s-%s", productId, size, colorCode, randomPart);
        }

        return sku;
    }
}
