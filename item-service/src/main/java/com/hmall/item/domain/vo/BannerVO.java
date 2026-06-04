package com.hmall.item.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图/广告位 VO
 */
@Data
@ApiModel(description = "轮播图/广告位 VO")
public class BannerVO {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("图片URL")
    private String imageUrl;

    @ApiModelProperty("链接URL")
    private String linkUrl;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("类型：carousel=轮播图，ad=广告位")
    private String type;

    @ApiModelProperty("广告位槽位标识")
    private String position;

    @ApiModelProperty("排序值（越小越靠前）")
    private Integer sort;

    @ApiModelProperty("状态：0=禁用，1=启用")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}