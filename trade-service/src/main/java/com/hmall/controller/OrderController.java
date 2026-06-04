package com.hmall.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.api.dto.OrderDTO;
import com.hmall.api.dto.OrderFormDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Order;
import com.hmall.domain.po.OrderDetail;
import com.hmall.domain.po.OrderLogistics;
import com.hmall.domain.po.LogisticsTrace;
import com.hmall.domain.vo.*;
import com.hmall.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "订单管理接口")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private final IOrderLogisticsService logisticsService;
    private final ILogisticsTraceService logisticsTraceService;
    private final IOrderDetailService orderDetailService;

    // ===== 客户端接口 =====
    @ApiOperation("根据id查询订单")
    @GetMapping("/orders/{id}")
    public OrderVO queryOrderById(@PathVariable("id") Long orderId) {
        return BeanUtils.copyBean(orderService.getById(orderId), OrderVO.class);
    }

    @ApiOperation("创建订单")
    @PostMapping("/orders")
    public Long createOrder(@RequestBody OrderFormDTO orderFormDTO) {
        return orderService.createOrder(orderFormDTO);
    }

    @ApiOperation("标记订单已支付")
    @PutMapping("/orders/{orderId}")
    public void markOrderPaySuccess(@PathVariable("orderId") Long orderId) {
        orderService.markOrderPaySuccess(orderId);
    }

    @ApiOperation("我的订单列表")
    @GetMapping("/orders")
    public PageDTO<OrderVO> myOrders(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, UserContext.getUser());
        if (status != null) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> result = orderService.page(new Page<>(page, size), wrapper);
        return PageDTO.of(result, OrderVO.class);
    }

    @ApiOperation("取消订单")
    @PutMapping("/orders/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id, UserContext.getUser());
        return R.ok();
    }

    @ApiOperation("确认收货")
    @PutMapping("/orders/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        orderService.confirmReceive(id, UserContext.getUser());
        return R.ok();
    }

    @ApiOperation("申请退款")
    @PostMapping("/orders/{id}/refund")
    public R<Void> refund(@PathVariable Long id) {
        orderService.refund(id, UserContext.getUser());
        return R.ok();
    }

    @ApiOperation("查询运费")
    @GetMapping("/orders/freight")
    public FreightVO freight(
            @RequestParam(value = "addressId", required = false) Long addressId,
            @RequestParam(value = "amount", defaultValue = "0") Integer amount) {
        FreightVO vo = new FreightVO();
        // 满99元（9900分）包邮，否则固定运费10元（1000分）
        int freeShippingThreshold = 9900;
        int defaultFreight = 1000;
        if (amount >= freeShippingThreshold) {
            vo.setFreight(0);
            vo.setFreeShipping(true);
        } else {
            vo.setFreight(defaultFreight);
            vo.setFreeShipping(false);
        }
        return vo;
    }

    @ApiOperation("查询订单物流轨迹")
    @GetMapping("/orders/{id}/logistics")
    public List<LogisticsTraceVO> logistics(@PathVariable("id") Long orderId) {
        List<LogisticsTrace> traces = logisticsTraceService.lambdaQuery()
                .eq(LogisticsTrace::getOrderId, orderId)
                .orderByAsc(LogisticsTrace::getTraceTime)
                .list();
        return BeanUtils.copyList(traces, LogisticsTraceVO.class);
    }

    // ===== 管理端接口 =====
    @ApiOperation("管理端订单列表")
    @GetMapping("/admin/orders")
    public PageDTO<OrderVO> adminList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) wrapper.eq(Order::getId, orderId);
        if (status != null) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> result = orderService.page(new Page<>(page, size), wrapper);
        return PageDTO.of(result, OrderVO.class);
    }

    @ApiOperation("修改订单状态")
    @PutMapping("/admin/orders/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Order order = orderService.getById(id);
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);
        return R.ok();
    }

    @ApiOperation("发货")
    @PutMapping("/admin/orders/{id}/ship")
    public R<Void> ship(@PathVariable Long id, @RequestParam String trackingNumber) {
        orderService.ship(id, trackingNumber);
        return R.ok();
    }

    @ApiOperation("退款审核")
    @PutMapping("/admin/orders/{id}/refund-audit")
    public R<Void> refundAudit(@PathVariable("id") Long orderId,
                               @RequestBody RefundAuditDTO dto) {
        orderService.refundAudit(orderId, dto.getApproved(), dto.getReason());
        return R.ok();
    }

    @ApiOperation("导出订单CSV")
    @GetMapping("/admin/orders/export")
    public ResponseEntity<byte[]> exportOrders(
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) wrapper.eq(Order::getId, orderId);
        if (status != null) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreateTime);
        List<Order> orders = orderService.list(wrapper);

        StringBuilder csv = new StringBuilder();
        csv.append("﻿"); // BOM for Excel
        csv.append("订单ID,用户ID,总金额(分),支付类型,状态,创建时间,支付时间,发货时间\n");
        String[] statusNames = {"", "未付款", "已付款", "已发货", "已完成", "已取消", "退款中"};
        for (Order o : orders) {
            String statusName = (o.getStatus() != null && o.getStatus() >= 1 && o.getStatus() <= 6)
                    ? statusNames[o.getStatus()] : "未知";
            csv.append(o.getId()).append(",")
                    .append(o.getUserId()).append(",")
                    .append(o.getTotalFee()).append(",")
                    .append(o.getPaymentType()).append(",")
                    .append(statusName).append(",")
                    .append(o.getCreateTime()).append(",")
                    .append(o.getPayTime()).append(",")
                    .append(o.getConsignTime()).append("\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    @ApiOperation("更新订单")
    @PutMapping("/orders")
    public void updateOrder(@RequestBody OrderDTO orderDTO) {
        com.hmall.domain.po.Order order = BeanUtil.copyProperties(orderDTO, com.hmall.domain.po.Order.class);
        orderService.updateById(order);
    }
}
