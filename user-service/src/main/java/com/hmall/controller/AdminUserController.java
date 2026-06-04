package com.hmall.controller;

import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.domain.vo.UserVO;
import com.hmall.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端用户管理")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final IUserService userService;

    @ApiOperation("分页查询用户列表")
    @GetMapping
    public PageDTO<UserVO> queryUsersPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("搜索关键词") @RequestParam(required = false) String keyword) {
        return userService.queryUsersPage(page, size, keyword);
    }

    @ApiOperation("修改用户状态")
    @PutMapping("/{id}/status")
    public R<Void> updateUserStatus(
            @ApiParam("用户ID") @PathVariable Long id,
            @ApiParam("状态值（可选，不提供则自动切换）") @RequestBody(required = false) Integer status) {
        if (status == null) {
            // 如果没有提供状态值，则自动切换状态
            userService.toggleUserStatus(id);
        } else {
            // 如果提供了状态值，则使用提供的值
            userService.updateUserStatus(id, status);
        }
        return R.ok();
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    public UserVO getUserDetail(
            @ApiParam("用户ID") @PathVariable Long id) {
        return userService.getUserDetail(id);
    }
}