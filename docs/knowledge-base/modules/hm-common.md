---
title: hm-common
tracks:
  - hm-common/
last_synced_commit: b25e21464938f9ce5f1cef682ae90224ca30f054
last_synced_date: 2026-06-02
sync_note: "RabbitMQ after-commit publish plus consumer retry/dead-letter semantics"
---

# hm-common

## 职责

跨所有微服务的共享基础库：统一响应封装、分页查询契约、通用异常、Mybatis-Plus
默认配置、JWT 解析、用户上下文持有、Web/Feign 拦截器、RabbitMQ 共享契约。
不包含业务逻辑。

## 公开接口与契约

- `com.hmall.common.domain.R<T>` / `PageDTO<T>` —— 响应封装类型；在仓库内
  使用不统一（详见 *注意事项与陷阱*）。`CommonExceptionAdvice` 用 `R<Void>`
  统一包装异常路径。
- `com.hmall.common.domain.PageQuery` —— 分页/排序参数基类。
- `CommonException` / `BadRequestException` / `UnauthorizedException` /
  `ForbiddenException` / `BizIllegalException` / `DbException` —— 全栈统一抛出。
- `UserContext` —— ThreadLocal 当前登录用户 ID；由 Web/Feign 拦截器写入。
- `MvcConfig` / `MybatisConfig` —— 自动装配，子服务通常无需自配。
- `com.hmall.common.mq.MqConstants` —— `trade.topic`、`pay.topic`、
  `delay.exchange`、死信 exchange、queue、routing key、延时关单 TTL 常量。
- `com.hmall.common.mq.event.*` —— 订单创建、支付成功、订单状态变更事件 DTO。
- `RabbitMqConfig` —— Jackson 消息转换、手动 ack listener factory、队列/交换机/绑定、
  TTL + DLX 延时关单、消费者重试队列、死信队列声明，以及 mandatory producer 发送配置。
- `MqMessagePublisher` / `RabbitMqMessagePublisher` —— 统一发送入口；同步发送异常、
  publisher confirm nack、publisher return 都会尝试写入 `mq_outbox_message`；存在事务同步时
  只在 `afterCommit` 后发送。
- `MqConsumerSupport` —— 统一消费失败处理：未超过重试次数时 nack 到专用 retry queue，
  超过上限后转发到 `hmall.mq.dead.queue` 并 ack 原消息。

## 上游

无 —— 它是基础库。

## 下游

`hm-api`、`hm-gateway`、所有业务微服务（`*-service`）。

## 关键文件

- `domain/R.java` —— 统一响应封装。
- `domain/PageDTO.java` / `domain/PageQuery.java` —— 分页契约。
- `exception/` —— 异常体系。
- `utils/UserContext.java` —— 用户上下文。
- `config/MvcConfig.java` —— Web MVC 默认配置。
- `interceptor/UserInfoInterceptor.java` —— 从 header 读取并写入 `UserContext`。
- `mq/MqConstants.java` —— MQ 拓扑命名。
- `mq/config/RabbitMqConfig.java` —— RabbitMQ 自动装配，包含主队列、延时关单队列、
  retry queue、dead queue。
- `mq/consumer/MqConsumerSupport.java` —— 消费失败重试/死信分流。
- `mq/event/*.java` —— 共享事件 DTO。
- `mq/outbox/*.java` —— 发布失败落表入口，覆盖同步异常、confirm nack 与 return。

## 注意事项与陷阱

- 响应封装在仓库内**不统一**：异常路径由 `CommonExceptionAdvice` 一致返
  `R<Void>`；成功路径混合 `R<T>`/`R<Void>`、`*VO`/`*DTO`/`PageDTO<T>`、
  裸 `void`/`Long`/`String`，甚至裸返数据库 entity（如
  `user-service/AddressController.list()` → `List<Address>`）。两个前端
  axios `r => r.data` 拦截器对两种形态都兼容。
- 新增 endpoint 优先 `R<Void>`(写) + `*VO`/`PageDTO<T>`(读)；**不要无前端
  协调地改既有 endpoint 形态**，否则破坏现网调用。
- `UserContext` 是 ThreadLocal，异步线程必须显式传递。
- 修改 `R` 字段顺序或名称会破坏前端解析。
- RabbitMQ 自动装配默认开启，可用 `hm.rabbitmq.enabled=false` 关闭；单元测试通过
  mock `RabbitTemplate` 保持 broker-free。
- RabbitMQ producer 发送注册到事务 `afterCommit`；调用方本地事务提交成功后才真正投递，
  避免业务回滚但消息已发出。
- RabbitMQ producer 失败落表是本 PR 的轻量恢复入口；同步发送异常只记录 outbox
  并不向上抛出，避免发布失败导致业务事务和 outbox 记录一起回滚丢失。
- 消费者使用 manual ack；监听方法必须在业务成功后 `basicAck`，失败交给
  `MqConsumerSupport.reject`。主队列失败消息先进入专用 retry queue，TTL 到期后回到
  原队列；达到 `CONSUMER_MAX_RETRIES` 后转入 `hmall.mq.dead.queue`。
