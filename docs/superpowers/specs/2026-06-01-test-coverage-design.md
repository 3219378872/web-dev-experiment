# 测试覆盖率补足设计文档

**日期**：2026-06-01
**目标**：为 6 个零测试 Maven 模块补齐测试，使每个模块各自达到 80% 行覆盖率。
**触发上下文**：上线前检查发现 6/12 模块零测试，不符合用户规则要求的 80% 最低覆盖率。

---

## 1. 当前状态

### 已有测试的模块（94 tests, 0 failures）

| 模块 | 测试文件数 | 测试数 | 覆盖类型 |
|------|-----------|--------|---------|
| hm-common | 8 | 57 | DTO、工具类、异常、拦截器 |
| hm-gateway | 3 | 21 | AuthGlobalFilter(Unit) + JwtTool(Unit) + GatewayAuth(Integration) |
| hm-service | 1 | 2 | HutoolTest |
| pay-service | 2 | 7 | 枚举测试 |
| user-service | 2 | 7 | 枚举 + JwtTool |

### 零测试模块

| 模块 | Java 文件数 | 关键类 |
|------|-----------|--------|
| trade-service | 24 | OrderController, CouponController, 4 ServiceImpl |
| item-service | 22 | ItemController, CategoryController, SearchController, ReviewController, 3 ServiceImpl |
| cart-service | 11 | CartController, CartServiceImpl |
| notify-service | 17 | 通知消息、模板相关 |
| file-service | 6 | MinIO 文件上传/下载 |
| hm-api | 22 | Feign 客户端接口 + Fallback |
| **合计** | **~102** | |

---

## 2. 总体策略

**原则：**
- 每个模块独立达标 80% 行覆盖率
- 三阶段分步推进，每阶段一个独立 PR
- Unit 测试为主（Mockito），关键路径加集成测试（Testcontainers）
- 遵循现有测试规范：JUnit 5 + Mockito + AssertJ + `@DisplayName`

**不做的：**
- E2E 测试（`e2e/` 目录已有 Playwright，后续单独任务）
- 纯 getter/setter 的覆盖（lombok 生成代码不计入有意义覆盖）
- DTO/Entity 类的结构测试（无业务逻辑，跳过）

---

## 3. Phase 1 — 核心交易链路（trade + cart + item）

**优先级：最高** — 涉及金额、库存、订单状态机。

### 3.1 cart-service（11 Java 文件）

| 被测类 | 测试类型 | 关键场景 |
|--------|---------|---------|
| `CartServiceImpl` | Unit (H2) | 添加商品到购物车、更新数量、删除购物车项、查询我的购物车 |

预估：~7 个测试方法（@WebMvcTest 与此项目 MyBatis-Plus 不兼容，Controller 测试移除）

### 3.2 trade-service（24 Java 文件）

| 被测类 | 测试类型 | 关键场景 |
|--------|---------|---------|
| `OrderServiceImpl` | Unit (H2) | 创建订单、取消/确认/退款/发货 + 状态机校验 |
| `CouponServiceImpl` | Unit (H2) | 领取优惠券、重复领取防护、可用券查询 |

预估：~18 个单元测试（OrderDetail/OrderLogistics 为 ServiceImpl 空壳，无需独立测试）

### 3.3 item-service（22 Java 文件）

| 被测类 | 测试类型 | 关键场景 |
|--------|---------|---------|
| `ItemServiceImpl` | Unit (H2) | 库存扣减、批量商品查询 |
| `ItemReviewServiceImpl` | Unit (H2) | 按商品 ID 查询评价、时间排序 |

预估：~5 个单元测试（CategoryServiceImpl 为 ServiceImpl 空壳）

### Phase 1 验证门控

```bash
mvn -q -pl cart-service,trade-service,item-service -am test  # 全部通过
python3 scripts/agent_harness.py check                          # PASS
python3 scripts/knowledge_base.py check                         # PASS
python3 scripts/engineering-lint.py                             # PASS
```

---

## 4. Phase 2 — 支撑服务（notify + file）

### 4.1 notify-service（17 Java 文件）

| 被测类 | 测试类型 | 关键场景 |
|--------|---------|---------|
| NotificationServiceImpl | Unit (H2) | getActiveNotifications 状态过滤、排序 |
| FeedbackServiceImpl | Unit (H2) | 基础 CRUD（save/getById） |
| CustomerMessageServiceImpl | Unit (H2) | 基础 CRUD（save/getById） |

预估：~6 个单元测试（Feedback/CustomerMessage 为 ServiceImpl 空壳）

### 4.2 file-service（6 Java 文件）

| 被测类 | 测试类型 | 关键场景 |
|--------|---------|---------|
| UploadServiceImpl | Unit (MockMultipartFile) | uploadImage（正常/空文件）、getFile |

预估：~4 个单元测试

### Phase 2 验证门控

```bash
mvn -q -pl notify-service,file-service -am test   # 全部通过
# 加上 Phase 1 的所有门控检查
```

---

## 5. Phase 3 — API 契约层（hm-api）

### 5.1 hm-api（22 Java 文件）

本模块主要是 Feign 接口定义 + DTO + 配置。**当前代码无 Fallback 实现**，
可测试逻辑仅 `DefaultFeignConfig.userInfoRequestInterceptor()`（RequestInterceptor）。

| 被测类 | 测试类型 | 关键场景 |
|--------|---------|---------|
| DefaultFeignConfig | Unit (Mockito) | RequestInterceptor 添加 user-info 请求头 |

预估：~3 个单元测试

### Phase 3 验证门控

```bash
mvn -q -pl hm-api -am test                           # 全部通过
# 加上 Phase 1+2 的所有门控检查
```

---

## 6. 测试编写规范

遵循仓库现有模式（参照 `BeanUtilsTest`、`AuthGlobalFilterTest`）：

```java
// ✅ 框架
@ExtendWith(MockitoExtension.class)    // Mockito
import static org.assertj.core.api.Assertions.assertThat;  // AssertJ

// ✅ 命名与注解
@Test
@DisplayName("methodName: 场景描述 —— 预期行为")

// ✅ 结构：Arrange → Act → Assert

// ✅ 覆盖：成功路径 + 边界条件(null/空/极值) + 异常路径
```

**禁止模式：**
- 不使用 PowerMock / 不 mock 静态方法（不利于维护）
- 不在测试中使用真实的 Nacos/Redis/RabbitMQ 连接（用 Mockito mock 或 Testcontainers）
- 不写 `assertTrue(true)` 之类的假测试

---

## 7. 里程碑与交付

| 阶段 | 模块 | 预估测试数 | PR |
|------|------|-----------|-----|
| Phase 1 | trade + cart + item | ~30 UT | `task/2026-06-01-test-phase1-core` |
| Phase 2 | notify + file | ~10 UT | `task/2026-06-01-test-phase2-support` |
| Phase 3 | hm-api | ~3 UT | `task/2026-06-01-test-phase3-api` |
| **合计** | 6 模块 | **~43 UT** | 3 PRs |

每阶段 PR 合并后需通过 CI 全流程（lint → test → integration → smoke → codex-review）。

---

## 8. 风险与假设

| 风险 | 缓解措施 |
|------|---------|
| 部分 Service 依赖过多难以 Mock | 优先测试纯逻辑方法，对重依赖方法标记 TODO 待重构后补测 |
| Testcontainers 在 CI 中启动慢 | 仅关键路径用集成测试，大部分走纯 Mock |
| item-service Elasticsearch 依赖 | Mock Elasticsearch client，不在测试中启动真实 ES |
| 80% 行覆盖率在 Feign Fallback 模块难以达成 | hm-api 的覆盖率计算排除纯接口定义，只统计 Fallback 实现类 |
