package com.fintech.notification.repository;

import com.fintech.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<NotificationLog> findByUsernameOrderByCreatedAtDesc(String username);
    List<NotificationLog> findByStatus(NotificationLog.NotificationStatus status);
    List<NotificationLog> findByCreatedAtAfter(LocalDateTime dateTime);
}
