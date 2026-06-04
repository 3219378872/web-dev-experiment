package com.hmall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.api.dto.OrderFormDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.mq.MqConstants;
import com.hmall.common.mq.consumer.MqConsumerSupport;
import com.hmall.common.mq.event.OrderCreatedEvent;
import com.hmall.common.mq.event.OrderStatusChangedEvent;
import com.hmall.common.mq.event.PaySuccessEvent;
import com.hmall.common.mq.outbox.MqMessagePublisher;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Order;
import com.hmall.domain.po.OrderDetail;
import com.hmall.domain.po.OrderLogistics;
import com.hmall.domain.po.LogisticsTrace;
import com.hmall.mapper.OrderMapper;
import com.hmall.service.IOrderDetailService;
import com.hmall.service.IOrderLogisticsService;
import com.hmall.service.ILogisticsTraceService;
import com.hmall.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rabbitmq.client.Channel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final IOrderDetailService detailService;
    private final IOrderLogisticsService logisticsService;
    private final ILogisticsTraceService logisticsTraceService;
    private final ItemClient itemClient;
    private final MqMessagePublisher mqMessagePublisher;
    private final MqConsumerSupport mqConsumerSupport;

    @Override
    @GlobalTransactional(name = "createOrder", rollbackFor = Exception.class)
    @Transactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        // 1.订单数据
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 3.扣减库存
        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }
        // 4.发布下单后的副作用事件：清车、通知、延时关单。
        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), order.getUserId(), new ArrayList<>(itemIds));
        mqMessagePublisher.publish(MqConstants.TRADE_EXCHANGE, MqConstants.ORDER_CREATE_KEY, event);
        mqMessagePublisher.publish(MqConstants.DELAY_EXCHANGE, MqConstants.ORDER_DELAY_KEY, event);
        return order.getId();
    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        lambdaUpdate()
                .set(Order::getStatus, 2)
                .set(Order::getPayTime, LocalDateTime.now())
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, 1)
                .update();
    }

    @RabbitListener(queues = MqConstants.TRADE_PAY_SUCCESS_QUEUE)
    public void onPaySuccess(PaySuccessEvent event, Message message, Channel channel) throws Exception {
        try {
            handlePaySuccess(event);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            mqConsumerSupport.reject(message, channel);
            throw e;
        }
    }

    public void handlePaySuccess(PaySuccessEvent event) {
        markOrderPaySuccess(event.getOrderId());
    }

    @RabbitListener(queues = MqConstants.ORDER_CLOSE_QUEUE)
    public void onOrderClose(OrderCreatedEvent event, Message message, Channel channel) throws Exception {
        try {
            handleOrderClose(event);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            mqConsumerSupport.reject(message, channel);
            throw e;
        }
    }

    public void handleOrderClose(OrderCreatedEvent event) {
        lambdaUpdate()
                .set(Order::getStatus, 5)
                .set(Order::getCloseTime, LocalDateTime.now())
                .eq(Order::getId, event.getOrderId())
                .eq(Order::getStatus, 1)
                .update();
    }

    @Override
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BadRequestException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BizIllegalException("当前状态不可取消");
        }
        order.setStatus(5);
        order.setCloseTime(LocalDateTime.now());
        updateById(order);
        publishStatusChanged(orderId, userId, "cancel");
    }

    @Override
    public void confirmReceive(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BadRequestException("订单不存在");
        }
        if (order.getStatus() != 3) {
            throw new BizIllegalException("当前状态不可确认收货");
        }
        order.setStatus(4);
        order.setEndTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void refund(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BadRequestException("订单不存在");
        }
        if (order.getStatus() != 2 && order.getStatus() != 3) {
            throw new BizIllegalException("当前状态不可申请退款");
        }
        order.setStatus(6);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        publishStatusChanged(orderId, userId, "refund");
    }

    @Override
    public void refundAudit(Long orderId, boolean approved, String reason) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BadRequestException("订单不存在");
        }
        if (order.getStatus() != 6) {
            throw new BizIllegalException("当前状态不可审核退款");
        }
        if (approved) {
            // 退款通过：关闭订单
            order.setStatus(5);
            order.setCloseTime(LocalDateTime.now());
        } else {
            // 退款驳回：恢复到退款前状态
            // 已发货(consignTime不为空) → 3，否则 → 2
            order.setStatus(order.getConsignTime() != null ? 3 : 2);
        }
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void ship(Long orderId, String trackingNumber) {
        Order order = getById(orderId);
        if (order == null) throw new BadRequestException("订单不存在");
        order.setStatus(3);
        order.setConsignTime(LocalDateTime.now());
        updateById(order);
        publishStatusChanged(orderId, order.getUserId(), "shipped");
        OrderLogistics logistics = logisticsService.lambdaQuery()
                .eq(OrderLogistics::getOrderId, orderId).one();
        if (logistics != null) {
            logistics.setLogisticsNumber(trackingNumber);
            logisticsService.updateById(logistics);
        }
        // 插入物流轨迹记录
        LogisticsTrace trace = new LogisticsTrace();
        trace.setOrderId(orderId);
        trace.setNode("已发货");
        trace.setTraceTime(LocalDateTime.now());
        trace.setDescription("商品已发货，运单号：" + trackingNumber);
        trace.setCreateTime(LocalDateTime.now());
        logisticsTraceService.save(trace);
    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }

    private void publishStatusChanged(Long orderId, Long userId, String status) {
        mqMessagePublisher.publish(
                MqConstants.TRADE_EXCHANGE,
                MqConstants.orderStatusKey(status),
                new OrderStatusChangedEvent(orderId, userId, status));
    }
}
