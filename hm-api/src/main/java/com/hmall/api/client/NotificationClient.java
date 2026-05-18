package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient("notify-service")
public interface NotificationClient {

    @GetMapping("/notifications/active")
    List<Map<String, Object>> getActiveNotifications();
}
