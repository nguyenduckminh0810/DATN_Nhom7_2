package com.auro.auro.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        return donHangRepository.findById(id).orElse(null);
    }

    // Lấy chi tiết đơn hàng theo ID đơn hàng
    public List<DonHangChiTiet> getChiTietByDonHangId(Long donHangId) {
        return donHangChiTietRepository.findByDonHang_Id(donHangId);
    }

    // Cập nhật trạng thái đơn hàng
    @Transactional
    public DonHang updateDonHang(Long id, DonHang updatedDonHang) {
        DonHang donHang = getDonHangById(id);
        if (!"pending".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Chỉ sửa đơn hàng ở trạng thái pending");
        }
        donHang.setTrangThai(updatedDonHang.getTrangThai());
        donHang.setDiaChiGiao(updatedDonHang.getDiaChiGiao());
        donHang.setGhiChu(updatedDonHang.getGhiChu());
        donHang.setCapNhatLuc(LocalDateTime.now());
        return donHangRepository.save(donHang);
    }

    // Xóa đơn hàng
    public void deleteDonHang(Long id) {
        if (!donHangRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy đơn hàng với id = " + id);
        }
        // Xóa chi tiết trước
        List<DonHangChiTiet> chiTietList = donHangChiTietRepository.findByDonHang_Id(id);
        donHangChiTietRepository.deleteAll(chiTietList);
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
        donHang.setTrangThai("cancelled");
        donHang.setCapNhatLuc(LocalDateTime.now());
        donHangRepository.save(donHang);
    }

    //
    @Transactional
    public List<DonHangResponse> getAllDonHangDTO() {
        List<DonHang> donHangList = donHangRepository.findAllWithChiTiet();

        return donHangList.stream().map(dh -> {
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
            return dto;
        }).collect(Collectors.toList());
    }

    // Lấy toàn bộ đơn hàng có phân trang
    public Page<DonHangResponse> getDonHangPhanTrang(Pageable pageable) {
        Page<DonHang> donHangPage = donHangRepository.findAll(pageable);

        return donHangPage.map(dh -> {
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
            return dto;
        });
    }
}
