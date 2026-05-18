package com.hmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.api.dto.LoginFormDTO;
import com.hmall.domain.dto.RegisterFormDTO;
import com.hmall.domain.dto.ResetPasswordDTO;
import com.hmall.domain.po.User;
import com.hmall.domain.vo.UserLoginVO;

public interface IUserService extends IService<User> {

    UserLoginVO login(LoginFormDTO loginFormDTO);

    void deductMoney(String pw, Integer totalFee);

    void sendVerifyCode(String email);

    void register(RegisterFormDTO form);

    void resetPassword(ResetPasswordDTO form);

    void updateProfile(User profile);
}
