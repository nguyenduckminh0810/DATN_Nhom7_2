package com.auro.auro.service;

import com.auro.auro.dto.request.VariantUpsertRequest;
import com.auro.auro.dto.response.InventoryItemResponse;
import com.auro.auro.dto.response.VariantResponse;
import com.auro.auro.model.*;
import com.auro.auro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

            BienTheSanPham saved = bienTheSanPhamRepository.save(variant);
            bienTheSanPhamRepository.flush();
            
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
        for (BienTheSanPham existingVariant : existingVariants) {
            if (!processedVariantIds.contains(existingVariant.getId())) {
                try {
                    // Xóa hình ảnh trước
                    hinhAnhRepository.deleteByBienThe_Id(existingVariant.getId());
                    hinhAnhRepository.flush();
                    
                    // Xóa biến thể
                    bienTheSanPhamRepository.deleteById(existingVariant.getId());
                } catch (Exception e) {
                    // Nếu không xóa được (do có tham chiếu), bỏ qua
                    System.out.println("⚠️ Cannot delete variant ID " + existingVariant.getId() + 
                            " (may be referenced by cart or order): " + e.getMessage());
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
     */
    @Transactional
    public VariantResponse updateStock(Long id, Integer stock) {
        BienTheSanPham variant = bienTheSanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với id: " + id));

        variant.setSoLuongTon(stock);
        BienTheSanPham updated = bienTheSanPhamRepository.save(variant);

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
     * Lấy tất cả biến thể với thông tin sản phẩm (cho trang inventory)
     */
    public List<InventoryItemResponse> getAllInventoryItems() {
        List<BienTheSanPham> variants = bienTheSanPhamRepository.findAll();
        return variants.stream()
                .map(this::toInventoryItemResponse)
                .toList();
    }

    /**
     * Chuyển đổi entity sang InventoryItemResponse
     */
    private InventoryItemResponse toInventoryItemResponse(BienTheSanPham variant) {
        InventoryItemResponse response = new InventoryItemResponse();
        
        // Variant info
        response.setId(variant.getId());
        response.setVariantSku(variant.getSku());
        response.setStock(variant.getSoLuongTon());
        response.setPrice(variant.getGia());
        
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
        
        // Load variant image
        try {
            var hinhAnh = hinhAnhRepository.findFirstByBienThe_IdOrderByThuTuAscIdAsc(variant.getId());
            if (hinhAnh.isPresent()) {
                response.setImageUrl(hinhAnh.get().getUrl());
            }
        } catch (Exception e) {
            // Ignore error
        }
        
        // Product info
        if (variant.getSanPham() != null) {
            SanPham product = variant.getSanPham();
            response.setProductId(product.getId());
            response.setProductName(product.getTen());
            response.setProductPrice(product.getGia());
            
            if (product.getDanhMuc() != null) {
                response.setCategoryName(product.getDanhMuc().getTen());
            }
            
            // Load product image
            try {
                var productImage = hinhAnhRepository.findFirstBySanPham_IdOrderByThuTuAscIdAsc(product.getId());
                if (productImage.isPresent()) {
                    response.setProductImage(productImage.get().getUrl());
                }
            } catch (Exception e) {
                // Ignore error
            }
        }
        
        // Use product image if variant image is not available
        if (response.getImageUrl() == null && response.getProductImage() != null) {
            response.setImageUrl(response.getProductImage());
        }
        
        // Last updated - use product's capNhatLuc or variant's creation time
        if (variant.getSanPham() != null && variant.getSanPham().getCapNhatLuc() != null) {
            response.setLastUpdated(variant.getSanPham().getCapNhatLuc());
        } else {
            response.setLastUpdated(LocalDateTime.now());
        }
        
        return response;
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
