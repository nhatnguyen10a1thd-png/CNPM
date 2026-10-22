package com.thinh.cosmetic.service.notification.impl;

import com.thinh.cosmetic.service.notification.EmailService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from-email:onboarding@resend.dev}")
    private String resendFromEmail;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.mail.from-name:LUNEA Cosmetics}")
    private String fromName;

    @Value("${app.mail.from-email:no-reply@lunea.vn}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    @Async
    @Override
    public void sendOtpEmail(String toEmail, String otpCode, int expiryMinutes) {
        if (toEmail == null || toEmail.isBlank() || !toEmail.contains("@")) {
            log.info(">>> [EMAIL SERVICE] Địa chỉ email '{}' không hợp lệ. Bỏ qua gửi email.", toEmail);
            return;
        }

        String subject = "LUNEA Cosmetics - Mã xác thực đặt lại mật khẩu: " + otpCode;
        String htmlBody = buildOtpHtml(otpCode, expiryMinutes);

        // 1. Ưu tiên gửi qua Resend REST API (HTTPS port 443 - không bao giờ bị Cloud/Render chặn cổng)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            boolean sent = sendViaResend(toEmail, subject, htmlBody);
            if (sent) return;
        }

        // 2. Fallback sang SMTP nếu có cấu hình đầy đủ tài khoản & mật khẩu
        if (mailSender != null && mailUsername != null && !mailUsername.isBlank() && mailPassword != null && !mailPassword.isBlank()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                String senderAddress = mailUsername.contains("@") ? mailUsername : fromEmail;
                helper.setFrom(new InternetAddress(senderAddress, fromName));
                helper.setTo(toEmail.trim());
                helper.setSubject(subject);
                helper.setText(htmlBody, true);

                mailSender.send(mimeMessage);
                log.info(">>> [EMAIL SERVICE - SMTP] Đã gửi thành công email chứa mã OTP đến: {}", toEmail);
                return;
            } catch (Exception ex) {
                log.error(">>> [EMAIL SERVICE - SMTP] Gửi email đến {} thất bại: {}. (Hãy kiểm tra lại App Password hoặc kết nối SMTP)", toEmail, ex.getMessage());
            }
        } else if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn(">>> [EMAIL SERVICE] Chưa cấu hình RESEND_API_KEY hoặc tài khoản SMTP hợp lệ.");
        }

        // 3. Fallback: In mã OTP nội bộ ra log để dễ dàng kiểm thử
        log.info(">>> [EMAIL SERVICE - FALLBACK] Mã OTP cho {} là: {}", toEmail, otpCode);
    }

    @Async
    @Override
    public void sendRegistrationOtpEmail(String toEmail, String otpCode, int expiryMinutes) {
        if (toEmail == null || toEmail.isBlank() || !toEmail.contains("@")) {
            log.info(">>> [EMAIL SERVICE] Địa chỉ email '{}' không hợp lệ. Bỏ qua gửi email.", toEmail);
            return;
        }

        String subject = "LUNEA Cosmetics - Mã kích hoạt đăng ký tài khoản: " + otpCode;
        String htmlBody = buildRegistrationOtpHtml(otpCode, expiryMinutes);

        // 1. Ưu tiên gửi qua Resend REST API (HTTPS port 443)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            boolean sent = sendViaResend(toEmail, subject, htmlBody);
            if (sent) return;
        }

        // 2. Fallback sang SMTP
        if (mailSender != null && mailUsername != null && !mailUsername.isBlank() && mailPassword != null && !mailPassword.isBlank()) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                String senderAddress = mailUsername.contains("@") ? mailUsername : fromEmail;
                helper.setFrom(new InternetAddress(senderAddress, fromName));
                helper.setTo(toEmail.trim());
                helper.setSubject(subject);
                helper.setText(htmlBody, true);

                mailSender.send(mimeMessage);
                log.info(">>> [EMAIL SERVICE - SMTP] Đã gửi thành công email kích hoạt đến: {}", toEmail);
                return;
            } catch (Exception ex) {
                log.error(">>> [EMAIL SERVICE - SMTP] Gửi email đăng ký đến {} thất bại: {}. (Hãy kiểm tra lại App Password hoặc kết nối SMTP)", toEmail, ex.getMessage());
            }
        } else if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn(">>> [EMAIL SERVICE] Chưa cấu hình RESEND_API_KEY hoặc tài khoản SMTP hợp lệ.");
        }

        // 3. Fallback: In mã OTP ra log
        log.info(">>> [EMAIL SERVICE - FALLBACK] Mã OTP đăng ký cho {} là: {}", toEmail, otpCode);
    }

    private boolean sendViaResend(String toEmail, String subject, String htmlContent) {
        try {
            String sender = (fromName != null && !fromName.isBlank())
                    ? fromName + " <" + resendFromEmail.trim() + ">"
                    : resendFromEmail.trim();

            String jsonBody = "{"
                    + "\"from\":\"" + escapeJson(sender) + "\","
                    + "\"to\":[\"" + escapeJson(toEmail.trim()) + "\"],"
                    + "\"subject\":\"" + escapeJson(subject) + "\","
                    + "\"html\":\"" + escapeJson(htmlContent) + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info(">>> [EMAIL SERVICE - RESEND API] Đã gửi thành công email tới {}. Response: {}", toEmail, response.body());
                return true;
            } else {
                log.error(">>> [EMAIL SERVICE - RESEND API] Gửi email tới {} thất bại (HTTP {}): {}", toEmail, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception ex) {
            log.error(">>> [EMAIL SERVICE - RESEND API] Lỗi khi kết nối Resend API: {}", ex.getMessage());
            return false;
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private String buildRegistrationOtpHtml(String otpCode, int expiryMinutes) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Kích hoạt tài khoản LUNEA</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #0f0f11; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #e6e6e6;">
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #0f0f11; padding: 40px 10px;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="100%%" style="max-width: 520px; background-color: #1a1a1e; border: 1px solid #2d2d35; border-radius: 8px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5);" cellspacing="0" cellpadding="0">
                                <!-- Header -->
                                <tr>
                                    <td align="center" style="padding: 36px 30px 20px; background: linear-gradient(180deg, #24242a 0%%, #1a1a1e 100%%); border-bottom: 1px solid rgba(212, 175, 55, 0.2);">
                                        <h1 style="margin: 0; font-family: Georgia, serif; font-size: 28px; font-weight: 400; letter-spacing: 4px; color: #d4af37;">L U N E A</h1>
                                        <p style="margin: 6px 0 0; font-size: 10px; letter-spacing: 3px; color: #a0a0a8; text-transform: uppercase;">Haute Parfumerie & Cosmetics</p>
                                    </td>
                                </tr>
                                <!-- Body -->
                                <tr>
                                    <td style="padding: 32px 30px 24px;">
                                        <h2 style="margin: 0 0 16px; font-family: Georgia, serif; font-size: 20px; color: #ffffff; font-weight: 500;">Chào mừng bạn đến với LUNEA</h2>
                                        <p style="margin: 0 0 18px; font-size: 14px; line-height: 1.6; color: #bbbbbf;">
                                            Kính chào quý khách,<br>
                                            Cảm ơn bạn đã lựa chọn trở thành thành viên của chuỗi cửa hàng mỹ phẩm cao cấp LUNEA. Để kích hoạt và hoàn tất quá trình tạo tài khoản, vui lòng sử dụng mã OTP xác minh bên dưới:
                                        </p>
                                        <!-- OTP Box -->
                                        <div style="margin: 28px 0; text-align: center;">
                                            <div style="display: inline-block; background-color: rgba(212, 175, 55, 0.08); border: 1px dashed #d4af37; border-radius: 6px; padding: 16px 32px;">
                                                <span style="font-family: 'Courier New', Courier, monospace; font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #d4af37;">%s</span>
                                            </div>
                                            <p style="margin: 10px 0 0; font-size: 12px; color: #8e8e93;">Mã có hiệu lực trong <b>%d phút</b></p>
                                        </div>
                                        <p style="margin: 0 0 12px; font-size: 13px; line-height: 1.6; color: #a0a0a5;">
                                            ⚠️ <b>Lưu ý:</b> Vui lòng không cung cấp mã OTP này cho bất kỳ ai để bảo vệ tính bảo mật cho tài khoản của bạn.
                                        </p>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td align="center" style="padding: 20px 30px; background-color: #141416; border-top: 1px solid #24242a;">
                                        <p style="margin: 0; font-size: 11px; color: #66666e; line-height: 1.5;">
                                            © 2026 LUNEA Store Chain Management. Thư này được gửi tự động, vui lòng không trả lời.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(otpCode, expiryMinutes);
    }

    private String buildOtpHtml(String otpCode, int expiryMinutes) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Khôi phục mật khẩu LUNEA</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #0f0f11; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #e6e6e6;">
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #0f0f11; padding: 40px 10px;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="100%%" style="max-width: 520px; background-color: #1a1a1e; border: 1px solid #2d2d35; border-radius: 8px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5);" cellspacing="0" cellpadding="0">
                                <!-- Header -->
                                <tr>
                                    <td align="center" style="padding: 36px 30px 20px; background: linear-gradient(180deg, #24242a 0%%, #1a1a1e 100%%); border-bottom: 1px solid rgba(212, 175, 55, 0.2);">
                                        <h1 style="margin: 0; font-family: Georgia, serif; font-size: 28px; font-weight: 400; letter-spacing: 4px; color: #d4af37;">L U N E A</h1>
                                        <p style="margin: 6px 0 0; font-size: 10px; letter-spacing: 3px; color: #a0a0a8; text-transform: uppercase;">Haute Parfumerie & Cosmetics</p>
                                    </td>
                                </tr>
                                <!-- Body -->
                                <tr>
                                    <td style="padding: 32px 30px 24px;">
                                        <h2 style="margin: 0 0 16px; font-family: Georgia, serif; font-size: 20px; color: #ffffff; font-weight: 500;">Xác thực khôi phục mật khẩu</h2>
                                        <p style="margin: 0 0 18px; font-size: 14px; line-height: 1.6; color: #bbbbbf;">
                                            Kính chào quý khách,<br>
                                            Hệ thống vừa nhận được yêu cầu thiết lập lại mật khẩu cho tài khoản LUNEA liên kết với email này. Vui lòng sử dụng mã xác thực bảo mật (OTP) dưới đây để tiếp tục:
                                        </p>
                                        <!-- OTP Box -->
                                        <div style="margin: 28px 0; text-align: center;">
                                            <div style="display: inline-block; background-color: rgba(212, 175, 55, 0.08); border: 1px dashed #d4af37; border-radius: 6px; padding: 16px 32px;">
                                                <span style="font-family: 'Courier New', Courier, monospace; font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #d4af37;">%s</span>
                                            </div>
                                            <p style="margin: 10px 0 0; font-size: 12px; color: #8e8e93;">Mã có hiệu lực trong <b>%d phút</b></p>
                                        </div>
                                        <p style="margin: 0 0 12px; font-size: 13px; line-height: 1.6; color: #a0a0a5;">
                                            ⚠️ <b>Lưu ý quan trọng:</b> Không chia sẻ mã OTP này cho bất kỳ ai, kể cả nhân viên hỗ trợ của LUNEA.
                                        </p>
                                        <p style="margin: 0; font-size: 13px; line-height: 1.6; color: #8e8e93;">
                                            Nếu quý khách không yêu cầu khôi phục mật khẩu, vui lòng bỏ qua thư này hoặc đổi mật khẩu tài khoản ngay để bảo vệ thông tin.
                                        </p>
                                    </td>
                                </tr>
                                <!-- Footer -->
                                <tr>
                                    <td align="center" style="padding: 20px 30px; background-color: #141416; border-top: 1px solid #24242a;">
                                        <p style="margin: 0; font-size: 11px; color: #66666e; line-height: 1.5;">
                                            © 2026 LUNEA Store Chain Management. Thư này được gửi tự động, vui lòng không trả lời.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(otpCode, expiryMinutes);
    }
}
