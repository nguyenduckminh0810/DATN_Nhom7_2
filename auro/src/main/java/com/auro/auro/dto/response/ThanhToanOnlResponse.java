package com.auro.auro.dto.response;

public class ThanhToanOnlResponse {

    private String status; // success, error
    private String message;
    private String paymentUrl;
    private String donHangId;

    public ThanhToanOnlResponse() {}
    
    public ThanhToanOnlResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public ThanhToanOnlResponse(String status, String message, String paymentUrl, String donHangId) {
        this.status = status;
        this.message = message;
        this.paymentUrl = paymentUrl;
        this.donHangId = donHangId;
    }
    
    public static ThanhToanOnlResponse success(String paymentUrl, String donHangId) {
        return new ThanhToanOnlResponse("success", "Tạo URL thanh toán thành công", paymentUrl, donHangId);
    }
    
    public static ThanhToanOnlResponse error(String message) {
        return new ThanhToanOnlResponse("error", message, null, null);
    }

    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPaymentUrl() {
        return paymentUrl;
    }
    
    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
    
    public String getDonHangId() {
        return donHangId;
    }
    
    public void setDonHangId(String donHangId) {
        this.donHangId = donHangId;
    }
}
