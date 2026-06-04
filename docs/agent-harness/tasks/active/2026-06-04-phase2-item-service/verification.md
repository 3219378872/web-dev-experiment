# Verification

## Test Strategy

### Unit Tests
- **ReviewController**: 测试评价列表查询、分页、过滤
- **ItemController**: 测试批量上下架、批量删除、相关推荐
- **BannerController**: 测试轮播图 CRUD、排序、前台/管理端查询
- **SeckillController**: 测试秒杀活动查询
- **Service 层**: 测试业务逻辑（分页查询、状态修改、关联查询）

### Integration Tests
- **端点契约验证**: 使用 Spring Boot Test + Testcontainers 验证端点行为
- **数据库操作**: 验证新增表的 CRUD 操作
- **权限验证**: 验证 `/admin/*` 端点的 Gateway 鉴权集成

## Verification Commands

```bash
# 1. 单元测试
mvn -B -ntp -pl item-service test

# 2. 集成测试（需要 Docker）
mvn -B -ntp -pl item-service verify -Pintegration

# 3. 全模块构建
mvn -B -ntp verify -DskipIntegrationTests

# 4. 端点功能验证（需要运行服务）
# GET /admin/reviews?page=1&size=10&rating=5
# PUT /admin/items/status (body: {"ids": [1,2,3], "status": 1})
# DELETE /admin/items (body: {"ids": [1,2,3]})
# GET /items/1/related
# GET /banners
# GET /admin/banners
# GET /ads
# GET /seckill/active
```

## Expected Results

### 端点行为
1. `GET /admin/reviews` 返回 `PageDTO<ReviewVO>`，支持 rating 过滤
2. `PUT /admin/items/status` 批量修改商品状态，返回 `R<Void>`
3. `DELETE /admin/items` 批量删除商品（逻辑删），返回 `R<Void>`
4. `GET /items/{id}/related` 返回相关商品列表（同分类，排除自身）
5. `GET /banners` 返回启用的轮播图列表（按 sort 排序）
6. `GET /admin/banners` 返回轮播图分页列表，支持 CRUD
7. `GET /ads` 返回广告位列表（按 type/position 过滤）
8. `GET /seckill/active` 返回进行中的秒杀活动（含 endTime、库存百分比）

### 测试覆盖率
- 新增代码行覆盖率 ≥ 80%
- 新增分支覆盖率 ≥ 80%

## Acceptance Criteria

- [ ] 所有 8 个端点实现并可访问
- [ ] 所有端点遵循 R<Void>（写）+ *VO/PageDTO（读）约定
- [ ] 新增 VO 类（ReviewVO、ItemVO、BannerVO、SeckillVO）
- [ ] 新增 PO 类（Banner、Seckill）
- [ ] 新增 Mapper、Service、ServiceImpl
- [ ] 新增 SQL 迁移脚本（banner、seckill 表）
- [ ] 单元测试通过，覆盖率达标
- [ ] 集成测试通过（如果启用）
- [ ] 不破坏现有端点
- [ ] Gateway `/admin/**` 鉴权正常工作

## Evidence Collection

- 测试输出日志
- JaCoCo 覆盖率报告
- 端点调用示例（curl/httpie）
- 代码变更 diff