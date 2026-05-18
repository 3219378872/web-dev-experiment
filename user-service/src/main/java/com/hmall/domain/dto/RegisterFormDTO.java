package com.hmall.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "注册表单")
public class RegisterFormDTO {
    @NotBlank
    @ApiModelProperty("用户名")
    private String username;
    @NotBlank
    @ApiModelProperty("密码")
    private String password;
    @NotBlank
    @ApiModelProperty("邮箱")
    private String email;
    @NotBlank
    @ApiModelProperty("验证码")
    private String code;
}
