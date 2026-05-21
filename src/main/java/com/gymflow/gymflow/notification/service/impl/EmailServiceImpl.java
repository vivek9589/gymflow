package com.gymflow.gymflow.notification.service.impl;

import com.gymflow.gymflow.notification.service.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;
    private String emailTemplateContent;

    @Value("${app.frontend-url}")
    private String frontendBaseUrl;

    /**
     * Load the dedicated HTML template file once into memory when the application starts up.
     */
    @PostConstruct
    public void init() {
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/forgot-password-template.html");
            this.emailTemplateContent = resource.getContentAsString(StandardCharsets.UTF_8);
            log.info("Successfully loaded dedicated forgot password email template.");
        } catch (Exception e) {
            log.error("Failed to read dedicated email template file from classpath!", e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            if (emailTemplateContent == null) {
                log.error("Cannot send email. Template was not initialized correctly.");
                return;
            }

            // Replace with your actual frontend verification domain routing
            String resetLink = frontendBaseUrl + "/reset-password?token=" + token;
            log.info("Generating security reset link: {}", resetLink);



            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Reset Your GymFlow Password");

            // Replace the template token dynamically
            String htmlContent = emailTemplateContent.replace("${resetLink}", resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Password reset email successfully sent asynchronously to email={}", toEmail);

        } catch (Exception e) {
            log.error("Failed to dispatch password reset email to={}", toEmail, e);
        }
    }
}