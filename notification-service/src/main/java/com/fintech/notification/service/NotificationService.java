package com.fintech.notification.service;

import com.fintech.notification.dto.ReportEvent;
import com.fintech.notification.entity.NotificationLog;
import com.fintech.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final EmailNotificationService emailNotificationService;
    private final SlackNotificationService slackNotificationService;
    private final NotificationLogRepository notificationLogRepository;

    @Transactional
    public NotificationLog sendReportNotification(ReportEvent event) {
        log.info("Processing report notification for user: {}", event.getUsername());

        NotificationLog emailLog = emailNotificationService.sendReportEmail(event);
        notificationLogRepository.save(emailLog);

        NotificationLog slackLog = slackNotificationService.sendReportSlack(event);
        notificationLogRepository.save(slackLog);

        if (emailLog.getStatus() == NotificationLog.NotificationStatus.SENT || 
            slackLog.getStatus() == NotificationLog.NotificationStatus.SENT) {
            log.info("Report notification sent successfully for user: {}", event.getUsername());
        } else {
            log.warn("Report notification failed for user: {}", event.getUsername());
        }

        return emailLog.getStatus() == NotificationLog.NotificationStatus.SENT ? emailLog : slackLog;
    }

    @Transactional
    public NotificationLog sendNotification(String to, String subject, String content, 
                                            NotificationLog.NotificationType type,
                                            NotificationLog.NotificationChannel channel) {
        
        NotificationLog log;
        
        if (channel == NotificationLog.NotificationChannel.EMAIL || channel == NotificationLog.NotificationChannel.BOTH) {
            log = emailNotificationService.sendSimpleEmail(to, subject, content);
            notificationLogRepository.save(log);
        }
        
        if (channel == NotificationLog.NotificationChannel.SLACK || channel == NotificationLog.NotificationChannel.BOTH) {
            log = slackNotificationService.sendSimpleSlack(content);
            notificationLogRepository.save(log);
        }

        return log;
    }

    public List<NotificationLog> getUserNotifications(Long userId) {
        return notificationLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<NotificationLog> getUserNotifications(String username) {
        return notificationLogRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    public List<NotificationLog> getFailedNotifications() {
        return notificationLogRepository.findByStatus(NotificationLog.NotificationStatus.FAILED);
    }
}
