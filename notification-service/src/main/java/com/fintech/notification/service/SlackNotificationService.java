package com.fintech.notification.service;

import com.fintech.notification.dto.ReportEvent;
import com.fintech.notification.entity.NotificationLog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackNotificationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${notification.slack.webhook-url:}")
    private String webhookUrl;

    @Value("${notification.slack.enabled:false}")
    private boolean slackEnabled;

    public NotificationLog sendReportSlack(ReportEvent event) {
        if (!slackEnabled || webhookUrl == null || webhookUrl.isEmpty()) {
            log.info("Slack notifications are disabled or webhook URL not configured");
            return NotificationLog.builder()
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .notificationType(NotificationLog.NotificationType.REPORT_GENERATED)
                    .channel(NotificationLog.NotificationChannel.SLACK)
                    .status(NotificationLog.NotificationStatus.PENDING)
                    .build();
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", "📊 Investment Report Generated");

            Map<String, Object> attachment = new HashMap<>();
            attachment.put("color", "#36a64f");
            attachment.put("title", "Your Investment Report is Ready");
            attachment.put("text", event.getReportSummary());
            attachment.put("fields", java.util.List.of(
                    Map.of("title", "User", "value", event.getUsername(), "short", true),
                    Map.of("title", "Confidence", "value", String.format("%.2f%%", event.getConfidenceScore() * 100), "short", true),
                    Map.of("title", "Generated At", "value", event.getCreatedAt().toString(), "short", true)
            ));

            payload.put("attachments", java.util.List.of(attachment));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);

            log.info("Slack notification sent successfully for user: {}", event.getUsername());

            return NotificationLog.builder()
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .notificationType(NotificationLog.NotificationType.REPORT_GENERATED)
                    .channel(NotificationLog.NotificationChannel.SLACK)
                    .subject("Investment Report Generated")
                    .content(payload.toString())
                    .status(NotificationLog.NotificationStatus.SENT)
                    .sentAt(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to send Slack notification for user {}: {}", event.getUsername(), e.getMessage());
            return NotificationLog.builder()
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .notificationType(NotificationLog.NotificationType.REPORT_GENERATED)
                    .channel(NotificationLog.NotificationChannel.SLACK)
                    .subject("Investment Report Generated")
                    .status(NotificationLog.NotificationStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    public NotificationLog sendSimpleSlack(String message) {
        if (!slackEnabled || webhookUrl == null || webhookUrl.isEmpty()) {
            log.info("Slack notifications are disabled or webhook URL not configured");
            return NotificationLog.builder()
                    .channel(NotificationLog.NotificationChannel.SLACK)
                    .status(NotificationLog.NotificationStatus.PENDING)
                    .build();
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);

            log.info("Simple Slack notification sent successfully");

            return NotificationLog.builder()
                    .notificationType(NotificationLog.NotificationType.SYSTEM_NOTIFICATION)
                    .channel(NotificationLog.NotificationChannel.SLACK)
                    .subject("System Notification")
                    .content(message)
                    .status(NotificationLog.NotificationStatus.SENT)
                    .sentAt(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to send simple Slack notification: {}", e.getMessage());
            return NotificationLog.builder()
                    .notificationType(NotificationLog.NotificationType.SYSTEM_NOTIFICATION)
                    .channel(NotificationLog.NotificationChannel.SLACK)
                    .subject("System Notification")
                    .status(NotificationLog.NotificationStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
