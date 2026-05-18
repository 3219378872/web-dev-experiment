package com.hmall.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "评价DTO")
public class ReviewDTO {
    @ApiModelProperty("评价id")
    private Long id;
    @ApiModelProperty("用户id")
    private Long userId;
    @ApiModelProperty("商品id")
    private Long itemId;
    @ApiModelProperty("评价内容")
    private String content;
    @ApiModelProperty("评价图片JSON数组")
    private String images;
    @ApiModelProperty("评分 1-5")
    private Integer rating;
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
