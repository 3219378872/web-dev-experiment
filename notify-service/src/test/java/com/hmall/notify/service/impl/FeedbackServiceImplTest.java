package com.hmall.notify.service.impl;

import com.hmall.notify.domain.po.Feedback;
import com.hmall.notify.mapper.FeedbackMapper;
import com.hmall.notify.service.IFeedbackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FeedbackServiceImplTest {

    @Autowired
    private IFeedbackService feedbackService;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Test
    @DisplayName("save + getById: 反馈数据正常持久化")
    void saveAndGetById_works() {
        Feedback fb = new Feedback();
        fb.setUserId(1L);
        fb.setContent("测试反馈");
        fb.setStatus(0);
        feedbackService.save(fb);

        Feedback saved = feedbackService.getById(fb.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getContent()).isEqualTo("测试反馈");
        assertThat(saved.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById: 不存在的ID返回 null")
    void getById_nonExistent_returnsNull() {
        assertThat(feedbackService.getById(999L)).isNull();
    }
}
