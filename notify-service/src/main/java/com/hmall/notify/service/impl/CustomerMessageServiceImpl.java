package com.hmall.notify.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.notify.domain.po.CustomerMessage;
import com.hmall.notify.mapper.CustomerMessageMapper;
import com.hmall.notify.service.ICustomerMessageService;
import org.springframework.stereotype.Service;

@Service
public class CustomerMessageServiceImpl extends ServiceImpl<CustomerMessageMapper, CustomerMessage>
        implements ICustomerMessageService {}
