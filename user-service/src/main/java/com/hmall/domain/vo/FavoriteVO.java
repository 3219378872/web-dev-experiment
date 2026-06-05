package com.hmall.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "收藏商品VO")
public class FavoriteVO {
    @ApiModelProperty("收藏记录id")
    private Long id;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("商品id")
    private Long itemId;

    @ApiModelProperty("商品名称")
    private String itemName;

    @ApiModelProperty("商品价格，单位为分")
    private Integer itemPrice;

    @ApiModelProperty("商品图片")
    private String itemImage;

    @ApiModelProperty("商品销量")
    private Integer itemSold;

    @ApiModelProperty("收藏时间")
    private LocalDateTime createTime;
}
