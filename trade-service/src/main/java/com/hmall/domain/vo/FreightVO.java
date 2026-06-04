package com.hmall.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "运费信息")
public class FreightVO {
    @ApiModelProperty("运费金额，单位为分")
    private Integer freight;
    @ApiModelProperty("是否包邮")
    private Boolean freeShipping;
}
