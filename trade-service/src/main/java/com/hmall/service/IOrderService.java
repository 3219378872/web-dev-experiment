package com.hmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.api.dto.OrderFormDTO;
import com.hmall.domain.po.Order;

public interface IOrderService extends IService<Order> {

    Long createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);

    void cancelOrder(Long orderId, Long userId);

    void confirmReceive(Long orderId, Long userId);

    void refund(Long orderId, Long userId);

    void ship(Long orderId, String trackingNumber);
}
