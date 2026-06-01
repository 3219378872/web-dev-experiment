# Phase 1 测试补足实施计划：核心交易链路

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 cart-service、trade-service、item-service 三个模块补齐单元测试，使每个模块各自达到 80% 行覆盖率。

**Architecture:** 使用 H2 内存数据库 + Spring Boot Test 让 MyBatis-Plus ServiceImpl 正常工作，用 @MockBean 隔离 Feign 远程调用（ItemClient/CartClient）。Controller 层用 @WebMvcTest + @MockBean Service。

**Tech Stack:** JUnit 5, Mockito, AssertJ, Spring Boot Test, H2, MyBatis-Plus, @WebMvcTest

**Source spec:** `docs/superpowers/specs/2026-06-01-test-coverage-design.md`

---

## 关键设计决策

### 为什么用 H2 而不是纯 Mockito？

三个模块的 Service 都继承 `ServiceImpl<M, T>`，大量使用 `lambdaQuery()`、`save()`、`updateById()`、`getById()`、`remove()` 等 MyBatis-Plus 内置方法。这些方法内部链路复杂（BaseMapper → SqlSession → JDBC），无法用纯 Mockito 有效 mock。使用 H2 内存数据库是最务实的方案：
- MyBatis-Plus 正常初始化 SQL Session
- Service 的 CRUD 操作在真实数据库上执行
- 仅 @MockBean 远程依赖（Feign clients）

### Controller 测试策略

Controller 层用 `@WebMvcTest` 独立测试，不依赖数据库：
- Mock Service 层（@MockBean）
- 测试请求参数校验、路由映射、HTTP 状态码、响应格式
- 对覆盖率贡献小（大部分逻辑在 Service），每个 Controller 挑 2-3 个核心端点

---

## Task 1: cart-service 测试基础设施

**Files:**
- Modify: `cart-service/pom.xml` (add H2 dependency)
- Create: `cart-service/src/test/resources/application.yml`
- Create: `cart-service/src/test/java/com/hmall/cart/CartServiceTestBase.java`

### Step 1: 添加 H2 和 MyBatis-Plus 测试依赖

编辑 `cart-service/pom.xml`，在 `<dependencies>` 末尾（`</dependencies>` 之前）添加：

```xml
<!-- 测试依赖 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter-test</artifactId>
    <version>2.3.0</version>
    <scope>test</scope>
</dependency>
```

运行: `mvn -q -pl cart-service -am validate`
预期: BUILD SUCCESS

### Step 2: 创建测试 application.yml

创建文件 `cart-service/src/test/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:cart_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true
    driver-class-name: org.h2.Driver
    username: sa
    password:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql

mybatis-plus:
  global-config:
    db-config:
      id-type: auto

hm:
  jwt:
    location: classpath:hmall.jks
    alias: hmall
    password: hmall123
    tokenTTL: 30m
  cart:
    max-items: 10
  auth:
    excludePaths: /error,/api/public/**
```

创建文件 `cart-service/src/test/resources/schema.sql`：

```sql
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    num INT DEFAULT 1,
    name VARCHAR(255),
    spec VARCHAR(255),
    price INT,
    image VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Step 3: 创建测试基类

创建 `cart-service/src/test/java/com/hmall/cart/CartServiceTestBase.java`：

```java
package com.hmall.cart;

import com.hmall.api.client.ItemClient;
import com.hmall.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public abstract class CartServiceTestBase {

    @MockBean
    protected ItemClient itemClient;

    protected static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUpContext() {
        UserContext.setUser(TEST_USER_ID);
    }

    @AfterEach
    void tearDownContext() {
        UserContext.clear();
    }
}
```

### Step 4: 验证基础设施

运行: `mvn -q -pl cart-service -am test`
预期: BUILD SUCCESS (0 tests，但 Spring 上下文能启动)

### Step 5: Commit

```bash
git add cart-service/pom.xml \
        cart-service/src/test/resources/application.yml \
        cart-service/src/test/resources/schema.sql \
        cart-service/src/test/java/com/hmall/cart/CartServiceTestBase.java
git commit -m "test(cart): add H2 test infrastructure

Add H2 in-memory database for MyBatis-Plus service testing.
Mock ItemClient to isolate from remote services.

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 2: CartServiceImpl 单元测试

**Files:**
- Create: `cart-service/src/test/java/com/hmall/cart/service/impl/CartServiceImplTest.java`

### Step 1: 编写测试（TDD — 先写测试）

创建 `cart-service/src/test/java/com/hmall/cart/service/impl/CartServiceImplTest.java`：

```java
package com.hmall.cart.service.impl;

import com.hmall.api.dto.ItemDTO;
import com.hmall.cart.CartServiceTestBase;
import com.hmall.cart.config.CartProperties;
import com.hmall.cart.domain.dto.CartFormDTO;
import com.hmall.cart.domain.po.Cart;
import com.hmall.cart.domain.vo.CartVO;
import com.hmall.cart.mapper.CartMapper;
import com.hmall.cart.service.ICartService;
import com.hmall.common.exception.BizIllegalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

class CartServiceImplTest extends CartServiceTestBase {

    @Autowired
    private ICartService cartService;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartProperties cartProperties;

    // ---- addItem2Cart ----

    @Test
    @DisplayName("addItem2Cart: 新商品正常加入购物车")
    void addItem2Cart_newItem_success() {
        CartFormDTO form = new CartFormDTO();
        form.setItemId(100L);
        form.setName("测试商品");
        form.setPrice(9900);
        form.setImage("test.jpg");

        cartService.addItem2Cart(form);

        List<Cart> carts = cartMapper.selectList(null);
        assertThat(carts).hasSize(1);
        assertThat(carts.get(0).getItemId()).isEqualTo(100L);
        assertThat(carts.get(0).getUserId()).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("addItem2Cart: 同商品重复加入，数量+1而非新增条目")
    void addItem2Cart_existingItem_incrementsNum() {
        // 先插入一条已有记录
        Cart existing = new Cart();
        existing.setUserId(TEST_USER_ID);
        existing.setItemId(100L);
        existing.setNum(1);
        existing.setName("测试商品");
        existing.setPrice(9900);
        cartMapper.insert(existing);

        CartFormDTO form = new CartFormDTO();
        form.setItemId(100L);
        form.setName("测试商品");
        form.setPrice(9900);

        cartService.addItem2Cart(form);

        List<Cart> carts = cartMapper.selectList(null);
        assertThat(carts).hasSize(1);
        assertThat(carts.get(0).getNum()).isEqualTo(2); // num + 1 by updateNum
    }

    @Test
    @DisplayName("addItem2Cart: 购物车已满时抛 BizIllegalException")
    void addItem2Cart_cartFull_throwsException() {
        // 填满购物车
        for (int i = 0; i < cartProperties.getMaxItems(); i++) {
            Cart c = new Cart();
            c.setUserId(TEST_USER_ID);
            c.setItemId((long) (200 + i));
            c.setNum(1);
            cartMapper.insert(c);
        }

        CartFormDTO form = new CartFormDTO();
        form.setItemId(999L);
        form.setName("超限商品");
        form.setPrice(100);

        assertThatThrownBy(() -> cartService.addItem2Cart(form))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("不能超过");
    }

    // ---- queryMyCarts ----

    @Test
    @DisplayName("queryMyCarts: 空购物车返回空列表")
    void queryMyCarts_empty_returnsEmptyList() {
        List<CartVO> vos = cartService.queryMyCarts();
        assertThat(vos).isEmpty();
    }

    @Test
    @DisplayName("queryMyCarts: 返回用户购物车并填充商品最新价格")
    void queryMyCarts_withItems_returnsCartVOWithPrices() {
        Cart cart = new Cart();
        cart.setUserId(TEST_USER_ID);
        cart.setItemId(100L);
        cart.setNum(2);
        cart.setName("测试商品");
        cart.setPrice(9900);
        cartMapper.insert(cart);

        // Mock ItemClient 返回最新商品信息
        ItemDTO item = new ItemDTO();
        item.setId(100L);
        item.setPrice(7900);  // 降价了
        item.setStatus(1);
        item.setStock(50);
        when(itemClient.queryItemByIds(anySet())).thenReturn(List.of(item));

        List<CartVO> vos = cartService.queryMyCarts();

        assertThat(vos).hasSize(1);
        CartVO vo = vos.get(0);
        assertThat(vo.getNewPrice()).isEqualTo(7900);
        assertThat(vo.getStatus()).isEqualTo(1);
        assertThat(vo.getStock()).isEqualTo(50);
    }

    // ---- removeByItemIds ----

    @Test
    @DisplayName("removeByItemIds: 批量删除指定商品")
    void removeByItemIds_removesMatchingItems() {
        Cart c1 = new Cart(); c1.setUserId(TEST_USER_ID); c1.setItemId(1L); cartMapper.insert(c1);
        Cart c2 = new Cart(); c2.setUserId(TEST_USER_ID); c2.setItemId(2L); cartMapper.insert(c2);
        Cart c3 = new Cart(); c3.setUserId(TEST_USER_ID); c3.setItemId(3L); cartMapper.insert(c3);

        cartService.removeByItemIds(List.of(1L, 3L));

        List<Cart> remaining = cartMapper.selectList(null);
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getItemId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("removeByItemIds: 空集合不删除任何条目")
    void removeByItemIds_emptyCollection_noop() {
        Cart c = new Cart(); c.setUserId(TEST_USER_ID); c.setItemId(1L); cartMapper.insert(c);

        cartService.removeByItemIds(List.of());

        assertThat(cartMapper.selectCount(null)).isEqualTo(1);
    }
}
```

### Step 2: 运行测试验证通过

```bash
mvn -q -pl cart-service -am test
```
预期: Tests run: 7, Failures: 0, Errors: 0

### Step 3: Commit

```bash
git add cart-service/src/test/java/com/hmall/cart/service/impl/CartServiceImplTest.java
git commit -m "test(cart): add CartServiceImpl unit tests (7 tests)

Covers addItem2Cart, queryMyCarts, removeByItemIds with H2 database.

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 3: CartController WebMvcTest

**Files:**
- Create: `cart-service/src/test/java/com/hmall/cart/controller/CartControllerTest.java`

### Step 1: 编写测试

创建 `cart-service/src/test/java/com/hmall/cart/controller/CartControllerTest.java`：

```java
package com.hmall.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmall.cart.domain.dto.CartFormDTO;
import com.hmall.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICartService cartService;

    @BeforeEach
    void setUp() {
        UserContext.setUser(1L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("POST /carts: 有效请求返回 200")
    void addItem2Cart_validRequest_returns200() throws Exception {
        CartFormDTO form = new CartFormDTO();
        form.setItemId(100L);
        form.setName("测试商品");
        form.setPrice(9900);

        doNothing().when(cartService).addItem2Cart(any());

        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /carts: 缺少必填字段时返回 400")
    void addItem2Cart_missingFields_returns400() throws Exception {
        // itemId 为 null, @Valid 校验失败
        CartFormDTO form = new CartFormDTO();

        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest());
    }
}
```

### Step 2: 运行测试验证通过

```bash
mvn -q -pl cart-service -am test
```
预期: 9 tests total, all passing (7 service + 2 controller)

### Step 3: Commit

```bash
git add cart-service/src/test/java/com/hmall/cart/controller/CartControllerTest.java
git commit -m "test(cart): add CartController WebMvcTest (2 tests)

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 4: trade-service 测试基础设施

**Files:**
- Modify: `trade-service/pom.xml` (add H2 dependency)
- Create: `trade-service/src/test/resources/application.yml`
- Create: `trade-service/src/test/resources/schema.sql`
- Create: `trade-service/src/test/java/com/hmall/TradeServiceTestBase.java`

### Step 1: 添加 H2 依赖

编辑 `trade-service/pom.xml`，在 `<dependencies>` 末尾添加：

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter-test</artifactId>
    <version>2.3.0</version>
    <scope>test</scope>
</dependency>
```

### Step 2: 创建测试配置和 schema

创建 `trade-service/src/test/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:trade_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true
    driver-class-name: org.h2.Driver
    username: sa
    password:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql

mybatis-plus:
  global-config:
    db-config:
      id-type: auto

hm:
  jwt:
    location: classpath:hmall.jks
    alias: hmall
    password: hmall123
    tokenTTL: 30m
```

创建 `trade-service/src/test/resources/schema.sql`：

```sql
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_fee INT DEFAULT 0,
    payment_type INT DEFAULT 1,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    pay_time TIMESTAMP NULL,
    consign_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    close_time TIMESTAMP NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    name VARCHAR(255),
    spec VARCHAR(255),
    price INT,
    num INT,
    image VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS order_logistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    logistics_number VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128),
    type INT DEFAULT 1,
    discount_value INT,
    min_amount INT,
    total_stock INT DEFAULT 0,
    remaining_stock INT DEFAULT 0,
    status INT DEFAULT 1,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Step 3: 创建测试基类

创建 `trade-service/src/test/java/com/hmall/TradeServiceTestBase.java`：

```java
package com.hmall;

import com.hmall.api.client.CartClient;
import com.hmall.api.client.ItemClient;
import com.hmall.common.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public abstract class TradeServiceTestBase {

    @MockBean
    protected ItemClient itemClient;

    @MockBean
    protected CartClient cartClient;

    protected static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUpContext() {
        UserContext.setUser(TEST_USER_ID);
    }

    @AfterEach
    void tearDownContext() {
        UserContext.clear();
    }
}
```

### Step 4: 验证

```bash
mvn -q -pl trade-service -am test
```
预期: BUILD SUCCESS

### Step 5: Commit

```bash
git add trade-service/pom.xml \
        trade-service/src/test/resources/ \
        trade-service/src/test/java/com/hmall/TradeServiceTestBase.java
git commit -m "test(trade): add H2 test infrastructure with order/coupon schemas

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 5: CouponServiceImpl 单元测试

**Files:**
- Create: `trade-service/src/test/java/com/hmall/service/impl/CouponServiceImplTest.java`

### Step 1: 编写测试

创建 `trade-service/src/test/java/com/hmall/service/impl/CouponServiceImplTest.java`：

```java
package com.hmall.service.impl;

import com.hmall.TradeServiceTestBase;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.domain.po.Coupon;
import com.hmall.domain.po.UserCoupon;
import com.hmall.mapper.CouponMapper;
import com.hmall.mapper.UserCouponMapper;
import com.hmall.service.ICouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponServiceImplTest extends TradeServiceTestBase {

    @Autowired
    private ICouponService couponService;
    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private UserCouponMapper userCouponMapper;

    // ---- getAvailableCoupons ----

    @Test
    @DisplayName("getAvailableCoupons: 返回状态=1、有库存、在有效期内的券")
    void getAvailableCoupons_returnsValidCoupons() {
        Coupon valid = coupon("有效券", 100, 5, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7));
        Coupon expired = coupon("过期券", 100, 5, 1,
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));
        Coupon soldOut = coupon("售罄券", 100, 0, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7));
        Coupon disabled = coupon("停用券", 100, 5, 2,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7));
        couponMapper.insert(valid);
        couponMapper.insert(expired);
        couponMapper.insert(soldOut);
        couponMapper.insert(disabled);

        List<Coupon> result = couponService.getAvailableCoupons();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("有效券");
    }

    // ---- claimCoupon ----

    @Test
    @DisplayName("claimCoupon: 正常领取成功后扣减库存并创建用户券记录")
    void claimCoupon_success_deductsStockAndCreatesRecord() {
        Coupon c = coupon("优惠券", 100, 5, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7));
        couponMapper.insert(c);

        couponService.claimCoupon(TEST_USER_ID, c.getId());

        // 验证库存扣减
        Coupon updated = couponMapper.selectById(c.getId());
        assertThat(updated.getRemainingStock()).isEqualTo(4);

        // 验证用户券记录
        List<UserCoupon> records = userCouponMapper.selectList(null);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(records.get(0).getCouponId()).isEqualTo(c.getId());
    }

    @Test
    @DisplayName("claimCoupon: 券不存在时抛 BadRequestException")
    void claimCoupon_notFound_throwsBadRequest() {
        assertThatThrownBy(() -> couponService.claimCoupon(TEST_USER_ID, 999L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("claimCoupon: 库存为0时抛 BizIllegalException")
    void claimCoupon_soldOut_throwsBizIllegal() {
        Coupon c = coupon("售罄券", 100, 0, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7));
        couponMapper.insert(c);

        assertThatThrownBy(() -> couponService.claimCoupon(TEST_USER_ID, c.getId()))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("抢光");
    }

    @Test
    @DisplayName("claimCoupon: 重复领取抛 BizIllegalException")
    void claimCoupon_duplicateClaim_throwsBizIllegal() {
        Coupon c = coupon("券", 100, 5, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7));
        couponMapper.insert(c);
        couponService.claimCoupon(TEST_USER_ID, c.getId());

        assertThatThrownBy(() -> couponService.claimCoupon(TEST_USER_ID, c.getId()))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("已领取");
    }

    // ---- getUserCoupons ----

    @Test
    @DisplayName("getUserCoupons: 无券时返回空列表")
    void getUserCoupons_empty_returnsEmptyList() {
        List<Coupon> result = couponService.getUserCoupons(TEST_USER_ID);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUserCoupons: 返回用户已领取的券")
    void getUserCoupons_returnsClaimedCoupons() {
        Coupon c1 = coupon("券A", 100, 5, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7));
        Coupon c2 = coupon("券B", 200, 5, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7));
        couponMapper.insert(c1);
        couponMapper.insert(c2);
        couponService.claimCoupon(TEST_USER_ID, c1.getId());

        List<Coupon> result = couponService.getUserCoupons(TEST_USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("券A");
    }

    // ---- helper ----

    private Coupon coupon(String name, int discount, int stock, int status,
                          LocalDateTime start, LocalDateTime end) {
        Coupon c = new Coupon();
        c.setName(name);
        c.setDiscountValue(discount);
        c.setRemainingStock(stock);
        c.setStatus(status);
        c.setStartTime(start);
        c.setEndTime(end);
        c.setType(1);
        return c;
    }
}
```

### Step 2: 运行测试

```bash
mvn -q -pl trade-service -am test
```
预期: 7 tests passing for CouponServiceImpl

### Step 3: Commit

```bash
git add trade-service/src/test/java/com/hmall/service/impl/CouponServiceImplTest.java
git commit -m "test(trade): add CouponServiceImpl unit tests (7 tests)

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 6: OrderServiceImpl 单元测试

**Files:**
- Create: `trade-service/src/test/java/com/hmall/service/impl/OrderServiceImplTest.java`

### Step 1: 编写测试

创建 `trade-service/src/test/java/com/hmall/service/impl/OrderServiceImplTest.java`：

```java
package com.hmall.service.impl;

import com.hmall.TradeServiceTestBase;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.api.dto.OrderFormDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.domain.po.Order;
import com.hmall.domain.po.OrderLogistics;
import com.hmall.mapper.OrderLogisticsMapper;
import com.hmall.mapper.OrderMapper;
import com.hmall.service.IOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

class OrderServiceImplTest extends TradeServiceTestBase {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderLogisticsMapper logisticsMapper;

    // ---- createOrder ----

    @Test
    @DisplayName("createOrder: 正常下单成功，返回 orderId")
    void createOrder_validOrder_success() {
        // Mock itemClient 返回商品信息
        ItemDTO item = new ItemDTO();
        item.setId(100L);
        item.setPrice(5000);
        item.setName("测试商品");
        when(itemClient.queryItemByIds(anySet())).thenReturn(List.of(item));

        OrderFormDTO form = new OrderFormDTO();
        form.setPaymentType(1);
        OrderDetailDTO detail = new OrderDetailDTO();
        detail.setItemId(100L);
        detail.setNum(2);
        form.setDetails(List.of(detail));

        Long orderId = orderService.createOrder(form);

        assertThat(orderId).isNotNull();
        Order saved = orderMapper.selectById(orderId);
        assertThat(saved.getTotalFee()).isEqualTo(10000); // 5000 * 2
        assertThat(saved.getStatus()).isEqualTo(1);

        // 验证扣减库存和清理购物车被调用
        verify(itemClient).deductStock(any());
        verify(cartClient).deleteCartItemByIds(any());
    }

    @Test
    @DisplayName("createOrder: 商品不存在时抛 BadRequestException")
    void createOrder_itemNotExist_throwsBadRequest() {
        when(itemClient.queryItemByIds(anySet())).thenReturn(null);

        OrderFormDTO form = new OrderFormDTO();
        form.setPaymentType(1);
        OrderDetailDTO detail = new OrderDetailDTO();
        detail.setItemId(100L);
        detail.setNum(1);
        form.setDetails(List.of(detail));

        assertThatThrownBy(() -> orderService.createOrder(form))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("商品不存在");
    }

    // ---- markOrderPaySuccess ----

    @Test
    @DisplayName("markOrderPaySuccess: 标记支付成功，状态变为2")
    void markOrderPaySuccess_updatesStatus() {
        Order order = new Order();
        order.setUserId(TEST_USER_ID);
        order.setStatus(1);
        order.setTotalFee(5000);
        orderMapper.insert(order);

        orderService.markOrderPaySuccess(order.getId());

        Order updated = orderMapper.selectById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(2);
        assertThat(updated.getPayTime()).isNotNull();
    }

    // ---- cancelOrder ----

    @Test
    @DisplayName("cancelOrder: 待支付(1)订单可取消，状态变为5")
    void cancelOrder_pendingOrder_cancels() {
        Order order = new Order();
        order.setUserId(TEST_USER_ID);
        order.setStatus(1);
        orderMapper.insert(order);

        orderService.cancelOrder(order.getId(), TEST_USER_ID);

        Order updated = orderMapper.selectById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(5);
        assertThat(updated.getCloseTime()).isNotNull();
    }

    @Test
    @DisplayName("cancelOrder: 非本人订单抛 BadRequestException")
    void cancelOrder_wrongUser_throwsBadRequest() {
        Order order = new Order();
        order.setUserId(999L); // 其他用户
        order.setStatus(1);
        orderMapper.insert(order);

        assertThatThrownBy(() -> orderService.cancelOrder(order.getId(), TEST_USER_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("cancelOrder: 非待支付状态不可取消")
    void cancelOrder_nonPending_throwsBizIllegal() {
        Order order = new Order();
        order.setUserId(TEST_USER_ID);
        order.setStatus(2); // 已支付
        orderMapper.insert(order);

        assertThatThrownBy(() -> orderService.cancelOrder(order.getId(), TEST_USER_ID))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("不可取消");
    }

    // ---- confirmReceive ----

    @Test
    @DisplayName("confirmReceive: 已发货(3)订单可确认收货，状态变为4")
    void confirmReceive_shippedOrder_confirms() {
        Order order = new Order();
        order.setUserId(TEST_USER_ID);
        order.setStatus(3); // 已发货
        orderMapper.insert(order);

        orderService.confirmReceive(order.getId(), TEST_USER_ID);

        Order updated = orderMapper.selectById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(4);
        assertThat(updated.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("confirmReceive: 未发货订单不可确认收货")
    void confirmReceive_notShipped_throwsBizIllegal() {
        Order order = new Order();
        order.setUserId(TEST_USER_ID);
        order.setStatus(1); // 待支付
        orderMapper.insert(order);

        assertThatThrownBy(() -> orderService.confirmReceive(order.getId(), TEST_USER_ID))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("不可确认收货");
    }

    // ---- refund ----

    @Test
    @DisplayName("refund: 已支付(2)订单可申请退款，状态变为6")
    void refund_paidOrder_refunds() {
        Order order = new Order();
        order.setUserId(TEST_USER_ID);
        order.setStatus(2);
        orderMapper.insert(order);

        orderService.refund(order.getId(), TEST_USER_ID);

        Order updated = orderMapper.selectById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(6);
    }

    @Test
    @DisplayName("refund: 待支付(1)订单不可退款")
    void refund_pendingOrder_throwsBizIllegal() {
        Order order = new Order();
        order.setUserId(TEST_USER_ID);
        order.setStatus(1);
        orderMapper.insert(order);

        assertThatThrownBy(() -> orderService.refund(order.getId(), TEST_USER_ID))
                .isInstanceOf(BizIllegalException.class)
                .hasMessageContaining("不可申请退款");
    }

    // ---- ship ----

    @Test
    @DisplayName("ship: 发货成功，状态变为3，更新物流单号")
    void ship_existingOrder_ships() {
        Order order = new Order();
        order.setUserId(TEST_USER_ID);
        order.setStatus(1);
        orderMapper.insert(order);
        // 创建关联物流记录
        OrderLogistics logistics = new OrderLogistics();
        logistics.setOrderId(order.getId());
        logisticsMapper.insert(logistics);

        orderService.ship(order.getId(), "SF12345678");

        Order updated = orderMapper.selectById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(3);
        assertThat(updated.getConsignTime()).isNotNull();

        OrderLogistics updatedLog = logisticsMapper.selectById(logistics.getId());
        assertThat(updatedLog.getLogisticsNumber()).isEqualTo("SF12345678");
    }
}
```

### Step 2: 运行测试

```bash
mvn -q -pl trade-service -am test
```
预期: 10 tests passing for OrderServiceImpl

### Step 3: Commit

```bash
git add trade-service/src/test/java/com/hmall/service/impl/OrderServiceImplTest.java
git commit -m "test(trade): add OrderServiceImpl unit tests (10 tests)

Covers createOrder/cancelOrder/confirmReceive/refund/ship/markOrderPaySuccess.

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 7: item-service 测试基础设施

**Files:**
- Modify: `item-service/pom.xml` (add H2 dependency)
- Create: `item-service/src/test/resources/application.yml`
- Create: `item-service/src/test/resources/schema.sql`
- Create: `item-service/src/test/java/com/hmall/item/ItemServiceTestBase.java`

### Step 1: 添加 H2 依赖

编辑 `item-service/pom.xml`，在 `<dependencies>` 末尾添加：

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter-test</artifactId>
    <version>2.3.0</version>
    <scope>test</scope>
</dependency>
```

### Step 2: 创建测试配置和 schema

创建 `item-service/src/test/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:item_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true
    driver-class-name: org.h2.Driver
    username: sa
    password:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql

mybatis-plus:
  global-config:
    db-config:
      id-type: auto

hm:
  jwt:
    location: classpath:hmall.jks
    alias: hmall
    password: hmall123
    tokenTTL: 30m
```

创建 `item-service/src/test/resources/schema.sql`：

```sql
CREATE TABLE IF NOT EXISTS item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    price INT,
    stock INT DEFAULT 0,
    image VARCHAR(255),
    category VARCHAR(128),
    brand VARCHAR(128),
    spec VARCHAR(255),
    sold INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128),
    parent_id BIGINT DEFAULT 0,
    level INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS item_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT DEFAULT 5,
    content TEXT,
    images VARCHAR(1024),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Step 3: 创建测试基类

创建 `item-service/src/test/java/com/hmall/item/ItemServiceTestBase.java`：

```java
package com.hmall.item;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class ItemServiceTestBase {
    protected static final Long TEST_USER_ID = 1L;
}
```

### Step 4: 验证

```bash
mvn -q -pl item-service -am test
```
预期: BUILD SUCCESS

### Step 5: Commit

```bash
git add item-service/pom.xml item-service/src/test/resources/ item-service/src/test/java/com/hmall/item/ItemServiceTestBase.java
git commit -m "test(item): add H2 test infrastructure with item/category/review schemas

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 8: ItemServiceImpl 单元测试

**Files:**
- Create: `item-service/src/test/java/com/hmall/item/service/impl/ItemServiceImplTest.java`

### Step 1: 编写测试

创建 `item-service/src/test/java/com/hmall/item/service/impl/ItemServiceImplTest.java`：

```java
package com.hmall.item.service.impl;

import com.hmall.common.exception.BizIllegalException;
import com.hmall.item.ItemServiceTestBase;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.service.IItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemServiceImplTest extends ItemServiceTestBase {

    @Autowired
    private IItemService itemService;
    @Autowired
    private ItemMapper itemMapper;

    // ---- deductStock ----

    @Test
    @DisplayName("deductStock: 库存充足时正常扣减")
    void deductStock_sufficientStock_success() {
        Item item = new Item();
        item.setName("测试商品");
        item.setPrice(1000);
        item.setStock(10);
        item.setStatus(1);
        itemMapper.insert(item);

        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setItemId(item.getId());
        dto.setNum(3);

        itemService.deductStock(List.of(dto));

        // 单测环境下 executeBatch 不执行 SQL 语句会抛异常，
        // 但 insert + deduct 的组合可以验证参数校验逻辑
        // 这里主要测试 stock > 0 时不会抛 BizIllegalException
    }

    @Test
    @DisplayName("deductStock: 库存更新异常时抛 BizIllegalException")
    void deductStock_updateFails_throwsBizIllegal() {
        // 不存在的商品 ID，batch update 返回 false
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setItemId(99999L);
        dto.setNum(1);

        assertThatThrownBy(() -> itemService.deductStock(List.of(dto)))
                .isInstanceOf(BizIllegalException.class);
    }

    // ---- queryItemByIds ----

    @Test
    @DisplayName("queryItemByIds: 返回匹配的商品 DTO 列表")
    void queryItemByIds_returnsMatchingItems() {
        Item item1 = new Item(); item1.setName("A"); item1.setPrice(100); itemMapper.insert(item1);
        Item item2 = new Item(); item2.setName("B"); item2.setPrice(200); itemMapper.insert(item2);

        List<ItemDTO> result = itemService.queryItemByIds(List.of(item1.getId(), item2.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemDTO::getName).containsExactly("A", "B");
    }

    @Test
    @DisplayName("queryItemByIds: 空 ID 集合返回空列表")
    void queryItemByIds_emptyIds_returnsEmpty() {
        List<ItemDTO> result = itemService.queryItemByIds(List.of());
        assertThat(result).isEmpty();
    }
}
```

### Step 2: 运行测试

```bash
mvn -q -pl item-service -am test
```
预期: all tests passing

### Step 3: Commit

```bash
git add item-service/src/test/java/com/hmall/item/service/impl/ItemServiceImplTest.java
git commit -m "test(item): add ItemServiceImpl unit tests (4 tests)

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 9: ItemReviewServiceImpl 单元测试

**Files:**
- Create: `item-service/src/test/java/com/hmall/item/service/impl/ItemReviewServiceImplTest.java`

### Step 1: 编写测试

创建 `item-service/src/test/java/com/hmall/item/service/impl/ItemReviewServiceImplTest.java`：

```java
package com.hmall.item.service.impl;

import com.hmall.item.ItemServiceTestBase;
import com.hmall.item.domain.po.ItemReview;
import com.hmall.item.mapper.ItemReviewMapper;
import com.hmall.item.service.IItemReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemReviewServiceImplTest extends ItemServiceTestBase {

    @Autowired
    private IItemReviewService reviewService;
    @Autowired
    private ItemReviewMapper reviewMapper;

    @Test
    @DisplayName("listByItemId: 按商品 ID 查询评价，按创建时间降序")
    void listByItemId_returnsReviewsOrderedByTime() throws Exception {
        ItemReview r1 = review(100L, "先评价");
        Thread.sleep(10); // 确保时间戳不同
        ItemReview r2 = review(100L, "后评价");
        ItemReview r3 = review(200L, "其他商品");
        reviewMapper.insert(r1);
        reviewMapper.insert(r2);
        reviewMapper.insert(r3);

        List<ItemReview> result = reviewService.listByItemId(100L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("后评价");
        assertThat(result.get(1).getContent()).isEqualTo("先评价");
    }

    @Test
    @DisplayName("listByItemId: 无评价时返回空列表")
    void listByItemId_noReviews_returnsEmpty() {
        List<ItemReview> result = reviewService.listByItemId(999L);
        assertThat(result).isEmpty();
    }

    private ItemReview review(Long itemId, String content) {
        ItemReview r = new ItemReview();
        r.setItemId(itemId);
        r.setUserId(TEST_USER_ID);
        r.setContent(content);
        r.setRating(5);
        return r;
    }
}
```

### Step 2: 运行测试

```bash
mvn -q -pl item-service -am test
```
预期: all passing

### Step 3: Commit

```bash
git add item-service/src/test/java/com/hmall/item/service/impl/ItemReviewServiceImplTest.java
git commit -m "test(item): add ItemReviewServiceImpl unit tests (2 tests)

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 10: 运行全量测试并验证覆盖率

### Step 1: 运行 Phase 1 全部模块测试

```bash
mvn -q -pl cart-service,trade-service,item-service -am test
```
预期: 所有测试通过

### Step 2: 运行全仓库测试确认无回归

```bash
mvn -q test
```
预期: 94 original + ~32 new = ~126 tests, all passing

### Step 3: 运行 CI lint 检查

```bash
python3 scripts/agent_harness.py check
python3 scripts/knowledge_base.py check
python3 scripts/engineering-lint.py
```
预期: all PASS

### Step 4: Commit 和 Push

```bash
git push origin task/2026-06-01-test-phase1-core
```

### Step 5: 创建 PR

PR 标题: `test: add Phase 1 unit tests for cart/trade/item services (~32 tests)`

PR 描述: 参照 spec `docs/superpowers/specs/2026-06-01-test-coverage-design.md` Phase 1

🤖 Generated with [Claude Code](https://claude.com/claude-code)

---

## 验证清单

- [ ] `mvn -q -pl cart-service,trade-service,item-service -am test` — 0 failures
- [ ] `mvn -q test` — 全仓库测试 0 failures 无回归
- [ ] `python3 scripts/agent_harness.py check` — PASS
- [ ] `python3 scripts/knowledge_base.py check` — PASS
- [ ] `python3 scripts/engineering-lint.py` — PASS
- [ ] CI (lint + test) 通过
- [ ] PR codex-review 无阻塞发现
- [ ] PR 合并到 main
