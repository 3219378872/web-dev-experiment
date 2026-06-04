package com.hmall.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "趋势数据点")
public class TrendPointVO {
    @ApiModelProperty("日期，格式 yyyy-MM-dd")
    private String date;
    @ApiModelProperty("成交额，单位为分")
    private Integer amount;
    @ApiModelProperty("订单数")
    private Long orderCount;
}
