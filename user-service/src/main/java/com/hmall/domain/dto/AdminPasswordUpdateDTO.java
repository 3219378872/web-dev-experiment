package com.hmall.domain.dto;

import com.hmall.domain.po.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "管理员资料与密码更新表单")
public class AdminPasswordUpdateDTO extends User {
    @ApiModelProperty("当前密码，修改密码时必填")
    private String currentPassword;
}
