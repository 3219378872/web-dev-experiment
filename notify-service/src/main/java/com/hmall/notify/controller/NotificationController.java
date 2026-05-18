package com.hmall.notify.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.notify.domain.po.Notification;
import com.hmall.notify.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping("/notifications/active")
    public List<Notification> getActiveNotifications() {
        return notificationService.getActiveNotifications();
    }

    @GetMapping("/admin/notifications")
    public PageDTO<Notification> adminList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<Notification> result = notificationService.lambdaQuery()
                .orderByDesc(Notification::getCreateTime)
                .page(new Page<>(page, size));
        return PageDTO.of(result);
    }

    @PostMapping("/admin/notifications")
    public R<Void> create(@RequestBody Notification notification) {
        notification.setCreateTime(LocalDateTime.now());
        notification.setStatus(1);
        notificationService.save(notification);
        return R.ok();
    }

    @PutMapping("/admin/notifications/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Notification notification) {
        notification.setId(id);
        notification.setUpdateTime(LocalDateTime.now());
        notificationService.updateById(notification);
        return R.ok();
    }

    @DeleteMapping("/admin/notifications/{id}")
    public R<Void> delete(@PathVariable Long id) {
        notificationService.removeById(id);
        return R.ok();
    }
}
