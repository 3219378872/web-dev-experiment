package com.hmall.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "退款审核请求")
public class RefundAuditDTO {
    @NotNull(message = "审核结果不能为空")
    @ApiModelProperty("是否通过 true-通过 false-驳回")
    private Boolean approved;
    @ApiModelProperty("审核原因")
    private String reason;
}
