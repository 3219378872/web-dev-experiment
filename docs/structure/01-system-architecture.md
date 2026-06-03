# 系统架构与部署拓扑

## 1. 系统分层架构图

从浏览器到数据层的完整分层。请求统一经 `hm-gateway` 路由与 JWT 鉴权后分发到各业务服务；
所有服务共享同一 MySQL 库与 Redis，并向 Nacos 注册、从 Nacos 拉取配置（含网关动态路由）。
跨服务事务由 Seata AT 协调，异步事件通过 RabbitMQ 传递。

```mermaid
flowchart TB
    subgraph L1[接入层]
        Browser[浏览器]
        WebNginx[nginx :80<br/>hmall-web dist]
        AdminNginx[nginx :81<br/>hmall-admin dist]
    end

    GW["hm-gateway :8080<br/>Spring Cloud Gateway<br/>路由 + CORS + JWT 解析<br/>/admin/** 需 admin 角色"]

    subgraph L3[业务微服务层]
        direction LR
        IS["item-service :8081<br/>/items /search /categories<br/>/admin/items /admin/reviews"]
        CS["cart-service :8082<br/>/carts<br/>@RabbitListener 消费下单事件"]
        US["user-service :8083<br/>/users /addresses /favorites"]
        TS["trade-service :8084<br/>/orders /coupons /my-coupons<br/>/admin/coupons /admin/orders<br/>@GlobalTransactional 下单"]
        PS["pay-service :8085<br/>/pay-orders<br/>@GlobalTransactional 支付"]
        NS["notify-service :8086<br/>/notifications /messages<br/>/feedbacks /admin/*<br/>@RabbitListener 消费支付事件"]
        FS["file-service :8087<br/>/upload /files<br/>MinIO 存储"]
    end

    subgraph L4[基础设施层]
        DB[("MySQL 8<br/>单库 hmall<br/>:3306")]
        Redis[("Redis 7<br/>:6379")]
        Nacos[("Nacos v2.1.0<br/>注册中心 + 配置中心<br/>:8848 / :9848")]
        Seata["Seata Server 1.8<br/>AT 分布式事务<br/>:8091"]
        RabbitMQ[("RabbitMQ 3.13<br/>订单事件队列<br/>:5672")]
        MinIO[("MinIO<br/>文件存储<br/>:9000")]
    end

    Browser --> WebNginx
    Browser --> AdminNginx
    WebNginx -- "/api 反代" --> GW
    AdminNginx -- "/api 反代" --> GW

    GW --> IS & CS & US & TS & PS & NS & FS

    IS & CS & US & TS & PS & NS & FS --> DB
    IS & CS & US & TS --> Redis
    IS & CS & US & TS & PS & NS & FS -. "注册 / 拉配置" .-> Nacos
    GW -. "gateway-routes.json 动态路由" .-> Nacos

    TS & PS -. "Seata AT 事务协调" .-> Seata
    TS & PS -. "发布订单/支付事件" .-> RabbitMQ
    CS & NS -. "消费事件" .-> RabbitMQ
    FS --> MinIO
```

**鉴权要点**：网关对部分路径放行（`/search`、`/users/login`、`/users/register`、
`/users/send-code`、`/notifications/active`、`/upload`、`/files`），其余请求需携带
合法 JWT；`/admin/**` 额外要求 `admin` 角色。详见
[03-sequence-diagrams.md](03-sequence-diagrams.md) 的鉴权时序。

## 2. 部署拓扑（docker-compose）

`docker-compose.yml` 将**整个后端栈容器化**：除基础设施外，`hm-gateway`、全部 7 个业务服务
都通过 `build: ./<服务>` 构建镜像运行，另有一次性 `nacos-init`（发布路由配置）、
`minio-init`（创建存储桶）与 `smoke-test`（依赖各服务就绪后跑冒烟）。仅 `:8080`（网关）、
前端 `:80/:81` 与基础设施端口对外暴露，服务间在 compose 网络内以服务名互访。

```mermaid
flowchart TB
    subgraph Infra[基础设施容器]
        MySQLC[("mysql:8.0<br/>:3306 库 hmall")]
        NacosC["nacos:v2.1.0<br/>:8848 / :9848"]
        RedisC[("redis:7.0<br/>:6379")]
        RabbitMQC[("rabbitmq:3.13<br/>:5672 / :15672")]
        SeataC["seata-server:1.8<br/>:8091 / :7091"]
        MinIOC[("minio<br/>:9000 / :9001")]
        InitC["nacos-init<br/>(curl, 一次性发布路由)"]
        MinIOInitC["minio-init<br/>(mc, 创建 hmall 桶)"]
    end

    subgraph App[应用容器（build: ./服务）]
        GWc["hm-gateway<br/>对外 :8080"]
        SvcC["7 业务服务<br/>user/item/cart/trade<br/>pay/notify/file"]
        SmokeC["smoke-test<br/>(curl, 依赖各服务就绪)"]
    end

    subgraph Front[前端容器（nginx）]
        WebC["hmall-web :80"]
        AdminC["hmall-admin :81"]
    end

    WebC --> GWc
    AdminC --> GWc
    GWc --> SvcC
    InitC -.->|"发布 gateway-routes.json"| NacosC
    MinIOInitC -.->|"创建 hmall 桶"| MinIOC
    GWc -.->|"拉路由"| NacosC
    SvcC -.->|"注册/配置"| NacosC
    SvcC --> MySQLC
    SvcC --> RedisC
    SvcC -.->|"Seata AT 事务"| SeataC
    SvcC -.->|"RabbitMQ 事件"| RabbitMQC
    SvcC --> MinIOC
    SmokeC -.->|"HTTP 探活"| GWc
```

> 初始化：`docs/sql/init-all-tables.sql` 建表与种子数据；
> `scripts/init-nacos-routes.sh` 向 Nacos 发布 `gateway-routes.json`；
> `minio-init` 容器自动创建 `hmall` 存储桶并设置公开下载权限。
>
> 中间件集成状态：Seata AT（分布式事务）、RabbitMQ（异步事件）、MinIO（文件存储）已全部接入；
> Elasticsearch 未集成，商品搜索走 DB。详见 [README.md](README.md)。
