package com.hmall.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "用户状态修改DTO")
public class UserStatusDTO {
    @ApiModelProperty(value = "用户状态：1-正常，2-冻结", required = true)
    private Integer status;
}