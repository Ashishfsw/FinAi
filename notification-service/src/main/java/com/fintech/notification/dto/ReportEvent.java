package com.fintech.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEvent {

    private Long reportId;
    private Long userId;
    private String username;
    private String userEmail;
    private String reportSummary;
    private String reportContent;
    private String status;
    private Double confidenceScore;
    private LocalDateTime createdAt;
    private String eventType;
}
