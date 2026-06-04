package com.hmall.service;

import com.hmall.domain.vo.*;

import java.util.List;

public interface IDashboardService {

    DashboardSummaryVO getSummary();

    List<TrendPointVO> getTrend(Integer days);

    List<CategoryShareVO> getCategoryShare();

    List<TopItemVO> getTopItems();

    DashboardTodoVO getTodo();

    List<OrderVO> getLatestOrders();
}
