package com.binarylabyrinth.adminservice.client;

import com.binarylabyrinth.adminservice.dto.external.NotificationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @GetMapping("/api/notifications")
    List<NotificationDto> listAllNotifications();
}
