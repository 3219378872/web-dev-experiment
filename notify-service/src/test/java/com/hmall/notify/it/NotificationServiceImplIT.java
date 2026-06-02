package com.hmall.notify.it;

import com.hmall.common.utils.UserContext;
import com.hmall.notify.domain.po.Notification;
import com.hmall.notify.service.INotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.bootstrap.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:notify_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/sql/data-notification.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class NotificationServiceImplIT {

    @BeforeEach
    void setUp() { UserContext.setUser(1L); }

    @AfterEach
    void tearDown() { UserContext.removeUser(); }

    @Autowired
    private INotificationService notificationService;

    @Test
    void getActiveNotifications_shouldReturnOnlyActive() {
        List<Notification> result = notificationService.getActiveNotifications();
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(n -> n.getStatus() == 1);
    }

    @Test
    void getActiveNotifications_shouldBeOrderedByPublishTimeDesc() {
        List<Notification> result = notificationService.getActiveNotifications();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getPublishTime())
                .isAfterOrEqualTo(result.get(1).getPublishTime());
    }
}
