package com.hmall.controller;

import com.hmall.api.dto.LoginFormDTO;
import com.hmall.common.domain.R;
import com.hmall.domain.dto.RegisterFormDTO;
import com.hmall.domain.dto.ResetPasswordDTO;
import com.hmall.domain.po.User;
import com.hmall.domain.vo.UserLoginVO;
import com.hmall.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @ApiOperation("用户登录接口")
    @PostMapping("login")
    public UserLoginVO login(@RequestBody @Validated LoginFormDTO loginFormDTO) {
        return userService.login(loginFormDTO);
    }

    @ApiOperation("扣减余额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pw", value = "支付密码"),
            @ApiImplicitParam(name = "amount", value = "支付金额")
    })
    @PutMapping("/money/deduct")
    public void deductMoney(@RequestParam("pw") String pw, @RequestParam("amount") Integer amount) {
        userService.deductMoney(pw, amount);
    }

    @ApiOperation("发送邮箱验证码")
    @PostMapping("/send-code")
    public R<Void> sendCode(@RequestParam("email") String email) {
        userService.sendVerifyCode(email);
        return R.ok();
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public R<Void> register(@RequestBody @Validated RegisterFormDTO form) {
        userService.register(form);
        return R.ok();
    }

    @ApiOperation("找回密码")
    @PostMapping("/reset-password")
    public R<Void> resetPassword(@RequestBody @Validated ResetPasswordDTO form) {
        userService.resetPassword(form);
        return R.ok();
    }

    @ApiOperation("修改个人信息")
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody User profile) {
        userService.updateProfile(profile);
        return R.ok();
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @ApiOperation("统计最近N天新增用户数")
    @GetMapping("/count-new")
    public Long countNewUsers(@RequestParam("days") Integer days) {
        return userService.countNewUsers(days);
    }
}

