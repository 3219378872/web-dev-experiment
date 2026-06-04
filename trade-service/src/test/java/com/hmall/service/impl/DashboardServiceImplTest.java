package com.hmall.service.impl;

import com.hmall.api.client.ItemClient;
import com.hmall.api.client.UserClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Order;
import com.hmall.domain.po.OrderDetail;
import com.hmall.domain.vo.*;
import com.hmall.mapper.OrderDetailMapper;
import com.hmall.mapper.OrderMapper;
import com.hmall.service.IDashboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
    "spring.cloud.bootstrap.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.nacos.config.enabled=false"
})
@Transactional
class DashboardServiceImplTest {

    @MockBean
    private ItemClient itemClient;

    @MockBean
    private UserClient userClient;

    @Autowired
    private IDashboardService dashboardService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        UserContext.setUser(TEST_USER_ID);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private Order createOrder(int status, int totalFee) {
        Order order = new Order();
        order.setUserId(TEST_USER_ID);
        order.setTotalFee(totalFee);
        order.setPaymentType(1);
        order.setStatus(status);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        return order;
    }

    private OrderDetail createOrderDetail(Long orderId, Long itemId, String name, int price, int num) {
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setItemId(itemId);
        detail.setName(name);
        detail.setPrice(price);
        detail.setNum(num);
        detail.setImage("/img/test.png");
        orderDetailMapper.insert(detail);
        return detail;
    }

    @Test
    void getSummary_returnsCorrectData() {
        // Create completed orders (status=4)
        Order completed = createOrder(4, 10000);
        completed.setEndTime(LocalDateTime.now());
        orderMapper.updateById(completed);
        createOrder(1, 5000); // pending

        when(userClient.countNewUsers(30)).thenReturn(5L);

        DashboardSummaryVO summary = dashboardService.getSummary();

        assertThat(summary.getTotalAmount()).isEqualTo(10000);
        assertThat(summary.getTotalOrders()).isEqualTo(2);
        assertThat(summary.getNewUsers()).isEqualTo(5L);
        assertThat(summary.getVisitors()).isEqualTo(0L);
    }

    @Test
    void getSummary_userClientFails_returnsZeroNewUsers() {
        createOrder(4, 10000);
        when(userClient.countNewUsers(30)).thenThrow(new RuntimeException("service unavailable"));

        DashboardSummaryVO summary = dashboardService.getSummary();

        assertThat(summary.getNewUsers()).isEqualTo(0L);
    }

    @Test
    void getSummary_noOrders_returnsZeros() {
        when(userClient.countNewUsers(30)).thenReturn(0L);

        DashboardSummaryVO summary = dashboardService.getSummary();

        assertThat(summary.getTotalAmount()).isEqualTo(0);
        assertThat(summary.getTotalOrders()).isEqualTo(0);
    }

    @Test
    void getTrend_returnsCorrectData() {
        Order order = createOrder(1, 10000);

        List<TrendPointVO> trend = dashboardService.getTrend(7);

        assertThat(trend).hasSize(7);
        // Today's data should have at least 1 order
        boolean hasTodayData = trend.stream()
                .anyMatch(t -> t.getOrderCount() > 0);
        assertThat(hasTodayData).isTrue();
    }

    @Test
    void getTrend_defaultDays_returns7() {
        List<TrendPointVO> trend = dashboardService.getTrend(null);

        assertThat(trend).hasSize(7);
    }

    @Test
    void getTrend_negativeDays_returns7() {
        List<TrendPointVO> trend = dashboardService.getTrend(-1);

        assertThat(trend).hasSize(7);
    }

    @Test
    void getCategoryShare_returnsGroupedData() {
        Order completed = createOrder(4, 10000);
        completed.setEndTime(LocalDateTime.now());
        orderMapper.updateById(completed);
        createOrderDetail(completed.getId(), 1L, "耳机", 5000, 1);
        createOrderDetail(completed.getId(), 2L, "电脑", 5000, 1);

        ItemDTO item1 = new ItemDTO();
        item1.setId(1L);
        item1.setCategory("电子产品");
        ItemDTO item2 = new ItemDTO();
        item2.setId(2L);
        item2.setCategory("电子产品");
        when(itemClient.queryItemByIds(anySet())).thenReturn(List.of(item1, item2));

        List<CategoryShareVO> share = dashboardService.getCategoryShare();

        assertThat(share).hasSize(1);
        assertThat(share.get(0).getCategory()).isEqualTo("电子产品");
        assertThat(share.get(0).getAmount()).isEqualTo(10000);
        assertThat(share.get(0).getPercentage()).isEqualTo(100.0);
    }

    @Test
    void getCategoryShare_noCompletedOrders_returnsEmpty() {
        List<CategoryShareVO> share = dashboardService.getCategoryShare();

        assertThat(share).isEmpty();
    }

    @Test
    void getCategoryShare_itemClientFails_returnsOtherCategory() {
        Order completed = createOrder(4, 10000);
        completed.setEndTime(LocalDateTime.now());
        orderMapper.updateById(completed);
        createOrderDetail(completed.getId(), 1L, "耳机", 5000, 2);

        when(itemClient.queryItemByIds(anySet())).thenThrow(new RuntimeException("fail"));

        List<CategoryShareVO> share = dashboardService.getCategoryShare();

        assertThat(share).hasSize(1);
        assertThat(share.get(0).getCategory()).isEqualTo("其他");
    }

    @Test
    void getTopItems_returnsTop5ByAmount() {
        Order completed = createOrder(4, 30000);
        completed.setEndTime(LocalDateTime.now());
        orderMapper.updateById(completed);
        createOrderDetail(completed.getId(), 1L, "便宜货", 1000, 1);
        createOrderDetail(completed.getId(), 2L, "贵货", 20000, 1);

        when(itemClient.queryItemByIds(anySet())).thenReturn(List.of());

        List<TopItemVO> topItems = dashboardService.getTopItems();

        assertThat(topItems).hasSize(2);
        assertThat(topItems.get(0).getItemId()).isEqualTo(2L);
        assertThat(topItems.get(0).getAmount()).isEqualTo(20000);
    }

    @Test
    void getTopItems_noCompletedOrders_returnsEmpty() {
        List<TopItemVO> topItems = dashboardService.getTopItems();

        assertThat(topItems).isEmpty();
    }

    @Test
    void getTodo_returnsCorrectCounts() {
        createOrder(1, 1000); // pending payment
        createOrder(2, 2000); // pending shipment
        createOrder(6, 3000); // pending refund
        Order completed = createOrder(4, 4000); // completed today
        completed.setEndTime(LocalDateTime.now());
        orderMapper.updateById(completed);

        DashboardTodoVO todo = dashboardService.getTodo();

        assertThat(todo.getPendingPayment()).isEqualTo(1L);
        assertThat(todo.getPendingShipment()).isEqualTo(1L);
        assertThat(todo.getPendingRefund()).isEqualTo(1L);
        assertThat(todo.getCompletedToday()).isEqualTo(1L);
    }

    @Test
    void getLatestOrders_returnsUpTo10() {
        for (int i = 0; i < 15; i++) {
            createOrder(1, 1000 * (i + 1));
        }

        List<OrderVO> latest = dashboardService.getLatestOrders();

        assertThat(latest).hasSize(10);
    }

    @Test
    void getLatestOrders_noOrders_returnsEmpty() {
        List<OrderVO> latest = dashboardService.getLatestOrders();

        assertThat(latest).isEmpty();
    }
}
