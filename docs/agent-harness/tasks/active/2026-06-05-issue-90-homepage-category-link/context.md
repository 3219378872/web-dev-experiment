# Issue #90: 首页分类按钮跳转至搜索页而非分类页

## 问题描述
首页左侧分类菜单（catmenu）和金刚区（kingkong）的分类按钮点击后跳转到搜索页（`/search?q=xxx`），而非分类页（`/category?cat=xxx`）。

## 根因分析
`Home.vue` 中分类链接的 `router-link` target 被硬编码为 `/search?q=...`。

## 修复范围
- `hmall-web/src/views/Home.vue`：
  - 左侧分类菜单：`:to="/search?q=${encodeURIComponent(c.name)}"` → `:to="/category?cat=${encodeURIComponent(c.name)}"`
  - 金刚区分类：`:to="/category"` → `:to="/category?cat=${encodeURIComponent(k[0])}"`

## 影响面
仅首页分类入口链接变更，不影响 Category.vue 本身的渲染逻辑。
