package com.hmall.notify.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.notify.domain.po.Feedback;
import com.hmall.notify.mapper.FeedbackMapper;
import com.hmall.notify.service.IFeedbackService;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback>
        implements IFeedbackService {}
