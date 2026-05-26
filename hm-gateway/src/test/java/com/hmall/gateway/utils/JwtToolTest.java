package com.hmall.gateway.utils;

import com.hmall.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("createToken + parseToken 往返：userId/role 准确还原")
    void roundTrip_preservesUserIdAndRole() {
        String token = jwtTool.createToken(42L, "admin", Duration.ofMinutes(5));
        JwtTool.TokenInfo info = jwtTool.parseToken(token);
        assertThat(info.getUserId()).isEqualTo(42L);
        assertThat(info.getRole()).isEqualTo("admin");
    }

    @Test
    @DisplayName("role payload 为 null 时回退为 \"user\"")
    void parseToken_nullRole_defaultsToUser() {
        // 直接构造 payload role=null 的 token：用 cn.hutool 的 JWT
        String token = cn.hutool.jwt.JWT.create()
                .setPayload("user", 7L)
                .setExpiresAt(new java.util.Date(System.currentTimeMillis() + 60_000))
                .setSigner(cn.hutool.jwt.signers.JWTSignerUtil.createSigner("rs256", keyPair))
                .sign();
        JwtTool.TokenInfo info = jwtTool.parseToken(token);
        assertThat(info.getUserId()).isEqualTo(7L);
        assertThat(info.getRole()).isEqualTo("user");
    }

    @Test
    @DisplayName("parseToken(null) -> 401 UnauthorizedException(\"未登录\")")
    void parseToken_null_throwsUnauthorizedNotLoggedIn() {
        assertThatThrownBy(() -> jwtTool.parseToken(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("未登录");
    }

    @Test
    @DisplayName("parseToken(\"garbage\") -> 无效的 token")
    void parseToken_garbage_throwsUnauthorizedInvalid() {
        assertThatThrownBy(() -> jwtTool.parseToken("not.a.real.jwt"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("无效的token");
    }

    @Test
    @DisplayName("parseToken 用不同密钥签发的 token -> 验证失败")
    void parseToken_signedByAnotherKey_throwsUnauthorized() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair otherKey = gen.generateKeyPair();
        JwtTool other = new JwtTool(otherKey);
        String foreignToken = other.createToken(1L, "user", Duration.ofMinutes(1));

        assertThatThrownBy(() -> jwtTool.parseToken(foreignToken))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("parseToken 已过期 token -> token已经过期")
    void parseToken_expired_throwsUnauthorizedExpired() {
        // 构造一个 expiresAt 明显在过去的 token，跳过 JWTValidator 的秒级 leeway
        String expired = cn.hutool.jwt.JWT.create()
                .setPayload("user", 9L)
                .setPayload("role", "user")
                .setExpiresAt(new java.util.Date(System.currentTimeMillis() - 60_000))
                .setSigner(cn.hutool.jwt.signers.JWTSignerUtil.createSigner("rs256", keyPair))
                .sign();
        assertThatThrownBy(() -> jwtTool.parseToken(expired))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("token已经过期");
    }

    @Test
    @DisplayName("parseToken payload user 缺失 -> 无效的token")
    void parseToken_missingUserPayload_throwsUnauthorized() {
        String token = cn.hutool.jwt.JWT.create()
                .setPayload("role", "user")
                .setExpiresAt(new java.util.Date(System.currentTimeMillis() + 60_000))
                .setSigner(cn.hutool.jwt.signers.JWTSignerUtil.createSigner("rs256", keyPair))
                .sign();
        assertThatThrownBy(() -> jwtTool.parseToken(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("无效的token");
    }

    @Test
    @DisplayName("parseToken payload user 非数字 -> 无效的token")
    void parseToken_nonNumericUser_throwsUnauthorized() {
        String token = cn.hutool.jwt.JWT.create()
                .setPayload("user", "alice")
                .setPayload("role", "user")
                .setExpiresAt(new java.util.Date(System.currentTimeMillis() + 60_000))
                .setSigner(cn.hutool.jwt.signers.JWTSignerUtil.createSigner("rs256", keyPair))
                .sign();
        assertThatThrownBy(() -> jwtTool.parseToken(token))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("无效的token");
    }
}
