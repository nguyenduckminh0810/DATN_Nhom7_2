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
import java.util.Optional;

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

        // Lấy danh sách các biến thể hiện tại để xóa hình ảnh trước
        List<BienTheSanPham> existingVariants = bienTheSanPhamRepository.findBySanPham_Id(sanPhamId);

        // Xóa tất cả hình ảnh của các biến thể trước
        for (BienTheSanPham variant : existingVariants) {
            hinhAnhRepository.deleteByBienThe_Id(variant.getId());
        }
        hinhAnhRepository.flush();

        // Sau đó mới xóa các biến thể
        bienTheSanPhamRepository.deleteBySanPham_Id(sanPhamId);
        bienTheSanPhamRepository.flush();

        List<BienTheSanPham> savedVariants = new ArrayList<>();

        // Lấy hoặc tạo chất liệu nếu có
        ChatLieu chatLieu = null;
        if (request.getMaterial() != null && !request.getMaterial().isEmpty()) {
            chatLieu = chatLieuRepository.findByTen(request.getMaterial())
                    .orElseGet(() -> {
                        ChatLieu newChatLieu = new ChatLieu();
                        newChatLieu.setTen(request.getMaterial());
                        return chatLieuRepository.save(newChatLieu);
                    });
        }

        // Tạo các biến thể mới
        for (VariantUpsertRequest.VariantItem item : request.getVariants()) {
            // Bỏ qua nếu stock = 0 hoặc null
            if (item.getStock() == null || item.getStock() <= 0) {
                continue;
            }

            BienTheSanPham variant = new BienTheSanPham();
            variant.setSanPham(sanPham);

            // Xử lý màu sắc
            if (item.getColor() != null && !item.getColor().isEmpty()) {
                MauSac mauSac = mauSacRepository.findByTen(item.getColor())
                        .orElseGet(() -> {
                            MauSac newMauSac = new MauSac();
                            newMauSac.setTen(item.getColor());
                            newMauSac.setMa(item.getColorHex());
                            return mauSacRepository.save(newMauSac);
                        });
                variant.setMauSac(mauSac);
            }

            // Xử lý kích cỡ
            if (item.getSize() != null && !item.getSize().isEmpty()) {
                KichCo kichCo = kichCoRepository.findByTen(item.getSize())
                        .orElseGet(() -> {
                            KichCo newKichCo = new KichCo();
                            newKichCo.setTen(item.getSize());
                            return kichCoRepository.save(newKichCo);
                        });
                variant.setKichCo(kichCo);
            }

            // Gán chất liệu
            variant.setChatLieu(chatLieu);

            // Gán SKU (nếu không có thì tự sinh)
            String sku = item.getSku();
            if (sku == null || sku.isEmpty()) {
                sku = generateSku("SP" + sanPham.getId(), item.getSize(), item.getColor());
            }
            variant.setSku(sku);

            // Gán số lượng tồn
            variant.setSoLuongTon(item.getStock());

            // Giá của biến thể (có thể null, sẽ dùng giá của sản phẩm)
            if (item.getPrice() != null) {
                variant.setGia(item.getPrice());
            } else {
                variant.setGia(null);
            }

            BienTheSanPham saved = bienTheSanPhamRepository.save(variant);

            // Xử lý hình ảnh nếu có imageUrl
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                // Xóa hình ảnh cũ của biến thể này (nếu có)
                hinhAnhRepository.deleteByBienThe_Id(saved.getId());

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
     * Sinh SKU tự động
     */
    private String generateSku(String baseSku, String size, String color) {
        String colorCode = color != null && color.length() >= 3
                ? color.substring(0, 3).toUpperCase()
                : "XXX";
        return String.format("%s-%s-%s", baseSku, size, colorCode);
    }
}
