package com.fintech.notification.service;

import com.fintech.notification.dto.ReportEvent;
import com.fintech.notification.entity.NotificationLog;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public NotificationLog sendReportEmail(ReportEvent event) {
        if (!emailEnabled) {
            log.info("Email notifications are disabled");
            return NotificationLog.builder()
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .userEmail(event.getUserEmail())
                    .notificationType(NotificationLog.NotificationType.REPORT_GENERATED)
                    .channel(NotificationLog.NotificationChannel.EMAIL)
                    .status(NotificationLog.NotificationStatus.PENDING)
                    .build();
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(event.getUserEmail());
            helper.setSubject("Your Investment Report is Ready");

            Context context = new Context();
            context.setVariable("username", event.getUsername());
            context.setVariable("reportSummary", event.getReportSummary());
            context.setVariable("confidenceScore", event.getConfidenceScore());
            context.setVariable("createdAt", event.getCreatedAt());

            String htmlContent = templateEngine.process("email/report-template", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email sent successfully to: {}", event.getUserEmail());

            return NotificationLog.builder()
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .userEmail(event.getUserEmail())
                    .notificationType(NotificationLog.NotificationType.REPORT_GENERATED)
                    .channel(NotificationLog.NotificationChannel.EMAIL)
                    .subject("Your Investment Report is Ready")
                    .content(htmlContent)
                    .status(NotificationLog.NotificationStatus.SENT)
                    .sentAt(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", event.getUserEmail(), e.getMessage());
            return NotificationLog.builder()
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .userEmail(event.getUserEmail())
                    .notificationType(NotificationLog.NotificationType.REPORT_GENERATED)
                    .channel(NotificationLog.NotificationChannel.EMAIL)
                    .subject("Your Investment Report is Ready")
                    .status(NotificationLog.NotificationStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    public NotificationLog sendSimpleEmail(String to, String subject, String content) {
        if (!emailEnabled) {
            log.info("Email notifications are disabled");
            return NotificationLog.builder()
                    .channel(NotificationLog.NotificationChannel.EMAIL)
                    .status(NotificationLog.NotificationStatus.PENDING)
                    .build();
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);

            log.info("Simple email sent successfully to: {}", to);

            return NotificationLog.builder()
                    .userEmail(to)
                    .notificationType(NotificationLog.NotificationType.SYSTEM_NOTIFICATION)
                    .channel(NotificationLog.NotificationChannel.EMAIL)
                    .subject(subject)
                    .content(content)
                    .status(NotificationLog.NotificationStatus.SENT)
                    .sentAt(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
            return NotificationLog.builder()
                    .userEmail(to)
                    .notificationType(NotificationLog.NotificationType.SYSTEM_NOTIFICATION)
                    .channel(NotificationLog.NotificationChannel.EMAIL)
                    .subject(subject)
                    .status(NotificationLog.NotificationStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
