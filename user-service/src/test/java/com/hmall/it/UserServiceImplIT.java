package com.hmall.it;

import com.hmall.api.dto.LoginFormDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.ForbiddenException;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.dto.RegisterFormDTO;
import com.hmall.domain.dto.ResetPasswordDTO;
import com.hmall.domain.po.User;
import com.hmall.domain.vo.UserLoginVO;
import com.hmall.enums.UserStatus;
import com.hmall.service.IUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/sql/data-user.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class UserServiceImplIT {

    @BeforeEach
    void setUp() { UserContext.setUser(1L); }

    @AfterEach
    void tearDown() { UserContext.removeUser(); }

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        LoginFormDTO form = new LoginFormDTO();
        form.setUsername("testuser");
        form.setPassword("admin123");

        UserLoginVO result = userService.login(form);

        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void login_wrongPassword_shouldThrow() {
        LoginFormDTO form = new LoginFormDTO();
        form.setUsername("testuser");
        form.setPassword("wrongpassword");

        assertThatThrownBy(() -> userService.login(form))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void login_frozenUser_shouldThrow() {
        User user = userService.getById(1L);
        user.setStatus(UserStatus.FROZEN);
        userService.updateById(user);

        LoginFormDTO form = new LoginFormDTO();
        form.setUsername("testuser");
        form.setPassword("admin123");

        assertThatThrownBy(() -> userService.login(form))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("用户被冻结");
    }

    @Test
    void register_withValidCode_shouldCreateUser() {
        redisTemplate.opsForValue().set("verify:code:new@test.com", "123456", Duration.ofMinutes(5));

        RegisterFormDTO form = new RegisterFormDTO();
        form.setUsername("newuser");
        form.setPassword("pass123");
        form.setEmail("new@test.com");
        form.setCode("123456");
        userService.register(form);

        User created = userService.lambdaQuery().eq(User::getUsername, "newuser").one();
        assertThat(created).isNotNull();
        assertThat(created.getEmail()).isEqualTo("new@test.com");
        assertThat(passwordEncoder.matches("pass123", created.getPassword())).isTrue();
        assertThat(redisTemplate.hasKey("verify:code:new@test.com")).isFalse();
    }

    @Test
    void register_duplicateUsername_shouldThrow() {
        redisTemplate.opsForValue().set("verify:code:testuser@test.com", "123456", Duration.ofMinutes(5));

        RegisterFormDTO form = new RegisterFormDTO();
        form.setUsername("testuser");
        form.setPassword("pass123");
        form.setEmail("testuser@test.com");
        form.setCode("123456");

        assertThatThrownBy(() -> userService.register(form))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void resetPassword_withValidCode_shouldUpdatePassword() {
        redisTemplate.opsForValue().set("verify:code:existing@test.com", "654321", Duration.ofMinutes(5));

        ResetPasswordDTO form = new ResetPasswordDTO();
        form.setEmail("existing@test.com");
        form.setCode("654321");
        form.setNewPassword("newpass123");
        userService.resetPassword(form);

        User user = userService.lambdaQuery().eq(User::getEmail, "existing@test.com").one();
        assertThat(passwordEncoder.matches("newpass123", user.getPassword())).isTrue();
        assertThat(redisTemplate.hasKey("verify:code:existing@test.com")).isFalse();
    }

    @Test
    void deductMoney_validPassword_shouldDeduct() {
        userService.deductMoney("admin123", 1000);
        User user = userService.getById(1L);
        assertThat(user.getBalance()).isEqualTo(9000);
    }
}
