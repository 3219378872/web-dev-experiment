package com.hmall.service.impl;

import com.hmall.UserServiceTestBase;
import com.hmall.api.dto.LoginFormDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.exception.ForbiddenException;
import com.hmall.domain.dto.RegisterFormDTO;
import com.hmall.domain.dto.ResetPasswordDTO;
import com.hmall.domain.po.User;
import com.hmall.domain.vo.UserLoginVO;
import com.hmall.enums.UserStatus;
import com.hmall.mapper.UserMapper;
import com.hmall.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest extends UserServiceTestBase {

    @Autowired
    private IUserService userService;

    @Autowired
    private UserMapper userMapper;

    private ValueOperations<String, Object> valueOps;

    @BeforeEach
    void setUp() {
        valueOps = prepareValueOps();
        insertTestUser(1L, "testuser", "testuser@test.com", "user", UserStatus.NORMAL, 10000);
        insertTestUser(2L, "existing", "existing@test.com", "user", UserStatus.NORMAL, 5000);
        insertTestUser(3L, "frozen", "frozen@test.com", "user", UserStatus.FROZEN, 0);
    }

    private void insertTestUser(Long id, String username, String email, String role, UserStatus status, int balance) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(status);
        user.setBalance(balance);
        user.setCreateTime(LocalDateTime.now());
        userService.save(user);
    }

    private LoginFormDTO loginForm(String u, String p) {
        LoginFormDTO dto = new LoginFormDTO();
        dto.setUsername(u);
        dto.setPassword(p);
        return dto;
    }

    // ───────────────────── login ─────────────────────

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("正常登录-返回token和用户信息")
        void login_success() {
            UserLoginVO vo = userService.login(loginForm("testuser", "admin123"));

            assertThat(vo.getToken()).isNotBlank();
            assertThat(vo.getUserId()).isEqualTo(1L);
            assertThat(vo.getUsername()).isEqualTo("testuser");
            assertThat(vo.getBalance()).isEqualTo(10000);
        }

        @Test
        @DisplayName("用户名不存在 → IllegalArgumentException")
        void userNotFound_throws() {
            assertThatThrownBy(() -> userService.login(loginForm("ghost", "admin123")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("用户被冻结 → ForbiddenException")
        void frozenUser_throws() {
            assertThatThrownBy(() -> userService.login(loginForm("frozen", "admin123")))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("用户被冻结");
        }

        @Test
        @DisplayName("密码错误 → BadRequestException")
        void wrongPassword_throws() {
            assertThatThrownBy(() -> userService.login(loginForm("testuser", "wrong")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("用户名或密码错误");
        }

        @Test
        @DisplayName("role为null-默认使用user")
        void login_nullRole_defaultsToUser() {
            // 将 testuser 的 role 设为 null
            User user = userService.getById(1L);
            user.setRole(null);
            userService.updateById(user);

            UserLoginVO vo = userService.login(loginForm("testuser", "admin123"));

            assertThat(vo.getToken()).isNotBlank();
            assertThat(vo.getUserId()).isEqualTo(1L);
        }
    }

    // ───────────────────── deductMoney ─────────────────────

    @Nested
    @DisplayName("deductMoney")
    class DeductMoneyTests {

        @Test
        @DisplayName("扣款成功-余额减少")
        void deduct_success() {
            // UserContext 已在 setUp 中设为 TEST_USER_ID=1
            userService.deductMoney("admin123", 1000);

            User user = userService.getById(1L);
            assertThat(user.getBalance()).isEqualTo(9000);
        }

        @Test
        @DisplayName("用户不存在 → BizIllegalException")
        void userNotFound_throws() {
            // 切换到不存在的用户
            com.hmall.common.utils.UserContext.setUser(999L);

            assertThatThrownBy(() -> userService.deductMoney("admin123", 100))
                    .isInstanceOf(BizIllegalException.class)
                    .hasMessageContaining("用户密码错误");
        }

        @Test
        @DisplayName("密码不匹配 → BizIllegalException")
        void wrongPassword_throws() {
            assertThatThrownBy(() -> userService.deductMoney("wrongpw", 100))
                    .isInstanceOf(BizIllegalException.class)
                    .hasMessageContaining("用户密码错误");
        }
    }

    // ───────────────────── sendVerifyCode ─────────────────────

    @Nested
    @DisplayName("sendVerifyCode")
    class SendVerifyCodeTests {

        @Test
        @DisplayName("mailSender为null-直接返回不发送")
        void mailSenderNull_returns() {
            // 保存原始值
            Object original = org.springframework.test.util.ReflectionTestUtils.getField(userService, "mailSender");
            try {
                ReflectionTestUtils.setField(userService, "mailSender", null);

                userService.sendVerifyCode("test@test.com");

                verify(mailSender, org.mockito.Mockito.never()).send(any(org.springframework.mail.SimpleMailMessage.class));
            } finally {
                ReflectionTestUtils.setField(userService, "mailSender", original);
            }
        }

        @Test
        @DisplayName("正常发送-存入Redis并发送邮件")
        void sendsEmail_andStoresCode() {
            ReflectionTestUtils.setField(userService, "mailFrom", "noreply@hmall.com");

            userService.sendVerifyCode("user@test.com");

            verify(valueOps).set(
                    eq("verify:code:user@test.com"),
                    any(String.class),
                    eq(Duration.ofMinutes(5)));
            verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
        }
    }

    // ───────────────────── register ─────────────────────

    @Nested
    @DisplayName("register")
    class RegisterTests {

        private RegisterFormDTO form(String username, String password, String email, String code) {
            RegisterFormDTO dto = new RegisterFormDTO();
            dto.setUsername(username);
            dto.setPassword(password);
            dto.setEmail(email);
            dto.setCode(code);
            return dto;
        }

        @Test
        @DisplayName("验证码为null → BadRequestException")
        void nullCode_throws() {
            when(valueOps.get("verify:code:test@test.com")).thenReturn(null);

            assertThatThrownBy(() -> userService.register(form("newuser", "pw", "test@test.com", "123")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("验证码错误");
        }

        @Test
        @DisplayName("验证码不匹配 → BadRequestException")
        void wrongCode_throws() {
            when(valueOps.get("verify:code:test@test.com")).thenReturn("999999");

            assertThatThrownBy(() -> userService.register(form("newuser", "pw", "test@test.com", "123456")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("验证码错误");
        }

        @Test
        @DisplayName("用户名已存在 → BadRequestException")
        void duplicateUsername_throws() {
            when(valueOps.get("verify:code:test@test.com")).thenReturn("123456");

            assertThatThrownBy(() -> userService.register(form("testuser", "pw", "test@test.com", "123456")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("用户名已存在");
        }

        @Test
        @DisplayName("注册成功-保存用户并删除验证码")
        void register_success() {
            when(valueOps.get("verify:code:new@test.com")).thenReturn("123456");

            userService.register(form("newuser", "pass123", "new@test.com", "123456"));

            User created = userService.lambdaQuery().eq(User::getUsername, "newuser").one();
            assertThat(created).isNotNull();
            assertThat(created.getEmail()).isEqualTo("new@test.com");
            assertThat(created.getRole()).isEqualTo("user");
            assertThat(created.getStatus()).isEqualTo(UserStatus.NORMAL);
            assertThat(passwordEncoder.matches("pass123", created.getPassword())).isTrue();
            verify(redisTemplate).delete("verify:code:new@test.com");
        }
    }

    // ───────────────────── resetPassword ─────────────────────

    @Nested
    @DisplayName("resetPassword")
    class ResetPasswordTests {

        private ResetPasswordDTO form(String email, String code, String newPw) {
            ResetPasswordDTO dto = new ResetPasswordDTO();
            dto.setEmail(email);
            dto.setCode(code);
            dto.setNewPassword(newPw);
            return dto;
        }

        @Test
        @DisplayName("验证码为null → BadRequestException")
        void nullCode_throws() {
            when(valueOps.get("verify:code:test@test.com")).thenReturn(null);

            assertThatThrownBy(() -> userService.resetPassword(form("test@test.com", "123", "newpw")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("验证码错误");
        }

        @Test
        @DisplayName("验证码不匹配 → BadRequestException")
        void wrongCode_throws() {
            when(valueOps.get("verify:code:test@test.com")).thenReturn("999999");

            assertThatThrownBy(() -> userService.resetPassword(form("test@test.com", "123456", "newpw")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("验证码错误");
        }

        @Test
        @DisplayName("邮箱未注册 → BadRequestException")
        void emailNotRegistered_throws() {
            when(valueOps.get("verify:code:ghost@test.com")).thenReturn("123456");

            assertThatThrownBy(() -> userService.resetPassword(form("ghost@test.com", "123456", "newpw")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("邮箱未注册");
        }

        @Test
        @DisplayName("重置成功-密码更新并删除验证码")
        void reset_success() {
            when(valueOps.get("verify:code:existing@test.com")).thenReturn("123456");

            userService.resetPassword(form("existing@test.com", "123456", "newpass"));

            User user = userService.lambdaQuery().eq(User::getEmail, "existing@test.com").one();
            assertThat(passwordEncoder.matches("newpass", user.getPassword())).isTrue();
            verify(redisTemplate).delete("verify:code:existing@test.com");
        }
    }

    // ───────────────────── updateProfile ─────────────────────

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfileTests {

        @Test
        @DisplayName("所有字段有值-全部更新")
        void allFields_updatesAll() {
            User profile = new User();
            profile.setNickname("新昵称");
            profile.setAvatar("avatar.png");
            profile.setEmail("newemail@test.com");

            userService.updateProfile(profile);

            User updated = userService.getById(1L);
            assertThat(updated.getNickname()).isEqualTo("新昵称");
            assertThat(updated.getAvatar()).isEqualTo("avatar.png");
            assertThat(updated.getEmail()).isEqualTo("newemail@test.com");
        }

        @Test
        @DisplayName("所有字段为空白-不修改")
        void allBlank_noChange() {
            User before = userService.getById(1L);
            String origNickname = before.getNickname();
            String origAvatar = before.getAvatar();

            User profile = new User();
            profile.setNickname("");
            profile.setAvatar("  ");
            profile.setEmail(null);

            userService.updateProfile(profile);

            User after = userService.getById(1L);
            assertThat(after.getNickname()).isEqualTo(origNickname);
            assertThat(after.getAvatar()).isEqualTo(origAvatar);
        }

        @Test
        @DisplayName("部分字段有值-仅更新有值字段")
        void partialUpdate() {
            User before = userService.getById(1L);
            String origAvatar = before.getAvatar();

            User profile = new User();
            profile.setNickname("新昵称");
            profile.setAvatar(null);
            profile.setEmail("  ");

            userService.updateProfile(profile);

            User after = userService.getById(1L);
            assertThat(after.getNickname()).isEqualTo("新昵称");
            assertThat(after.getAvatar()).isEqualTo(origAvatar);
        }
    }
}
