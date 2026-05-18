package com.hmall.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "优惠券DTO")
public class CouponDTO {
    @ApiModelProperty("优惠券id")
    private Long id;
    @ApiModelProperty("优惠券名称")
    private String name;
    @ApiModelProperty("优惠券描述")
    private String description;
    @ApiModelProperty("优惠类型 1-满减 2-折扣")
    private Integer discountType;
    @ApiModelProperty("减免金额(分)或折扣百分比")
    private Integer discountValue;
    @ApiModelProperty("最低消费金额(分)")
    private Integer minAmount;
    @ApiModelProperty("剩余数量")
    private Integer remainingStock;
    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;
    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;
}
