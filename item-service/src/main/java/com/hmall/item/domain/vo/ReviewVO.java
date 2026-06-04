package com.hmall.item.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价脱敏 VO
 */
@Data
@ApiModel(description = "评价脱敏 VO")
public class ReviewVO {

    @ApiModelProperty("评价ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("商品ID")
    private Long itemId;

    @ApiModelProperty("商品名称")
    private String itemName;

    @ApiModelProperty("评价内容")
    private String content;

    @ApiModelProperty("评价图片")
    private String images;

    @ApiModelProperty("评分")
    private Integer rating;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}