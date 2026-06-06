package com.hmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.api.dto.OrderFormDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.domain.po.Order;
import com.hmall.domain.vo.OrderVO;

public interface IOrderService extends IService<Order> {

    Long createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);

    PageDTO<OrderVO> queryUserOrders(Long userId, Integer page, Integer size, Integer status, String keyword);

    void cancelOrder(Long orderId, Long userId);

    void confirmReceive(Long orderId, Long userId);

    void refund(Long orderId, Long userId);

    void ship(Long orderId, String trackingNumber);

    /**
     * 退款审核
     * @param orderId 订单id
     * @param approved 是否通过
     * @param reason 审核原因
     */
    void refundAudit(Long orderId, boolean approved, String reason);

    /**
     * 逻辑删除订单（仅限已取消 status=5 或交易完成 status=4 状态）
     * @param orderId 订单id
     * @param userId 当前用户id，校验归属
     */
    void deleteOrder(Long orderId, Long userId);
}
