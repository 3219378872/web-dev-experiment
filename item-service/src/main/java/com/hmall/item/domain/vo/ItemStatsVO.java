package com.hmall.item.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "管理端商品统计")
public class ItemStatsVO {
    @ApiModelProperty("商品总数（不含已删除）")
    private Long total;
    @ApiModelProperty("在售数量")
    private Long onSale;
    @ApiModelProperty("已下架数量")
    private Long offSale;
    @ApiModelProperty("库存预警数量（库存<20且在售）")
    private Long lowStock;
}
