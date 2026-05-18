package com.hmall.gateway.utils;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.hmall.common.exception.UnauthorizedException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTool {
    private final JWTSigner jwtSigner;

    public JwtTool(KeyPair keyPair) {
        this.jwtSigner = JWTSignerUtil.createSigner("rs256", keyPair);
    }

    /**
     * 创建 access-token
     *
     * @return access-token
     */
    public String createToken(Long userId, String role, Duration ttl) {
        // 1.生成jws
        return JWT.create()
                .setPayload("user", userId)
                .setPayload("role", role)
                .setExpiresAt(new Date(System.currentTimeMillis() + ttl.toMillis()))
                .setSigner(jwtSigner)
                .sign();
    }

    /**
     * 解析token
     *
     * @param token token
     * @return 解析刷新token得到的用户信息
     */
    public TokenInfo parseToken(String token) {
        if (token == null) {
            throw new UnauthorizedException("未登录");
        }
        JWT jwt;
        try {
            jwt = JWT.of(token).setSigner(jwtSigner);
        } catch (Exception e) {
            throw new UnauthorizedException("无效的token", e);
        }
        if (!jwt.verify()) {
            throw new UnauthorizedException("无效的token");
        }
        try {
            JWTValidator.of(jwt).validateDate();
        } catch (ValidateException e) {
            throw new UnauthorizedException("token已经过期");
        }
        Object userPayload = jwt.getPayload("user");
        Object rolePayload = jwt.getPayload("role");
        if (userPayload == null) {
            throw new UnauthorizedException("无效的token");
        }
        try {
            Long userId = Long.valueOf(userPayload.toString());
            String role = rolePayload != null ? rolePayload.toString() : "user";
            return new TokenInfo(userId, role);
        } catch (RuntimeException e) {
            throw new UnauthorizedException("无效的token");
        }
    }

    @Data
    @AllArgsConstructor
    public static class TokenInfo {
        private Long userId;
        private String role;
    }
}