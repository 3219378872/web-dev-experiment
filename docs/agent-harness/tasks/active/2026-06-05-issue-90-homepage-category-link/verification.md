# 验证清单

## 构建验证
- [x] `npm test --if-present` 通过（26 tests passed）
- [x] `npm run build` 通过（无编译错误）

## 功能验证
- [x] 首页左侧分类菜单点击后 URL 为 `/category?cat=xxx`
- [x] 金刚区分类按钮点击后 URL 为 `/category?cat=xxx`
- [x] 编码后的中文分类名在 URL 中正确显示

## 回归验证
- [x] 其他页面路由未受影响
- [x] Category.vue 页面本身无需修改（它已有 `cat` query 参数处理逻辑）
