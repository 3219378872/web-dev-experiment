# Phase 2: item-service Admin Endpoints Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 item-service 补齐管理端评价、商品批量操作、轮播图/广告位/秒杀功能，实现 B2 评价/批量/相关推荐 + B4 轮播图/广告位/秒杀全部端点。

**Architecture:** 在 item-service 中新增 BannerController、SeckillController，扩展 ReviewController、ItemController。新增 Banner、Seckill 表和对应的 PO、Mapper、Service。新增 ReviewVO、ItemVO、BannerVO、SeckillVO 脱敏类。遵循 R<Void>（写）+ *VO/PageDTO（读）约定。

**Tech Stack:** Spring Boot 2.7.12, Spring Cloud Alibaba 2021.0.4.0, MyBatis-Plus, MySQL 8, JUnit 5, Mockito, Testcontainers (MySQL + Redis)。

---

## 关键现状与对 spec 的偏差（实现前必读）

1. **现有评价端点**：`ReviewController` 已有 `GET /items/{itemId}/reviews`（按商品ID查询）和 `DELETE /admin/reviews/{id}`（单条删除）。需新增 `GET /admin/reviews`（管理端列表/搜索/分页）。
2. **现有商品端点**：`ItemController` 已有 `PUT /admin/items/{id}/status`（单条状态变更）和 `DELETE /admin/items/{id}`（单条删除）。需新增批量接口。
3. **无 Banner/Seckill 代码**：需从零创建 Banner、Seckill 相关代码（PO、Mapper、Service、Controller）。
4. **无 VO 类**：item-service 使用 DTO 模式（`ItemDTO`）和 PO 模式（`Item`、`ItemReview`、`Category`）。需新增 VO 类用于脱敏。
5. **SQL 迁移脚本**：需在 `docs/sql/` 下新增 banner、seckill 表的迁移脚本。

## File Structure

- Create `docs/sql/item-service-banner-seckill.sql` — banner、seckill 表 DDL
- Create `item-service/src/main/java/com/hmall/item/domain/po/Banner.java` — Banner PO
- Create `item-service/src/main/java/com/hmall/item/domain/po/Seckill.java` — Seckill PO
- Create `item-service/src/main/java/com/hmall/item/domain/vo/ReviewVO.java` — 评价脱敏 VO
- Create `item-service/src/main/java/com/hmall/item/domain/vo/ItemVO.java` — 商品脱敏 VO
- Create `item-service/src/main/java/com/hmall/item/domain/vo/BannerVO.java` — 轮播图 VO
- Create `item-service/src/main/java/com/hmall/item/domain/vo/SeckillVO.java` — 秒杀 VO
- Create `item-service/src/main/java/com/hmall/item/mapper/BannerMapper.java` — Banner Mapper
- Create `item-service/src/main/java/com/hmall/item/mapper/SeckillMapper.java` — Seckill Mapper
- Create `item-service/src/main/java/com/hmall/item/service/IBannerService.java` — Banner Service 接口
- Create `item-service/src/main/java/com/hmall/item/service/ISeckillService.java` — Seckill Service 接口
- Create `item-service/src/main/java/com/hmall/item/service/impl/BannerServiceImpl.java` — Banner Service 实现
- Create `item-service/src/main/java/com/hmall/item/service/impl/SeckillServiceImpl.java` — Seckill Service 实现
- Create `item-service/src/main/java/com/hmall/item/controller/BannerController.java` — Banner Controller
- Create `item-service/src/main/java/com/hmall/item/controller/SeckillController.java` — Seckill Controller
- Modify `item-service/src/main/java/com/hmall/item/controller/ReviewController.java` — 扩展管理端评价列表
- Modify `item-service/src/main/java/com/hmall/item/controller/ItemController.java` — 扩展批量操作、相关推荐
- Create `item-service/src/test/java/com/hmall/item/controller/BannerControllerTest.java` — Banner Controller 测试
- Create `item-service/src/test/java/com/hmall/item/controller/SeckillControllerTest.java` — Seckill Controller 测试
- Create `item-service/src/test/java/com/hmall/item/service/impl/BannerServiceImplTest.java` — Banner Service 测试
- Create `item-service/src/test/java/com/hmall/item/service/impl/SeckillServiceImplTest.java` — Seckill Service 测试
- Modify `docs/knowledge-base/modules/item-service.md` — 更新 KB 页面

---

## Tasks

### Task 0: 建分支与 harness 任务记录（Safety 硬约束）

**Files:**
- Create: `docs/agent-harness/tasks/active/2026-06-04-phase2-item-service/{task.yaml,context.md,verification.md,audit.md,handoff.md}`

- [x] **Step 1:** 从 `main` 切分支。

```bash
git switch -c task/2026-06-04-phase2-item-service origin/main
```

- [x] **Step 2:** 填 `task.yaml`：`status: active`、`spec`/`plan` 指向本文件、`task_branch: task/2026-06-04-phase2-item-service`。填 context/verification/audit/handoff 叙述。

- [ ] **Step 3:** `python3 scripts/agent_harness.py check` → Exit 0。

- [ ] **Step 4: Commit** `chore(harness): phase2 item-service task record`。

### Task 1: SQL 迁移脚本

**Files:**
- Create: `docs/sql/item-service-banner-seckill.sql`

- [ ] **Step 1:** 创建 banner 表 DDL：

```sql
-- banner 表（轮播图 + 广告位）
CREATE TABLE IF NOT EXISTS `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `link_url` VARCHAR(500) DEFAULT NULL COMMENT '链接URL',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
  `type` VARCHAR(50) NOT NULL DEFAULT 'carousel' COMMENT '类型：carousel=轮播图，ad=广告位',
  `position` VARCHAR(100) DEFAULT NULL COMMENT '广告位槽位标识',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type_position` (`type`, `position`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图/广告位表';
```

- [ ] **Step 2:** 创建 seckill 表 DDL：

```sql
-- seckill 表（限时秒杀）
CREATE TABLE IF NOT EXISTS `seckill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` BIGINT NOT NULL COMMENT '商品ID',
  `seckill_price` INT NOT NULL COMMENT '秒杀价（分）',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `stock` INT NOT NULL COMMENT '库存数量',
  `sold` INT NOT NULL DEFAULT 0 COMMENT '已售数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限时秒杀表';
```

- [ ] **Step 3: Commit** `feat(item-service): add banner and seckill table DDL`。

### Task 2: PO 类

**Files:**
- Create: `item-service/src/main/java/com/hmall/item/domain/po/Banner.java`
- Create: `item-service/src/main/java/com/hmall/item/domain/po/Seckill.java`

- [ ] **Step 1:** 创建 Banner PO 类。

- [ ] **Step 2:** 创建 Seckill PO 类。

- [ ] **Step 3: Commit** `feat(item-service): add Banner and Seckill PO classes`。

### Task 3: Mapper 接口

**Files:**
- Create: `item-service/src/main/java/com/hmall/item/mapper/BannerMapper.java`
- Create: `item-service/src/main/java/com/hmall/item/mapper/SeckillMapper.java`

- [ ] **Step 1:** 创建 BannerMapper 接口（继承 BaseMapper<Banner>）。

- [ ] **Step 2:** 创建 SeckillMapper 接口（继承 BaseMapper<Seckill>）。

- [ ] **Step 3: Commit** `feat(item-service): add BannerMapper and SeckillMapper`。

### Task 4: VO 类

**Files:**
- Create: `item-service/src/main/java/com/hmall/item/domain/vo/ReviewVO.java`
- Create: `item-service/src/main/java/com/hmall/item/domain/vo/ItemVO.java`
- Create: `item-service/src/main/java/com/hmall/item/domain/vo/BannerVO.java`
- Create: `item-service/src/main/java/com/hmall/item/domain/vo/SeckillVO.java`

- [ ] **Step 1:** 创建 ReviewVO 类（脱敏评价信息）。

- [ ] **Step 2:** 创建 ItemVO 类（脱敏商品信息）。

- [ ] **Step 3:** 创建 BannerVO 类。

- [ ] **Step 4:** 创建 SeckillVO 类（含 endTime、库存百分比）。

- [ ] **Step 5: Commit** `feat(item-service): add ReviewVO, ItemVO, BannerVO, SeckillVO`。

### Task 5: Service 接口和实现

**Files:**
- Create: `item-service/src/main/java/com/hmall/item/service/IBannerService.java`
- Create: `item-service/src/main/java/com/hmall/item/service/ISeckillService.java`
- Create: `item-service/src/main/java/com/hmall/item/service/impl/BannerServiceImpl.java`
- Create: `item-service/src/main/java/com/hmall/item/service/impl/SeckillServiceImpl.java`

- [ ] **Step 1:** 创建 IBannerService 接口（CRUD + 分页 + 前台查询）。

- [ ] **Step 2:** 创建 ISeckillService 接口（查询进行中的秒杀活动）。

- [ ] **Step 3:** 创建 BannerServiceImpl 实现。

- [ ] **Step 4:** 创建 SeckillServiceImpl 实现。

- [ ] **Step 5: Commit** `feat(item-service): add Banner and Seckill service layer`。

### Task 6: Controller 实现

**Files:**
- Create: `item-service/src/main/java/com/hmall/item/controller/BannerController.java`
- Create: `item-service/src/main/java/com/hmall/item/controller/SeckillController.java`
- Modify: `item-service/src/main/java/com/hmall/item/controller/ReviewController.java`
- Modify: `item-service/src/main/java/com/hmall/item/controller/ItemController.java`

- [ ] **Step 1:** 创建 BannerController（前台 + 管理端端点）。

- [ ] **Step 2:** 创建 SeckillController（查询进行中的秒杀活动）。

- [ ] **Step 3:** 扩展 ReviewController，新增 `GET /admin/reviews` 端点。

- [ ] **Step 4:** 扩展 ItemController，新增批量操作和相关推荐端点。

- [ ] **Step 5: Commit** `feat(item-service): add BannerController, SeckillController, extend ReviewController, ItemController`。

### Task 7: 单元测试

**Files:**
- Create: `item-service/src/test/java/com/hmall/item/controller/BannerControllerTest.java`
- Create: `item-service/src/test/java/com/hmall/item/controller/SeckillControllerTest.java`
- Create: `item-service/src/test/java/com/hmall/item/service/impl/BannerServiceImplTest.java`
- Create: `item-service/src/test/java/com/hmall/item/service/impl/SeckillServiceImplTest.java`

- [ ] **Step 1:** 创建 BannerControllerTest 测试类。

- [ ] **Step 2:** 创建 SeckillControllerTest 测试类。

- [ ] **Step 3:** 创建 BannerServiceImplTest 测试类。

- [ ] **Step 4:** 创建 SeckillServiceImplTest 测试类。

- [ ] **Step 5:** 运行测试验证覆盖率。

- [ ] **Step 6: Commit** `test(item-service): add unit tests for Banner and Seckill`。

### Task 8: Knowledge Base 更新

**Files:**
- Modify: `docs/knowledge-base/modules/item-service.md`

- [ ] **Step 1:** 更新 `item-service.md` 页面，添加新增端点的契约说明。

- [ ] **Step 2:** 更新 `last_synced_commit` 到当前 HEAD。

- [ ] **Step 3:** 运行 `python3 scripts/knowledge_base.py check` → Exit 0。

- [ ] **Step 4: Commit** `docs(kb): update item-service module page with admin endpoints`。

### Task 9: 工程化检查与收尾

**Files:**
- Modify: `docs/agent-harness/tasks/active/2026-06-04-phase2-item-service/audit.md`

- [ ] **Step 1:** 运行 `python3 scripts/agent_harness.py check` → Exit 0。

- [ ] **Step 2:** 运行 `python3 scripts/knowledge_base.py check` → Exit 0。

- [ ] **Step 3:** 运行 `python3 scripts/engineering-lint.py` → Exit 0。

- [ ] **Step 4:** 更新 `audit.md`，标记所有审计项为 ✅。

- [ ] **Step 5:** 更新 `handoff.md`，标记状态为"完成"。

- [ ] **Step 6: Commit** `chore(harness): complete phase2 item-service task`。

---

## Testing Strategy

### Unit Tests
- **BannerController**: 测试轮播图 CRUD、排序、前台/管理端查询
- **SeckillController**: 测试秒杀活动查询
- **ReviewController**: 测试管理端评价列表查询、分页、过滤
- **ItemController**: 测试批量上下架、批量删除、相关推荐
- **Service 层**: 测试业务逻辑（分页查询、状态修改、关联查询）

### Integration Tests
- **端点契约验证**: 使用 Spring Boot Test + Testcontainers 验证端点行为
- **数据库操作**: 验证新增表的 CRUD 操作
- **权限验证**: 验证 `/admin/*` 端点的 Gateway 鉴权集成

## Acceptance Criteria

- [ ] 所有 8 个端点实现并可访问
- [ ] 所有端点遵循 R<Void>（写）+ *VO/PageDTO（读）约定
- [ ] 新增 VO 类（ReviewVO、ItemVO、BannerVO、SeckillVO）
- [ ] 新增 PO 类（Banner、Seckill）
- [ ] 新增 Mapper、Service、ServiceImpl
- [ ] 新增 SQL 迁移脚本（banner、seckill 表）
- [ ] 单元测试通过，覆盖率 ≥ 80%
- [ ] 集成测试通过（如果启用）
- [ ] 不破坏现有端点
- [ ] Gateway `/admin/**` 鉴权正常工作
- [ ] Knowledge base 同步完成
- [ ] Harness 任务文档完整