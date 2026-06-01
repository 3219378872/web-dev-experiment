# Service 层分支覆盖率门限设计文档

**日期**：2026-06-01
**目标**：为后端各业务模块的 `service.impl` 包建立 **70% 分支覆盖率（JaCoCo BRANCH）** 硬门控，未达标时阻断 CI 合并。
**触发上下文**：当前 JaCoCo 仅挂在 `integration` profile 且该 profile 跳过单测，门限看不到单元测试覆盖；现有规则为 `BUNDLE/INSTRUCTION/minimum 0.10/haltOnFailure=false`，形同虚设。

---

## 1. 当前状态

- JaCoCo 0.8.10 仅定义在根 `pom.xml` 的 `integration` profile 内；该 profile 设 `skipUnitTests=true`，故覆盖率只统计 `**/*IT.java` 集成测试，**单测覆盖不被计入**。
- 现有 check 规则：`element=BUNDLE` / `INSTRUCTION` / `COVEREDRATIO minimum 0.10` / `haltOnFailure=false` —— 永不让构建失败。
- `maven-failsafe-plugin` 也只定义在 `integration` profile 内，因此**默认构建下 `mvn verify` 不跑 IT、不依赖 DB**。
- CI（`.github/workflows/ci.yml`）：`test` job 跑 `mvn -B -ntp -q test`（无 JaCoCo）；`integration` job 跑 `mvn -Pintegration verify -DskipUnitTests=true`。

### 实测当前 service.impl 分支覆盖（单测，2026-06-01）

| 模块 | service.impl 包 | 分支覆盖 | 已覆盖/总 | 对 70% |
|---|---|---|---|---|
| item-service | com.hmall.item.service.impl | 100% | 2/2 | ✅ |
| cart-service | com.hmall.cart.service.impl | 91.7% | 11/12 | ✅ |
| trade-service | com.hmall.service.impl | 76.2% | 32/42 | ✅ |
| file-service | com.hmall.file.service.impl | 75.0% | 3/4 | ✅ |
| notify-service | com.hmall.notify.service.impl | 无分支 | 0/0 | ⚠️ 豁免 |
| user-service | com.hmall.service.impl | 0% | 0/34 | ❌ |
| pay-service | com.hmall.service.impl | 0% | 0/12 | ❌ |
| hm-service | com.hmall.service.impl | 0% | 0/44 | ❌ |

> 注：user-service 99.4% 的提交（#31）已被 #32 回滚，故其 service.impl 当前 0%。
> 包命名两种约定均需覆盖：`com.hmall.<module>.service.impl`（cart/file/item/notify）与 `com.hmall.service.impl`（user/trade/pay/hm-service）。

---

## 2. 门限语义（已确认决策）

| 维度 | 取值 | 说明 |
|---|---|---|
| 范围 | `service.impl` 包 | 仅业务逻辑类，排除 controller/mapper/domain/config/utils/DTO |
| 计数器 | `BRANCH` `COVEREDRATIO` | 即"条件覆盖率"；JaCoCo 无独立 condition 计数器，BRANCH 即分支/条件覆盖 |
| 粒度 | `element=PACKAGE` | 一个模块的 service.impl 包整体算平均，类间互补，单个小类不拖垮门限 |
| 阈值 | `minimum 0.70` | 达标模块统一 70% |
| 覆盖来源 | 仅单元测试（surefire） | 不合并 IT |
| 强制力度 | `haltOnFailure=true` | 未达标 `mvn verify` 失败、阻断 PR 合并 |

---

## 3. 管道改造（根 `pom.xml`）

- 将 `jacoco-maven-plugin` 的 `prepare-agent` / `report` / `check` 三个 execution **从 `integration` profile 上移到主构建 `<build><plugins>`**，对全部子模块生效（JaCoCo 逐模块统计）。
  - `prepare-agent` → `initialize` 阶段（为 surefire 注入 argLine）。
  - `report` + `check` → `verify` 阶段。
- 默认构建下 failsafe 不存在（只在 integration profile），故 `mvn verify` 仅执行：compile → surefire 单测 → jacoco report → jacoco check，**无需 DB**。
- 移除 `integration` profile 内重复的旧 JaCoCo 配置，避免双份；该 profile 的 `skipUnitTests=true` / IT 路径不受影响。

### CI 改造（`.github/workflows/ci.yml`）

- `test` job：`mvn -B -ntp -q test` → **`mvn -B -ntp -q verify`**，使单测 + 门限在同一 job 内执行并门控。
- `integration` / `smoke` / `lint` / `codex-review` job 不变。

---

## 4. 父 POM 默认规则（达标模块即刻 70%）

```
jacoco:check
  rule:
    element = PACKAGE
    includes = [ */service/impl ]      # 精确语法在实现期以 RED 测试校准
    excludes = [ */com/hmall/notify/service/impl ]   # notify 无分支，豁免
    limit:
      counter = BRANCH
      value   = COVEREDRATIO
      minimum = 0.70
  haltOnFailure = true
```

- cart / file / item / trade：当前 75–100%，立即生效 70% 硬门控并通过。
- hm-common / hm-api / hm-gateway：无 service.impl 包，规则不命中、自然通过。
- notify-service：service.impl 无分支（0/0），直接 `exclude`（门限对 0 分支无意义）。

---

## 5. 棘轮覆盖（落后模块在各自 `pom.xml` override）

落后模块在自身 `pom.xml` 内用 `combine.self="override"` 覆盖父规则的 limit 为当前基线，使门限即刻全量生效且 **CI 全绿**：

| 模块 | 当前基线 | 本次锁定 minimum | 注释 |
|---|---|---|---|
| user-service | 0% (0/34) | 0.00 | `<!-- TODO(coverage-ratchet): raise to 0.70 -->` |
| pay-service | 0% (0/12) | 0.00 | `<!-- TODO(coverage-ratchet): raise to 0.70 -->` |
| hm-service | 0% (0/44) | 0.00 | `<!-- TODO(coverage-ratchet): raise to 0.70 -->` |

棘轮约定：每补一批单测就上调该模块 minimum，达到 0.70 后删除 override 继承父规则。本次仅落门限配置，**不写业务测试**（0.00 为占位基线）。

---

## 6. 验证策略（TDD：先证明门限会拦）

1. **RED**：父规则设 70%、暂不加 override，跑 `mvn -B -ntp -q verify`，确认 user/pay/hm-service 如期失败、cart/file/item/trade/notify 通过 —— 证明门限真的在拦且 includes/excludes 语法命中正确的包。
2. **GREEN**：加上三个落后模块的 0.00 override，`mvn verify` 全绿。
3. **反向校验**：临时把 cart 阈值设 0.95 应失败、恢复 0.70 应通过，确认门限非空转。
4. notify-service 若未被 0 分支自动豁免而失败，则确认 `exclude` 已生效。

---

## 7. Harness / 文档 / 交付

- 改 pom + CI 属实质变更：在 `docs/agent-harness/tasks/active/` 建任务记录（context / verification / audit / handoff + `task.yaml` status: active）。
- 分支：`task/2026-06-01-service-branch-coverage-gate` → 本地验收 → 推远程 → PR → 过 CI/review → 合并删分支。
- 交付前三件套：`python3 scripts/agent_harness.py check`、`python3 scripts/knowledge_base.py check`、`python3 scripts/engineering-lint.py`。
- KB：相关测试/构建页 bump `last_synced_commit` 并在 `sync_note` 说明（K005 轻量豁免），或更新对应 `modules/*` / `flows/*` 页。
- `pom.xml` 改动需保持 `AGENTS.md ↔ CLAUDE.md` 字节镜像不受影响（本次不改这两文件）。

---

## 8. 范围边界（YAGNI）

- 不引入跨模块聚合报告模块（`report-aggregate`）。
- 不合并单测 + IT 的 exec。
- 不为落后模块补业务测试（留作后续棘轮工作）。
- 不改动既有 endpoint 形态、鉴权、Seata、Gateway 路由。
