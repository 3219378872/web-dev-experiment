package com.hmall.gateway.filters;

import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter , Ordered {
    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request
        ServerHttpRequest request = exchange.getRequest();
        //判断是否需要拦截
        if(isExclude(request.getPath().toString())){
            return chain.filter(exchange);
        }
        //获取token校验
        String token = null;
        List<String> authorization = request.getHeaders().get("authorization");
        if(Objects.nonNull(authorization) && !authorization.isEmpty()){
            token = authorization.get(0);
        }
        JwtTool.TokenInfo tokenInfo = null;
        try {
            tokenInfo = jwtTool.parseToken(token);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // admin 路径校验角色
        if (antPathMatcher.match("/admin/**", request.getPath().toString())
                && !"admin".equals(tokenInfo.getRole())) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        //传递用户信息
        String userInfo = tokenInfo.getUserId().toString();
        ServerWebExchange webExchange = exchange.mutate()
                .request(builder -> builder
                        .header("user-info", userInfo)
                        .header("role-info", tokenInfo.getRole()))
                .build();
        //放行
        return chain.filter(webExchange);
    }

    private boolean isExclude(String path) {
        List<String> excludePaths = authProperties.getExcludePaths();
        for (String excludePath : excludePaths) {
            if(antPathMatcher.match(excludePath,path)){
               return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
