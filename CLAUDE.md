# CLAUDE.md

本文件向 Claude Code 与其它 agent 工具说明在本仓库中如何工作。
`AGENTS.md` 是本文件的镜像，请同时更新并保持字节一致。

## Project Shape

仓库是一个 Spring Cloud 微服务后端 + Vue3 双前端 + Docker Compose 一键编排的电商样例：

- **后端聚合 pom**（`pom.xml`）：`hm-common`、`hm-api`、`hm-gateway` 与
  业务微服务 `user-service`、`item-service`、`cart-service`、`trade-service`、
  `pay-service`、`notify-service`、`file-service`、`hm-service`（聚合服务）。
- **基础设施**：Nacos（配置/注册）、MySQL 8、Redis 7、Seata（TCC/AT）、RabbitMQ、MinIO。
- **前端**：
  - `hmall-web/`：客户端 Vue 3 + Element Plus + Pinia + 13 页面。
  - `hmall-admin/`：管理后台 Vue 3 + Element Plus + ECharts + 13 页面。
- **编排**：`docker-compose.yml` 一键拉起所有服务；`docs/` 放设计与运行说明。

## Agent Harness

实质性变更必须使用仓库本地的 agent harness（`docs/agent-harness/`）。
在 `docs/agent-harness/tasks/active/` 下建立任务记录，含 context、verification、
audit、handoff 四个叙述文件与 `task.yaml` 结构化文件。
长流程文档放 `docs/agent-harness/`，不要塞进本文件。

approved 设计放 `docs/superpowers/specs/`，多步实施计划放 `docs/superpowers/plans/`。
交付前请运行：

```bash
python3 scripts/agent_harness.py check
python3 scripts/knowledge_base.py check
python3 scripts/engineering-lint.py
```

每个实质性任务必须走 branch→PR 循环：从 `main` 切 `task/YYYY-MM-DD-short-slug`、
本地验收、推远程、开 PR、过 CI 与 review、合并后删除远程分支，把分支/PR/CI/review/
清理证据写进 harness 任务记录。

## CI / GitHub Actions

`.github/workflows/` 下三个工作流（参照 bidking-controller 配置，不得跳过）：

- `ci.yml` —— 每 PR + push to main 触发，5 个 job：
  - **lint**：harness check、KB check（PR 时 `--base origin/<base>` 启用 K005）、
    AGENTS↔CLAUDE 字节镜像、engineering-lint。
  - **test**：`mvn -B -ntp -q test` 后端单元测试。
  - **integration**：MySQL 8 + Redis 7 service container，`mvn -Pintegration verify`。
  - **smoke**：harness summary、`mvn compile`、`hmall-web` + `hmall-admin` 的
    `npm ci / npm test --if-present / npm run build`、`docker compose config -q`。
  - **codex-review**（PR-only，blocking）：依赖前 4 个 job 通过，用
    `openai/codex-action@v1` 做任务完成度与合规审查；输出
    `blocking findings: none` 才放行。需仓库 secrets：
    `OPENAI_API_KEY`、`OPENAI_RESPONSES_API_ENDPOINT`。
- `knowledge-base-sync.yml` —— 每周一 06:00 UTC 跑 `knowledge_base.py sync-report`，
  通过 `peter-evans/create-pull-request@v6` 开 `chore: knowledge base sync` PR。
- `pr-cleanup.yml` —— PR 关闭即删除合并分支。

CI 默认不放过任何一个 job。若某 job 因密钥缺失需临时绕过，必须在 PR 描述与
对应 harness 任务的 `handoff.md` 写明原因。

## Knowledge Base

`docs/knowledge-base/` 是"项目当前如何工作"的策展层：每个 Maven 模块 / 前端目录
一个 `modules/<name>.md`，跨服务流程一个 `flows/<name>.md`，索引在
`docs/knowledge-base/INDEX.md`。

每页带 YAML frontmatter 声明 `tracks`（负责覆盖的仓库路径）与
`last_synced_commit`。`scripts/knowledge_base.py check` 是阻塞性 lint，规则：

| ID | 规则 |
| --- | --- |
| K001 | 页面 frontmatter 可解析 |
| K002 | 每个 Maven 模块与前端目录都被恰好一个 modules 页面覆盖 |
| K003 | `tracks` 中的路径在仓库中存在 |
| K004 | 页面 markdown 链接全部可解析（含 CLAUDE.md/AGENTS.md/INDEX.md/README.md） |
| K005 | PR 改动 tracks 路径时也必须改对应页面（需 `--base` 才触发） |
| K006 | INDEX.md 引用了全部页面 |

新增 Maven 模块或前端目录必须在同一 PR 里加 `modules/<name>.md`（K002）。
若代码改动确实不需要内容更新，bump `last_synced_commit` 到当前 HEAD 并在
`sync_note` 写明原因 —— 这是 K005 的轻量豁免。

## Module Map

后端模块（Maven）：

- `hm-common/`：跨服务共享的 DTO、统一 `Result`/`PageDTO` 响应封装、异常、工具、
  鉴权拦截器、Mybatis-Plus 配置。任何接口都必须用 `Result<T>` 包装。
- `hm-api/`：Feign 客户端定义与 fallback，跨服务调用契约层。
- `hm-gateway/`：Spring Cloud Gateway，处理路由、CORS、JWT 解析与转发。
- `user-service/`：用户、角色、地址、JWT 登录、注册、找回密码。
- `item-service/`：商品、分类、品牌、规格、库存、搜索（Elasticsearch 可选）。
- `cart-service/`：购物车与会话购物车。
- `trade-service/`：订单与订单项、优惠券领取/使用、退款、发货、管理端订单操作；
  Seata TCC 协调下单事务。
- `pay-service/`：支付单、模拟支付、对账回调、Seata 参与方。
- `notify-service/`：站内信、短信/邮件渠道、消息模板。
- `file-service/`：MinIO 文件上传、签名 URL、图片元数据。
- `hm-service/`：聚合演示服务，聚合常用读接口。

详细模块说明见各服务 `README` 或 `docs/`。`hm-common.dto.Result` 与
`PageDTO` 是公共契约，任何破坏性改动需 spec + 调用方迁移说明。

## Commands

后端测试：

```bash
mvn -q test                       # 全部模块单测
mvn -q -pl trade-service -am test # 单个模块
```

后端集成测试（依赖 docker）：

```bash
mvn -q -pl trade-service -am verify -Pintegration
```

启动整个栈：

```bash
docker compose up -d
docker compose ps
docker compose logs -f hm-gateway
```

前端：

```bash
cd hmall-web   && npm ci && npm run dev
cd hmall-admin && npm ci && npm run dev
```

前端测试与构建：

```bash
cd hmall-web   && npm test && npm run build
cd hmall-admin && npm test && npm run build
```

Harness CLI：

```bash
python3 scripts/agent_harness.py new <slug>
python3 scripts/agent_harness.py check
python3 scripts/agent_harness.py summary
python3 scripts/agent_harness.py complete <full-slug>
```

Knowledge Base CLI：

```bash
python3 scripts/knowledge_base.py check                # 结构+链接，K005 跳过
python3 scripts/knowledge_base.py check --base origin/main   # 含 K005 共变
python3 scripts/knowledge_base.py sync-report --output /tmp/kb-sync.md
```

## Testing Layers

按顺序推进测试覆盖：

1. **单元测试**：JUnit 5 + Mockito（后端）、Vitest + @vue/test-utils（前端）。
   目标核心 service/util 80% 覆盖率。
2. **集成测试**：Spring Boot Test + Testcontainers（MySQL/Redis），
   控制器→服务→DB 全链路。
3. **冒烟测试**：`scripts/smoke/` 下 bash/python HTTP 验证脚本，针对运行中的
   docker-compose 栈。
4. **E2E 测试**：Playwright（`e2e/` 目录），覆盖 hmall-web 与 hmall-admin 的
   核心用户旅程（登录、加购、下单、管理端 CRUD）。
5. **视觉回归**：基于 Playwright 截图与浏览器实测，修复 UI 缺陷。

## Safety

- 不在源码中硬编码密钥/口令/Token。所有真实凭据走环境变量。
- API 必须用 `Result<T>` 封装，不允许裸返 entity。
- 跨服务调用走 `hm-api` 的 Feign 客户端，不要直接 HTTP。
- 修改鉴权、Seata 事务、Gateway 路由前必须先在 `docs/agent-harness/` 建立任务记录。
- 数据库迁移使用 SQL 脚本而非 JPA 自动 DDL；migration 文件不可改写。
