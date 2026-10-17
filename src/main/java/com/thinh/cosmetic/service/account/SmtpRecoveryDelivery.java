package com.thinh.cosmetic.service.account;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Profile("!demo")
public class SmtpRecoveryDelivery implements RecoveryDelivery {
    private final ObjectProvider<JavaMailSender> sender;
    private final String from;
    private final String baseUrl;

    public SmtpRecoveryDelivery(ObjectProvider<JavaMailSender> sender,
            @Value("${lunea.recovery.sender:no-reply@lunea.test}") String from,
            @Value("${lunea.recovery.public-base-url:http://localhost:8080}") String baseUrl) {
        this.sender = sender;
        this.from = from;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    @Override
    public void send(String email, String rawToken, Instant expiresAt) {
        JavaMailSender mailSender = sender.getIfAvailable();
        if (mailSender == null) throw new RecoveryDeliveryException();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("LUNEA — Đặt lại mật khẩu");
        message.setText("Mở liên kết để đặt lại mật khẩu LUNEA:\n" + baseUrl + "/#reset=" + rawToken
                + "\nLiên kết chỉ dùng một lần và hết hạn lúc " + expiresAt
                + ".\nNếu bạn không yêu cầu, hãy bỏ qua email này.");
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            // Provider exceptions may contain addresses or secrets. Never propagate their text.
            throw new RecoveryDeliveryException();
        }
    }
}
