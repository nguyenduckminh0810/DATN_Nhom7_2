package com.auro.auro.dto.response;

public class VNPayReturnDTO {

    private String vnp_TmnCode;
    private String vnp_Amount;
    private String vnp_BankCode;
    private String vnp_BankTranNo;
    private String vnp_CardType;
    private String vnp_PayDate;
    private String vnp_OrderInfo;
    private String vnp_TransactionNo;
    private String vnp_ResponseCode;
    private String vnp_TransactionStatus;
    private String vnp_TxnRef;
    private String vnp_SecureHashType;
    private String vnp_SecureHash;

    public VNPayReturnDTO() {}

    public String getVnp_TmnCode() {
        return vnp_TmnCode;
    }
    
    public void setVnp_TmnCode(String vnp_TmnCode) {
        this.vnp_TmnCode = vnp_TmnCode;
    }
    
    public String getVnp_Amount() {
        return vnp_Amount;
    }
    
    public void setVnp_Amount(String vnp_Amount) {
        this.vnp_Amount = vnp_Amount;
    }
    
    public String getVnp_BankCode() {
        return vnp_BankCode;
    }
    
    public void setVnp_BankCode(String vnp_BankCode) {
        this.vnp_BankCode = vnp_BankCode;
    }
    
    public String getVnp_BankTranNo() {
        return vnp_BankTranNo;
    }
    
    public void setVnp_BankTranNo(String vnp_BankTranNo) {
        this.vnp_BankTranNo = vnp_BankTranNo;
    }
    
    public String getVnp_CardType() {
        return vnp_CardType;
    }
    
    public void setVnp_CardType(String vnp_CardType) {
        this.vnp_CardType = vnp_CardType;
    }
    
    public String getVnp_PayDate() {
        return vnp_PayDate;
    }
    
    public void setVnp_PayDate(String vnp_PayDate) {
        this.vnp_PayDate = vnp_PayDate;
    }
    
    public String getVnp_OrderInfo() {
        return vnp_OrderInfo;
    }
    
    public void setVnp_OrderInfo(String vnp_OrderInfo) {
        this.vnp_OrderInfo = vnp_OrderInfo;
    }
    
    public String getVnp_TransactionNo() {
        return vnp_TransactionNo;
    }
    
    public void setVnp_TransactionNo(String vnp_TransactionNo) {
        this.vnp_TransactionNo = vnp_TransactionNo;
    }
    
    public String getVnp_ResponseCode() {
        return vnp_ResponseCode;
    }
    
    public void setVnp_ResponseCode(String vnp_ResponseCode) {
        this.vnp_ResponseCode = vnp_ResponseCode;
    }
    
    public String getVnp_TransactionStatus() {
        return vnp_TransactionStatus;
    }
    
    public void setVnp_TransactionStatus(String vnp_TransactionStatus) {
        this.vnp_TransactionStatus = vnp_TransactionStatus;
    }
    
    public String getVnp_TxnRef() {
        return vnp_TxnRef;
    }
    
    public void setVnp_TxnRef(String vnp_TxnRef) {
        this.vnp_TxnRef = vnp_TxnRef;
    }
    
    public String getVnp_SecureHashType() {
        return vnp_SecureHashType;
    }
    
    public void setVnp_SecureHashType(String vnp_SecureHashType) {
        this.vnp_SecureHashType = vnp_SecureHashType;
    }
    
    public String getVnp_SecureHash() {
        return vnp_SecureHash;
    }
    
    public void setVnp_SecureHash(String vnp_SecureHash) {
        this.vnp_SecureHash = vnp_SecureHash;
    }
    
    public boolean isSuccess() {
        return "00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus);
    }
    
    public String getResponseMessage() {
        if ("00".equals(vnp_ResponseCode)) {
            return "Giao dịch thành công";
        } else if ("07".equals(vnp_ResponseCode)) {
            return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
        } else if ("09".equals(vnp_ResponseCode)) {
            return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
        } else if ("10".equals(vnp_ResponseCode)) {
            return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
        } else if ("11".equals(vnp_ResponseCode)) {
            return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.";
        } else if ("12".equals(vnp_ResponseCode)) {
            return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.";
        } else if ("13".equals(vnp_ResponseCode)) {
            return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch.";
        } else if ("24".equals(vnp_ResponseCode)) {
            return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
        } else if ("51".equals(vnp_ResponseCode)) {
            return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
        } else if ("65".equals(vnp_ResponseCode)) {
            return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
        } else if ("75".equals(vnp_ResponseCode)) {
            return "Ngân hàng thanh toán đang bảo trì.";
        } else if ("79".equals(vnp_ResponseCode)) {
            return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch";
        } else {
            return "Giao dịch không thành công";
        }
    }

}
