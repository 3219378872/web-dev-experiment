package com.hmall.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.api.dto.OrderDTO;
import com.hmall.api.dto.OrderFormDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Order;
import com.hmall.domain.vo.OrderVO;
import com.hmall.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Api(tags = "订单管理接口")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;

    // ===== 客户端接口 =====
    @ApiOperation("根据id查询订单")
    @GetMapping("/orders/{id}")
    public OrderVO queryOrderById(@Param("订单id") @PathVariable("id") Long orderId) {
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

    @ApiOperation("更新订单")
    @PutMapping("/orders")
    public void updateOrder(@RequestBody OrderDTO orderDTO) {
        com.hmall.domain.po.Order order = BeanUtil.copyProperties(orderDTO, com.hmall.domain.po.Order.class);
        orderService.updateById(order);
    }
}
