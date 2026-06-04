package com.hmall.controller;

import com.hmall.domain.vo.*;
import com.hmall.service.IDashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "数据看板接口")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @ApiOperation("看板概览")
    @GetMapping("/summary")
    public DashboardSummaryVO summary() {
        return dashboardService.getSummary();
    }

    @ApiOperation("趋势数据")
    @GetMapping("/trend")
    public List<TrendPointVO> trend(@RequestParam(value = "days", defaultValue = "7") Integer days) {
        return dashboardService.getTrend(days);
    }

    @ApiOperation("品类占比")
    @GetMapping("/category-share")
    public List<CategoryShareVO> categoryShare() {
        return dashboardService.getCategoryShare();
    }

    @ApiOperation("热销TOP")
    @GetMapping("/top-items")
    public List<TopItemVO> topItems() {
        return dashboardService.getTopItems();
    }

    @ApiOperation("待办事项")
    @GetMapping("/todo")
    public DashboardTodoVO todo() {
        return dashboardService.getTodo();
    }

    @ApiOperation("最新订单")
    @GetMapping("/latest-orders")
    public List<OrderVO> latestOrders() {
        return dashboardService.getLatestOrders();
    }
}
