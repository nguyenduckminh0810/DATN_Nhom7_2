package com.auro.auro.service;

import com.auro.auro.model.DonHang;
import com.auro.auro.model.DonHangChiTiet;
import com.auro.auro.model.KhachHang;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String emailFrom;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    // gửi email xác nhận đơn hàng
    public void guiEmailXacNhanDonHang(DonHang dh) {
        try{
        String emailNguoiNhan = layEmailNguoiNhan(dh.getKhachHang());

        if(emailNguoiNhan == null) {
            log.warn("Không có email để gửi xác nhận cho đơn hàng: {}", dh.getSoDonHang());
            return;
        }

        String subject = "Xác nhận đơn hàng #" + dh.getSoDonHang();
        String content = taoNoiDungEmailXacNhan(dh);

        guiEmail(emailNguoiNhan,subject,content);

        log.info("Đã gửi email xác nhận đơn hàng {} đến {}",dh.getSoDonHang(), emailNguoiNhan);

        
    }catch(Exception e) {
        log.error("Lỗi khi gửi email xác nhận đơn hàng {}: {}", dh.getSoDonHang(), e.getMessage());

    }
}
    // gửi email thông báo thanh toán thành công
    public void guiEmailThanhToanThanhCong(DonHang dh, String maGiaoDich) {
        try{
            String emailNguoiNhan = layEmailNguoiNhan(dh.getKhachHang());
            
            if (emailNguoiNhan == null) {
                log.warn("Không có email để gửi thông báo thanh toán cho đơn hàng: {}", 
                         dh.getSoDonHang());
                return;
            }

            String subject = "Thanh toán thành công - Đơn hàng #" + dh.getSoDonHang();
            String content = taoNoiDungEmailThanhToan(dh, maGiaoDich);
            guiEmail(emailNguoiNhan, subject, content);
            
            log.info("Đã gửi email thanh toán thành công cho đơn hàng {} đến {}", 
                     dh.getSoDonHang(), emailNguoiNhan);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email thanh toán cho đơn hàng {}: {}", 
                      dh.getSoDonHang(), e.getMessage());
    }
}

// lấy email từ người nhận
private String layEmailNguoiNhan(KhachHang khachHang) {
    if (khachHang == null) {
        return null;
    }

    String email = khachHang.getEmail();
    
    if (email == null || email.trim().isEmpty()) {
        if (khachHang.getTaiKhoan() != null) {
            email = khachHang.getTaiKhoan().getEmail();
        }
    }

    if (email != null && !email.trim().isEmpty() && email.contains("@")) {
        return email.trim();
    }

    return null;
}
    // gửi email
    private void guiEmail(String to, String subject, String htmlContent) throws MessagingException {
        if (!emailEnabled) {
            log.info("Email disabled - Skip gửi email đến: {}", to);
            log.debug("Subject: {}", subject);
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(emailFrom);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    // nội dung email xác nhận đơn hàng
    private String taoNoiDungEmailXacNhan(DonHang donHang) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head><meta charset='UTF-8'></head>");
        html.append("<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        
        // Header
        html.append("<div style='background-color: #2c3e50; color: white; padding: 20px; text-align: center;'>");
        html.append("<h1 style='margin: 0;'>AURO STORE</h1>");
        html.append("</div>");
        
        // Content
        html.append("<div style='padding: 20px;'>");
        html.append("<h2 style='color: #2c3e50;'>Xác nhận đơn hàng</h2>");
        html.append("<p>Xin chào <strong>").append(donHang.getKhachHang().getHoTen()).append("</strong>,</p>");
        html.append("<p>Cảm ơn bạn đã đặt hàng tại AURO Store. Đơn hàng của bạn đã được tiếp nhận.</p>");
        
        // Thông tin đơn hàng
        html.append("<div style='background-color: #f4f4f4; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
        html.append("<h3 style='margin-top: 0; color: #2c3e50;'>Thông tin đơn hàng</h3>");
        html.append("<p><strong>Mã đơn hàng:</strong> ").append(donHang.getSoDonHang()).append("</p>");
        html.append("<p><strong>Ngày đặt:</strong> ").append(formatDate(donHang.getTaoLuc())).append("</p>");
        html.append("<p><strong>Trạng thái:</strong> ").append(donHang.getTrangThai()).append("</p>");
        html.append("<p><strong>Địa chỉ giao hàng:</strong><br>").append(donHang.getDiaChiGiao()).append("</p>");
        html.append("</div>");
        
        // Chi tiết đơn hàng
        if (donHang.getChiTietList() != null && !donHang.getChiTietList().isEmpty()) {
            html.append("<h3 style='color: #2c3e50;'>Chi tiết đơn hàng</h3>");
            html.append("<table style='width: 100%; border-collapse: collapse;'>");
            html.append("<thead>");
            html.append("<tr style='background-color: #2c3e50; color: white;'>");
            html.append("<th style='padding: 10px; text-align: left;'>Sản phẩm</th>");
            html.append("<th style='padding: 10px; text-align: center;'>Số lượng</th>");
            html.append("<th style='padding: 10px; text-align: right;'>Đơn giá</th>");
            html.append("<th style='padding: 10px; text-align: right;'>Thành tiền</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            for (DonHangChiTiet ct : donHang.getChiTietList()) {
                html.append("<tr style='border-bottom: 1px solid #ddd;'>");
                html.append("<td style='padding: 10px;'>").append(ct.getTenHienThi()).append("</td>");
                html.append("<td style='padding: 10px; text-align: center;'>").append(ct.getSoLuong()).append("</td>");
                html.append("<td style='padding: 10px; text-align: right;'>").append(formatMoney(ct.getDonGia())).append("</td>");
                html.append("<td style='padding: 10px; text-align: right;'>").append(formatMoney(ct.getThanhTien())).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
        }
        
        // Tổng tiền
        html.append("<div style='margin-top: 20px; padding: 15px; background-color: #f4f4f4; border-radius: 5px;'>");
        html.append("<p style='margin: 5px 0;'><strong>Tạm tính:</strong> <span style='float: right;'>").append(formatMoney(donHang.getTamTinh())).append("</span></p>");
        html.append("<p style='margin: 5px 0;'><strong>Giảm giá:</strong> <span style='float: right; color: red;'>-").append(formatMoney(donHang.getGiamGiaTong())).append("</span></p>");
        html.append("<p style='margin: 5px 0;'><strong>Phí vận chuyển:</strong> <span style='float: right;'>").append(formatMoney(donHang.getPhiVanChuyen())).append("</span></p>");
        html.append("<hr style='margin: 10px 0;'>");
        html.append("<p style='margin: 5px 0; font-size: 18px;'><strong>Tổng thanh toán:</strong> <span style='float: right; color: #e74c3c;'>").append(formatMoney(donHang.getTongThanhToan())).append("</span></p>");
        html.append("</div>");
        
        // Footer
        html.append("<p style='margin-top: 30px;'>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>");
        html.append("<p>Trân trọng,<br><strong>AURO Store Team</strong></p>");
        html.append("</div>");
        
        // Footer
        html.append("<div style='background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #666;'>");
        html.append("<p>© 2025 AURO Store. All rights reserved.</p>");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    // email báo thanh toán thành công
    private String taoNoiDungEmailThanhToan(DonHang donHang, String maGiaoDich) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head><meta charset='UTF-8'></head>");
        html.append("<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        
        html.append("<div style='background-color: #27ae60; color: white; padding: 20px; text-align: center;'>");
        html.append("<h1 style='margin: 0;'>✓ THANH TOÁN THÀNH CÔNG</h1>");
        html.append("</div>");
        
        html.append("<div style='padding: 20px;'>");
        html.append("<p>Xin chào <strong>").append(donHang.getKhachHang().getHoTen()).append("</strong>,</p>");
        html.append("<p>Đơn hàng <strong>#").append(donHang.getSoDonHang()).append("</strong> của bạn đã được thanh toán thành công.</p>");
        
        html.append("<div style='background-color: #d4edda; padding: 15px; border-radius: 5px; border-left: 4px solid #28a745; margin: 20px 0;'>");
        html.append("<p style='margin: 5px 0;'><strong>Số tiền:</strong> ").append(formatMoney(donHang.getTongThanhToan())).append("</p>");
        html.append("<p style='margin: 5px 0;'><strong>Mã giao dịch:</strong> ").append(maGiaoDich).append("</p>");
        html.append("<p style='margin: 5px 0;'><strong>Phương thức:</strong> ").append(donHang.getPaymentMethod()).append("</p>");
        html.append("</div>");
        
        html.append("<p>Đơn hàng của bạn sẽ được xử lý và giao đến địa chỉ:</p>");
        html.append("<p style='padding: 10px; background-color: #f4f4f4; border-radius: 5px;'>").append(donHang.getDiaChiGiao()).append("</p>");
        
        html.append("<p style='margin-top: 30px;'>Cảm ơn bạn đã tin tưởng AURO Store!</p>");
        html.append("<p>Trân trọng,<br><strong>AURO Store Team</strong></p>");
        html.append("</div>");
        
        html.append("<div style='background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #666;'>");
        html.append("<p>© 2025 AURO Store. All rights reserved.</p>");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    // Helper methods
    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0đ";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
    

    private String formatDate(java.time.LocalDateTime date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }

    // Gửi email đặt lại mật khẩu
    public void guiEmailDatLaiMatKhau(String email, String resetToken, String resetUrl) {
        try {
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                log.warn("Email không hợp lệ để gửi link đặt lại mật khẩu: {}", email);
                return;
            }

            String subject = "Đặt lại mật khẩu - AURO Store";
            String content = taoNoiDungEmailDatLaiMatKhau(resetToken, resetUrl);
            guiEmail(email, subject, content);

            log.info("Đã gửi email đặt lại mật khẩu đến {}", email);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đặt lại mật khẩu đến {}: {}", email, e.getMessage());
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu", e);
        }
    }

    // Nội dung email đặt lại mật khẩu
    private String taoNoiDungEmailDatLaiMatKhau(String resetToken, String resetUrl) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head><meta charset='UTF-8'></head>");
        html.append("<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");

        // Header
        html.append("<div style='background-color: #2c3e50; color: white; padding: 20px; text-align: center;'>");
        html.append("<h1 style='margin: 0;'>AURO STORE</h1>");
        html.append("</div>");

        // Content
        html.append("<div style='padding: 20px;'>");
        html.append("<h2 style='color: #2c3e50;'>Đặt lại mật khẩu</h2>");
        html.append("<p>Xin chào,</p>");
        html.append("<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của bạn tại AURO Store.</p>");
        html.append("<p>Vui lòng click vào nút bên dưới để đặt lại mật khẩu:</p>");

        // Button
        html.append("<div style='text-align: center; margin: 30px 0;'>");
        html.append("<a href='").append(resetUrl).append("' ");
        html.append("style='display: inline-block; padding: 15px 30px; background-color: #2c3e50; ");
        html.append("color: white; text-decoration: none; border-radius: 5px; font-weight: bold;'>");
        html.append("Đặt lại mật khẩu</a>");
        html.append("</div>");

        // Alternative link
        html.append("<p style='color: #666; font-size: 14px;'>Nếu nút không hoạt động, bạn có thể copy và dán link sau vào trình duyệt:</p>");
        html.append("<p style='color: #2c3e50; word-break: break-all;'>").append(resetUrl).append("</p>");

        // Security notice
        html.append("<div style='background-color: #fff3cd; padding: 15px; border-radius: 5px; border-left: 4px solid #ffc107; margin: 20px 0;'>");
        html.append("<p style='margin: 5px 0;'><strong>Lưu ý bảo mật:</strong></p>");
        html.append("<ul style='margin: 5px 0; padding-left: 20px;'>");
        html.append("<li>Link này chỉ có hiệu lực trong 24 giờ</li>");
        html.append("<li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>");
        html.append("<li>Không chia sẻ link này với bất kỳ ai</li>");
        html.append("</ul>");
        html.append("</div>");

        // Footer
        html.append("<p style='margin-top: 30px;'>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>");
        html.append("<p>Trân trọng,<br><strong>AURO Store Team</strong></p>");
        html.append("</div>");

        // Footer
        html.append("<div style='background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #666;'>");
        html.append("<p>© 2025 AURO Store. All rights reserved.</p>");
        html.append("</div>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

}
