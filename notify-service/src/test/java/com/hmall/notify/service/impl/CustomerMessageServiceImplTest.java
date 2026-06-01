package com.hmall.notify.service.impl;

import com.hmall.notify.domain.po.CustomerMessage;
import com.hmall.notify.mapper.CustomerMessageMapper;
import com.hmall.notify.service.ICustomerMessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CustomerMessageServiceImplTest {

    @Autowired
    private ICustomerMessageService messageService;

    @Autowired
    private CustomerMessageMapper messageMapper;

    @Test
    @DisplayName("save + getById: 客服消息正常持久化")
    void saveAndGetById_works() {
        CustomerMessage msg = new CustomerMessage();
        msg.setUserId(1L);
        msg.setContent("订单咨询");
        msg.setStatus(0);
        messageService.save(msg);

        CustomerMessage saved = messageService.getById(msg.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getContent()).isEqualTo("订单咨询");
    }

    @Test
    @DisplayName("getById: 不存在的ID返回 null")
    void getById_nonExistent_returnsNull() {
        assertThat(messageService.getById(999L)).isNull();
    }
}
