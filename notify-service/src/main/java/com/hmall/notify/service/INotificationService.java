package com.hmall.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.notify.domain.po.Notification;
import java.util.List;

public interface INotificationService extends IService<Notification> {
    List<Notification> getActiveNotifications();
}
