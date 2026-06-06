package com.hmall.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "用户优惠券VO")
public class UserCouponVO extends CouponVO {
    @ApiModelProperty("用户券状态：1未使用，0已使用")
    private Integer userCouponStatus;

    @ApiModelProperty("使用订单id")
    private Long usedOrderId;

    @ApiModelProperty("使用时间")
    private LocalDateTime useTime;

    @ApiModelProperty("领取时间")
    private LocalDateTime claimTime;
}
