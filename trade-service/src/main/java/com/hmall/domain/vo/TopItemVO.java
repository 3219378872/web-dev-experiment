package com.hmall.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "热销商品TOP")
public class TopItemVO {
    @ApiModelProperty("商品id")
    private Long itemId;
    @ApiModelProperty("商品名称")
    private String name;
    @ApiModelProperty("商品图片")
    private String image;
    @ApiModelProperty("销量")
    private Integer sold;
    @ApiModelProperty("成交额，单位为分")
    private Integer amount;
}
