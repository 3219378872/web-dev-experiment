package com.hmall.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "重置密码表单")
public class ResetPasswordDTO {
    @NotBlank
    @ApiModelProperty("邮箱")
    private String email;
    @NotBlank
    @ApiModelProperty("验证码")
    private String code;
    @NotBlank
    @ApiModelProperty("新密码")
    private String newPassword;
}
