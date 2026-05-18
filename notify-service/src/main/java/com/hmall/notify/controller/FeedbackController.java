package com.hmall.notify.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.notify.domain.po.Feedback;
import com.hmall.notify.service.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FeedbackController {

    private final IFeedbackService feedbackService;

    @PostMapping("/feedbacks")
    public R<Void> submit(@RequestBody Feedback feedback) {
        feedback.setUserId(UserContext.getUser());
        feedback.setStatus(0);
        feedback.setCreateTime(LocalDateTime.now());
        feedbackService.save(feedback);
        return R.ok();
    }

    @GetMapping("/my-feedbacks")
    public PageDTO<Feedback> myFeedbacks(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<Feedback> result = feedbackService.lambdaQuery()
                .eq(Feedback::getUserId, UserContext.getUser())
                .orderByDesc(Feedback::getCreateTime)
                .page(new Page<>(page, size));
        return PageDTO.of(result);
    }

    @GetMapping("/admin/feedbacks")
    public PageDTO<Feedback> adminList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<Feedback> result = feedbackService.lambdaQuery()
                .orderByDesc(Feedback::getCreateTime)
                .page(new Page<>(page, size));
        return PageDTO.of(result);
    }

    @PutMapping("/admin/feedbacks/{id}/reply")
    public R<Void> reply(@PathVariable Long id, @RequestBody Feedback feedback) {
        feedback.setId(id);
        feedback.setStatus(1);
        feedback.setReplyTime(LocalDateTime.now());
        feedbackService.updateById(feedback);
        return R.ok();
    }
}
