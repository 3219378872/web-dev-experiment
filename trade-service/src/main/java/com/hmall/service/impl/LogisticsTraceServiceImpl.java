package com.hmall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.domain.po.LogisticsTrace;
import com.hmall.mapper.LogisticsTraceMapper;
import com.hmall.service.ILogisticsTraceService;
import org.springframework.stereotype.Service;

@Service
public class LogisticsTraceServiceImpl extends ServiceImpl<LogisticsTraceMapper, LogisticsTrace>
        implements ILogisticsTraceService {
}
