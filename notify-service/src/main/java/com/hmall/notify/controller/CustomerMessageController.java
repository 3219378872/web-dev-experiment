package com.hmall.notify.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.notify.domain.po.CustomerMessage;
import com.hmall.notify.service.ICustomerMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CustomerMessageController {

    private final ICustomerMessageService messageService;

    @PostMapping("/messages")
    public R<Void> send(@RequestBody CustomerMessage message) {
        message.setUserId(UserContext.getUser());
        message.setStatus(0);
        message.setCreateTime(LocalDateTime.now());
        messageService.save(message);
        return R.ok();
    }

    @GetMapping("/admin/messages")
    public PageDTO<CustomerMessage> adminList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<CustomerMessage> result = messageService.lambdaQuery()
                .orderByDesc(CustomerMessage::getCreateTime)
                .page(new Page<>(page, size));
        return PageDTO.of(result);
    }

    @PutMapping("/admin/messages/{id}/reply")
    public R<Void> reply(@PathVariable Long id, @RequestBody CustomerMessage message) {
        message.setId(id);
        message.setStatus(1);
        message.setReplyTime(LocalDateTime.now());
        messageService.updateById(message);
        return R.ok();
    }
}
