# 基础设施接入设计：MinIO / RabbitMQ / Seata

- **状态**: approved
- **日期**: 2026-06-02
- **作者**: agent harness 协作（brainstorming）
- **范围**: 把当前仅"规划但未接入"的 MinIO、RabbitMQ、Seata 三套中间件真正接入并在 `docker-compose` 中跑通。

## 1. 背景与现状

三套中间件在 `CLAUDE.md` 中被描述为基础设施，但代码与编排里均未真正接入：

- **Seata**：完全未接入。无任何 `@GlobalTransactional` / TCC action，下单链路全走 Feign
  同步调用，跨服务事务无一致性保障，`docker-compose.yml` 无 seata 容器。
- **RabbitMQ**：`hm-common/pom.xml` 已声明 `spring-amqp` + `spring-rabbit` 依赖，但全仓库
  **零** `RabbitTemplate` / `@RabbitListener` 使用，compose 无 broker。属"依赖已声明、从未使用"。
- **MinIO**：`file-service` 当前用**本地文件系统**（`MultipartFile.transferTo` 到 `./uploads`）
  + DB 存元数据，无 MinIO client，compose 无 MinIO。

### 现状链路中的分布式事务隐患（Seata 靶点）

- `OrderServiceImpl.createOrder`（裸 `@Transactional`，仅本地）：
  本地存单 → `cartClient.deleteCartItemByIds`（Feign）→ `itemClient.deductStock`（Feign）。
  若扣库存失败，本地订单回滚但**购物车已删除、无补偿**。
- `PayOrderServiceImpl.tryPayOrderByBalance`（裸 `@Transactional`，仅本地）：
  `userClient.deductMoney`（Feign）→ 改支付单 → `orderClient.updateById`（Feign）。
  跨 user/pay/trade 三库，无全局事务。

## 2. 目标与非目标

### 目标

- 三套中间件真正接入，且在 `docker-compose` 中可一键拉起、跑通。
- 每套作为**独立的 spec → plan → branch/PR 循环**，可独立合并、过 CI。
- 单元测试**不依赖**外部中间件（保持 `test` job 轻量、可重复）。

### 非目标

- 不引入 Seata TCC（本设计选 AT 模式，理由见 §5）。
- 不做生产级高可用集群（单实例中间件，样例项目可接受，文档标注）。
- 不无前端协调地破坏既有 endpoint 返回形态（遵守 `CLAUDE.md` 的 hm-common 约束）。

## 3. 落地顺序（关键）

按"改动独立性"递增、按事务边界耦合关系排序：

1. **MinIO**（最独立，仅动 file-service + 前端取图 + compose）
2. **RabbitMQ**（动 pay/trade/notify/cart，引入异步事件）
3. **Seata AT**（最深，包住 createOrder / tryPay 两条跨服务事务）

**RabbitMQ 必须先于 Seata**：支付成功后的状态流转一旦异步化，会改变 Seata 事务边界。
顺序反了，Seata 落地时要把已异步化的副作用从全局事务里摘出去会返工。

## 4. 共享基础设施（docker-compose 新增容器）

| 容器 | 镜像 | 端口 | 依赖 | healthcheck |
| --- | --- | --- | --- | --- |
| `minio` | `minio/minio` (RELEASE 固定 tag) | 9000(API)/9001(Console) | 无 | curl `/minio/health/live` |
| `minio-init` | `minio/mc` | — | minio | 建 `hmall` 桶、`mc anonymous set download` 设公开读，退出 |
| `rabbitmq` | `rabbitmq:3.13-management` | 5672/15672 | 无 | `rabbitmq-diagnostics ping` |
| `seata-server` | `seataio/seata-server:1.8.0` | 7091(控制台)/8091 | nacos、mysql | curl `:7091/health` |

- Seata 用 **Nacos 作配置 + 注册中心**（与现有 nacos 复用），事务模式 AT。
- 凭据**全走环境变量 / compose env，不硬编码**（遵守 Safety 约束）；MinIO/RabbitMQ
  默认账号也用 env 注入。
- 配置下发延续现有 `hm.*` + Nacos 模式：MinIO endpoint、RabbitMQ host、Seata
  tx-group 通过 compose 环境变量 + Nacos 配置注入；本地 `application.yaml` 留
  `localhost` 默认值，保证单测不依赖外部中间件。

### 对 CI 的影响

- `integration` job 已有 MySQL+Redis service container；按子系统**增量**加
  minio/rabbitmq/seata service container（仅相关模块集成测试用）。
- 单测不依赖中间件：MQ 用 `@MockBean RabbitTemplate`，MinIO mock client，
  Seata 单测关闭（`seata.enabled=false`）。`test` job 不变重。
- 每个子系统 PR 必须更新对应 `docs/knowledge-base/modules/*.md`（K005）。
- 改鉴权/Seata/Gateway 前**必须先在 `docs/agent-harness/` 建任务记录**
  （`CLAUDE.md` Safety 硬约束）：三个子系统各建一个 active 任务。

## 5. 子系统一：MinIO

### 改动范围

`file-service`（核心）+ `hmall-web` / `hmall-admin` 取图逻辑 + gateway 路由 + compose。

### 依赖与客户端

- `file-service/pom.xml` 引入 `io.minio:minio:8.5.x`。
- 新增 `MinioClient` 配置 Bean，endpoint/accessKey/secretKey/bucket 全走 `hm.minio.*` 配置。

### UploadServiceImpl 改造

- `uploadImage`：不再 `transferTo` 本地，改为
  `minioClient.putObject(bucket, "<date>/<uuid>.<ext>", stream, contentType)`。
- `Upload.filePath` 存**对象 key**（如 `2026-06-02/uuid.jpg`），DB 元数据表结构不变。
- 返回 URL 统一为 `/files/{key}` 形式的**稳定公开 URL**，前端取图逻辑零改动。

### 访问路径（公开桶 + 网关反代）

- MinIO 桶 `hmall` 设 `public-read`（`minio-init` 用 `mc anonymous set download`）。
- `FileController` 的 `GET /files/**` 改为**流式代理**到 MinIO（`getObject`），
  而非读本地盘。**采用 file-service 代理**而非 gateway 直连 minio：保留鉴权扩展点、
  不暴露 MinIO 端口给前端、URL 统一经 gateway。
- `download(Long id)` 旧接口保留并改为按 key 代理，兼容历史调用。

### 历史数据兼容

样例项目无生产数据；读取做**降级**：key 命中 MinIO 取 MinIO，命中旧 `/uploads/`
前缀则回退本地盘（过渡期逻辑，注释标注"MinIO 全量迁移后可移除"）。

### 测试矩阵

- **单元**：`MinioClient` mock，断言 putObject 入参、key 生成、异常包装（沿用
  现有 `UploadServiceImplTest`）。
- **集成**：`integration` job 加 minio service container / Testcontainers 起 MinIO，
  真实上传 → 代理下载闭环。
- **冒烟**：`scripts/smoke/` 加上传 + 取图 HTTP 校验。

## 6. 子系统二：RabbitMQ

### 异步化场景（全部纳入）

支付成功回调、下单成功事件、订单状态变更通知、延时关闭超时订单。

### 拓扑（topic exchange + 死信延时）

统一 `MessageConfig` 声明（hm-common 已有 amqp 依赖），各服务按需绑队列：

| Exchange | 类型 | Routing Key | 生产者 | 消费者 |
| --- | --- | --- | --- | --- |
| `trade.topic` | topic | `order.create` | trade(createOrder) | cart(清车)、notify(下单站内信) |
| `pay.topic` | topic | `pay.success` | pay(支付成功) | trade(改单为已支付) |
| `trade.topic` | topic | `order.status.{shipped,refund,cancel}` | trade | notify(发通知) |
| `delay.exchange` | DLX + TTL | `order.delay` | trade(下单时) | trade(到期取消未支付单) |

延时关单用 **DLX + TTL 队列**（不依赖延迟插件，compose 零额外装配），死信落
`order.close.queue`。

### 可靠性设计

- **生产者**：`publisher-confirms` + `publisher-returns` 开启；发送失败落本地轻量
  重试表。
- **消费者**：`manual ack` + 业务幂等（消费前查状态 / 去重表）；失败 `nack` 进重试，
  超限进死信告警队列。
- **与现有 Feign 并存**：异步化的是"通知类"副作用（清车、改状态、发信）；主调用链
  保留同步返回给前端，MQ 仅解耦后置动作。

### 关键改造点

- `pay.tryPayOrderByBalance` 第 5 步 `orderClient.updateById` → 改发 `pay.success`
  事件，trade 异步消费改单（与 Seata 边界交互见 §7）。
- `createOrder` 的 `cartClient.deleteCartItemByIds` → 改发 `order.create` 事件异步清车；
  但**库存扣减是事务关键路径，保留同步并交给 Seata**，不走 MQ。

### 测试矩阵

- **单元**：`@MockBean RabbitTemplate` 断言发送的 exchange/key/payload；消费者逻辑
  普通方法单测 + 幂等断言。单测**不连真实 broker**。
- **集成**：`integration` 加 rabbitmq service container，发→收闭环 + 死信延时
  （缩短 TTL）验证。

## 7. 子系统三：Seata AT

### 模式与装配

- AT 模式；Seata Server 用 Nacos 配置 + 注册。
- 各业务服务引入 `spring-cloud-starter-alibaba-seata`，配置 `seata.tx-service-group`、
  `seata.service.vgroup-mapping`。
- **每个业务库建 `undo_log` 表**（SQL 脚本，遵守"migration 用 SQL 不用 JPA DDL"）：
  trade、pay、user、item、cart 库各一张。
- 数据源由 Seata `DataSourceProxy` 代理（starter 自动）。

### 事务边界（两处加 `@GlobalTransactional`）

1. **`OrderServiceImpl.createOrder`**：包住 本地存单 + `deductStock` + `deleteCartItem`。
   任一参与方失败，AT 用 undo_log 自动反向补偿（恢复库存、恢复购物车、回滚订单）。
2. **`PayOrderServiceImpl.tryPayOrderByBalance`**：包住 `deductMoney`(user) + 改支付单 +
   改订单状态。失败则余额、支付单、订单一致回滚。

### 与 RabbitMQ 的协同（关键设计原则）

支付成功通知若走 MQ 异步，就**脱离了 Seata 全局事务**（消息发出后无法被 AT 回滚）。
职责划分：

- **事务内**：只放需强一致的 DB 写（扣款、改支付单、改订单核心状态）——同步 + Seata AT。
- **事务提交后**：发 MQ 通知（站内信、清车等副作用），用 **本地消息表 +
  `@TransactionalEventListener(AFTER_COMMIT)`** 保证"事务成功才发消息"，避免发了消息
  但事务回滚。

即 **Seata 管强一致 DB 链路，MQ 管最终一致的副作用通知**，两者职责不重叠。
这也是"RabbitMQ 先做、Seata 后做"的根因——Seata 落地时把已异步化的副作用从全局
事务里摘出去。

### 测试矩阵

- **单元**：`seata.enabled=false`，单测不依赖 Seata Server（现有单测无需改）。
- **集成**：`integration` 加 seata-server service container，构造"扣库存失败"场景，
  断言订单 / 购物车 / 库存全回滚（核心验收用例）。
- **冒烟**：下单成功路径端到端。

### 风险

- AT 全局锁可能影响并发下单吞吐（样例项目可接受，文档标注）。
- 触碰 Seata 事务前必须先建 harness 任务记录（Safety 硬约束）。

## 8. 验收标准

每个子系统独立满足：

- [ ] `docker compose up -d` 后对应中间件 healthy，业务链路跑通。
- [ ] 单元测试不依赖外部中间件，`mvn test` 通过。
- [ ] 集成测试（含对应 service container）覆盖核心场景。
- [ ] 对应 `docs/knowledge-base/modules/*.md` 已更新（K005 通过）。
- [ ] `python3 scripts/agent_harness.py check` / `knowledge_base.py check` /
  `engineering-lint.py` 全过。
- [ ] 走 branch → PR → CI → review → 合并 → 删远程分支循环。

## 9. 后续

本 spec 之后用 writing-plans skill 产出实施计划。实施按 §3 顺序拆为三个独立
branch/PR；每个子系统先在 `docs/agent-harness/tasks/active/` 建任务记录。
