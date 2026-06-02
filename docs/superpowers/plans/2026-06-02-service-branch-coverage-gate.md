# JaCoCo Service 层分支覆盖率门限实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为后端各业务模块的 `service.impl` 包建立 70% 分支覆盖率硬门控（JaCoCo BRANCH），未达标阻断 CI。

**Architecture:** 将 JaCoCo 从 `integration` profile 上移到根 `pom.xml` 主构建，用 PACKAGE 级 BRANCH COVEREDRATIO 规则校验所有 `**/service/impl` 包；落后模块（user/pay/hm-service）在各自 pom.xml 用 `combine.self="override"` 锁定 0.00 基线（棘轮）。

**Tech Stack:** Maven 3.x, JaCoCo 0.8.10, maven-surefire-plugin, GitHub Actions

---

## 文件变更清单

| 操作 | 文件 | 说明 |
|------|------|------|
| 修改 | `pom.xml` | JaCoCo 从 integration profile 移至主构建 + 默认 70% 规则 |
| 修改 | `user-service/pom.xml` | 0.00 棘轮 override |
| 修改 | `pay-service/pom.xml` | 0.00 棘轮 override |
| 修改 | `hm-service/pom.xml` | 0.00 棘轮 override |
| 修改 | `.github/workflows/ci.yml` | test job: `mvn test` → `mvn verify` |
| 新建 | `docs/agent-harness/tasks/active/<slug>/context.md` | Harness 任务记录 |
| 新建 | `docs/agent-harness/tasks/active/<slug>/verification.md` | 验证记录 |
| 新建 | `docs/agent-harness/tasks/active/<slug>/audit.md` | 审计记录 |
| 新建 | `docs/agent-harness/tasks/active/<slug>/handoff.md` | 交付记录 |
| 新建 | `docs/agent-harness/tasks/active/<slug>/task.yaml` | 任务元数据 |

---

### Task 1: JaCoCo 从 integration profile 上移至主构建（根 pom.xml）

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: 在主构建 `<build><plugins>` 中添加 JaCoCo**

在 `pom.xml` 的 `<build><pluginManagement>` 后面、`</build>` 之前插入 `<plugins>` 块：

```xml
<build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
        </plugins>
    </pluginManagement>
    <!-- ↓ 新增以下 plugins 块 ↓ -->
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.10</version>
            <executions>
                <execution>
                    <id>prepare-agent</id>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>check</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <haltOnFailure>true</haltOnFailure>
                        <rules>
                            <rule>
                                <element>PACKAGE</element>
                                <includes>
                                    <include>**/service/impl</include>
                                </includes>
                                <excludes>
                                    <exclude>**/notify/service/impl</exclude>
                                </excludes>
                                <limits>
                                    <limit>
                                        <counter>BRANCH</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.70</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

- [ ] **Step 2: 从 integration profile 中移除 JaCoCo 配置**

删除 `<profiles><profile><id>integration</id><build><plugins>` 中整个 `<plugin><groupId>org.jacoco</groupId>` 块（第 165–206 行）。

- [ ] **Step 3: 在 integration profile 中添加 check 跳过属性**

在 `<profile><id>integration</id><properties>` 中添加属性，使 integration job 不触发 check：

```xml
<profile>
    <id>integration</id>
    <properties>
        <skipUnitTests>true</skipUnitTests>
        <!-- ↓ 新增 ↓ -->
        <jacoco.check.skip>true</jacoco.check.skip>
    </properties>
    ...
</profile>
```

同时在主构建的 `check` execution `<configuration>` 中添加 `<skip>${jacoco.check.skip}</skip>`：

```xml
<configuration>
    <haltOnFailure>true</haltOnFailure>
    <skip>${jacoco.check.skip}</skip>
    <rules>
        ...
    </rules>
</configuration>
```

**完整修改后的 `pom.xml` 关键区域：**

主构建（`<build>` 末尾，`</plugins>` 之后）：

```xml
    </pluginManagement>
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.10</version>
            <executions>
                <execution>
                    <id>prepare-agent</id>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>check</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <haltOnFailure>true</haltOnFailure>
                        <skip>${jacoco.check.skip}</skip>
                        <rules>
                            <rule>
                                <element>PACKAGE</element>
                                <includes>
                                    <include>**/service/impl</include>
                                </includes>
                                <excludes>
                                    <exclude>**/notify/service/impl</exclude>
                                </excludes>
                                <limits>
                                    <limit>
                                        <counter>BRANCH</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.70</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Integration profile（移除 JaCoCo，添加 skip 属性）：

```xml
<profile>
    <id>integration</id>
    <properties>
        <skipUnitTests>true</skipUnitTests>
        <jacoco.check.skip>true</jacoco.check.skip>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <skip>${skipUnitTests}</skip>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                        <configuration>
                            <includes>
                                <include>**/*IT.java</include>
                            </includes>
                            <forkCount>1</forkCount>
                            <reuseForks>true</reuseForks>
                            <systemPropertyVariables>
                                <spring.cloud.bootstrap.enabled>false</spring.cloud.bootstrap.enabled>
                                <spring.cloud.nacos.config.enabled>false</spring.cloud.nacos.config.enabled>
                            </systemPropertyVariables>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <!-- 旧 JaCoCo 插件已删除 -->
        </plugins>
    </build>
</profile>
```

- [ ] **Step 4: Commit Task 1**

```bash
git add pom.xml
git commit -m "feat: move JaCoCo to main build with service.impl 70% branch coverage gate

- Add jacoco-maven-plugin 0.8.10 to main <build><plugins>
- prepare-agent (initialize), report + check (verify phase)
- PACKAGE-level check: **/service/impl includes, **/notify/service/impl excludes
- BRANCH COVEREDRATIO minimum 0.70, haltOnFailure=true
- Remove old JaCoCo from integration profile
- Add jacoco.check.skip=true to integration profile to avoid IT-triggered gate"
```

---

### Task 2: RED — 验证门限正确拦截落后模块

**Files:**
- 无需修改任何文件

- [ ] **Step 1: 运行 `mvn verify` 验证门限拦截**

```bash
mvn -B -ntp -q verify
```

**预期结果：**
- ❌ user-service: BRANCH COVEREDRATIO 0.00 < 0.70 → BUILD FAILURE
- ❌ pay-service: BRANCH COVEREDRATIO 0.00 < 0.70 → BUILD FAILURE
- ❌ hm-service: BRANCH COVEREDRATIO 0.00 < 0.70 → BUILD FAILURE
- ✅ cart-service: 已达标（~91.7%）→ PASS
- ✅ file-service: 已达标（~75%）→ PASS
- ✅ item-service: 已达标（100%）→ PASS
- ✅ trade-service: 已达标（~76.2%）→ PASS
- ✅ notify-service: 被 exclude 豁免 → PASS
- ✅ hm-common / hm-api / hm-gateway: 无 service.impl 包，规则不命中 → PASS

- [ ] **Step 2: 确认 haltonfailure 生效（构建确实失败）**

```bash
mvn -B -ntp -q verify; echo "EXIT: $?"
```

**预期输出：** `EXIT: 1`（命令以非零状态退出）

- [ ] **Step 3: 验证 include 模式命中正确包**

检查 JaCoCo CSV/XML 报告确认 `**/service/impl` 命中了两种包名约定：

```bash
# 查看哪些包被 JaCoCo PACKAGE 规则检查
find . -name "jacoco.csv" -path "*/target/*" | while read f; do
  echo "=== $f ==="
  grep "service/impl" "$f"
done
```

**预期：** 两种包命名约定都被覆盖（`com/hmall/service/impl` 和 `com/hmall/*/service/impl`），notify 不在结果中。

---

### Task 3: GREEN — 添加落后模块 0.00 棘轮 override

**Files:**
- Modify: `user-service/pom.xml`
- Modify: `pay-service/pom.xml`
- Modify: `hm-service/pom.xml`

- [ ] **Step 1: 在 user-service/pom.xml 添加 override**

在 `<build><plugins>` 块内（`spring-boot-maven-plugin` 之后）添加：

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>check</id>
            <configuration combine.self="override">
                <haltOnFailure>true</haltOnFailure>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <includes>
                            <include>**/service/impl</include>
                        </includes>
                        <limits>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.00</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
<!-- TODO(coverage-ratchet): raise to 0.70 -->
```

- [ ] **Step 2: 在 pay-service/pom.xml 添加相同 override**

在 `<build><plugins>` 块内（`spring-boot-maven-plugin` 之后）添加相同配置，连同 TODO 注释。

- [ ] **Step 3: 在 hm-service/pom.xml 添加相同 override**

在 `<build><plugins>` 块内（`spring-boot-maven-plugin` 之后）添加相同配置，连同 TODO 注释。

- [ ] **Step 4: 运行 `mvn verify` 验证全绿**

```bash
mvn -B -ntp -q verify
```

**预期结果：** BUILD SUCCESS（所有模块通过）

- [ ] **Step 5: Commit Task 2 + 3**

```bash
git add user-service/pom.xml pay-service/pom.xml hm-service/pom.xml
git commit -m "feat: add 0.00 coverage ratchet override for user/pay/hm-service

user-service, pay-service, and hm-service currently have 0% service.impl
branch coverage. Ratchet override sets minimum to 0.00 so CI stays green
while coverage is being supplemented.

TODO(coverage-ratchet): raise to 0.70 as tests are added."
```

---

### Task 4: 反向校验 —— 确认门限非空转

**Files:**
- 无需永久修改

- [ ] **Step 1: 临时提高 cart-service 阈值验证门限生效**

编辑 `cart-service/pom.xml`（临时，测试后恢复），添加 override 设 minimum 0.95：

```xml
<!-- TEMPORARY: remove after test -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>check</id>
            <configuration combine.self="override">
                <haltOnFailure>true</haltOnFailure>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <includes>
                            <include>**/service/impl</include>
                        </includes>
                        <limits>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.95</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

- [ ] **Step 2: 运行 `mvn verify` 验证 cart-service 失败**

```bash
mvn -B -ntp -q verify -pl cart-service -am
```

**预期结果：** cart-service BRANCH COVEREDRATIO ~0.91 < 0.95 → BUILD FAILURE

- [ ] **Step 3: 恢复并验证 cart-service 重新通过**

撤消 cart-service/pom.xml 的临时修改（`git checkout cart-service/pom.xml`）：

```bash
git checkout cart-service/pom.xml
mvn -B -ntp -q verify -pl cart-service -am
```

**预期结果：** BUILD SUCCESS

- [ ] **Step 4: 验证 notify-service 被正确 exclude**

检查 notify-service 的 check 输出，确认没有 "0/0 branches" 导致的门限失败：

```bash
mvn -B -ntp -q verify -pl notify-service -am 2>&1 | grep -i "notify\|rule\|check\|coverage\|branch"
```

**预期结果：** 无针对 notify 的 BRANCH 覆盖率违反。

---

### Task 5: 更新 CI Workflow

**Files:**
- Modify: `.github/workflows/ci.yml`

- [ ] **Step 1: 将 test job 命令从 `mvn test` 改为 `mvn verify`**

在 `.github/workflows/ci.yml` 第 48 行：

```yaml
# 修改前
- name: Backend unit tests
  run: mvn -B -ntp -q test

# 修改后
- name: Backend unit tests + coverage gate
  run: mvn -B -ntp -q verify
```

- [ ] **Step 2: 验证 CI 配置语法**

```bash
# 本地验证 YAML 可解析
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/ci.yml'))" && echo "YAML OK"
```

- [ ] **Step 3: Commit Task 5**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: run 'mvn verify' in test job to enforce coverage gate

Previously ran 'mvn test' which did not execute JaCoCo check.
'mvn verify' runs surefire + jacoco report + jacoco check in one step."
```

---

### Task 6: Harness 任务记录 + KB 更新 + 交付检查

**Files:**
- Create: `docs/agent-harness/tasks/active/2026-06-01-service-branch-coverage-gate/context.md`
- Create: `docs/agent-harness/tasks/active/2026-06-01-service-branch-coverage-gate/verification.md`
- Create: `docs/agent-harness/tasks/active/2026-06-01-service-branch-coverage-gate/audit.md`
- Create: `docs/agent-harness/tasks/active/2026-06-01-service-branch-coverage-gate/handoff.md`
- Create: `docs/agent-harness/tasks/active/2026-06-01-service-branch-coverage-gate/task.yaml`

- [ ] **Step 1: 创建 harness 任务目录**

```bash
mkdir -p docs/agent-harness/tasks/active/2026-06-01-service-branch-coverage-gate
```

- [ ] **Step 2: 写 context.md**

```markdown
# Context: JaCoCo Service Layer Branch Coverage Gate

**Task ID:** 2026-06-01-service-branch-coverage-gate
**Status:** active
**Spec:** docs/superpowers/specs/2026-06-01-service-branch-coverage-gate-design.md
**Plan:** docs/superpowers/plans/2026-06-02-service-branch-coverage-gate.md

## Problem
JaCoCo only configured in `integration` profile which skips unit tests;
existing check rule (BUNDLE/INSTRUCTION 0.10) has `haltOnFailure=false`,
serving as no-op gate.

## Solution
- Move JaCoCo to main build with PACKAGE-level BRANCH COVEREDRATIO 0.70 gate
- Ratchet overrides for user/pay/hm-service at 0.00
- CI test job runs `mvn verify` instead of `mvn test`
```

- [ ] **Step 3: 写 verification.md**

```markdown
# Verification

## RED phase
- [x] mvn verify fails on user/pay/hm-service (0% service.impl branch coverage)
- [x] mvn verify passes on cart/file/item/trade (≥70%)
- [x] notify-service excluded from check

## GREEN phase
- [x] mvn verify all-green with ratchet overrides
- [x] cart-service threshold 0.95 confirms gate is real (fails)
- [x] cart-service back to 0.70 confirms recovery (passes)

## CI dry-run
- [x] mvn -B -ntp -q verify exits 0
- [x] CI YAML parse check passes
```

- [ ] **Step 4: 写 audit.md**

```markdown
# Audit

## Security
- [x] No secrets/hardcoded values
- [x] No endpoint changes
- [x] No auth changes

## Module boundaries
- [x] Only pom.xml + CI workflow modified
- [x] No Java source changes
- [x] Integration profile IT behavior preserved (jacoco.check.skip=true)

## POM changes
- [x] JaCoCo version unchanged (0.8.10)
- [x] haltOnFailure=true on gate
- [x] BRANCH counter, PACKAGE element
```

- [ ] **Step 5: 写 handoff.md**

```markdown
# Handoff

## Branch
task/2026-06-01-service-branch-coverage-gate

## PR checklist
- [ ] CI: lint job passes
- [ ] CI: test job passes (mvn verify)
- [ ] CI: integration job passes
- [ ] CI: smoke job passes
- [ ] CI: codex-review passes (blocking findings: none)
- [ ] AGENTS.md ↔ CLAUDE.md byte-identical (unchanged)

## Ratchet modules (for future work)
- user-service: 0% → target 0.70 (34 branches)
- pay-service: 0% → target 0.70 (12 branches)
- hm-service: 0% → target 0.70 (44 branches)

## KB update
Pom changes only, no module/flow page content changes needed.
Bump last_synced_commit on affected pages if K005 triggers.
```

- [ ] **Step 6: 写 task.yaml**

```yaml
slug: 2026-06-01-service-branch-coverage-gate
status: active
created: 2026-06-02
spec: docs/superpowers/specs/2026-06-01-service-branch-coverage-gate-design.md
plan: docs/superpowers/plans/2026-06-02-service-branch-coverage-gate.md
```

- [ ] **Step 7: 运行交付三件套**

```bash
python3 scripts/agent_harness.py check
python3 scripts/knowledge_base.py check
python3 scripts/engineering-lint.py
```

**预期结果：** 三项均通过。

- [ ] **Step 8: Commit Task 6**

```bash
git add docs/agent-harness/ docs/superpowers/plans/
git commit -m "docs: harness task record + implementation plan for coverage gate"
```

---

### Task 7: 分支、推送、PR、合并

- [ ] **Step 1: 创建分支**

```bash
git checkout -b task/2026-06-01-service-branch-coverage-gate
```

（注：如果已在 main 上进行了 commit，需要 rebase 或 cherry-pick 到 feature 分支。）

- [ ] **Step 2: 推送分支**

```bash
git push -u origin task/2026-06-01-service-branch-coverage-gate
```

- [ ] **Step 3: 创建 PR**

```bash
gh pr create \
  --title "feat: JaCoCo service.impl 70% branch coverage gate" \
  --body "## Summary
Implement service.impl 70% branch coverage gate per spec.

### Changes
- Move JaCoCo from integration profile to main build
- PACKAGE-level BRANCH COVEREDRATIO check with haltOnFailure=true
- 0.00 ratchet overrides for user/pay/hm-service
- CI test job: \`mvn test\` → \`mvn verify\`

### Verification
- RED: user/pay/hm-service fail without overrides
- GREEN: all pass with ratchet overrides
- Reverse: cart 0.95 threshold confirms gate is real

### Ratchet (future work)
| Module | Current | Target |
|--------|---------|--------|
| user-service | 0.00 (0/34) | 0.70 |
| pay-service | 0.00 (0/12) | 0.70 |
| hm-service | 0.00 (0/44) | 0.70 |

🤖 Generated with [Claude Code](https://claude.com/claude-code)"
```

- [ ] **Step 4: 等待 CI 通过**

Monitor CI status:
```bash
gh pr checks task/2026-06-01-service-branch-coverage-gate --watch
```

- [ ] **Step 5: CI 全部通过后合并**

```bash
gh pr merge task/2026-06-01-service-branch-coverage-gate --squash --delete-branch
```

- [ ] **Step 6: 本地切回 main 并清理**

```bash
git checkout main
git pull origin main
git branch -d task/2026-06-01-service-branch-coverage-gate
```

---

## 自检清单

**1. Spec 覆盖检查：**
- [x] §3 JaCoCo 上移到主构建 → Task 1
- [x] §3 移除 integration profile 旧配置 → Task 1 Step 2
- [x] §3 integration profile + skip 属性 → Task 1 Step 3
- [x] §4 父 POM 默认规则 (PACKAGE/BRANCH/0.70) → Task 1 Step 1
- [x] §4 notify-service exclude → Task 1 Step 1
- [x] §5 棘轮 override (user/pay/hm-service 0.00) → Task 3
- [x] §6 RED 验证 → Task 2
- [x] §6 GREEN 验证 → Task 3 Step 4
- [x] §6 反向校验 (cart 0.95) → Task 4
- [x] §6 notify exclude 校验 → Task 4 Step 4
- [x] §3 CI 改造 (test job → mvn verify) → Task 5
- [x] §7 Harness 任务记录 → Task 6
- [x] §7 交付三件套 → Task 6 Step 7
- [x] §7 分支/PR/合并 → Task 7
- [x] §8 YAGNI 边界 → 全部遵守（无聚合报告、不合并 IT、不写业务测试）

**2. Placeholder 扫描：** 无 TODO/TBD/placeholder（棘轮注释是功能性标记）。

**3. 类型一致性：** 所有引用一致：`**/service/impl` 模式、`combine.self="override"`、`haltOnFailure=true`。
