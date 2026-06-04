package com.hmall.domain.vo;

import com.hmall.enums.UserStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "用户脱敏 VO")
public class UserVO {

    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("用户状态：1正常，2冻结")
    private UserStatus status;

    @ApiModelProperty("账户余额")
    private Integer balance;

    @ApiModelProperty("角色：user/admin")
    private String role;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}