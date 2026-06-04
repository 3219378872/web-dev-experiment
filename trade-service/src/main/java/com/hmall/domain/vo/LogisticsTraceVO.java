package com.hmall.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "物流轨迹VO")
public class LogisticsTraceVO {
    @ApiModelProperty("轨迹id")
    private Long id;
    @ApiModelProperty("订单id")
    private Long orderId;
    @ApiModelProperty("物流节点")
    private String node;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("轨迹时间")
    private LocalDateTime traceTime;
}
