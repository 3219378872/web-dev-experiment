package com.hmall.notify.service.impl;

import com.hmall.notify.domain.po.Notification;
import com.hmall.notify.mapper.NotificationMapper;
import com.hmall.notify.service.INotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class NotificationServiceImplTest {

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Test
    @DisplayName("getActiveNotifications: 只返回状态=1的通知，按发布时间降序")
    void getActiveNotifications_returnsActiveOrderedByTime() {
        Notification n1 = notif("公告A", 1, LocalDateTime.now().minusHours(2));
        Notification n2 = notif("公告B", 1, LocalDateTime.now());
        Notification n3 = notif("公告C", 0, LocalDateTime.now());  // 禁用
        notificationMapper.insert(n1);
        notificationMapper.insert(n2);
        notificationMapper.insert(n3);

        List<Notification> result = notificationService.getActiveNotifications();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("公告B");  // 最新在前
        assertThat(result.get(1).getTitle()).isEqualTo("公告A");
    }

    @Test
    @DisplayName("getActiveNotifications: 无活跃通知时返回空列表")
    void getActiveNotifications_noActive_returnsEmpty() {
        List<Notification> result = notificationService.getActiveNotifications();
        assertThat(result).isEmpty();
    }

    private Notification notif(String title, int status, LocalDateTime publishTime) {
        Notification n = new Notification();
        n.setTitle(title);
        n.setStatus(status);
        n.setPublishTime(publishTime);
        return n;
    }
}
