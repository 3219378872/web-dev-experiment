package com.hmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.api.dto.LoginFormDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.domain.dto.RegisterFormDTO;
import com.hmall.domain.dto.ResetPasswordDTO;
import com.hmall.domain.po.User;
import com.hmall.domain.vo.UserLoginVO;
import com.hmall.domain.vo.UserVO;

import java.util.List;

public interface IUserService extends IService<User> {

    UserLoginVO login(LoginFormDTO loginFormDTO);

    void deductMoney(String pw, Integer totalFee);

    void sendVerifyCode(String email);

    void register(RegisterFormDTO form);

    void resetPassword(ResetPasswordDTO form);

    void updateProfile(User profile);

    /**
     * 分页查询用户列表（管理端）
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词（模糊匹配 username/phone）
     * @return 用户分页数据
     */
    PageDTO<UserVO> queryUsersPage(Integer page, Integer size, String keyword);

    /**
     * 修改用户状态（管理端）
     * @param userId 用户ID
     * @param status 状态值（1=正常，2=冻结）
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 切换用户状态（管理端）
     * @param userId 用户ID
     */
    void toggleUserStatus(Long userId);

    /**
     * 获取用户详情（管理端，脱敏）
     * @param userId 用户ID
     * @return 用户脱敏信息
     */
    UserVO getUserDetail(Long userId);

    /**
     * 更新用户个人资料（支持密码修改）
     * @param profile 用户资料
     */
    void updateProfileWithPassword(User profile);

    /**
     * 获取管理员权限码列表
     * @return 权限码列表
     */
    List<String> getAdminPermissions();
}
