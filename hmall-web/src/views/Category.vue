<template>
  <div class="category-page">
    <div class="wrap">
      <div class="crumb">
        <router-link to="/">首页</router-link><span class="s">/</span
        ><b style="color: var(--ink)">商品分类</b>
      </div>

      <div class="layout">
        <!-- 左侧筛选 -->
        <aside class="filters">
          <div class="fbox">
            <h4>商品分类</h4>
            <div class="ftree">
              <a
                v-for="cat in categoriesWithCount"
                :key="cat.id"
                :class="{ on: activeCatId === cat.id }"
                @click="selectCategory(cat.id)"
              >
                {{ cat.name }}
                <span>{{ cat.count }}</span>
              </a>
            </div>
          </div>
        </aside>

        <!-- 右侧主体 -->
        <main>
          <div class="banner-cat">
            <div>
              <h2>好集商城 · 全部分类</h2>
              <p>精选好物，品质生活，全场正品保障</p>
              <div class="tags">
                <span>满300减50</span><span>分期免息</span><span>以旧换新</span>
              </div>
            </div>
            <router-link class="btn btn-gold" to="/flashsale">⚡ 秒杀专区</router-link>
          </div>

          <div class="sortbar">
            <span
              v-for="s in sortOptions"
              :key="s.key"
              class="s"
              :class="{ on: sortKey === s.key }"
              @click="sortKey = s.key"
              >{{ s.label }}</span
            >
            <span class="right"
              >共 <b>{{ filteredItems.length }}</b> 件商品</span
            >
          </div>

          <div class="grid g4">
            <ProductCard v-for="item in filteredItems" :key="item.id" :item="item" />
          </div>

          <div
            v-if="!filteredItems.length"
            style="text-align: center; padding: 60px 0; color: var(--ink-3)"
          >
            该分类下暂无商品
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { getCategories, getItems } from '@/api/item';
import ProductCard from '@/components/ProductCard.vue';

const categories = ref([]);
const items = ref([]);
const activeCatId = ref(null);
const sortKey = ref('default');

const sortOptions = [
  { key: 'default', label: '综合' },
  { key: 'sold', label: '销量 ↓' },
  { key: 'priceAsc', label: '价格 ↑' },
  { key: 'priceDesc', label: '价格 ↓' },
  { key: 'new', label: '新品' },
];

// 基于已加载的 items 客户端计算每个分类商品数，渲染带 count 字段的分类列表
const categoriesWithCount = computed(() =>
  categories.value.map((cat) => ({
    ...cat,
    count: items.value.filter((i) => i.category === cat.name).length,
  }))
);

// 按分类名称匹配商品（后端 item 只有 category 字段，值为分类名称字符串）
const filteredItems = computed(() => {
  const activeCatName =
    activeCatId.value != null
      ? categories.value.find((c) => c.id === activeCatId.value)?.name
      : null;

  const baseList = activeCatName
    ? items.value.filter((i) => i.category === activeCatName)
    : [...items.value];

  return sortItems(baseList, sortKey.value);
});

function sortItems(list, key) {
  const sorted = [...list];
  switch (key) {
    case 'sold':
      sorted.sort((a, b) => (b.sold || 0) - (a.sold || 0));
      break;
    case 'priceAsc':
      sorted.sort((a, b) => (a.price || 0) - (b.price || 0));
      break;
    case 'priceDesc':
      sorted.sort((a, b) => (b.price || 0) - (a.price || 0));
      break;
    case 'new':
      sorted.sort((a, b) => (b.createTime || '').localeCompare(a.createTime || ''));
      break;
    default:
      break;
  }
  return sorted;
}

function selectCategory(id) {
  activeCatId.value = activeCatId.value === id ? null : id;
}

onMounted(async () => {
  try {
    categories.value = await getCategories();
  } catch (err) {
    /* ignore */
  }
  try {
    const data = await getItems({ page: 1, size: 100 });
    items.value = data?.list || [];
  } catch (err) {
    /* ignore */
  }
});
</script>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 206px 1fr;
  gap: 18px;
  margin: 8px 0 30px;
}
.filters {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.fbox {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 16px;
}
.fbox h4 {
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 7px;
}
.fbox h4::before {
  content: '';
  width: 4px;
  height: 13px;
  border-radius: 2px;
  background: var(--brand);
}
.ftree a {
  display: flex;
  justify-content: space-between;
  padding: 7px 9px;
  border-radius: 7px;
  font-size: 13px;
  color: var(--ink-2);
  cursor: pointer;
}
.ftree a:hover {
  background: var(--brand-softer);
  color: var(--brand);
}
.ftree a.on {
  background: var(--brand-soft);
  color: var(--brand-700);
  font-weight: 700;
}
.ftree a span {
  font-size: 11px;
  color: var(--ink-3);
}

.sortbar {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
  margin-bottom: 14px;
}
.sortbar .s {
  padding: 7px 15px;
  border-radius: 7px;
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-2);
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}
.sortbar .s.on {
  background: var(--brand);
  color: #fff;
  font-weight: 700;
}
.sortbar .s:hover:not(.on) {
  color: var(--brand);
}
.sortbar .right {
  margin-left: auto;
  font-size: 12.5px;
  color: var(--ink-3);
  display: flex;
  align-items: center;
  gap: 14px;
}
.sortbar .right b {
  color: var(--price);
}

.banner-cat {
  background: linear-gradient(120deg, #ff7a45, var(--brand));
  border-radius: var(--radius);
  color: #fff;
  padding: 24px 28px;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.banner-cat h2 {
  font-size: 26px;
  font-weight: 900;
}
.banner-cat p {
  opacity: 0.9;
  font-size: 13.5px;
  margin-top: 4px;
}
.banner-cat .tags {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
.banner-cat .tags span {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
}

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .filters {
    display: none;
  }
  .g4 {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 768px) {
  .g4 {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
