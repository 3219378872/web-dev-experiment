package com.hmall.utils;

import com.hmall.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * user-service 自有 JwtTool 与 hm-gateway 的实现一致（约定双侧用同一公私钥对，
 * gateway 解析 user-service 签发的 token），本测试覆盖签发/解析/异常分支。
 * 任何一侧改变 payload 字段约定都必须同步另一侧 + 本测试。
 */
class JwtToolTest {

    private static KeyPair keyPair;
    private static JwtTool jwtTool;

    @BeforeAll
    static void generateKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();
        jwtTool = new JwtTool(keyPair);
    }

    @Test
    @DisplayName("createToken + parseToken 往返：userId 与 role 透传")
    void roundTrip_preservesUserIdAndRole() {
        String token = jwtTool.createToken(123L, "admin", Duration.ofMinutes(10));
        JwtTool.TokenInfo info = jwtTool.parseToken(token);
        assertThat(info.getUserId()).isEqualTo(123L);
        assertThat(info.getRole()).isEqualTo("admin");
    }

    @Test
    @DisplayName("parseToken(null) -> 未登录")
    void parseToken_null_throws() {
        assertThatThrownBy(() -> jwtTool.parseToken(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("未登录");
    }

    @Test
    @DisplayName("parseToken 任意非法字符串 -> 无效的 token")
    void parseToken_garbage_throws() {
        assertThatThrownBy(() -> jwtTool.parseToken("xxx.yyy.zzz"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("无效的token");
    }

    @Test
    @DisplayName("parseToken 已过期 token -> token已经过期")
    void parseToken_expired_throws() {
        String expired = cn.hutool.jwt.JWT.create()
                .setPayload("user", 1L)
                .setPayload("role", "user")
                .setExpiresAt(new java.util.Date(System.currentTimeMillis() - 60_000))
                .setSigner(cn.hutool.jwt.signers.JWTSignerUtil.createSigner("rs256", keyPair))
                .sign();
        assertThatThrownBy(() -> jwtTool.parseToken(expired))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("token已经过期");
    }
}
