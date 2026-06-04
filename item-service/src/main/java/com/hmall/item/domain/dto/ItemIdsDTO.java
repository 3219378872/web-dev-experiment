package com.hmall.item.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "商品ID列表DTO")
public class ItemIdsDTO {

    @ApiModelProperty(value = "商品ID列表", required = true)
    private List<Long> ids;
}
