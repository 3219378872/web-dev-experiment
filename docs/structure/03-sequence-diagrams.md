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

`tryPayOrderByBalance` 标注 `@Transactional` 且**无 try/catch、OrderClient 无 fallback/熔断**。
由于第 5 步的 `OrderClient` 指向未注册的 `order-service`（见 [02](02-module-dependencies.md)），
**当前每次余额支付都会在该步抛异常**，进而触发下面的失败流程——这是仓库现状，而非偶发分支。

```mermaid
sequenceDiagram
    autonumber
    actor U as 用户
    participant GW as hm-gateway
    participant PS as pay-service
    participant US as user-service

    U->>GW: POST /pay-orders/{id}（余额支付）
    GW->>PS: 转发
    activate PS
    Note over PS: 开启本地 @Transactional（无 try/catch）
    PS->>US: 步骤3 UserClient.deductMoney(pw, amount)
    US-->>US: user-service 独立事务扣余额并提交
    US-->>PS: 扣减成功（已落库，不受 PS 事务控制）
    PS->>PS: 步骤4 markPayOrderSuccess()<br/>pay_order 置成功（仅本地、尚未提交）
    PS-->PS: 步骤5 orderClient.updateById(order)<br/>目标 order-service 无实例 → 抛异常（无 fallback）
    Note over PS,US: ⚠️ 异常向外传播 → PS 本地事务回滚：<br/>步骤4 的 pay_order 更新被撤销（仍未支付）；<br/>但步骤3 已提交的余额扣减无补偿（无 Seata）
    PS-->>U: 错误响应（经 CommonExceptionAdvice 包成 R<Void>）
    deactivate PS
```

> **结论（按真实代码）**：当前余额支付链路因 `OrderClient` 失效而无法正常完成——
> 余额被扣、但支付单回滚为未支付、订单状态不更新、用户收到错误。这是"无分布式事务 +
> 失效 Feign 客户端"叠加出的数据不一致缺陷，修复方向是修正 `OrderClient` 的服务名/路径
> 并为跨服务写操作引入补偿或本地消息表。
