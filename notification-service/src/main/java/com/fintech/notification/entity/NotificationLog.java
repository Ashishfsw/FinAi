package com.fintech.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String username;

    private String userEmail;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private String errorMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        REPORT_GENERATED,
        REBALANCING_RECOMMENDATION,
        MARKET_ALERT,
        SYSTEM_NOTIFICATION
    }

    public enum NotificationChannel {
        EMAIL,
        SLACK,
        BOTH
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED
    }
}
