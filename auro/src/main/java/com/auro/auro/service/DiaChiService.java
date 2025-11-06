package com.auro.auro.service;

import com.auro.auro.dto.request.DiaChiRequest;
import com.auro.auro.dto.response.DiaChiResponse;
import com.auro.auro.exception.BadRequestException;
import com.auro.auro.exception.ResourceNotFoundException;
import com.auro.auro.model.DiaChi;
import com.auro.auro.model.KhachHang;
import com.auro.auro.repository.DiaChiRepository;
import com.auro.auro.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaChiService {
    private final DiaChiRepository diaChiRepository;
    private final KhachHangRepository khachHangRepository;

    /**
     * Lấy tất cả địa chỉ của khách hàng
     */
    public List<DiaChiResponse> getDiaChiByKhachHang(Long khachHangId) {
        List<DiaChi> diaChiList = diaChiRepository.findByKhachHang_IdOrderByMacDinhDescIdDesc(khachHangId);
        return diaChiList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy địa chỉ mặc định
     */
    public DiaChiResponse getDiaChiMacDinh(Long khachHangId) {
        DiaChi diaChi = diaChiRepository.findFirstByKhachHang_IdAndMacDinhTrue(khachHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ mặc định"));
        return convertToResponse(diaChi);
    }

    /**
     * Lấy chi tiết một địa chỉ
     */
    public DiaChiResponse getDiaChiById(Long khachHangId, Long diaChiId) {
        DiaChi diaChi = diaChiRepository.findById(diaChiId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        if (!diaChi.getKhachHang().getId().equals(khachHangId)) {
            throw new BadRequestException("Địa chỉ không thuộc về khách hàng này");
        }

        return convertToResponse(diaChi);
    }

    /**
     * Thêm địa chỉ mới
     */
    @Transactional
    public DiaChiResponse themDiaChi(Long khachHangId, DiaChiRequest request) {
        KhachHang khachHang = khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng"));

        // Nếu đây là địa chỉ mặc định, bỏ mặc định của các địa chỉ khác
        if (request.getMacDinh() != null && request.getMacDinh()) {
            boMacDinhCacDiaChiKhac(khachHangId);
        }

        // Nếu đây là địa chỉ đầu tiên, tự động set làm mặc định
        List<DiaChi> existingAddresses = diaChiRepository.findByKhachHang_IdOrderByMacDinhDescIdDesc(khachHangId);
        if (existingAddresses.isEmpty()) {
            request.setMacDinh(true);
        }

        DiaChi diaChi = new DiaChi();
        diaChi.setKhachHang(khachHang);
        diaChi.setHoTen(request.getHoTen());
        diaChi.setSoDienThoai(request.getSoDienThoai());
        diaChi.setDiaChi1(request.getDiaChi1());
        diaChi.setPhuongXa(request.getPhuongXa());
        diaChi.setQuanHuyen(request.getQuanHuyen());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setMacDinh(request.getMacDinh() != null ? request.getMacDinh() : false);

        DiaChi saved = diaChiRepository.save(diaChi);
        return convertToResponse(saved);
    }

    /**
     * Cập nhật địa chỉ
     */
    @Transactional
    public DiaChiResponse capNhatDiaChi(Long khachHangId, Long diaChiId, DiaChiRequest request) {
        DiaChi diaChi = diaChiRepository.findById(diaChiId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        if (!diaChi.getKhachHang().getId().equals(khachHangId)) {
            throw new BadRequestException("Địa chỉ không thuộc về khách hàng này");
        }

        // Nếu set làm mặc định, bỏ mặc định của các địa chỉ khác
        if (request.getMacDinh() != null && request.getMacDinh() && !diaChi.getMacDinh()) {
            boMacDinhCacDiaChiKhac(khachHangId);
        }

        diaChi.setHoTen(request.getHoTen());
        diaChi.setSoDienThoai(request.getSoDienThoai());
        diaChi.setDiaChi1(request.getDiaChi1());
        diaChi.setPhuongXa(request.getPhuongXa());
        diaChi.setQuanHuyen(request.getQuanHuyen());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setMacDinh(request.getMacDinh() != null ? request.getMacDinh() : false);

        DiaChi updated = diaChiRepository.save(diaChi);
        return convertToResponse(updated);
    }

    /**
     * Đặt địa chỉ làm mặc định
     */
    @Transactional
    public DiaChiResponse datMacDinh(Long khachHangId, Long diaChiId) {
        DiaChi diaChi = diaChiRepository.findById(diaChiId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        if (!diaChi.getKhachHang().getId().equals(khachHangId)) {
            throw new BadRequestException("Địa chỉ không thuộc về khách hàng này");
        }

        // Bỏ mặc định của các địa chỉ khác
        boMacDinhCacDiaChiKhac(khachHangId);

        // Set mặc định cho địa chỉ này
        diaChi.setMacDinh(true);
        DiaChi updated = diaChiRepository.save(diaChi);

        return convertToResponse(updated);
    }

    /**
     * Xóa địa chỉ
     */
    @Transactional
    public void xoaDiaChi(Long khachHangId, Long diaChiId) {
        DiaChi diaChi = diaChiRepository.findById(diaChiId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        if (!diaChi.getKhachHang().getId().equals(khachHangId)) {
            throw new BadRequestException("Địa chỉ không thuộc về khách hàng này");
        }

        // Không cho xóa địa chỉ mặc định nếu còn địa chỉ khác
        if (diaChi.getMacDinh()) {
            List<DiaChi> otherAddresses = diaChiRepository.findByKhachHang_IdOrderByMacDinhDescIdDesc(khachHangId)
                    .stream()
                    .filter(dc -> !dc.getId().equals(diaChiId))
                    .collect(Collectors.toList());

            if (!otherAddresses.isEmpty()) {
                throw new BadRequestException("Vui lòng đặt địa chỉ khác làm mặc định trước khi xóa");
            }
        }

        diaChiRepository.delete(diaChi);
    }

    /**
     * Helper: Bỏ mặc định của các địa chỉ khác
     */
    private void boMacDinhCacDiaChiKhac(Long khachHangId) {
        List<DiaChi> diaChiList = diaChiRepository.findByKhachHang_IdOrderByMacDinhDescIdDesc(khachHangId);
        diaChiList.forEach(dc -> {
            if (dc.getMacDinh()) {
                dc.setMacDinh(false);
                diaChiRepository.save(dc);
            }
        });
    }

    /**
     * Helper: Convert entity to response
     */
    private DiaChiResponse convertToResponse(DiaChi diaChi) {
        String diaChiDayDu = String.format("%s, %s, %s, %s",
                diaChi.getDiaChi1(),
                diaChi.getPhuongXa(),
                diaChi.getQuanHuyen(),
                diaChi.getTinhThanh());

        return DiaChiResponse.builder()
                .id(diaChi.getId())
                .hoTen(diaChi.getHoTen())
                .soDienThoai(diaChi.getSoDienThoai())
                .diaChi1(diaChi.getDiaChi1())
                .phuongXa(diaChi.getPhuongXa())
                .quanHuyen(diaChi.getQuanHuyen())
                .tinhThanh(diaChi.getTinhThanh())
                .macDinh(diaChi.getMacDinh())
                .diaChiDayDu(diaChiDayDu)
                .build();
    }
}
