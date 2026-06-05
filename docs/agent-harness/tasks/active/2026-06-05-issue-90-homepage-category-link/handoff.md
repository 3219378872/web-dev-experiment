# 交接记录

## 变更摘要
- `hmall-web/src/views/Home.vue`：修改左侧分类菜单和金刚区分类按钮的跳转链接，从 `/search?q=xxx` 改为 `/category?cat=xxx`

## PR 描述
Fix #90: 首页分类按钮跳转至分类页而非搜索页

- 左侧分类菜单（catmenu）router-link target 改为 `/category?cat=${encodeURIComponent(c.name)}`
- 金刚区（kingkong）router-link target 改为 `/category?cat=${encodeURIComponent(k[0])}`

## CI 状态
- lint: 待验证
- test: 通过（26 tests）
- build: 通过

## 已知问题
- 无
