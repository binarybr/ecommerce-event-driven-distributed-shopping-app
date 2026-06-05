package com.binarylabyrinth.adminservice.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationDto {
    private Long id;
    private String recipient;
    private String subject;
    private String message;
    private String type;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
