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
        DonHang donHang = getDonHangById(id);

        // Cho phép xóa ở nhiều trạng thái
        List<String> deletableStatuses = List.of("Chờ xác nhận", "Đang xử lý", "Đã hủy");
        if (!deletableStatuses.contains(donHang.getTrangThai())) {
            throw new RuntimeException("Chỉ được xóa đơn hàng ở trạng thái Chờ xác nhận, Đang xử lý hoặc Đã hủy");
        }

        // Xóa chi tiết trước
        donHangChiTietRepository.deleteAll(donHangChiTietRepository.findByDonHang_Id(id));

        // Xóa đơn hàng
        donHangRepository.deleteById(id);
    }

    // Xóa mềm đơn hàng
    // Delete (Soft delete)
    @Transactional
    public void softDeleteDonHang(Long id) {
        DonHang donHang = getDonHangById(id);
        if (!"pending".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Chỉ xóa đơn hàng ở trạng thái pending");
        }
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

    // ===== HELPER METHOD: CONVERT ENTITY TO DTO =====
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
