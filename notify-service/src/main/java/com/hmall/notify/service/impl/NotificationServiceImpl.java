package com.hmall.notify.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.notify.domain.po.Notification;
import com.hmall.notify.mapper.NotificationMapper;
import com.hmall.notify.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements INotificationService {

    @Override
    public List<Notification> getActiveNotifications() {
        return lambdaQuery()
                .eq(Notification::getStatus, 1)
                .orderByDesc(Notification::getPublishTime)
                .list();
    }
}
