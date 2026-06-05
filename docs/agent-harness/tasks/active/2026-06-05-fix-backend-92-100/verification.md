# 验证步骤与结果

## 1. 编译验证
- [x] `mvn -q compile -pl cart-service -am -DskipTests` - 编译成功
- [x] `mvn -q compile -pl pay-service -am -DskipTests` - 编译成功

## 2. 单元测试
- [x] `mvn -q test -pl cart-service -am` - BUILD SUCCESS
- [x] `mvn test -pl pay-service -am` - BUILD SUCCESS

## 3. 人工代码审查
- [x] `addItem2Cart()`: 新增购物车时会通过 `itemClient.queryItemByIds()` 获取商品名称/图片/价格
- [x] `handleCartItems()`: 当购物车快照中 name/image/price 为 null 时，会用实时商品数据回填
- [x] `tryPayOrderByBalance()`: 增加了 `po == null` 的校验，返回明确的错误信息
- [x] MQ 发布: 包裹在 try-catch 中，失败时记录日志不影响支付单状态更新
- [x] `OrderClient` 和 `UserClient`: 添加了 `fallbackFactory` 属性指向新的 FallbackFactory 类
- [x] `hm-api/pom.xml`: 增加了 `spring-cloud-starter-circuitbreaker-resilience4j` 依赖
- [x] `pay-service/src/main/resources/application.yaml`: 增加了 `feign.circuitbreaker.enabled: true` 配置

## 4. 边界条件检查
- [x] 支付单不存在时 → 返回 "支付单不存在" 而非 NPE
- [x] MQ 不可用时 → 支付单状态已本地更新，降级日志记录
- [x] trade-service 不可用时 → FallbackFactory 返回 null，调用方已有 null 检查
- [x] user-service 不可用时 → FallbackFactory 抛出明确异常而非 FeignException
