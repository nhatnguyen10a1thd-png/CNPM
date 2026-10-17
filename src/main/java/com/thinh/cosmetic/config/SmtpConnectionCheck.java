package com.thinh.cosmetic.config;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/** Checks SMTP/TLS/auth without sending a message; used only by an explicit CLI profile. */
@Component
@Profile("smtp-check & !demo")
@RequiredArgsConstructor
public class SmtpConnectionCheck implements ApplicationRunner {
    private final JavaMailSender mailSender;

    @Override
    public void run(ApplicationArguments args) {
        if (!(mailSender instanceof JavaMailSenderImpl smtp)) {
            throw new IllegalStateException("SMTP sender is unavailable.");
        }
        try {
            smtp.testConnection();
        } catch (MessagingException exception) {
            // Provider error messages can contain addresses or credentials.
            throw new IllegalStateException("SMTP connection or authentication failed.");
        }
        System.out.println("SMTP connection and authentication succeeded; no message was sent.");
    }
}
