package com.hmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmall.api.client.ItemClient;
import com.hmall.api.client.UserClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.domain.po.Order;
import com.hmall.domain.po.OrderDetail;
import com.hmall.domain.vo.*;
import com.hmall.service.IDashboardService;
import com.hmall.service.IOrderDetailService;
import com.hmall.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final IOrderService orderService;
    private final IOrderDetailService orderDetailService;
    private final ItemClient itemClient;
    private final UserClient userClient;

    private static final int FREE_SHIPPING_THRESHOLD = 9900; // 99元包邮

    @Override
    public DashboardSummaryVO getSummary() {
        DashboardSummaryVO vo = new DashboardSummaryVO();

        // 成交额：已完成订单（status=4）的 totalFee 之和
        List<Order> completedOrders = orderService.lambdaQuery()
                .eq(Order::getStatus, 4)
                .list();
        int totalAmount = completedOrders.stream().mapToInt(Order::getTotalFee).sum();
        vo.setTotalAmount(totalAmount);

        // 订单总数
        vo.setTotalOrders(orderService.count());

        // 新增用户数：通过 Feign 拉取，失败则返回 0
        try {
            Long newUsers = userClient.countNewUsers(30);
            vo.setNewUsers(newUsers != null ? newUsers : 0L);
        } catch (Exception e) {
            log.warn("获取新增用户数失败，返回0: {}", e.getMessage());
            vo.setNewUsers(0L);
        }

        // 访客数无埋点，返回 0
        vo.setVisitors(0L);
        return vo;
    }

    @Override
    public List<TrendPointVO> getTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();

        List<Order> orders = orderService.lambdaQuery()
                .ge(Order::getCreateTime, startDateTime)
                .list();

        Map<String, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE)));

        List<TrendPointVO> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            List<Order> dayOrders = grouped.getOrDefault(dateStr, List.of());

            TrendPointVO point = new TrendPointVO();
            point.setDate(dateStr);
            point.setOrderCount((long) dayOrders.size());
            point.setAmount(dayOrders.stream().mapToInt(Order::getTotalFee).sum());
            result.add(point);
        }
        return result;
    }

    @Override
    public List<CategoryShareVO> getCategoryShare() {
        // 获取已完成订单的详情
        List<Order> completedOrders = orderService.lambdaQuery()
                .eq(Order::getStatus, 4)
                .list();
        if (completedOrders.isEmpty()) {
            return List.of();
        }

        Set<Long> orderIds = completedOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toSet());

        List<OrderDetail> details = orderDetailService.lambdaQuery()
                .in(OrderDetail::getOrderId, orderIds)
                .list();
        if (details.isEmpty()) {
            return List.of();
        }

        // 通过 ItemClient 获取商品品类信息
        Set<Long> itemIds = details.stream()
                .map(OrderDetail::getItemId)
                .collect(Collectors.toSet());

        Map<Long, String> itemCategoryMap = new HashMap<>();
        try {
            List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
            if (items != null) {
                itemCategoryMap = items.stream()
                        .collect(Collectors.toMap(ItemDTO::getId, ItemDTO::getCategory, (a, b) -> a));
            }
        } catch (Exception e) {
            log.warn("获取商品品类信息失败: {}", e.getMessage());
        }

        // 按品类聚合金额
        Map<String, Integer> categoryAmount = new LinkedHashMap<>();
        for (OrderDetail detail : details) {
            String category = itemCategoryMap.getOrDefault(detail.getItemId(), "其他");
            int amount = detail.getPrice() * detail.getNum();
            categoryAmount.merge(category, amount, Integer::sum);
        }

        int totalAmount = categoryAmount.values().stream().mapToInt(Integer::intValue).sum();

        return categoryAmount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> {
                    CategoryShareVO vo = new CategoryShareVO();
                    vo.setCategory(e.getKey());
                    vo.setAmount(e.getValue());
                    vo.setPercentage(totalAmount > 0 ? Math.round(e.getValue() * 10000.0 / totalAmount) / 100.0 : 0);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TopItemVO> getTopItems() {
        // 获取已完成订单的详情
        List<Order> completedOrders = orderService.lambdaQuery()
                .eq(Order::getStatus, 4)
                .list();
        if (completedOrders.isEmpty()) {
            return List.of();
        }

        Set<Long> orderIds = completedOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toSet());

        List<OrderDetail> details = orderDetailService.lambdaQuery()
                .in(OrderDetail::getOrderId, orderIds)
                .list();
        if (details.isEmpty()) {
            return List.of();
        }

        // 按商品聚合销量和金额
        Map<Long, int[]> itemStats = new HashMap<>(); // [sold, amount]
        Map<Long, String> itemNames = new HashMap<>();
        Map<Long, String> itemImages = new HashMap<>();
        for (OrderDetail detail : details) {
            itemStats.merge(detail.getItemId(),
                    new int[]{detail.getNum(), detail.getPrice() * detail.getNum()},
                    (a, b) -> new int[]{a[0] + b[0], a[1] + b[1]});
            itemNames.putIfAbsent(detail.getItemId(), detail.getName());
            itemImages.putIfAbsent(detail.getItemId(), detail.getImage());
        }

        // 尝试通过 ItemClient 获取最新商品信息
        try {
            List<ItemDTO> items = itemClient.queryItemByIds(itemStats.keySet());
            if (items != null) {
                for (ItemDTO item : items) {
                    itemNames.put(item.getId(), item.getName());
                    itemImages.put(item.getId(), item.getImage());
                }
            }
        } catch (Exception e) {
            log.warn("获取商品信息失败，使用订单详情中的数据: {}", e.getMessage());
        }

        return itemStats.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue()[1], a.getValue()[1]))
                .limit(5)
                .map(e -> {
                    TopItemVO vo = new TopItemVO();
                    vo.setItemId(e.getKey());
                    vo.setName(itemNames.getOrDefault(e.getKey(), "未知商品"));
                    vo.setImage(itemImages.get(e.getKey()));
                    vo.setSold(e.getValue()[0]);
                    vo.setAmount(e.getValue()[1]);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public DashboardTodoVO getTodo() {
        DashboardTodoVO vo = new DashboardTodoVO();

        // 待发货：status=2（已付款未发货）
        vo.setPendingShipment(orderService.lambdaQuery()
                .eq(Order::getStatus, 2).count());

        // 待处理退款：status=6（申请退款）
        vo.setPendingRefund(orderService.lambdaQuery()
                .eq(Order::getStatus, 6).count());

        // 待付款：status=1
        vo.setPendingPayment(orderService.lambdaQuery()
                .eq(Order::getStatus, 1).count());

        // 今日已完成：status=4 且 endTime 在今天
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        vo.setCompletedToday(orderService.lambdaQuery()
                .eq(Order::getStatus, 4)
                .ge(Order::getEndTime, todayStart)
                .count());

        return vo;
    }

    @Override
    public List<OrderVO> getLatestOrders() {
        List<Order> orders = orderService.lambdaQuery()
                .orderByDesc(Order::getCreateTime)
                .last("LIMIT 10")
                .list();
        return BeanUtils.copyList(orders, OrderVO.class);
    }
}
