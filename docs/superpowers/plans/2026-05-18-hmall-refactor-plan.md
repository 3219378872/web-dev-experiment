# hmall 电商平台重构实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构 hmall 项目，使其满足《Web开发技术》实验指导书全部要求（基本 + 进阶 + 拓展），达到满分标准。

**Architecture:** Spring Cloud 微服务（Gateway + 7 微服务）+ Vue 3 双前端（客户端 + 管理后台）。在现有 5 个微服务基础上扩展功能，新增 notify-service 和 file-service。前端独立项目通过 Nginx 部署，后端通过 Gateway 统一入口。

**Tech Stack:** Spring Boot 2.7.12 + Spring Cloud 2021.0.3 + MyBatis-Plus 3.4.3 + Nacos + OpenFeign + Redis + RabbitMQ + Vue 3 + Vite + Element Plus + Pinia + Docker Compose

---

## Phase 1: 后端基础设施

### Task 1.1: 扩展 JwtTool 支持角色字段

**Files:**
- Modify: `hm-gateway/src/main/java/com/hmall/gateway/utils/JwtTool.java`
- Modify: `user-service/src/main/java/com/hmall/utils/JwtTool.java`

- [ ] **Step 1: 修改 Gateway JwtTool — 创建 token 时包含 role**

```java
// hm-gateway/src/main/java/com/hmall/gateway/utils/JwtTool.java

// 修改 createToken 方法签名，增加 role 参数
public String createToken(Long userId, String role, Duration ttl) {
    return JWT.create()
            .setPayload("user", userId)
            .setPayload("role", role)
            .setExpiresAt(new Date(System.currentTimeMillis() + ttl.toMillis()))
            .setSigner(jwtSigner)
            .sign();
}

// 新增内部类作为解析结果
@Data
@AllArgsConstructor
public static class TokenInfo {
    private Long userId;
    private String role;
}

// 修改 parseToken 返回 TokenInfo
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
```

- [ ] **Step 2: 同步修改 user-service 的 JwtTool（同上，两个文件保持一致的逻辑）**

- [ ] **Step 3: Commit**

```bash
git add hm-gateway/src/main/java/com/hmall/gateway/utils/JwtTool.java \
        user-service/src/main/java/com/hmall/utils/JwtTool.java
git commit -m "feat: JwtTool 扩展 role 字段支持 user/admin 角色区分"
```

---

### Task 1.2: 改造网关 AuthGlobalFilter 支持 /admin/** 角色鉴权

**Files:**
- Modify: `hm-gateway/src/main/java/com/hmall/gateway/filters/AuthGlobalFilter.java`
- Modify: `hm-gateway/src/main/resources/application.yaml`

- [ ] **Step 1: 修改 AuthGlobalFilter 增加角色校验**

```java
// hm-gateway/src/main/java/com/hmall/gateway/filters/AuthGlobalFilter.java

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // 1. 判断是否免登录路径
        if (isExclude(path)) {
            return chain.filter(exchange);
        }

        // 2. 获取并校验 token
        String token = null;
        List<String> authorization = request.getHeaders().get("authorization");
        if (authorization != null && !authorization.isEmpty()) {
            token = authorization.get(0);
        }
        JwtTool.TokenInfo tokenInfo;
        try {
            tokenInfo = jwtTool.parseToken(token);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 3. admin 路径校验角色
        if (antPathMatcher.match("/admin/**", path) && !"admin".equals(tokenInfo.getRole())) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 4. 传递用户信息到下游
        String userInfo = tokenInfo.getUserId().toString();
        ServerWebExchange webExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        return chain.filter(webExchange);
    }

    private boolean isExclude(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() { return 0; }
}
```

- [ ] **Step 2: 更新网关配置，扩展放行路径**

```yaml
# hm-gateway/src/main/resources/application.yaml
server:
  port: 8080
hm:
  jwt:
    location: classpath:hmall.jks
    alias: hmall
    password: hmall123
    tokenTTL: 30m
  auth:
    excludePaths:
      - /search/**
      - /users/login
      - /users/register
      - /users/send-code
      - /users/reset-password
      - /items/**
      - /hi
      - /notifications/active
      - /upload/**
      - /files/**
```

- [ ] **Step 3: Commit**

```bash
git add hm-gateway/src/main/java/com/hmall/gateway/filters/AuthGlobalFilter.java \
        hm-gateway/src/main/resources/application.yaml
git commit -m "feat: 网关增加 /admin/** 角色鉴权，扩展公开路径"
```

---

### Task 1.3: hm-common 添加 Redis 配置

**Files:**
- Modify: `hm-common/pom.xml` (添加 spring-boot-starter-data-redis 依赖，scope=provided)
- Create: `hm-common/src/main/java/com/hmall/common/config/RedisConfig.java`
- Modify: `hm-common/src/main/resources/META-INF/spring.factories`

- [ ] **Step 1: 创建 RedisConfig**

```java
// hm-common/src/main/java/com/hmall/common/config/RedisConfig.java
package com.hmall.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);

        Jackson2JsonRedisSerializer<Object> jacksonSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jacksonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jacksonSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
```

- [ ] **Step 2: 注册到 spring.factories**

```
# hm-common/src/main/resources/META-INF/spring.factories (追加内容)
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.hmall.common.config.RedisConfig
```

- [ ] **Step 3: Commit**

```bash
git add hm-common/src/main/java/com/hmall/common/config/RedisConfig.java \
        hm-common/src/main/resources/META-INF/spring.factories
git commit -m "feat: hm-common 添加 Redis 序列化配置（Jackson）"
```

---

### Task 1.4: hm-api 新增 Feign 客户端接口

**Files:**
- Create: `hm-api/src/main/java/com/hmall/api/client/ReviewClient.java`
- Create: `hm-api/src/main/java/com/hmall/api/client/FavoriteClient.java`
- Create: `hm-api/src/main/java/com/hmall/api/client/CouponClient.java`
- Create: `hm-api/src/main/java/com/hmall/api/client/NotificationClient.java`
- Create: `hm-api/src/main/java/com/hmall/api/client/FileClient.java`
- Create: `hm-api/src/main/java/com/hmall/api/dto/ReviewDTO.java`
- Create: `hm-api/src/main/java/com/hmall/api/dto/CouponDTO.java`

- [ ] **Step 1: 创建 DTO**

```java
// hm-api/src/main/java/com/hmall/api/dto/ReviewDTO.java
package com.hmall.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "评价DTO")
public class ReviewDTO {
    @ApiModelProperty("评价id")
    private Long id;
    @ApiModelProperty("用户id")
    private Long userId;
    @ApiModelProperty("商品id")
    private Long itemId;
    @ApiModelProperty("评价内容")
    private String content;
    @ApiModelProperty("评价图片")
    private String images;
    @ApiModelProperty("评分 1-5")
    private Integer rating;
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
```

```java
// hm-api/src/main/java/com/hmall/api/dto/CouponDTO.java
package com.hmall.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "优惠券DTO")
public class CouponDTO {
    @ApiModelProperty("优惠券id")
    private Long id;
    @ApiModelProperty("优惠券名称")
    private String name;
    @ApiModelProperty("优惠券描述")
    private String description;
    @ApiModelProperty("优惠类型 1-满减 2-折扣")
    private Integer discountType;
    @ApiModelProperty("减免金额(分) 或 折扣百分比")
    private Integer discountValue;
    @ApiModelProperty("最低消费金额(分)")
    private Integer minAmount;
    @ApiModelProperty("剩余数量")
    private Integer remainingStock;
    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;
    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;
}
```

- [ ] **Step 2: 创建 Feign 客户端接口**

```java
// hm-api/src/main/java/com/hmall/api/client/ReviewClient.java
package com.hmall.api.client;

import com.hmall.api.dto.ReviewDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("item-service")
public interface ReviewClient {

    @GetMapping("/items/{itemId}/reviews")
    List<ReviewDTO> getReviews(@PathVariable("itemId") Long itemId);

    @PostMapping("/items/{itemId}/reviews")
    void saveReview(@PathVariable("itemId") Long itemId, @RequestBody ReviewDTO review);

    @DeleteMapping("/admin/reviews/{id}")
    void deleteReview(@PathVariable("id") Long id);
}
```

```java
// hm-api/src/main/java/com/hmall/api/client/FavoriteClient.java
package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("user-service")
public interface FavoriteClient {

    @PostMapping("/favorites")
    void addFavorite(@RequestParam("itemId") Long itemId);

    @DeleteMapping("/favorites/{itemId}")
    void removeFavorite(@PathVariable("itemId") Long itemId);

    @GetMapping("/favorites/check/{itemId}")
    Boolean isFavorite(@PathVariable("itemId") Long itemId);
}
```

```java
// hm-api/src/main/java/com/hmall/api/client/CouponClient.java
package com.hmall.api.client;

import com.hmall.api.dto.CouponDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("trade-service")
public interface CouponClient {

    @GetMapping("/coupons")
    List<CouponDTO> getAvailableCoupons();

    @PostMapping("/coupons/{id}/claim")
    void claimCoupon(@PathVariable("id") Long id);

    @GetMapping("/my-coupons")
    List<CouponDTO> getMyCoupons();
}
```

```java
// hm-api/src/main/java/com/hmall/api/client/NotificationClient.java
package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient("notify-service")
public interface NotificationClient {

    @GetMapping("/notifications/active")
    List<Map<String, Object>> getActiveNotifications();
}
```

```java
// hm-api/src/main/java/com/hmall/api/client/FileClient.java
package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient("file-service")
public interface FileClient {

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, String> uploadImage(@RequestPart("file") MultipartFile file);

    @GetMapping("/files/{id}")
    byte[] downloadFile(@PathVariable("id") Long id);
}
```

- [ ] **Step 3: Commit**

```bash
git add hm-api/src/main/java/com/hmall/api/client/ReviewClient.java \
        hm-api/src/main/java/com/hmall/api/client/FavoriteClient.java \
        hm-api/src/main/java/com/hmall/api/client/CouponClient.java \
        hm-api/src/main/java/com/hmall/api/client/NotificationClient.java \
        hm-api/src/main/java/com/hmall/api/client/FileClient.java \
        hm-api/src/main/java/com/hmall/api/dto/ReviewDTO.java \
        hm-api/src/main/java/com/hmall/api/dto/CouponDTO.java
git commit -m "feat: hm-api 新增 Review/Favorite/Coupon/Notification/File Feign 客户端接口"
```

---

### Task 1.5: hm-common 扩展 UserContext 支持 role 透传

**Files:**
- Modify: `hm-common/src/main/java/com/hmall/common/utils/UserContext.java`
- Modify: `hm-common/src/main/java/com/hmall/common/interceptors/UserInfoInterceptor.java`

- [ ] **Step 1: 扩展 UserContext 增加 role**

```java
// hm-common/src/main/java/com/hmall/common/utils/UserContext.java
package com.hmall.common.utils;

public class UserContext {
    private static final ThreadLocal<Long> tl = new ThreadLocal<>();
    private static final ThreadLocal<String> roleTL = new ThreadLocal<>();

    public static void setUser(Long userId) {
        tl.set(userId);
    }

    public static Long getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }

    public static void setRole(String role) {
        roleTL.set(role);
    }

    public static String getRole() {
        return roleTL.get();
    }

    public static void removeRole() {
        roleTL.remove();
    }

    public static void clear() {
        removeUser();
        removeRole();
    }
}
```

- [ ] **Step 2: 更新 UserInfoInterceptor，从 header 解析并设置 role**

UserInfoInterceptor 逻辑 — 从 Gateway 传递的 header 中读取 user-info，目前只存 userId。扩展为 Gateway 同时传 role-info header。

网关在 AuthGlobalFilter 中也需额外透传 role-info（已在 Task 1.2 中覆盖：Gateway 将 userId 放在 user-info header，这里需要同时放 role-info）。

在 AuthGlobalFilter 的 filter 方法中，透传 user-info 后增加：

```java
// 在 AuthGlobalFilter 中，build request 时增加 role-info header
ServerWebExchange webExchange = exchange.mutate()
        .request(builder -> builder
                .header("user-info", tokenInfo.getUserId().toString())
                .header("role-info", tokenInfo.getRole()))
        .build();
```

在 UserInfoInterceptor 中：

```java
// 在 preHandle 中增加
String roleInfo = request.getHeader("role-info");
if (StrUtil.isNotBlank(roleInfo)) {
    UserContext.setRole(roleInfo);
}
// afterCompletion 中增加
UserContext.clear();
```

- [ ] **Step 3: Commit**

```bash
git add hm-common/src/main/java/com/hmall/common/utils/UserContext.java \
        hm-common/src/main/java/com/hmall/common/interceptors/UserInfoInterceptor.java \
        hm-gateway/src/main/java/com/hmall/gateway/filters/AuthGlobalFilter.java
git commit -m "feat: UserContext 扩展 role 透传，Gateway 同步下发 role-info"
```

---

## Phase 2: 新增微服务

### Task 2.1: 创建 notify-service 模块

**Files:**
- Create: `notify-service/pom.xml`
- Create: `notify-service/src/main/java/com/hmall/notify/NotifyApplication.java`
- Create: `notify-service/src/main/resources/application.yaml`
- Create: `notify-service/src/main/resources/bootstrap.yaml`
- Create: `notify-service/src/main/java/com/hmall/notify/domain/po/Notification.java`
- Create: `notify-service/src/main/java/com/hmall/notify/domain/po/Feedback.java`
- Create: `notify-service/src/main/java/com/hmall/notify/domain/po/CustomerMessage.java`
- Create: `notify-service/src/main/java/com/hmall/notify/mapper/NotificationMapper.java`
- Create: `notify-service/src/main/java/com/hmall/notify/mapper/FeedbackMapper.java`
- Create: `notify-service/src/main/java/com/hmall/notify/mapper/CustomerMessageMapper.java`
- Create: `notify-service/src/main/java/com/hmall/notify/service/INotificationService.java`
- Create: `notify-service/src/main/java/com/hmall/notify/service/IFeedbackService.java`
- Create: `notify-service/src/main/java/com/hmall/notify/service/ICustomerMessageService.java`
- Create: `notify-service/src/main/java/com/hmall/notify/service/impl/NotificationServiceImpl.java`
- Create: `notify-service/src/main/java/com/hmall/notify/service/impl/FeedbackServiceImpl.java`
- Create: `notify-service/src/main/java/com/hmall/notify/service/impl/CustomerMessageServiceImpl.java`
- Create: `notify-service/src/main/java/com/hmall/notify/controller/NotificationController.java`
- Create: `notify-service/src/main/java/com/hmall/notify/controller/FeedbackController.java`
- Create: `notify-service/src/main/java/com/hmall/notify/controller/CustomerMessageController.java`
- Modify: `pom.xml` (添加 notify-service 模块)

- [ ] **Step 1: 创建 notify-service/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>hmall</artifactId>
        <groupId>com.heima</groupId>
        <version>1.0.0</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>notify-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.heima</groupId>
            <artifactId>hm-common</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建启动类**

```java
// notify-service/src/main/java/com/hmall/notify/NotifyApplication.java
package com.hmall.notify;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hmall.notify.mapper")
public class NotifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotifyApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建配置文件**

```yaml
# notify-service/src/main/resources/application.yaml
server:
  port: 8086
spring:
  application:
    name: notify-service
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://${hm.db.host:localhost}:${hm.db.port:3306}/hmall?useUnicode=true&characterEncoding=UTF-8&useSSL=false
    username: ${hm.db.username:root}
    password: ${hm.db.password:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
mybatis-plus:
  type-aliases-package: com.hmall.notify.domain.po
  global-config:
    db-config:
      update-strategy: not_null
      id-type: auto
logging:
  level:
    com.hmall: debug
```

```yaml
# notify-service/src/main/resources/bootstrap.yaml
spring:
  cloud:
    nacos:
      server-addr: ${hm.nacos.host:localhost}:${hm.nacos.port:8848}
```

- [ ] **Step 4: 创建 PO 实体类**

```java
// notify-service/src/main/java/com/hmall/notify/domain/po/Notification.java
package com.hmall.notify.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("notifications")
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private LocalDateTime publishTime;
    private Integer status;     // 1-发布, 0-下架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

```java
// notify-service/src/main/java/com/hmall/notify/domain/po/Feedback.java
package com.hmall.notify.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("feedbacks")
public class Feedback implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private String reply;
    private Integer status;     // 0-待处理, 1-已回复, 2-已关闭
    private LocalDateTime createTime;
    private LocalDateTime replyTime;
}
```

```java
// notify-service/src/main/java/com/hmall/notify/domain/po/CustomerMessage.java
package com.hmall.notify.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("customer_messages")
public class CustomerMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private String reply;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime replyTime;
}
```

- [ ] **Step 5: 创建 Mapper 接口**

```java
// notify-service/src/main/java/com/hmall/notify/mapper/NotificationMapper.java
package com.hmall.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.notify.domain.po.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {}
```

```java
// notify-service/src/main/java/com/hmall/notify/mapper/FeedbackMapper.java
package com.hmall.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.notify.domain.po.Feedback;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {}
```

```java
// notify-service/src/main/java/com/hmall/notify/mapper/CustomerMessageMapper.java
package com.hmall.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.notify.domain.po.CustomerMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMessageMapper extends BaseMapper<CustomerMessage> {}
```

- [ ] **Step 6: 创建 Service 接口和实现**

```java
// notify-service/src/main/java/com/hmall/notify/service/INotificationService.java
package com.hmall.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.notify.domain.po.Notification;
import java.util.List;

public interface INotificationService extends IService<Notification> {
    List<Notification> getActiveNotifications();
}
```

```java
// notify-service/src/main/java/com/hmall/notify/service/impl/NotificationServiceImpl.java
package com.hmall.notify.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.notify.domain.po.Notification;
import com.hmall.notify.mapper.NotificationMapper;
import com.hmall.notify.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements INotificationService {

    @Override
    public List<Notification> getActiveNotifications() {
        return lambdaQuery()
                .eq(Notification::getStatus, 1)
                .orderByDesc(Notification::getPublishTime)
                .list();
    }
}
```

```java
// notify-service/src/main/java/com/hmall/notify/service/IFeedbackService.java
package com.hmall.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.notify.domain.po.Feedback;

public interface IFeedbackService extends IService<Feedback> {}
```

```java
// notify-service/src/main/java/com/hmall/notify/service/impl/FeedbackServiceImpl.java
package com.hmall.notify.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.notify.domain.po.Feedback;
import com.hmall.notify.mapper.FeedbackMapper;
import com.hmall.notify.service.IFeedbackService;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback>
        implements IFeedbackService {}
```

```java
// notify-service/src/main/java/com/hmall/notify/service/ICustomerMessageService.java
package com.hmall.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.notify.domain.po.CustomerMessage;

public interface ICustomerMessageService extends IService<CustomerMessage> {}
```

```java
// notify-service/src/main/java/com/hmall/notify/service/impl/CustomerMessageServiceImpl.java
package com.hmall.notify.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.notify.domain.po.CustomerMessage;
import com.hmall.notify.mapper.CustomerMessageMapper;
import com.hmall.notify.service.ICustomerMessageService;
import org.springframework.stereotype.Service;

@Service
public class CustomerMessageServiceImpl extends ServiceImpl<CustomerMessageMapper, CustomerMessage>
        implements ICustomerMessageService {}
```

- [ ] **Step 7: 创建 Controller**

```java
// notify-service/src/main/java/com/hmall/notify/controller/NotificationController.java
package com.hmall.notify.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.notify.domain.po.Notification;
import com.hmall.notify.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping("/notifications/active")
    public List<Notification> getActiveNotifications() {
        return notificationService.getActiveNotifications();
    }

    @GetMapping("/admin/notifications")
    public PageDTO<Notification> adminList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<Notification> result = notificationService.lambdaQuery()
                .orderByDesc(Notification::getCreateTime)
                .page(new Page<>(page, size));
        return PageDTO.of(result);
    }

    @PostMapping("/admin/notifications")
    public R<Void> create(@RequestBody Notification notification) {
        notification.setCreateTime(LocalDateTime.now());
        notification.setStatus(1);
        notificationService.save(notification);
        return R.ok();
    }

    @PutMapping("/admin/notifications/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Notification notification) {
        notification.setId(id);
        notification.setUpdateTime(LocalDateTime.now());
        notificationService.updateById(notification);
        return R.ok();
    }

    @DeleteMapping("/admin/notifications/{id}")
    public R<Void> delete(@PathVariable Long id) {
        notificationService.removeById(id);
        return R.ok();
    }
}
```

```java
// notify-service/src/main/java/com/hmall/notify/controller/FeedbackController.java
package com.hmall.notify.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.notify.domain.po.Feedback;
import com.hmall.notify.service.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FeedbackController {

    private final IFeedbackService feedbackService;

    @PostMapping("/feedbacks")
    public R<Void> submit(@RequestBody Feedback feedback) {
        feedback.setUserId(UserContext.getUser());
        feedback.setStatus(0);
        feedback.setCreateTime(LocalDateTime.now());
        feedbackService.save(feedback);
        return R.ok();
    }

    @GetMapping("/my-feedbacks")
    public PageDTO<Feedback> myFeedbacks(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<Feedback> result = feedbackService.lambdaQuery()
                .eq(Feedback::getUserId, UserContext.getUser())
                .orderByDesc(Feedback::getCreateTime)
                .page(new Page<>(page, size));
        return PageDTO.of(result);
    }

    @GetMapping("/admin/feedbacks")
    public PageDTO<Feedback> adminList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<Feedback> result = feedbackService.lambdaQuery()
                .orderByDesc(Feedback::getCreateTime)
                .page(new Page<>(page, size));
        return PageDTO.of(result);
    }

    @PutMapping("/admin/feedbacks/{id}/reply")
    public R<Void> reply(@PathVariable Long id, @RequestBody Feedback feedback) {
        feedback.setId(id);
        feedback.setStatus(1);
        feedback.setReplyTime(LocalDateTime.now());
        feedbackService.updateById(feedback);
        return R.ok();
    }
}
```

```java
// notify-service/src/main/java/com/hmall/notify/controller/CustomerMessageController.java
package com.hmall.notify.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.notify.domain.po.CustomerMessage;
import com.hmall.notify.service.ICustomerMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CustomerMessageController {

    private final ICustomerMessageService messageService;

    @PostMapping("/messages")
    public R<Void> send(@RequestBody CustomerMessage message) {
        message.setUserId(UserContext.getUser());
        message.setStatus(0);
        message.setCreateTime(LocalDateTime.now());
        messageService.save(message);
        return R.ok();
    }

    @GetMapping("/admin/messages")
    public PageDTO<CustomerMessage> adminList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<CustomerMessage> result = messageService.lambdaQuery()
                .orderByDesc(CustomerMessage::getCreateTime)
                .page(new Page<>(page, size));
        return PageDTO.of(result);
    }

    @PutMapping("/admin/messages/{id}/reply")
    public R<Void> reply(@PathVariable Long id, @RequestBody CustomerMessage message) {
        message.setId(id);
        message.setStatus(1);
        message.setReplyTime(LocalDateTime.now());
        messageService.updateById(message);
        return R.ok();
    }
}
```

- [ ] **Step 8: 在主 pom.xml 的 modules 中添加 notify-service，然后 Commit**

```bash
git add notify-service/ pom.xml
git commit -m "feat: 新增 notify-service 微服务（公告/反馈/客服留言）"
```

---

### Task 2.2: 创建 file-service 模块

**Files:**
- Create: `file-service/pom.xml`
- Create: `file-service/src/main/java/com/hmall/file/FileApplication.java`
- Create: `file-service/src/main/resources/application.yaml`
- Create: `file-service/src/main/resources/bootstrap.yaml`
- Create: `file-service/src/main/java/com/hmall/file/domain/po/Upload.java`
- Create: `file-service/src/main/java/com/hmall/file/mapper/UploadMapper.java`
- Create: `file-service/src/main/java/com/hmall/file/service/IUploadService.java`
- Create: `file-service/src/main/java/com/hmall/file/service/impl/UploadServiceImpl.java`
- Create: `file-service/src/main/java/com/hmall/file/controller/FileController.java`
- Modify: `pom.xml` (添加 file-service 模块)

- [ ] **Step 1: 创建 file-service/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>hmall</artifactId>
        <groupId>com.heima</groupId>
        <version>1.0.0</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>file-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.heima</groupId>
            <artifactId>hm-common</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建启动类**

```java
// file-service/src/main/java/com/hmall/file/FileApplication.java
package com.hmall.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hmall.file.mapper")
public class FileApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建配置文件**

```yaml
# file-service/src/main/resources/application.yaml
server:
  port: 8087
spring:
  application:
    name: file-service
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://${hm.db.host:localhost}:${hm.db.port:3306}/hmall?useUnicode=true&characterEncoding=UTF-8&useSSL=false
    username: ${hm.db.username:root}
    password: ${hm.db.password:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
mybatis-plus:
  type-aliases-package: com.hmall.file.domain.po
file:
  upload-dir: ./uploads
```

```yaml
# file-service/src/main/resources/bootstrap.yaml
spring:
  cloud:
    nacos:
      server-addr: ${hm.nacos.host:localhost}:${hm.nacos.port:8848}
```

- [ ] **Step 4: 创建 PO 和 Mapper**

```java
// file-service/src/main/java/com/hmall/file/domain/po/Upload.java
package com.hmall.file.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("uploads")
public class Upload implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String originalName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String fileType;
    private LocalDateTime createTime;
}
```

```java
// file-service/src/main/java/com/hmall/file/mapper/UploadMapper.java
package com.hmall.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.file.domain.po.Upload;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UploadMapper extends BaseMapper<Upload> {}
```

- [ ] **Step 5: 创建 Service**

```java
// file-service/src/main/java/com/hmall/file/service/IUploadService.java
package com.hmall.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.file.domain.po.Upload;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadService extends IService<Upload> {
    Upload uploadImage(MultipartFile file);
    Upload getFile(Long id);
}
```

```java
// file-service/src/main/java/com/hmall/file/service/impl/UploadServiceImpl.java
package com.hmall.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BadRequestException;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.mapper.UploadMapper;
import com.hmall.file.service.IUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.time.LocalDateTime;

@Service
public class UploadServiceImpl extends ServiceImpl<UploadMapper, Upload>
        implements IUploadService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public Upload uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("文件为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName);
        String newFileName = IdUtil.fastSimpleUUID() + "." + ext;
        String dateDir = LocalDateTime.now().toLocalDate().toString();
        File dir = new File(uploadDir, dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, newFileName);
        try {
            file.transferTo(dest);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
        Upload upload = new Upload();
        upload.setOriginalName(originalName);
        upload.setFilePath("/uploads/" + dateDir + "/" + newFileName);
        upload.setFileSize(file.getSize());
        upload.setContentType(file.getContentType());
        upload.setFileType("image");
        upload.setCreateTime(LocalDateTime.now());
        save(upload);
        return upload;
    }

    @Override
    public Upload getFile(Long id) {
        return getById(id);
    }
}
```

- [ ] **Step 6: 创建 Controller**

```java
// file-service/src/main/java/com/hmall/file/controller/FileController.java
package com.hmall.file.controller;

import cn.hutool.core.io.FileUtil;
import com.hmall.common.domain.R;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FileController {

    private final IUploadService uploadService;
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadImage(@RequestPart("file") MultipartFile file) {
        Upload upload = uploadService.uploadImage(file);
        Map<String, String> result = new HashMap<>();
        result.put("id", upload.getId().toString());
        result.put("url", upload.getFilePath());
        return result;
    }

    @GetMapping("/files/{id}")
    public byte[] download(@PathVariable Long id) {
        Upload upload = uploadService.getFile(id);
        if (upload == null) {
            throw new RuntimeException("文件不存在");
        }
        return FileUtil.readBytes(uploadDir + "/../" + upload.getFilePath());
    }
}
```

- [ ] **Step 7: 在主 pom.xml 的 modules 中添加 file-service，然后 Commit**

```bash
git add file-service/ pom.xml
git commit -m "feat: 新增 file-service 微服务（图片上传/下载）"
```

---

### Task 2.3: 执行数据库 DDL

**Files:**
- Create: `docs/sql/init-extra-tables.sql`

- [ ] **Step 1: 创建 DDL SQL 文件**

```sql
-- docs/sql/init-extra-tables.sql
-- 商品评价
CREATE TABLE IF NOT EXISTS item_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    content TEXT,
    images VARCHAR(1024),
    rating TINYINT NOT NULL DEFAULT 5,
    create_time DATETIME NOT NULL,
    INDEX idx_item_id (item_id),
    INDEX idx_user_id (user_id)
);

-- 商品收藏
CREATE TABLE IF NOT EXISTS user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    UNIQUE KEY uk_user_item (user_id, item_id)
);

-- 收货地址
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(64) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(32),
    city VARCHAR(32),
    district VARCHAR(32),
    detail VARCHAR(255) NOT NULL,
    is_default TINYINT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_user_id (user_id)
);

-- 优惠券
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    discount_type TINYINT NOT NULL,
    discount_value INT NOT NULL,
    min_amount INT DEFAULT 0,
    total_stock INT NOT NULL,
    remaining_stock INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME
);

-- 用户优惠券
CREATE TABLE IF NOT EXISTS user_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1,
    used_order_id BIGINT,
    create_time DATETIME,
    use_time DATETIME,
    INDEX idx_user_id (user_id)
);

-- 系统公告
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    publish_time DATETIME NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

-- 用户反馈
CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    reply TEXT,
    status TINYINT DEFAULT 0,
    create_time DATETIME,
    reply_time DATETIME
);

-- 客服留言
CREATE TABLE IF NOT EXISTS customer_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    reply TEXT,
    status TINYINT DEFAULT 0,
    create_time DATETIME,
    reply_time DATETIME
);

-- 文件记录
CREATE TABLE IF NOT EXISTS uploads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(64),
    file_type VARCHAR(32),
    create_time DATETIME
);

-- 扩展 user 表（如不存在则添加）
ALTER TABLE user ADD COLUMN IF NOT EXISTS email VARCHAR(128);
ALTER TABLE user ADD COLUMN IF NOT EXISTS avatar VARCHAR(512);
ALTER TABLE user ADD COLUMN IF NOT EXISTS nickname VARCHAR(64);
ALTER TABLE user ADD COLUMN IF NOT EXISTS role VARCHAR(16) DEFAULT 'user';

-- 扩展 item 表（分类管理）
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

-- 轮播图表
CREATE TABLE IF NOT EXISTS banners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128),
    image_url VARCHAR(512) NOT NULL,
    link_url VARCHAR(512),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);
```

- [ ] **Step 2: 执行 SQL（在 MySQL 中手动执行或由 DBA 执行）**

```bash
# 仅在手动执行时
mysql -u root -p hmall < docs/sql/init-extra-tables.sql
```

- [ ] **Step 3: Commit**

```bash
git add docs/sql/init-extra-tables.sql
git commit -m "feat: 新增数据库 DDL（评价/收藏/地址/优惠券/公告/反馈/留言/文件/分类/轮播）"
```

---

## Phase 3: 现有服务功能扩展

### Task 3.1: 扩展 user-service（注册/验证码/找回密码/收藏/地址）

**Files:**
- Modify: `user-service/pom.xml` (添加 spring-boot-starter-mail, spring-boot-starter-data-redis)
- Create: `user-service/src/main/java/com/hmall/domain/po/Address.java`
- Create: `user-service/src/main/java/com/hmall/domain/po/UserFavorite.java`
- Create: `user-service/src/main/java/com/hmall/mapper/AddressMapper.java`
- Create: `user-service/src/main/java/com/hmall/mapper/UserFavoriteMapper.java`
- Create: `user-service/src/main/java/com/hmall/domain/dto/RegisterFormDTO.java`
- Create: `user-service/src/main/java/com/hmall/domain/dto/ResetPasswordDTO.java`
- Create: `user-service/src/main/java/com/hmall/service/IAddressService.java`
- Create: `user-service/src/main/java/com/hmall/service/IFavoriteService.java`
- Create: `user-service/src/main/java/com/hmall/service/impl/AddressServiceImpl.java`
- Create: `user-service/src/main/java/com/hmall/service/impl/FavoriteServiceImpl.java`
- Modify: `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`
- Create: `user-service/src/main/java/com/hmall/controller/AddressController.java`
- Create: `user-service/src/main/java/com/hmall/controller/FavoriteController.java`
- Modify: `user-service/src/main/java/com/hmall/controller/UserController.java`

- [ ] **Step 1: 更新 pom.xml 依赖**

```xml
<!-- 在 user-service/pom.xml 的 dependencies 中添加 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

- [ ] **Step 2: 创建 Address PO + Mapper**

```java
// user-service/src/main/java/com/hmall/domain/po/Address.java
package com.hmall.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("addresses")
public class Address implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

```java
// user-service/src/main/java/com/hmall/mapper/AddressMapper.java
package com.hmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.domain.po.Address;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {}
```

- [ ] **Step 3: 创建 UserFavorite PO + Mapper**

```java
// user-service/src/main/java/com/hmall/domain/po/UserFavorite.java
package com.hmall.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_favorites")
public class UserFavorite implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long itemId;
    private LocalDateTime createTime;
}
```

```java
// user-service/src/main/java/com/hmall/mapper/UserFavoriteMapper.java
package com.hmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.domain.po.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {}
```

- [ ] **Step 4: 创建 AddressService 和 FavoriteService**

```java
// user-service/src/main/java/com/hmall/service/IAddressService.java
package com.hmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.domain.po.Address;
import java.util.List;

public interface IAddressService extends IService<Address> {
    List<Address> listByUserId(Long userId);
    void setDefault(Long id, Long userId);
}
```

```java
// user-service/src/main/java/com/hmall/service/impl/AddressServiceImpl.java
package com.hmall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.domain.po.Address;
import com.hmall.mapper.AddressMapper;
import com.hmall.service.IAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address>
        implements IAddressService {

    @Override
    public List<Address> listByUserId(Long userId) {
        return lambdaQuery().eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .list();
    }

    @Override
    @Transactional
    public void setDefault(Long id, Long userId) {
        lambdaUpdate().eq(Address::getUserId, userId).set(Address::getIsDefault, 0).update();
        lambdaUpdate().eq(Address::getId, id).eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 1).update();
    }
}
```

```java
// user-service/src/main/java/com/hmall/service/IFavoriteService.java
package com.hmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.domain.po.UserFavorite;

public interface IFavoriteService extends IService<UserFavorite> {
    boolean isFavorite(Long userId, Long itemId);
    void addFavorite(Long userId, Long itemId);
    void removeFavorite(Long userId, Long itemId);
}
```

```java
// user-service/src/main/java/com/hmall/service/impl/FavoriteServiceImpl.java
package com.hmall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.domain.po.UserFavorite;
import com.hmall.mapper.UserFavoriteMapper;
import com.hmall.service.IFavoriteService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class FavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite>
        implements IFavoriteService {

    @Override
    public boolean isFavorite(Long userId, Long itemId) {
        return lambdaQuery()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getItemId, itemId)
                .count() > 0;
    }

    @Override
    public void addFavorite(Long userId, Long itemId) {
        if (isFavorite(userId, itemId)) {
            throw new BizIllegalException("已收藏");
        }
        UserFavorite f = new UserFavorite();
        f.setUserId(userId);
        f.setItemId(itemId);
        f.setCreateTime(LocalDateTime.now());
        save(f);
    }

    @Override
    public void removeFavorite(Long userId, Long itemId) {
        lambdaUpdate()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getItemId, itemId)
                .remove();
    }
}
```

- [ ] **Step 5: 创建 AddressController 和 FavoriteController**

```java
// user-service/src/main/java/com/hmall/controller/AddressController.java
package com.hmall.controller;

import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Address;
import com.hmall.service.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @GetMapping
    public List<Address> list() {
        return addressService.listByUserId(UserContext.getUser());
    }

    @PostMapping
    public R<Void> save(@RequestBody Address address) {
        address.setUserId(UserContext.getUser());
        address.setCreateTime(LocalDateTime.now());
        addressService.save(address);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Address address) {
        address.setId(id);
        address.setUpdateTime(LocalDateTime.now());
        addressService.updateById(address);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        addressService.removeById(id);
        return R.ok();
    }

    @PutMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id, UserContext.getUser());
        return R.ok();
    }
}
```

```java
// user-service/src/main/java/com/hmall/controller/FavoriteController.java
package com.hmall.controller;

import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.UserFavorite;
import com.hmall.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @PostMapping
    public R<Void> add(@RequestParam("itemId") Long itemId) {
        favoriteService.addFavorite(UserContext.getUser(), itemId);
        return R.ok();
    }

    @DeleteMapping("/{itemId}")
    public R<Void> remove(@PathVariable Long itemId) {
        favoriteService.removeFavorite(UserContext.getUser(), itemId);
        return R.ok();
    }

    @GetMapping
    public List<UserFavorite> list() {
        return favoriteService.lambdaQuery()
                .eq(UserFavorite::getUserId, UserContext.getUser())
                .orderByDesc(UserFavorite::getCreateTime)
                .list();
    }

    @GetMapping("/check/{itemId}")
    public Boolean check(@PathVariable Long itemId) {
        return favoriteService.isFavorite(UserContext.getUser(), itemId);
    }
}
```

- [ ] **Step 6: 创建注册 DTO 和找回密码 DTO**

```java
// user-service/src/main/java/com/hmall/domain/dto/RegisterFormDTO.java
package com.hmall.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "注册表单")
public class RegisterFormDTO {
    @NotBlank
    @ApiModelProperty("用户名")
    private String username;
    @NotBlank
    @ApiModelProperty("密码")
    private String password;
    @NotBlank
    @ApiModelProperty("邮箱")
    private String email;
    @NotBlank
    @ApiModelProperty("验证码")
    private String code;
}
```

```java
// user-service/src/main/java/com/hmall/domain/dto/ResetPasswordDTO.java
package com.hmall.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "重置密码表单")
public class ResetPasswordDTO {
    @NotBlank
    @ApiModelProperty("邮箱")
    private String email;
    @NotBlank
    @ApiModelProperty("验证码")
    private String code;
    @NotBlank
    @ApiModelProperty("新密码")
    private String newPassword;
}
```

- [ ] **Step 7: 扩展 UserServiceImpl（注册/验证码/找回密码/个人信息）**

```java
// user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java 新增方法

// 新增依赖注入
private final RedisTemplate<String, Object> redisTemplate;
private final JavaMailSender mailSender;
@Value("${spring.mail.username}")
private String mailFrom;

@Override
public void sendVerifyCode(String email) {
    String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
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
    if (StrUtil.isNotBlank(profile.getNickname())) {
        user.setNickname(profile.getNickname());
    }
    if (StrUtil.isNotBlank(profile.getAvatar())) {
        user.setAvatar(profile.getAvatar());
    }
    if (StrUtil.isNotBlank(profile.getEmail())) {
        user.setEmail(profile.getEmail());
    }
    updateById(user);
}

// 修改 login 方法——createToken 调用时传入 role
@Override
public UserLoginVO login(LoginFormDTO loginDTO) {
    // ... 前面校验逻辑不变 ...
    String token = jwtTool.createToken(user.getId(), user.getRole(), jwtProperties.getTokenTTL());
    // ...
}
```

- [ ] **Step 8: 扩展 UserController**

```java
// user-service/src/main/java/com/hmall/controller/UserController.java 新增方法

@PostMapping("/send-code")
public R<Void> sendCode(@RequestParam("email") String email) {
    userService.sendVerifyCode(email);
    return R.ok();
}

@PostMapping("/register")
public R<Void> register(@RequestBody @Validated RegisterFormDTO form) {
    userService.register(form);
    return R.ok();
}

@PostMapping("/reset-password")
public R<Void> resetPassword(@RequestBody @Validated ResetPasswordDTO form) {
    userService.resetPassword(form);
    return R.ok();
}

@PutMapping("/profile")
public R<Void> updateProfile(@RequestBody User profile) {
    userService.updateProfile(profile);
    return R.ok();
}

@GetMapping("/{id}")
public User getById(@PathVariable Long id) {
    return userService.getById(id);
}
```

- [ ] **Step 9: 扩展 User PO（增加 email/avatar/nickname/role 字段）**

```java
// 在 user-service/src/main/java/com/hmall/domain/po/User.java 中增加字段
private String email;
private String avatar;
private String nickname;
private String role;    // "user" 或 "admin"
```

- [ ] **Step 10: Commit**

```bash
git add user-service/
git commit -m "feat: user-service 扩展注册/验证码/找回密码/地址/收藏/个人信息"
```

---

### Task 3.2: 扩展 item-service（评价 + 分类管理）

**Files:**
- Create: `item-service/src/main/java/com/hmall/item/domain/po/ItemReview.java`
- Create: `item-service/src/main/java/com/hmall/item/domain/po/Category.java`
- Create: `item-service/src/main/java/com/hmall/item/mapper/ItemReviewMapper.java`
- Create: `item-service/src/main/java/com/hmall/item/mapper/CategoryMapper.java`
- Create: `item-service/src/main/java/com/hmall/item/service/IItemReviewService.java`
- Create: `item-service/src/main/java/com/hmall/item/service/ICategoryService.java`
- Create: `item-service/src/main/java/com/hmall/item/service/impl/ItemReviewServiceImpl.java`
- Create: `item-service/src/main/java/com/hmall/item/service/impl/CategoryServiceImpl.java`
- Modify: `item-service/src/main/java/com/hmall/item/controller/ItemController.java`
- Create: `item-service/src/main/java/com/hmall/item/controller/CategoryController.java`
- Create: `item-service/src/main/java/com/hmall/item/controller/ReviewController.java`

- [ ] **Step 1: 创建 ItemReview PO + Mapper**

```java
// item-service/src/main/java/com/hmall/item/domain/po/ItemReview.java
package com.hmall.item.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("item_reviews")
public class ItemReview implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long itemId;
    private String content;
    private String images;
    private Integer rating;
    private LocalDateTime createTime;
}
```

```java
// item-service/src/main/java/com/hmall/item/mapper/ItemReviewMapper.java
package com.hmall.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.item.domain.po.ItemReview;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemReviewMapper extends BaseMapper<ItemReview> {}
```

- [ ] **Step 2: 创建 Category PO + Mapper**

```java
// item-service/src/main/java/com/hmall/item/domain/po/Category.java
package com.hmall.item.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("categories")
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

```java
// item-service/src/main/java/com/hmall/item/mapper/CategoryMapper.java
package com.hmall.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.item.domain.po.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {}
```

- [ ] **Step 3: 创建 Service**

```java
// item-service/src/main/java/com/hmall/item/service/IItemReviewService.java
package com.hmall.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.item.domain.po.ItemReview;
import java.util.List;

public interface IItemReviewService extends IService<ItemReview> {
    List<ItemReview> listByItemId(Long itemId);
}
```

```java
// item-service/src/main/java/com/hmall/item/service/impl/ItemReviewServiceImpl.java
package com.hmall.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.mapper.ItemReviewMapper;
import com.hmall.item.service.IItemReviewService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ItemReviewServiceImpl extends ServiceImpl<ItemReviewMapper, ItemReview>
        implements IItemReviewService {

    @Override
    public List<ItemReview> listByItemId(Long itemId) {
        return lambdaQuery()
                .eq(ItemReview::getItemId, itemId)
                .orderByDesc(ItemReview::getCreateTime)
                .list();
    }
}
```

```java
// item-service/src/main/java/com/hmall/item/service/ICategoryService.java
package com.hmall.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.item.domain.po.Category;

public interface ICategoryService extends IService<Category> {}
```

```java
// item-service/src/main/java/com/hmall/item/service/impl/CategoryServiceImpl.java
package com.hmall.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.item.domain.po.Category;
import com.hmall.item.mapper.CategoryMapper;
import com.hmall.item.service.ICategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements ICategoryService {}
```

- [ ] **Step 4: 创建 Controller**

```java
// item-service/src/main/java/com/hmall/item/controller/ReviewController.java
package com.hmall.item.controller;

import com.hmall.api.dto.ReviewDTO;
import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.service.IItemReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewController {

    private final IItemReviewService reviewService;

    @GetMapping("/items/{itemId}/reviews")
    public List<ItemReview> getReviews(@PathVariable Long itemId) {
        return reviewService.listByItemId(itemId);
    }

    @PostMapping("/items/{itemId}/reviews")
    public R<Void> saveReview(@PathVariable Long itemId, @RequestBody ReviewDTO dto) {
        ItemReview review = new ItemReview();
        review.setUserId(UserContext.getUser());
        review.setItemId(itemId);
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        review.setRating(dto.getRating());
        review.setCreateTime(LocalDateTime.now());
        reviewService.save(review);
        return R.ok();
    }

    @DeleteMapping("/admin/reviews/{id}")
    public R<Void> deleteReview(@PathVariable Long id) {
        reviewService.removeById(id);
        return R.ok();
    }
}
```

```java
// item-service/src/main/java/com/hmall/item/controller/CategoryController.java
package com.hmall.item.controller;

import com.hmall.common.domain.R;
import com.hmall.item.domain.po.Category;
import com.hmall.item.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping
    public List<Category> list() {
        return categoryService.lambdaQuery()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder)
                .list();
    }

    @PostMapping
    public R<Void> save(@RequestBody Category category) {
        category.setCreateTime(LocalDateTime.now());
        categoryService.save(category);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        category.setUpdateTime(LocalDateTime.now());
        categoryService.updateById(category);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return R.ok();
    }
}
```

- [ ] **Step 5: 扩展 ItemController（增加 admin 端商品管理接口）**

在现有 ItemController 中增加以下方法：

```java
@PostMapping("/admin/items")           // 新增商品
@PutMapping("/admin/items/{id}")       // 编辑商品
@PutMapping("/admin/items/{id}/status") // 上下架
@DeleteMapping("/admin/items/{id}")    // 删除商品
```

这些方法的实现遵循已有 Controller 的模式（委托 ItemService 处理）。

- [ ] **Step 6: Commit**

```bash
git add item-service/
git commit -m "feat: item-service 扩展商品评价和分类管理"
```

---

### Task 3.3: 扩展 trade-service（优惠券 + 退款 + 管理端操作）

**Files:**
- Create: `trade-service/src/main/java/com/hmall/domain/po/Coupon.java`
- Create: `trade-service/src/main/java/com/hmall/domain/po/UserCoupon.java`
- Create: `trade-service/src/main/java/com/hmall/mapper/CouponMapper.java`
- Create: `trade-service/src/main/java/com/hmall/mapper/UserCouponMapper.java`
- Create: `trade-service/src/main/java/com/hmall/service/ICouponService.java`
- Create: `trade-service/src/main/java/com/hmall/service/impl/CouponServiceImpl.java`
- Create: `trade-service/src/main/java/com/hmall/controller/CouponController.java`
- Modify: `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`
- Modify: `trade-service/src/main/java/com/hmall/controller/OrderController.java`

- [ ] **Step 1: 创建 Coupon/UserCoupon PO + Mapper**

```java
// trade-service/src/main/java/com/hmall/domain/po/Coupon.java
package com.hmall.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("coupons")
public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer discountType;
    private Integer discountValue;
    private Integer minAmount;
    private Integer totalStock;
    private Integer remainingStock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createTime;
}
```

```java
// trade-service/src/main/java/com/hmall/domain/po/UserCoupon.java
package com.hmall.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_coupons")
public class UserCoupon implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status;
    private Long usedOrderId;
    private LocalDateTime createTime;
    private LocalDateTime useTime;
}
```

对应的 Mapper 都是空接口继承 `BaseMapper<Entity>` 加 `@Mapper`。

- [ ] **Step 2: 创建 CouponService + Controller**

```java
// trade-service/src/main/java/com/hmall/controller/CouponController.java
package com.hmall.controller;

import com.hmall.common.domain.R;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.po.Coupon;
import com.hmall.domain.po.UserCoupon;
import com.hmall.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    @GetMapping("/coupons")
    public List<Coupon> available() {
        return couponService.getAvailableCoupons();
    }

    @PostMapping("/coupons/{id}/claim")
    public R<Void> claim(@PathVariable Long id) {
        couponService.claimCoupon(UserContext.getUser(), id);
        return R.ok();
    }

    @GetMapping("/my-coupons")
    public List<Coupon> myCoupons() {
        return couponService.getUserCoupons(UserContext.getUser());
    }

    @PostMapping("/admin/coupons")
    public R<Void> create(@RequestBody Coupon coupon) {
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setRemainingStock(coupon.getTotalStock());
        couponService.save(coupon);
        return R.ok();
    }

    @DeleteMapping("/admin/coupons/{id}")
    public R<Void> delete(@PathVariable Long id) {
        couponService.removeById(id);
        return R.ok();
    }
}
```

- [ ] **Step 3: 扩展 OrderServiceImpl — 退款 + 发货 + 管理端订单操作**

```java
// 在 OrderServiceImpl 中新增：
@Override
@Transactional
public void refund(Long orderId, Long userId) {
    Order order = getById(orderId);
    if (order == null || !order.getUserId().equals(userId)) {
        throw new BadRequestException("订单不存在");
    }
    if (order.getStatus() != 2 && order.getStatus() != 3) {
        throw new BizIllegalException("当前状态不可申请退款");
    }
    order.setStatus(4); // 退款中
    order.setUpdateTime(LocalDateTime.now());
    updateById(order);
}

@Override
@Transactional
public void ship(Long orderId, String trackingNumber) {
    Order order = getById(orderId);
    if (order == null) throw new BadRequestException("订单不存在");
    order.setStatus(3);  // 已发货
    order.setConsignTime(LocalDateTime.now());
    updateById(order);
    // 更新物流
    OrderLogistics logistics = logisticsService.lambdaQuery()
            .eq(OrderLogistics::getOrderId, orderId).one();
    if (logistics != null) {
        logistics.setLogisticsNumber(trackingNumber);
        logisticsService.updateById(logistics);
    }
}
```

- [ ] **Step 4: 扩展 OrderController — 管理端接口**

```java
// 在 OrderController 中新增：
@PutMapping("/{id}/cancel")
public R<Void> cancel(@PathVariable Long id) {
    orderService.cancelOrder(id, UserContext.getUser());
    return R.ok();
}

@PutMapping("/{id}/confirm")
public R<Void> confirm(@PathVariable Long id) {
    orderService.confirmReceive(id, UserContext.getUser());
    return R.ok();
}

@PostMapping("/{id}/refund")
public R<Void> refund(@PathVariable Long id) {
    orderService.refund(id, UserContext.getUser());
    return R.ok();
}

@GetMapping("/admin/orders")
public PageDTO<Order> adminList(
        @RequestParam(required = false) Long orderId,
        @RequestParam(required = false) Integer status,
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "size", defaultValue = "10") Integer size) {
    Page<Order> p = new Page<>(page, size);
    LambdaQueryChainWrapper<Order> wrapper = orderService.lambdaQuery();
    if (orderId != null) wrapper.eq(Order::getId, orderId);
    if (status != null) wrapper.eq(Order::getStatus, status);
    return PageDTO.of(wrapper.orderByDesc(Order::getCreateTime).page(p));
}

@PutMapping("/admin/orders/{id}/status")
public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
    Order order = orderService.getById(id);
    order.setStatus(status);
    order.setUpdateTime(LocalDateTime.now());
    orderService.updateById(order);
    return R.ok();
}

@PutMapping("/admin/orders/{id}/ship")
public R<Void> ship(@PathVariable Long id, @RequestParam String trackingNumber) {
    orderService.ship(id, trackingNumber);
    return R.ok();
}

@PostMapping("/admin/orders/export")
public byte[] export(HttpServletResponse response) {
    // 使用 EasyExcel 或 Hutool ExcelUtil 导出
    // 返回 Excel 文件流
    List<Order> orders = orderService.list();
    // ... Excel 生成逻辑 ...
    return excelBytes;
}
```

- [ ] **Step 5: Commit**

```bash
git add trade-service/
git commit -m "feat: trade-service 扩展优惠券/退款/管理端订单操作"
```

---

## Phase 4: hmall-web 客户端前端

### Task 4.1: 初始化 Vue 3 项目

- [ ] **Step 1: 使用 Vite 创建项目**

```bash
cd hmall
npm create vite@latest hmall-web -- --template vue
cd hmall-web
npm install
npm install element-plus @element-plus/icons-vue pinia vue-router axios
npm install -D @vitejs/plugin-vue
```

- [ ] **Step 2: 创建 vite.config.js**

```js
// hmall-web/vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

- [ ] **Step 3: 创建 main.js（入口文件）**

```js
// hmall-web/src/main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/global.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
```

- [ ] **Step 4: Commit**

```bash
git add hmall-web/
git commit -m "feat: 初始化 hmall-web Vue 3 项目（Vite + Element Plus + Pinia + Vue Router）"
```

---

### Task 4.2: 创建路由和 Axios 封装

**Files:**
- Create: `hmall-web/src/router/index.js`
- Create: `hmall-web/src/api/request.js`
- Create: `hmall-web/src/api/user.js`
- Create: `hmall-web/src/api/item.js`
- Create: `hmall-web/src/api/cart.js`
- Create: `hmall-web/src/api/order.js`
- Create: `hmall-web/src/api/address.js`
- Create: `hmall-web/src/api/common.js`

- [ ] **Step 1: 创建 Axios 请求封装**

```js
// hmall-web/src/api/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.authorization = token
  }
  return config
})

request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.msg || '请求失败')
    return Promise.reject(error)
  }
)

export default request
```

- [ ] **Step 2: 创建各 API 模块**

```js
// hmall-web/src/api/user.js
import request from './request'
export const login = (data) => request.post('/users/login', data)
export const register = (data) => request.post('/users/register', data)
export const sendCode = (email) => request.post('/users/send-code', null, { params: { email } })
export const resetPassword = (data) => request.post('/users/reset-password', data)
export const updateProfile = (data) => request.put('/users/profile', data)

// hmall-web/src/api/item.js
import request from './request'
export const getItems = (params) => request.get('/items', { params })
export const getItemById = (id) => request.get(`/items/${id}`)
export const searchItems = (params) => request.get('/items/search', { params })
export const getCategories = () => request.get('/categories')
export const getReviews = (itemId) => request.get(`/items/${itemId}/reviews`)
export const submitReview = (itemId, data) => request.post(`/items/${itemId}/reviews`, data)
export const getCategories = () => request.get('/categories')

// hmall-web/src/api/cart.js
import request from './request'
export const getCart = () => request.get('/carts')
export const addToCart = (data) => request.post('/carts', data)
export const updateCartItem = (id, data) => request.put(`/carts/${id}`, data)
export const deleteCartItem = (id) => request.delete(`/carts/${id}`)

// hmall-web/src/api/order.js
import request from './request'
export const createOrder = (data) => request.post('/orders', data)
export const getOrders = (params) => request.get('/orders', { params })
export const cancelOrder = (id) => request.put(`/orders/${id}/cancel`)
export const confirmOrder = (id) => request.put(`/orders/${id}/confirm`)
export const refundOrder = (id) => request.post(`/orders/${id}/refund`)
export const getCoupons = () => request.get('/coupons')
export const claimCoupon = (id) => request.post(`/coupons/${id}/claim`)
export const getMyCoupons = () => request.get('/my-coupons')

// hmall-web/src/api/address.js
import request from './request'
export const getAddresses = () => request.get('/addresses')
export const saveAddress = (data) => request.post('/addresses', data)
export const updateAddress = (id, data) => request.put(`/addresses/${id}`, data)
export const deleteAddress = (id) => request.delete(`/addresses/${id}`)
export const setDefaultAddress = (id) => request.put(`/addresses/${id}/default`)

// hmall-web/src/api/common.js
import request from './request'
export const submitFeedback = (data) => request.post('/feedbacks', data)
export const getNotifications = () => request.get('/notifications/active')
export const addFavorite = (itemId) => request.post('/favorites', null, { params: { itemId } })
export const removeFavorite = (itemId) => request.delete(`/favorites/${itemId}`)
export const getFavorites = () => request.get('/favorites')
export const checkFavorite = (itemId) => request.get(`/favorites/check/${itemId}`)
```

- [ ] **Step 3: 创建路由配置**

```js
// hmall-web/src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/category', name: 'Category', component: () => import('@/views/Category.vue') },
  { path: '/item/:id', name: 'ItemDetail', component: () => import('@/views/ItemDetail.vue') },
  { path: '/search', name: 'Search', component: () => import('@/views/Search.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/notifications', name: 'Notifications', component: () => import('@/views/Notifications.vue') },
  {
    path: '/cart', name: 'Cart', component: () => import('@/views/Cart.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/order/confirm', name: 'OrderConfirm', component: () => import('@/views/OrderConfirm.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders', name: 'OrderList', component: () => import('@/views/OrderList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/favorites', name: 'FavoriteList', component: () => import('@/views/FavoriteList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/addresses', name: 'AddressList', component: () => import('@/views/AddressList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile', name: 'Profile', component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/feedback', name: 'Feedback', component: () => import('@/views/Feedback.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 4: Commit**

```bash
git add hmall-web/src/router/ hmall-web/src/api/
git commit -m "feat: hmall-web 路由配置和 API 请求封装"
```

---

### Task 4.3: 创建 Pinia 状态管理

**Files:**
- Create: `hmall-web/src/stores/user.js`
- Create: `hmall-web/src/stores/cart.js`
- Create: `hmall-web/src/stores/favorite.js`

- [ ] **Step 1: 创建 user store**

```js
// hmall-web/src/stores/user.js
import { defineStore } from 'pinia'
import { login as loginApi } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    userId: (state) => state.userInfo?.userId
  },
  actions: {
    async login(loginForm) {
      const data = await loginApi(loginForm)
      this.token = data.token
      this.userInfo = { userId: data.userId, username: data.username, balance: data.balance }
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
```

- [ ] **Step 2: 创建 cart store**

```js
// hmall-web/src/stores/cart.js
import { defineStore } from 'pinia'
import { getCart, addToCart, updateCartItem, deleteCartItem } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    selectedIds: []
  }),
  getters: {
    selectedItems: (state) => state.items.filter(i => state.selectedIds.includes(i.id)),
    totalAmount: (state) => state.selectedItems.reduce((sum, i) => sum + i.price * i.num, 0),
    totalCount: (state) => state.selectedIds.length
  },
  actions: {
    async fetchCart() {
      this.items = await getCart()
      this.selectedIds = this.items.map(i => i.id)
    },
    async addItem(form) {
      await addToCart(form)
      await this.fetchCart()
    },
    async updateItem(id, data) {
      await updateCartItem(id, data)
      await this.fetchCart()
    },
    async removeItem(id) {
      await deleteCartItem(id)
      await this.fetchCart()
    },
    toggleSelect(id) {
      const idx = this.selectedIds.indexOf(id)
      if (idx >= 0) this.selectedIds.splice(idx, 1)
      else this.selectedIds.push(id)
    },
    toggleAll() {
      if (this.selectedIds.length === this.items.length) {
        this.selectedIds = []
      } else {
        this.selectedIds = this.items.map(i => i.id)
      }
    }
  }
})
```

- [ ] **Step 3: Commit**

```bash
git add hmall-web/src/stores/
git commit -m "feat: hmall-web Pinia stores（user/cart/favorite）"
```

---

### Task 4.4: 创建公共组件

**Files:**
- Create: `hmall-web/src/components/AppHeader.vue`
- Create: `hmall-web/src/components/AppFooter.vue`
- Create: `hmall-web/src/components/ProductCard.vue`
- Create: `hmall-web/src/components/StarRating.vue`
- Create: `hmall-web/src/components/ReviewSection.vue`
- Create: `hmall-web/src/components/CouponSelector.vue`
- Create: `hmall-web/src/App.vue`

- [ ] **Step 1: 创建 App.vue（含布局框架）**

```vue
<!-- hmall-web/src/App.vue -->
<template>
  <div id="app">
    <AppHeader />
    <main class="main-content">
      <router-view />
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
</script>

<style scoped>
.main-content {
  min-height: calc(100vh - 120px);
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px;
}
</style>
```

- [ ] **Step 2: 创建 AppHeader.vue**

```vue
<!-- hmall-web/src/components/AppHeader.vue -->
<template>
  <el-header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">hmall 电商平台</router-link>
      <el-input v-model="keyword" placeholder="搜索商品" class="search-input"
                @keyup.enter="search">
        <template #append>
          <el-button @click="search"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <div class="header-actions">
        <template v-if="userStore.isLoggedIn">
          <router-link to="/cart">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0">
              <el-button text><el-icon><ShoppingCart /></el-icon>购物车</el-button>
            </el-badge>
          </router-link>
          <router-link to="/orders"><el-button text>我的订单</el-button></router-link>
          <router-link to="/profile"><el-button text>个人中心</el-button></router-link>
          <el-button text @click="userStore.logout()">退出</el-button>
        </template>
        <template v-else>
          <router-link to="/login"><el-button type="primary">登录</el-button></router-link>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { Search, ShoppingCart } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const keyword = ref('')

function search() {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { q: keyword.value.trim() } })
  }
}
</script>
```

- [ ] **Step 3: 创建 AppFooter.vue（简化版页脚）**

```vue
<!-- hmall-web/src/components/AppFooter.vue -->
<template>
  <el-footer class="app-footer">
    <p>&copy; 2026 hmall 电商平台 | Web开发技术课程大作业</p>
  </el-footer>
</template>
<style scoped>
.app-footer { text-align: center; padding: 20px; color: #999; background: #f5f5f5; }
</style>
```

- [ ] **Step 4: 创建 ProductCard.vue（商品卡片组件）**

```vue
<!-- hmall-web/src/components/ProductCard.vue -->
<template>
  <el-card class="product-card" shadow="hover" @click="goDetail">
    <img :src="item.image || '/placeholder.png'" class="product-image" />
    <div class="product-info">
      <h3 class="product-name">{{ item.name }}</h3>
      <p class="product-price">&yen;{{ (item.price / 100).toFixed(2) }}</p>
      <p class="product-sold">已售 {{ item.sold }}</p>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
const props = defineProps({ item: Object })
const router = useRouter()
function goDetail() { router.push(`/item/${props.item.id}`) }
</script>

<style scoped>
.product-card { cursor: pointer; margin-bottom: 16px; }
.product-image { width: 100%; height: 200px; object-fit: cover; }
.product-name { font-size: 14px; margin: 8px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price { color: #f56c6c; font-size: 18px; font-weight: bold; }
.product-sold { color: #999; font-size: 12px; }
</style>
```

- [ ] **Step 5: 创建 StarRating.vue（星级评分组件）**

```vue
<!-- hmall-web/src/components/StarRating.vue -->
<template>
  <span class="star-rating">
    <el-icon v-for="i in 5" :key="i" :class="{ filled: i <= modelValue }"
             @click="$emit('update:modelValue', i)">
      <StarFilled v-if="i <= modelValue" />
      <Star v-else />
    </el-icon>
  </span>
</template>
<script setup>
defineProps({ modelValue: { type: Number, default: 0 } })
defineEmits(['update:modelValue'])
</script>
<style scoped>
.star-rating { display: inline-flex; gap: 2px; }
.filled { color: #f7ba2a; }
</style>
```

- [ ] **Step 6: Commit**

```bash
git add hmall-web/src/App.vue hmall-web/src/components/
git commit -m "feat: hmall-web 公共组件（Header/Footer/ProductCard/StarRating）"
```

---

### Task 4.5: 创建页面视图（核心 12 个页面）

由于前端页面代码量大，此处给出每个页面的核心结构和关键逻辑。每个页面使用 Element Plus 组件，遵循 Vue 3 Composition API 风格。

- [ ] **Step 1: Home.vue — 首页**

轮播图（el-carousel）、分类导航（el-menu）、热门/新品/促销商品列表（el-row + ProductCard）。数据通过 `/items` 接口获取，按 sold/price/createTime 排序。

- [ ] **Step 2: Login.vue — 登录/注册/找回密码**

el-tabs 三栏切换：登录表单（用户名+密码）、注册表单（用户名+邮箱+密码+验证码+发送验证码按钮）、找回密码表单（邮箱+验证码+新密码）。

- [ ] **Step 3: ItemDetail.vue — 商品详情**

商品图片轮播、名称、价格、规格、库存、数量选择器、加入购物车按钮、收藏按钮。底部评价区（ReviewSection）。

- [ ] **Step 4: Cart.vue — 购物车**

el-table 展示商品列表，每行：勾选框、图片、名称、单价、数量增减、小计、删除。底部：全选、总价、结算按钮。

- [ ] **Step 5: OrderConfirm.vue — 订单确认**

地址选择器（已保存地址列表，可切换）、商品清单、优惠券选择器（CouponSelector）、总价计算、提交订单按钮。

- [ ] **Step 6: OrderList.vue — 我的订单**

el-tabs 按状态切换：全部/待支付/待发货/待收货/已完成/已取消。每项订单展示商品缩略、金额、状态、操作按钮。

- [ ] **Step 7: Search.vue — 搜索结果**

顶部排序栏（综合/价格/销量/新品），商品网格展示，分页。

- [ ] **Step 8: FavoriteList.vue — 收藏列表**

商品网格 + 取消收藏按钮。

- [ ] **Step 9: AddressList.vue — 地址管理**

地址卡片列表，每项：收货人、手机、地址、默认标签、编辑/删除按钮。新增按钮弹出 el-dialog 表单。

- [ ] **Step 10: Profile.vue — 个人信息**

el-form：头像上传、昵称、邮箱修改。

- [ ] **Step 11: Feedback.vue — 用户反馈**

el-input textarea + 提交按钮。

- [ ] **Step 12: Notifications.vue — 公告列表**

el-timeline 展示公告标题和内容。

- [ ] **Step 13: Commit（每个页面完成后分别 commit）**

```bash
git add hmall-web/src/views/
git commit -m "feat: hmall-web 页面视图（Home/Login/ItemDetail/Cart/Order/...）"
```

---

### Task 4.6: 创建全局样式

**Files:**
- Create: `hmall-web/src/styles/global.css`

- [ ] **Step 1: 全局样式**

```css
/* hmall-web/src/styles/global.css */
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif; color: #333; background: #f5f7fa; }
a { text-decoration: none; color: inherit; }
```

- [ ] **Step 2: Commit**

```bash
git add hmall-web/src/styles/
git commit -m "feat: hmall-web 全局样式"
```

---

## Phase 5: hmall-admin 管理后台前端

### Task 5.1: 初始化管理后台项目

- [ ] **Step 1: 使用 Vite 创建项目**

```bash
cd hmall
npm create vite@latest hmall-admin -- --template vue
cd hmall-admin
npm install
npm install element-plus @element-plus/icons-vue pinia vue-router axios echarts vue-echarts
```

- [ ] **Step 2: 创建 vite.config.js**

```js
// hmall-admin/vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: { alias: { '@': path.resolve(__dirname, 'src') } },
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

- [ ] **Step 3: 创建 main.js（入口）**

```js
// hmall-admin/src/main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
```

- [ ] **Step 4: 创建路由**

```js
// hmall-admin/src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  {
    path: '/', component: () => import('@/components/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'users', name: 'UserList', component: () => import('@/views/UserList.vue') },
      { path: 'items', name: 'ItemList', component: () => import('@/views/ItemList.vue') },
      { path: 'items/add', name: 'ItemAdd', component: () => import('@/views/ItemEdit.vue') },
      { path: 'items/:id/edit', name: 'ItemEdit', component: () => import('@/views/ItemEdit.vue') },
      { path: 'categories', name: 'CategoryList', component: () => import('@/views/CategoryList.vue') },
      { path: 'orders', name: 'OrderList', component: () => import('@/views/OrderList.vue') },
      { path: 'orders/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue') },
      { path: 'reviews', name: 'ReviewList', component: () => import('@/views/ReviewList.vue') },
      { path: 'banners', name: 'BannerList', component: () => import('@/views/BannerList.vue') },
      { path: 'notifications', name: 'NotificationList', component: () => import('@/views/NotificationList.vue') },
      { path: 'feedbacks', name: 'FeedbackList', component: () => import('@/views/FeedbackList.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue') },
      { path: '', redirect: '/dashboard' }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('adminToken')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 5: Commit**

```bash
git add hmall-admin/
git commit -m "feat: 初始化 hmall-admin 管理后台 Vue 3 项目"
```

---

### Task 5.2: 创建 AdminLayout 和管理后台登录页

**Files:**
- Create: `hmall-admin/src/components/AdminLayout.vue`
- Create: `hmall-admin/src/components/StatCard.vue`
- Create: `hmall-admin/src/components/ChartPanel.vue`
- Create: `hmall-admin/src/components/ImageUpload.vue`
- Create: `hmall-admin/src/views/Login.vue`
- Create: `hmall-admin/src/api/auth.js`

- [ ] **Step 1: 创建 AdminLayout.vue（侧边栏 + 顶栏布局）**

```vue
<!-- hmall-admin/src/components/AdminLayout.vue -->
<template>
  <el-container class="admin-layout">
    <el-aside width="220px">
      <div class="logo">hmall 管理后台</div>
      <el-menu :default-active="route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon>数据看板</el-menu-item>
        <el-sub-menu index="users-group">
          <template #title><el-icon><User /></el-icon>用户管理</template>
          <el-menu-item index="/users">用户列表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="items-group">
          <template #title><el-icon><Goods /></el-icon>商品管理</template>
          <el-menu-item index="/items">商品列表</el-menu-item>
          <el-menu-item index="/items/add">新增商品</el-menu-item>
          <el-menu-item index="/categories">分类管理</el-menu-item>
          <el-menu-item index="/reviews">评价管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/orders"><el-icon><Tickets /></el-icon>订单管理</el-menu-item>
        <el-sub-menu index="system-group">
          <template #title><el-icon><Setting /></el-icon>系统管理</template>
          <el-menu-item index="/banners">轮播图管理</el-menu-item>
          <el-menu-item index="/notifications">公告管理</el-menu-item>
          <el-menu-item index="/feedbacks">反馈管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/profile"><el-icon><UserFilled /></el-icon>个人中心</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <span>管理员，你好</span>
        <el-button text @click="logout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()
function logout() {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminInfo')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout { height: 100vh; }
.logo { color: #fff; text-align: center; padding: 16px; font-size: 18px; font-weight: bold; }
.admin-header { display: flex; justify-content: space-between; align-items: center; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.1); }
</style>
```

- [ ] **Step 2: 创建 Login.vue**

管理员登录表单（用户名+密码），调用 `/users/login` 接口，登录成功后检查 role=admin，存储 token 跳转到 dashboard。

- [ ] **Step 3: Commit**

```bash
git add hmall-admin/src/components/AdminLayout.vue hmall-admin/src/views/Login.vue
git commit -m "feat: hmall-admin AdminLayout 布局和管理员登录"
```

---

### Task 5.3: 创建管理后台核心页面

每个页面按以下规格创建，使用 Element Plus 的 table/form/dialog 组件：

- [ ] **Dashboard.vue** — 统计卡片（StatCard: 总用户数/总订单数/总销售额/今日订单）+ ECharts 图表（销量趋势折线图、热销排行柱状图、订单状态饼图）

- [ ] **UserList.vue** — el-table（用户名/手机号/邮箱/状态/注册时间）+ 搜索框 + 禁用/启用操作按钮

- [ ] **ItemList.vue** — el-table（商品图片/名称/价格/库存/状态）+ 筛选（分类/状态）+ 新增/编辑/上下架/删除按钮 + 分页

- [ ] **ItemEdit.vue** — el-form（名称/价格/库存/分类/品牌/规格/图片上传/富文本详情）+ 提交按钮

- [ ] **CategoryList.vue** — el-table 树形展示 + 新增/编辑/删除操作

- [ ] **OrderList.vue** — el-table（订单号/用户/金额/状态/时间）+ 状态筛选 + 详情/发货/退款操作按钮 + 导出 Excel 按钮

- [ ] **OrderDetail.vue** — 订单信息 + 商品清单 + 地址 + 支付信息 + 物流信息 + 状态操作

- [ ] **ReviewList.vue** — el-table（商品/用户/内容/评分/时间）+ 删除按钮

- [ ] **BannerList.vue** — el-table（图片预览/标题/链接/排序/状态）+ 新增/编辑/删除 dialog

- [ ] **NotificationList.vue** — el-table（标题/发布时间/状态）+ 新建/编辑/删除 dialog

- [ ] **FeedbackList.vue** — el-table（用户/内容/状态/时间）+ 查看详情 + 回复 dialog

- [ ] **Profile.vue** — 管理员修改密码和个人信息

- [ ] **Step: Commit**

```bash
git add hmall-admin/src/views/
git commit -m "feat: hmall-admin 核心管理页面（Dashboard/用户/商品/订单/评价/系统管理）"
```

---

## Phase 6: Redis 缓存 + Docker 部署

### Task 6.1: 集成 Redis 缓存

**Files:**
- Modify: `item-service/pom.xml` (添加 spring-boot-starter-data-redis)
- Modify: `item-service/src/main/java/com/hmall/item/service/impl/ItemServiceImpl.java` (添加缓存注解)
- Modify: `item-service/src/main/java/com/hmall/item/service/impl/CategoryServiceImpl.java` (添加缓存注解)
- Modify: `item-service/src/main/resources/application.yaml` (添加 Redis 连接配置)
- Modify: `cart-service/pom.xml` (添加 spring-boot-starter-data-redis)
- Modify: `cart-service/src/main/resources/application.yaml` (添加 Redis 连接配置)

- [ ] **Step 1: 为 item-service 添加 Redis 依赖和配置**

```xml
<!-- item-service/pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
# item-service/src/main/resources/application.yaml 追加
spring:
  redis:
    host: ${hm.redis.host:localhost}
    port: ${hm.redis.port:6379}
  cache:
    type: redis
    redis:
      time-to-live: 30m
```

- [ ] **Step 2: 在 ItemServiceImpl 添加 @Cacheable/@CacheEvict**

```java
// item-service ItemServiceImpl
// 商品详情缓存
@Override
@Cacheable(value = "item", key = "#id")
public Item getById(Long id) {
    return getBaseMapper().selectById(id);
}

// 更新时清除缓存
// 在 updateItem 方法上加 @CacheEvict(value = "item", key = "#item.id")
// 在 saveItem 方法后不需要额外处理（新商品无缓存）

// 热门商品缓存
// 在 getHotItems 方法上 @Cacheable(value = "hotItems", key = "'top20'")
```

- [ ] **Step 3: 分类列表缓存**

```java
// CategoryServiceImpl
@Override
@Cacheable(value = "categories", key = "'all'")
public List<Category> list() { ... }

// 增删改时 @CacheEvict(value = "categories", allEntries = true)
```

- [ ] **Step 4: 在各服务 application.yaml 中添加启用缓存的配置 `spring.cache.type: redis`**

- [ ] **Step 5: Commit**

```bash
git add item-service/ cart-service/
git commit -m "feat: item-service/cart-service 集成 Redis 缓存（商品详情/分类/热门商品）"
```

---

### Task 6.2: Docker Compose 编排

**Files:**
- Create: `docker-compose.yml` (项目根目录)
- Create: Dockerfile for each service that doesn't have one
- Create: `hmall-web/nginx.conf`
- Create: `hmall-admin/nginx.conf`

- [ ] **Step 1: 创建根目录 docker-compose.yml**

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: hmall
    ports: ["3306:3306"]
    volumes: ["./docker/mysql:/var/lib/mysql"]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  nacos:
    image: nacos/nacos-server:v2.1.0
    environment:
      MODE: standalone
    ports: ["8848:8848"]
    depends_on: [mysql]

  redis:
    image: redis:7.0
    ports: ["6379:6379"]

  rabbitmq:
    image: rabbitmq:3.9-management
    ports: ["5672:5672", "15672:15672"]

  hm-gateway:
    build: ./hm-gateway
    ports: ["8080:8080"]
    depends_on: [nacos]

  user-service:
    build: ./user-service
    depends_on: [nacos, mysql, redis]

  item-service:
    build: ./item-service
    depends_on: [nacos, mysql, redis]

  cart-service:
    build: ./cart-service
    depends_on: [nacos, mysql, redis]

  trade-service:
    build: ./trade-service
    depends_on: [nacos, mysql]

  pay-service:
    build: ./pay-service
    depends_on: [nacos, mysql]

  notify-service:
    build: ./notify-service
    depends_on: [nacos, mysql]

  file-service:
    build: ./file-service
    depends_on: [nacos, mysql]

  hmall-web:
    image: nginx:alpine
    volumes:
      - "./hmall-web/dist:/usr/share/nginx/html"
      - "./hmall-web/nginx.conf:/etc/nginx/conf.d/default.conf"
    ports: ["80:80"]
    depends_on: [hm-gateway]

  hmall-admin:
    image: nginx:alpine
    volumes:
      - "./hmall-admin/dist:/usr/share/nginx/html"
      - "./hmall-admin/nginx.conf:/etc/nginx/conf.d/default.conf"
    ports: ["81:80"]
    depends_on: [hm-gateway]
```

- [ ] **Step 2: 创建前端 Nginx 配置**

```nginx
# hmall-web/nginx.conf
server {
    listen 80;
    server_name localhost;
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    location /api/ {
        proxy_pass http://hm-gateway:8080/;
        proxy_set_header Host $host;
    }
}
```

```nginx
# hmall-admin/nginx.conf (同上，只是端口不同，proxy_pass 同)
```

- [ ] **Step 3: 为每个缺少 Dockerfile 的服务创建 Dockerfile**

```dockerfile
# 每个服务通用 Dockerfile 模板
FROM openjdk:11-jre
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

- [ ] **Step 4: Commit**

```bash
git add docker-compose.yml */Dockerfile hmall-web/nginx.conf hmall-admin/nginx.conf
git commit -m "feat: Docker Compose 编排 + 各服务 Dockerfile + 前端 Nginx 配置"
```

---

## Phase 7: 联调测试

### Task 7.1: 启动基础设施

- [ ] **Step 1: 启动 MySQL、Nacos、Redis、RabbitMQ**

```bash
docker-compose up -d mysql nacos redis rabbitmq
```

- [ ] **Step 2: 等待 Nacos 就绪（访问 http://localhost:8848/nacos 确认）**

- [ ] **Step 3: 执行数据库初始化脚本**

```bash
mysql -h 127.0.0.1 -u root -proot hmall < docs/sql/init-extra-tables.sql
```

---

### Task 7.2: 启动后端服务并验证 API

- [ ] **Step 1: 编译所有模块**

```bash
mvn clean package -DskipTests
```

- [ ] **Step 2: 按顺序启动服务**

```bash
# 逐一启动（先启动不依赖其他服务的，再启动依赖方）
# 1. Gateway
java -jar hm-gateway/target/hm-gateway.jar &
# 2. 各微服务
java -jar user-service/target/user-service.jar &
java -jar item-service/target/item-service.jar &
java -jar cart-service/target/cart-service.jar &
java -jar trade-service/target/trade-service.jar &
java -jar pay-service/target/pay-service.jar &
java -jar notify-service/target/notify-service.jar &
java -jar file-service/target/file-service.jar &
```

- [ ] **Step 3: 验证关键 API（使用 curl 或 Apifox）**

```bash
# 商品列表
curl http://localhost:8080/items?page=1&size=10
# 用户登录
curl -X POST http://localhost:8080/users/login -H 'Content-Type: application/json' -d '{"username":"test","password":"123456"}'
# 公告
curl http://localhost:8080/notifications/active
```

- [ ] **Step 4: 修复 API 问题（迭代）**

---

### Task 7.3: 启动前端并验证页面

- [ ] **Step 1: 构建前端**

```bash
cd hmall-web && npm run build
cd ../hmall-admin && npm run build
```

- [ ] **Step 2: 启动 nginx 或直接使用 Vite dev server 调试**

```bash
cd hmall-web && npm run dev
```

- [ ] **Step 3: 验证关键页面流程**

- 首页加载（轮播/分类/推荐商品）
- 搜索 → 商品详情 → 登录 → 加购 → 结算 → 下单
- 管理后台登录 → 看板 → 商品管理 → 订单管理

- [ ] **Step 4: 修复前端问题（迭代）**

---

### Task 7.4: 最终 Commit 和文档

- [ ] **Step 1: 提交所有修复**

```bash
git add -A
git commit -m "fix: 联调修复"
```

- [ ] **Step 2: 更新 CLAUDE.md 或 README 项目说明**

---

## 附录：文件清单

### 后端新创建文件

```
notify-service/pom.xml
notify-service/src/main/java/com/hmall/notify/NotifyApplication.java
notify-service/src/main/java/com/hmall/notify/domain/po/Notification.java
notify-service/src/main/java/com/hmall/notify/domain/po/Feedback.java
notify-service/src/main/java/com/hmall/notify/domain/po/CustomerMessage.java
notify-service/src/main/java/com/hmall/notify/mapper/NotificationMapper.java
notify-service/src/main/java/com/hmall/notify/mapper/FeedbackMapper.java
notify-service/src/main/java/com/hmall/notify/mapper/CustomerMessageMapper.java
notify-service/src/main/java/com/hmall/notify/service/INotificationService.java
notify-service/src/main/java/com/hmall/notify/service/IFeedbackService.java
notify-service/src/main/java/com/hmall/notify/service/ICustomerMessageService.java
notify-service/src/main/java/com/hmall/notify/service/impl/NotificationServiceImpl.java
notify-service/src/main/java/com/hmall/notify/service/impl/FeedbackServiceImpl.java
notify-service/src/main/java/com/hmall/notify/service/impl/CustomerMessageServiceImpl.java
notify-service/src/main/java/com/hmall/notify/controller/NotificationController.java
notify-service/src/main/java/com/hmall/notify/controller/FeedbackController.java
notify-service/src/main/java/com/hmall/notify/controller/CustomerMessageController.java
notify-service/src/main/resources/application.yaml
notify-service/src/main/resources/bootstrap.yaml

file-service/pom.xml
file-service/src/main/java/com/hmall/file/FileApplication.java
file-service/src/main/java/com/hmall/file/domain/po/Upload.java
file-service/src/main/java/com/hmall/file/mapper/UploadMapper.java
file-service/src/main/java/com/hmall/file/service/IUploadService.java
file-service/src/main/java/com/hmall/file/service/impl/UploadServiceImpl.java
file-service/src/main/java/com/hmall/file/controller/FileController.java
file-service/src/main/resources/application.yaml
file-service/src/main/resources/bootstrap.yaml

hm-common/src/main/java/com/hmall/common/config/RedisConfig.java

hm-api/src/main/java/com/hmall/api/client/ReviewClient.java
hm-api/src/main/java/com/hmall/api/client/FavoriteClient.java
hm-api/src/main/java/com/hmall/api/client/CouponClient.java
hm-api/src/main/java/com/hmall/api/client/NotificationClient.java
hm-api/src/main/java/com/hmall/api/client/FileClient.java
hm-api/src/main/java/com/hmall/api/dto/ReviewDTO.java
hm-api/src/main/java/com/hmall/api/dto/CouponDTO.java

user-service/src/main/java/com/hmall/domain/dto/RegisterFormDTO.java
user-service/src/main/java/com/hmall/domain/dto/ResetPasswordDTO.java
user-service/src/main/java/com/hmall/domain/po/Address.java
user-service/src/main/java/com/hmall/domain/po/UserFavorite.java
user-service/src/main/java/com/hmall/mapper/AddressMapper.java
user-service/src/main/java/com/hmall/mapper/UserFavoriteMapper.java
user-service/src/main/java/com/hmall/service/IAddressService.java
user-service/src/main/java/com/hmall/service/IFavoriteService.java
user-service/src/main/java/com/hmall/service/impl/AddressServiceImpl.java
user-service/src/main/java/com/hmall/service/impl/FavoriteServiceImpl.java
user-service/src/main/java/com/hmall/controller/AddressController.java
user-service/src/main/java/com/hmall/controller/FavoriteController.java

item-service/src/main/java/com/hmall/item/domain/po/ItemReview.java
item-service/src/main/java/com/hmall/item/domain/po/Category.java
item-service/src/main/java/com/hmall/item/mapper/ItemReviewMapper.java
item-service/src/main/java/com/hmall/item/mapper/CategoryMapper.java
item-service/src/main/java/com/hmall/item/service/IItemReviewService.java
item-service/src/main/java/com/hmall/item/service/ICategoryService.java
item-service/src/main/java/com/hmall/item/service/impl/ItemReviewServiceImpl.java
item-service/src/main/java/com/hmall/item/service/impl/CategoryServiceImpl.java
item-service/src/main/java/com/hmall/item/controller/ReviewController.java
item-service/src/main/java/com/hmall/item/controller/CategoryController.java

trade-service/src/main/java/com/hmall/domain/po/Coupon.java
trade-service/src/main/java/com/hmall/domain/po/UserCoupon.java
trade-service/src/main/java/com/hmall/mapper/CouponMapper.java
trade-service/src/main/java/com/hmall/mapper/UserCouponMapper.java
trade-service/src/main/java/com/hmall/service/ICouponService.java
trade-service/src/main/java/com/hmall/service/impl/CouponServiceImpl.java
trade-service/src/main/java/com/hmall/controller/CouponController.java

docker-compose.yml
docs/sql/init-extra-tables.sql
```

### 前端文件清单

```
hmall-web/    (~20 files: vite.config.js, main.js, router, stores×3, api×6, views×12, components×6, styles/global.css)
hmall-admin/  (~20 files: vite.config.js, main.js, router, stores×1, api×5, views×13, components×4, styles/global.css)
```
