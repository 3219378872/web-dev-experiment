package com.hmall.item.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "批量商品状态修改DTO")
public class ItemStatusDTO {

    @ApiModelProperty(value = "商品ID列表", required = true)
    private List<Long> ids;

    @ApiModelProperty(value = "商品状态：1-上架，2-下架，3-删除", required = true)
    private Integer status;
}
