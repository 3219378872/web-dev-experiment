package com.hmall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.dto.LoginFormDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.exception.ForbiddenException;
import com.hmall.common.utils.UserContext;
import com.hmall.config.JwtProperties;
import com.hmall.domain.dto.AdminPasswordUpdateDTO;
import com.hmall.domain.dto.RegisterFormDTO;
import com.hmall.domain.dto.ResetPasswordDTO;
import com.hmall.domain.po.User;
import com.hmall.domain.vo.UserLoginVO;
import com.hmall.domain.vo.UserVO;
import com.hmall.enums.UserStatus;
import com.hmall.mapper.UserMapper;
import com.hmall.service.IUserService;
import com.hmall.utils.JwtTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTool jwtTool;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private JavaMailSender mailSender;
    @Value("${spring.mail.username:}")
    private String mailFrom;

    public UserServiceImpl(PasswordEncoder passwordEncoder, JwtTool jwtTool, JwtProperties jwtProperties, RedisTemplate<String, Object> redisTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.jwtTool = jwtTool;
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public UserLoginVO login(LoginFormDTO loginDTO) {
        // 1.数据校验
        String identifier = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        if (StrUtil.isBlank(identifier) || StrUtil.isBlank(password)) {
            throw new BadRequestException("用户名或密码错误");
        }
        // 2.支持用户名 / 邮箱 / 手机号三种登录标识，命中任一即可
        User user = lambdaQuery()
                .eq(User::getUsername, identifier)
                .or().eq(User::getEmail, identifier)
                .or().eq(User::getPhone, identifier)
                .last("limit 1")
                .one();
        if (user == null) {
            throw new BadRequestException("用户名或密码错误");
        }
        // 3.校验是否禁用
        if (user.getStatus() == UserStatus.FROZEN) {
            throw new ForbiddenException("用户被冻结");
        }
        // 4.校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }
        // 5.生成TOKEN
        String role = user.getRole() != null ? user.getRole() : "user";
        String token = jwtTool.createToken(user.getId(), role, jwtProperties.getTokenTTL());
        // 6.封装VO返回
        UserLoginVO vo = new UserLoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        vo.setToken(token);
        vo.setRole(role);
        return vo;
    }

    @Override
    @Transactional
    public void deductMoney(String pw, Integer totalFee) {
        log.info("开始扣款");
        // 1.校验密码
        User user = getById(UserContext.getUser());
        if(user == null || !passwordEncoder.matches(pw, user.getPassword())){
            // 密码错误
            throw new BizIllegalException("用户密码错误");
        }

        // 2.尝试扣款
        try {
            baseMapper.updateMoney(UserContext.getUser(), totalFee);
        } catch (Exception e) {
            throw new RuntimeException("扣款失败，可能是余额不足！", e);
        }
        log.info("扣款成功");
    }

    @Override
    public void sendVerifyCode(String email) {
        if (mailSender == null) {
            log.warn("MailSender not configured, cannot send verification code");
            return;
        }
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        redisTemplate.opsForValue().set("verify:code:" + email, code, Duration.ofMinutes(5));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("hmall 验证码");
        message.setText("您的验证码是：" + code + "，5分钟内有效。");
        mailSender.send(message);
    }

    @Override
    public void register(RegisterFormDTO form) {
        String cachedCode = (String) redisTemplate.opsForValue().get("verify:code:" + form.getEmail());
        if (cachedCode == null || !cachedCode.equals(form.getCode())) {
            throw new BadRequestException("验证码错误或已过期");
        }
        if (lambdaQuery().eq(User::getUsername, form.getUsername()).count() > 0) {
            throw new BadRequestException("用户名已存在");
        }
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setEmail(form.getEmail());
        user.setRole("user");
        user.setStatus(UserStatus.NORMAL);
        user.setCreateTime(LocalDateTime.now());
        save(user);
        redisTemplate.delete("verify:code:" + form.getEmail());
    }

    @Override
    public void resetPassword(ResetPasswordDTO form) {
        String cachedCode = (String) redisTemplate.opsForValue().get("verify:code:" + form.getEmail());
        if (cachedCode == null || !cachedCode.equals(form.getCode())) {
            throw new BadRequestException("验证码错误或已过期");
        }
        User user = lambdaQuery().eq(User::getEmail, form.getEmail()).one();
        if (user == null) {
            throw new BadRequestException("邮箱未注册");
        }
        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        updateById(user);
        redisTemplate.delete("verify:code:" + form.getEmail());
    }

    @Override
    public void updateProfile(User profile) {
        User user = getById(UserContext.getUser());
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        if (StrUtil.isNotBlank(profile.getNickname())) {
            user.setNickname(profile.getNickname());
        }
        if (StrUtil.isNotBlank(profile.getAvatar())) {
            user.setAvatar(profile.getAvatar());
        }
        if (StrUtil.isNotBlank(profile.getEmail())) {
            user.setEmail(profile.getEmail());
        }
        if (StrUtil.isNotBlank(profile.getGender())) {
            user.setGender(profile.getGender());
        }
        if (profile.getBirthday() != null) {
            user.setBirthday(profile.getBirthday());
        }
        if (StrUtil.isNotBlank(profile.getPassword())) {
            user.setPassword(passwordEncoder.encode(profile.getPassword()));
        }
        updateById(user);
    }

    @Override
    public UserVO getCurrentUserProfile() {
        User user = getById(UserContext.getUser());
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        return convertToUserVO(user);
    }

    @Override
    public PageDTO<UserVO> queryUsersPage(Integer page, Integer size, String keyword) {
        return queryUsersPage(page, size, keyword, null);
    }

    @Override
    public PageDTO<UserVO> queryUsersPage(Integer page, Integer size, String keyword, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, UserStatus.of(status));
        }

        // 执行分页查询
        Page<User> pageParam = new Page<>(page, size);
        Page<User> result = page(pageParam, wrapper);

        // 转换为 UserVO（脱敏）
        List<UserVO> voList = result.getRecords().stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());

        return new PageDTO<>(result.getTotal(), result.getPages(), voList);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        // 验证状态值
        if (status != UserStatus.NORMAL.getValue() && status != UserStatus.FROZEN.getValue()) {
            throw new BadRequestException("无效的用户状态");
        }

        // 查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // 更新状态
        user.setStatus(UserStatus.of(status));
        updateById(user);
    }

    @Override
    public void toggleUserStatus(Long userId) {
        // 查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // 切换状态
        if (user.getStatus() == UserStatus.NORMAL) {
            user.setStatus(UserStatus.FROZEN);
        } else {
            user.setStatus(UserStatus.NORMAL);
        }
        updateById(user);
    }

    @Override
    public UserVO getUserDetail(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        return convertToUserVO(user);
    }

    @Override
    public void updateProfileWithPassword(User profile) {
        User user = getById(UserContext.getUser());
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        if (StrUtil.isNotBlank(profile.getPassword())) {
            if (!(profile instanceof AdminPasswordUpdateDTO)) {
                throw new BadRequestException("当前密码不能为空");
            }
            String currentPassword = ((AdminPasswordUpdateDTO) profile).getCurrentPassword();
            if (StrUtil.isBlank(currentPassword)) {
                throw new BadRequestException("当前密码不能为空");
            }
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new BadRequestException("当前密码错误");
            }
        }
        updateProfile(profile);
    }

    @Override
    public List<String> getAdminPermissions() {
        // 获取当前登录用户
        Long userId = UserContext.getUser();
        User user = getById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // 基于角色返回权限码
        List<String> permissions = new ArrayList<>();
        if ("admin".equals(user.getRole())) {
            // 管理员拥有所有权限
            permissions.add("user:manage");
            permissions.add("item:manage");
            permissions.add("order:manage");
            permissions.add("coupon:manage");
            permissions.add("notification:manage");
            permissions.add("feedback:manage");
            permissions.add("banner:manage");
            permissions.add("dashboard:view");
        } else {
            // 普通用户无管理权限
            // 可以根据需求扩展更细粒度的权限
        }

        return permissions;
    }

    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        // 不设置 password，实现脱敏
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setBalance(user.getBalance());
        vo.setRole(user.getRole());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setNickname(user.getNickname());
        vo.setGender(user.getGender());
        vo.setBirthday(user.getBirthday());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }

    @Override
    public Long countNewUsers(Integer days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return lambdaQuery()
                .ge(User::getCreateTime, since)
                .count();
    }
}
