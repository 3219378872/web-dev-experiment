# 核心业务时序图

三条最关键的链路：JWT 登录与鉴权透传、下单、余额支付。所有跨服务调用均为**同步 Feign**，
**无 Seata 分布式事务**，仅依赖各服务本地 `@Transactional`。

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
    participant CS as cart-service

    U->>GW: POST /orders (OrderFormDTO)
    GW->>TS: 转发（已注入 user-info）
    activate TS
    Note over TS: 开启本地 @Transactional
    TS->>IS: ItemClient.queryItemByIds(itemIds)
    IS-->>TS: 商品价格/信息
    TS->>TS: 保存 order + order_detail（本地库）
    TS->>CS: CartClient.deleteCartItemByIds(itemIds)
    CS-->>TS: ok
    TS->>IS: ItemClient.deductStock(details)
    alt 扣库存失败
        IS-->>TS: 抛异常
        TS->>TS: 本地事务回滚（order 撤销）
        Note right of TS: ⚠️ 无 Seata：已清的购物车 / 已扣的库存<br/>不会被自动补偿，存在最终不一致风险
        TS-->>U: 错误（CommonExceptionAdvice 包成 R<Void>）
    else 成功
        IS-->>TS: ok
        TS-->>U: 订单创建成功（orderId）
    end
    deactivate TS
```

## 3. 余额支付（pay-service `PayOrderServiceImpl.tryPayOrderByBalance`）

```mermaid
sequenceDiagram
    autonumber
    actor U as 用户
    participant GW as hm-gateway
    participant PS as pay-service
    participant US as user-service
    participant TS as trade-service

    U->>GW: POST /pay-orders/{id}（余额支付）
    GW->>PS: 转发
    activate PS
    PS->>US: UserClient.deductMoney(pw, amount)
    US-->>PS: 扣减成功
    PS->>PS: markPayOrderSuccess(id)<br/>pay_order.status=成功, pay_success_time
    PS--xTS: OrderClient.updateById(order) ✗ 失效<br/>意图 order.status=2, pay_time=now
    Note over PS,TS: ⚠️ OrderClient 目标 order-service 未注册、路径 /users≠/orders，<br/>该回写当前命中不了（详见 02 文档）；故 pay_order 已成功、<br/>但 order 状态未被同步更新
    PS-->>U: 支付成功
    deactivate PS
    Note over PS,US: 同步 Feign 串行；无 Seata，无跨服务补偿
```
