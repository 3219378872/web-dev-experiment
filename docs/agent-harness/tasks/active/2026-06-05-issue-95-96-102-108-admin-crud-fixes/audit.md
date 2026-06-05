# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| #95 查询参数完整 | done | ItemList.vue fetch() 传递 category、minPrice、maxPrice；ItemController.adminQueryItemByPage 接收并处理 |
| #95 批量操作有事件 | done | batchOnSale/batchOffSale/batchDelete 绑定点击事件，调用 batchUpdateItemStatus/batchDeleteItems |
| #95 标签徽章基于总数 | done | 新增 /admin/items/stats 接口，tabs badge 基于 stats 而非当前页数据 |
| #96 移除假数据 | done | CategoryList.vue 移除 LV1_COUNTS/PALETTE 硬编码，level1Count/level2Count 基于真实 categories 计算 |
| #102 接入 Banner API | done | BannerList.vue 使用 getBanners/saveBanner/updateBanner/deleteBanner/updateBannerStatus |
| #108 分类动态获取 | done | ItemEdit.vue 分类下拉从 getCategories() 动态获取 |
| #108 图片真实上传 | done | ItemEdit.vue 接入 /upload/image 接口，handleFileChange 调用 uploadImage |
| #108 规格动态管理 | done | ItemEdit.vue 添加 specGroups 数组，支持增删规格组和规格值 |
| #108 发布设置绑定 | done | ItemEdit.vue isSeckill/isRecommend/isReturnable 绑定真实 reactive 字段 |
| 无硬编码密钥 | done | 审查所有变更文件，无新增密钥 |
| API 响应形态兼容 | done | 新增 /admin/items/stats 返回 Map；现有接口形态未破坏 |
