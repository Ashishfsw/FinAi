package com.fintech.notification.kafka;

import com.fintech.notification.dto.ReportEvent;
import com.fintech.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReportEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "report-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleReportEvent(ReportEvent event) {
        log.info("Received report event for user: {}, reportId: {}", event.getUsername(), event.getReportId());

        try {
            if ("REPORT_COMPLETED".equals(event.getEventType())) {
                notificationService.sendReportNotification(event);
            } else {
                log.warn("Unhandled event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing report event for user {}: {}", event.getUsername(), e.getMessage());
        }
    }
}
