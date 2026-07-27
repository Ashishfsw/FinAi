package com.fintech.notification.controller;

import com.fintech.notification.entity.NotificationLog;
import com.fintech.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationLog>> getUserNotifications(@PathVariable Long userId) {
        List<NotificationLog> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<List<NotificationLog>> getUserNotificationsByUsername(@PathVariable String username) {
        List<NotificationLog> notifications = notificationService.getUserNotifications(username);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/failed")
    public ResponseEntity<List<NotificationLog>> getFailedNotifications() {
        List<NotificationLog> notifications = notificationService.getFailedNotifications();
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationLog> sendNotification(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestParam(defaultValue = "SYSTEM_NOTIFICATION") String type,
            @RequestParam(defaultValue = "EMAIL") String channel) {
        
        NotificationLog.NotificationType notificationType = NotificationLog.NotificationType.valueOf(type);
        NotificationLog.NotificationChannel notificationChannel = NotificationLog.NotificationChannel.valueOf(channel);
        
        NotificationLog notification = notificationService.sendNotification(to, subject, content, notificationType, notificationChannel);
        return ResponseEntity.ok(notification);
    }
}
