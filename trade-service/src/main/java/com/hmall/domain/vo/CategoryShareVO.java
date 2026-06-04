package com.hmall.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "品类占比")
public class CategoryShareVO {
    @ApiModelProperty("品类名称")
    private String category;
    @ApiModelProperty("成交额，单位为分")
    private Integer amount;
    @ApiModelProperty("占比百分比")
    private Double percentage;
}
