package com.hmall.item.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀 VO
 */
@Data
@ApiModel(description = "秒杀 VO")
public class SeckillVO {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("商品ID")
    private Long itemId;

    @ApiModelProperty("商品名称")
    private String itemName;

    @ApiModelProperty("商品图片")
    private String itemImage;

    @ApiModelProperty("原价（分）")
    private Integer originalPrice;

    @ApiModelProperty("秒杀价（分）")
    private Integer seckillPrice;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("库存数量")
    private Integer stock;

    @ApiModelProperty("已售数量")
    private Integer sold;

    @ApiModelProperty("库存百分比（已售/库存*100）")
    private Integer stockPercent;

    @ApiModelProperty("状态：0=禁用，1=启用")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}