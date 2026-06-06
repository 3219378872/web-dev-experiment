package com.hmall.controller;

import com.hmall.common.domain.R;
import com.hmall.domain.dto.AdminPasswordUpdateDTO;
import com.hmall.domain.vo.UserVO;
import com.hmall.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端个人中心")
@RestController
@RequestMapping("/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final IUserService userService;

    @ApiOperation("获取管理员个人信息")
    @GetMapping
    public UserVO getProfile() {
        return userService.getCurrentUserProfile();
    }

    @ApiOperation("修改管理员信息/密码")
    @PutMapping
    public R<Void> updateProfile(@RequestBody AdminPasswordUpdateDTO profile) {
        userService.updateProfileWithPassword(profile);
        return R.ok();
    }

    @ApiOperation("获取管理员权限码")
    @GetMapping("/permissions")
    public List<String> getPermissions() {
        return userService.getAdminPermissions();
    }
}
