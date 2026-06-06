---
title: hmall-admin
tracks:
  - hmall-admin/
last_synced_commit: f68eaef
last_synced_date: 2026-06-06
sync_note: "fix(#136,#137,#138,#142): 后台商品/分类/轮播页移除无后端合同的假入口，商品编辑仅提交持久化字段，分类管理读取 /admin/categories，轮播图接入真实上传预览与排序接口。"

---

# hmall-admin

## 职责

管理后台前端。Vue 3 + Element Plus + ECharts + Pinia + Vite，
覆盖商品、分类、订单、用户、优惠券、评价、文件、统计大盘共 13 页面。

## 公开接口与契约

- 所有 API 请求经 [hm-gateway](hm-gateway.md)；走管理端 `/admin/**` 路由，
  需要管理员角色 JWT。
- ECharts 大盘数据走 `/admin/stats/**`（trade/item/user 聚合接口）。
- 文件上传走 [file-service](file-service.md) `/files/upload`。

## 上游

后台运营人员浏览器。

## 下游

[hm-gateway](hm-gateway.md)；上传走 [file-service](file-service.md)。

## 关键文件

- `package.json`、`vite.config.ts`。
- `.eslintrc.cjs`、`.prettierrc` —— 代码质量与格式化配置。
- `src/api/` —— 管理端 API。
- `src/router/index.ts` —— 路由，含权限 meta。
- `src/views/` —— 13 个页面（商品/订单/用户/分类/优惠券/评价/文件/统计 等）。
- `src/components/` —— 表格、表单、图表共享组件。

## 注意事项与陷阱

- 管理端操作必须走 `R` 二次确认（如发货、退款、删除）。
- 订单列表筛选参数为 `orderId`、`userId`、`status`、`page`、`size`；
  导出复用当前筛选但不传分页。批量发货复用单订单 `/admin/orders/{id}/ship`，
  逐个提交选中的待发货订单。
- 订单详情物流来自 `GET /orders/{id}/logistics` 的 `LogisticsTraceVO`，不得使用静态物流号
  或静态时间线。支付方式展示使用 `OrderVO.paymentType`。
- 退款审核只对 `status=6` 订单显示，通过 `/admin/orders/{id}/refund-audit`
  提交 `{ approved, reason }`。
- 大盘图表数据按日聚合，避免实时全量扫描。
- 富文本/图片上传只能走 [file-service](file-service.md) 预签名，不允许直传 MinIO。
- 商品管理不暴露批量导入或待审核 tab，除非后端新增 import/audit 合同；
  商品编辑只提交后端持久化字段（名称、分类、品牌、价格、库存、规格、主图、发布开关），
  当前后端只保存单张 `image` 主图。
- 分类管理使用 `GET /admin/categories` 展示启用与禁用分类；前台 `GET /categories`
  仍只返回启用分类。分类图片、拖拽排序、展开配置没有后端合同，不在管理端展示。
- 轮播管理通过 `POST /upload/image` 上传图片并展示真实 `imageUrl`；
  排序使用 `PUT /admin/banners/{id}/sort`，前端只提供上移/下移更新 sort 值。
- 修改商品状态必须显示当前状态机，避免误操作。
- 提交前运行 `npm run lint` 和 `npm run format`；pre-commit hook 自动检查。
