package com.auro.auro.service;

import com.auro.auro.config.VNPayConfig;
import com.auro.auro.dto.request.ThanhToanOnlRequest;
import com.auro.auro.dto.response.ThanhToanOnlResponse;
import com.auro.auro.model.DonHang;
import com.auro.auro.repository.DonHangRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThanhToanOnlService {

    private final VNPayConfig vnPayConfig;
    private final DonHangRepository donHangRepo;
    private final EmailService emailService;

    public ThanhToanOnlResponse taoUrlThanhToan(ThanhToanOnlRequest request, String ipAddress) {
        // Tạo URL
        try{
            // validate đơn hàng
            DonHang dh = donHangRepo.findById(request.getDonHangId()).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            // Check đơn hàng thanh toán chưa
            if("paid".equals(dh.getPaymentStatus())){
                return ThanhToanOnlResponse.error("Đơn hàng đã được thanh toán");
            }

            // Tạo mã gd
            String txnRef = dh.getSoDonHang();

            // Tính tiền VNPay (tiền * 100)
            Long amount = request.getSoTien().longValue() * 100;

            // Tạo tham số cho VNPay
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", vnPayConfig.getVersion());
            vnpParams.put("vnp_Command", vnPayConfig.getCommand());
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(amount));
            vnpParams.put("vnp_CurrCode", vnPayConfig.getCurrencyCode());
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", request.getMoTa() != null ? request.getMoTa() : "Thanh toan don hang " + txnRef);
            vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
            vnpParams.put("vnp_Locale", vnPayConfig.getLocale());
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", ipAddress);

            // Tạo thời gian 15p
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            calendar.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            String queryString = vnPayConfig.hashAllFields(vnpParams);
            String secureHash = vnPayConfig.createSecureHash(vnpParams);

            // tạo url hoàn chỉnh
            String paymentUrl = vnPayConfig.getVnpUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;

            return ThanhToanOnlResponse.success(paymentUrl, txnRef);
        }catch(Exception e) {
            return ThanhToanOnlResponse.error("Lỗi tạo URL thanh toán: " + e.getMessage());
        }
    }

    // Xử lý callback
    @Transactional
    public Map<String,Object> xuLyCallback(Map<String, String> vnpParams) {
        Map<String,Object> result = new HashMap<>();

        try{
            String vnpSecureHash = vnpParams.get("vnp_SecureHash");

            Map<String, String> paramsToHash = new HashMap<>(vnpParams);
            paramsToHash.remove("vnp_SecureHash");
            paramsToHash.remove("vnp_SecureHashType");

            String calculatedHash = vnPayConfig.createSecureHash(paramsToHash);

            if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
                result.put("success", false);
                result.put("message", "Chữ ký không hợp lệ");
                return result;
            }

            String responseCode = vnpParams.get("vnp_ResponseCode");
            String transactionStatus = vnpParams.get("vnp_TransactionStatus");
            String txnRef = vnpParams.get("vnp_TxnRef");
            String amount = vnpParams.get("vnp_Amount");
            String bankCode = vnpParams.get("vnp_BankCode");
            String transactionNo = vnpParams.get("vnp_TransactionNo");

            // tìm đơn hàng
            DonHang dh = donHangRepo.findBySoDonHang(txnRef).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            // check giao dịch
            boolean isSuccess = "00".equals(responseCode) && "00".equals(transactionStatus);

            // cập nhật trạng thái đơn hàng
            if(isSuccess) {
                dh.setPaymentStatus("paid");
                dh.setTrangThai("Đã thanh toán");
                dh.setCapNhatLuc(LocalDateTime.now());
                if (dh.getTongThanhToan() == null) {
                    java.math.BigDecimal soTienThuc = new java.math.BigDecimal(vnpParams.get("vnp_Amount"))
                            .divide(new java.math.BigDecimal(100));
                    dh.setTongThanhToan(soTienThuc);
                }

                result.put("success", true);
                result.put("message", "Thanh toán thành công");
                try {
                    emailService.guiEmailThanhToanThanhCong(dh, transactionNo);
                } catch (Exception e) {
                    log.error("Lỗi khi gửi email thanh toán cho đơn hàng {}: {}", 
                              dh.getSoDonHang(), e.getMessage());
                }
            }else{
                dh.setPaymentStatus("failed");
                result.put("success", false);
                result.put("message", layThongBaoLoi(responseCode));
            }
            donHangRepo.save(dh);

            // Thêm thông tin vào result
            result.put("donHangId",dh.getId());
            result.put("soDonHang",txnRef);
            result.put("soTien",Long.parseLong(amount) / 100); // /100 để về tiền thật
            result.put("nganHang",bankCode);
            result.put("maGiaoDich",transactionNo);
            result.put("responseCode",responseCode);

            return result;

        }catch(Exception e) {
            result.put("sucess",false);
            result.put("message", "Lỗi khi xử lý kết quả thanh toán: " + e.getMessage());
            return result;
        }
    }

    // check trạng thánh thanh toán
    public Map<String, Object> kiemTraTrangThaiThanhToan(Long donHangId) {
        Map<String, Object> result = new HashMap<>();

        try {
            DonHang donHang = donHangRepo.findById(donHangId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            result.put("success", true);
            result.put("donHangId", donHang.getId());
            result.put("soDonHang", donHang.getSoDonHang());
            result.put("paymentStatus", donHang.getPaymentStatus());
            result.put("paymentMethod", donHang.getPaymentMethod());
            result.put("trangThai", donHang.getTrangThai());
            result.put("tongTien", donHang.getTongThanhToan());

            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi khi kiểm tra trạng thái: " + e.getMessage());
            return result;
        }
    }

    // lấy thông báo lỗi
    private String layThongBaoLoi(String responseCode) {
        switch (responseCode) {
            case "00":
                return "Giao dịch thành công";
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09":
                return "Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "10":
                return "Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "12":
                return "Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13":
                return "Quý khách nhập sai mật khẩu xác thực giao dịch (OTP).";
            case "24":
                return "Khách hàng hủy giao dịch";
            case "51":
                return "Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65":
                return "Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì.";
            case "79":
                return "KH nhập sai mật khẩu thanh toán quá số lần quy định.";
            default:
                return "Giao dịch không thành công";
        }
    }
}
