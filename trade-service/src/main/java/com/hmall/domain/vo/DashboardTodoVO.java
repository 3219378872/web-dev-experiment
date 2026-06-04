package com.hmall.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "管理端待办事项")
public class DashboardTodoVO {
    @ApiModelProperty("待发货订单数")
    private Long pendingShipment;
    @ApiModelProperty("待处理退款数")
    private Long pendingRefund;
    @ApiModelProperty("待付款订单数")
    private Long pendingPayment;
    @ApiModelProperty("已完成订单数（今日）")
    private Long completedToday;
}
