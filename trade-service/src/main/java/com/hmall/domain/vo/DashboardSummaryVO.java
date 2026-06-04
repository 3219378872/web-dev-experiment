package com.hmall.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "数据看板概览")
public class DashboardSummaryVO {
    @ApiModelProperty("成交额，单位为分")
    private Integer totalAmount;
    @ApiModelProperty("订单总数")
    private Long totalOrders;
    @ApiModelProperty("新增用户数")
    private Long newUsers;
    @ApiModelProperty("访客数（无埋点，暂返回0）")
    private Long visitors;
}
