package com.hmall.controller;

import com.hmall.TradeServiceTestBase;
import com.hmall.domain.po.Order;
import com.hmall.service.IOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class OrderControllerAdminTest extends TradeServiceTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IOrderService orderService;

    @Test
    void adminList_filtersByUserId() throws Exception {
        Order user42 = order(42L, 2);
        Order user99 = order(99L, 2);
        orderService.save(user42);
        orderService.save(user99);

        mockMvc.perform(get("/admin/orders")
                        .param("page", "1")
                        .param("size", "10")
                        .param("userId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].userId").value(42));
    }

    @Test
    void exportOrders_filtersByUserId() throws Exception {
        Order user42 = order(42L, 2);
        Order user99 = order(99L, 2);
        orderService.save(user42);
        orderService.save(user99);

        mockMvc.perform(get("/admin/orders/export")
                        .param("userId", "42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("," + user42.getUserId() + ",")))
                .andExpect(content().string(not(containsString("," + user99.getUserId() + ","))));
    }

    private Order order(Long userId, Integer status) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(status);
        order.setPaymentType(3);
        order.setTotalFee(9900);
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
}
