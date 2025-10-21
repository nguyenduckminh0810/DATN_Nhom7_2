package com.auro.auro.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.auro.auro.dto.response.DonHangChiTietResponse;
import com.auro.auro.dto.response.DonHangResponse;
import com.auro.auro.model.DonHang;
import com.auro.auro.model.DonHangChiTiet;
import com.auro.auro.repository.DonHangChiTietRepository;
import com.auro.auro.repository.DonHangRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DonHangService {

    private final DonHangRepository donHangRepository;
    private final DonHangChiTietRepository donHangChiTietRepository;

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
}
