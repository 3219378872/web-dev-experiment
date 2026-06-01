# Audit

## 受影响的文件（回滚删除）

### cart-service
- src/test/java/.../CartServiceTestBase.java
- src/test/java/.../CartServiceImplTest.java
- src/test/resources/application.yml, bootstrap.yml, schema.sql

### trade-service
- src/test/java/.../TradeServiceTestBase.java
- src/test/java/.../CouponServiceImplTest.java
- src/test/java/.../OrderServiceImplTest.java
- src/test/resources/application.yml, schema.sql

### item-service
- src/test/java/.../ItemServiceTestBase.java
- src/test/java/.../ItemServiceImplTest.java
- src/test/java/.../ItemReviewServiceImplTest.java
- src/test/resources/application.yml, bootstrap.yaml, schema.sql

### notify-service
- src/test/java/.../NotificationServiceImplTest.java
- src/test/java/.../FeedbackServiceImplTest.java
- src/test/java/.../CustomerMessageServiceImplTest.java
- src/test/resources/application.yml, schema.sql

### file-service
- src/test/java/.../UploadServiceImplTest.java
- src/test/resources/application.yml, schema.sql

### hm-api
- src/test/java/.../DefaultFeignConfigTest.java

### hm-common
- src/test/java/.../PageDTOTest.java（新增测试移除）

### user-service
- src/test/java/.../JwtToolTest.java（新增测试移除）

### KB 页面
- 10 个 KB 页面的 sync_commit 已更新

## 恢复的文件
- docs/superpowers/specs/2026-06-01-test-coverage-design.md
- docs/superpowers/plans/2026-06-01-test-phase1-core.md
