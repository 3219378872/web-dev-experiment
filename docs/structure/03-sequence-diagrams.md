# 核心业务时序图

三条最关键的链路：JWT 登录与鉴权透传、下单、余额支付。跨服务事务由 **Seata AT** 协调，
异步副作用（清车、通知、延时关单、订单状态更新）通过 **RabbitMQ** 事件驱动。

## 1. JWT 登录与鉴权透传

登录后由 user-service 颁发 JWT；后续请求经网关解析 token，把用户身份以请求头
`user-info` / `role-info` 注入下游，下游 `UserInfoInterceptor` 写入 `UserContext`（ThreadLocal）。

```mermaid
sequenceDiagram
    autonumber
    actor U as 浏览器
    participant GW as hm-gateway
    participant US as user-service
    participant Svc as 下游服务<br/>(trade/pay/...)
    participant Ctx as UserContext<br/>(ThreadLocal)

    Note over U,US: 登录（/users/login 网关放行）
    U->>GW: POST /users/login (username,password)
    GW->>US: 转发
    US->>US: 校验密码，JwtTool 生成 JWT(userId, role)
    US-->>U: 返回 token

    Note over U,Ctx: 携带 token 的后续请求
    U->>GW: GET /orders  Authorization: Bearer <token>
    GW->>GW: AuthGlobalFilter: JwtTool.parseToken<br/>验签 + 校验过期 + 角色
    alt token 无效 / 过期
        GW-->>U: 401 未授权
    else /admin/** 且非 admin
        GW-->>U: 403 禁止
    else 通过
        GW->>Svc: 转发，注入头 user-info=userId, role-info=role
        Svc->>Ctx: UserInfoInterceptor.preHandle 写入 userId/role
        Svc->>Svc: 业务读 UserContext.getUser()
        Svc-->>U: 返回数据
        Svc->>Ctx: afterCompletion 清理 ThreadLocal
    end
```

## 2. 下单（trade-service `OrderServiceImpl.createOrder`）

```mermaid
sequenceDiagram
    autonumber
    actor U as 用户
    participant GW as hm-gateway
    participant TS as trade-service
    participant IS as item-service
    participant Seata as Seata Server
    participant RMQ as RabbitMQ
    participant CS as cart-service
    participant NS as notify-service

    U->>GW: POST /orders (OrderFormDTO)
    GW->>TS: 转发（已注入 user-info）
    activate TS
    Note over TS: 开启 @GlobalTransactional（Seata AT）

    TS->>IS: ItemClient.queryItemByIds(itemIds)
    IS-->>TS: 商品价格/信息
    TS->>TS: 保存 order + order_detail（本地库）
    TS->>Seata: 注册分支事务（undo_log）
    TS->>IS: ItemClient.deductStock(details)
    IS->>Seata: 注册分支事务（undo_log）
    alt 扣库存失败
        IS-->>TS: 抛异常
        TS->>Seata: 触发全局回滚
        Seata->>TS: 回滚 order（undo_log 补偿）
        Seata->>IS: 回滚库存（undo_log 补偿）
        TS-->>U: 错误（CommonExceptionAdvice 包成 R<Void>）
    else 成功
        IS-->>TS: ok
        TS->>RMQ: 发布 OrderCreatedEvent
        TS->>Seata: 提交全局事务
        TS-->>U: 订单创建成功（orderId）
    end
    deactivate TS

    Note over RMQ,NS: 异步副作用（事务提交后消费）
    par 清空购物车
        RMQ->>CS: 消费 OrderCreatedEvent
        CS->>CS: removeByItemIds(itemIds)
    and 发送站内信
        RMQ->>NS: 消费 OrderCreatedEvent
        NS->>NS: 保存"下单成功"消息
    and 延时关单（30分钟）
        RMQ->>RMQ: 延时队列（TTL 30min）
        RMQ->>TS: 消费延时消息 → 关闭未支付订单
    end
```

## 3. 余额支付（pay-service `PayOrderServiceImpl.tryPayOrderByBalance`）

`tryPayOrderByBalance` 标注 `@GlobalTransactional` + `@Transactional`，
通过 Seata AT 协调跨服务事务。支付成功后发布 RabbitMQ 事件，由 trade-service 异步更新订单状态，
notify-service 发送支付成功通知。

```mermaid
sequenceDiagram
    autonumber
    actor U as 用户
    participant GW as hm-gateway
    participant PS as pay-service
    participant US as user-service
    participant Seata as Seata Server
    participant RMQ as RabbitMQ
    participant TS as trade-service
    participant NS as notify-service

    U->>GW: POST /pay-orders/{id}（余额支付）
    GW->>PS: 转发
    activate PS
    Note over PS: 开启 @GlobalTransactional（Seata AT）
    PS->>US: UserClient.deductMoney(pw, amount)
    US->>Seata: 注册分支事务（undo_log）
    US-->>PS: 扣减成功
    PS->>PS: markPayOrderSuccess()<br/>pay_order 置成功
    PS->>Seata: 注册分支事务（undo_log）
    alt 全局事务提交
        PS->>Seata: 提交全局事务
        PS->>RMQ: 发布 PaySuccessEvent
        PS-->>U: 支付成功
    else 全局事务回滚
        Seata->>PS: 回滚 pay_order（undo_log 补偿）
        Seata->>US: 回滚余额（undo_log 补偿）
        PS-->>U: 错误响应
    end
    deactivate PS

    Note over RMQ,NS: 异步更新订单状态（事务提交后消费）
    par 更新订单状态
        RMQ->>TS: 消费 PaySuccessEvent
        TS->>TS: markOrderPaySuccess(orderId)
    and 发送支付成功通知
        RMQ->>NS: 消费 PaySuccessEvent
        NS->>NS: 保存"支付成功"消息
    end
```

> **架构改进**：原 `OrderClient` 失效问题已通过 **RabbitMQ 事件驱动** 解决——pay-service 发布
> `PaySuccessEvent`，trade-service 消费后异步更新订单状态。Seata AT 确保支付与余额扣减的原子性。
