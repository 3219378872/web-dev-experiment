<template>
  <div class="search-page">
    <div class="wrap">
      <div class="crumb">
        <router-link to="/">首页</router-link><span class="s">/</span>搜索结果
      </div>

      <div class="srow">
        <h2>
          “<b>{{ query }}</b
          >” 的搜索结果
        </h2>
        <span class="muted" style="font-size: 13px"
          >为您找到 <b style="color: var(--price)">{{ total }}</b> 件相关商品</span
        >
      </div>

      <div class="related" style="margin-bottom: 14px">
        <span>相关搜索：</span>
        <a v-for="r in related" :key="r" @click="goSearch(r)">{{ r }}</a>
      </div>

      <div class="empty-hint">
        🔎 已为您开启「模糊匹配」，同时展示精准匹配与相关推荐。可在右侧切换匹配模式。
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
        <span class="mode">
          匹配：<span class="tg"
            ><span :class="{ on: fuzzy }" @click="fuzzy = true">模糊</span
            ><span :class="{ on: !fuzzy }" @click="fuzzy = false">精准</span></span
          >
        </span>
      </div>

      <div class="grid g5">
        <ProductCard v-for="item in sortedItems" :key="item.id" :item="item" />
      </div>

      <div v-if="!items.length" style="text-align: center; padding: 60px 0; color: var(--ink-3)">
        暂无搜索结果
      </div>

      <div v-if="items.length" class="pager">
        <span>‹ 上一页</span>
        <a class="cur">1</a><a>2</a><a>3</a><a>4</a><a>5</a> <span class="dots">…</span><a>14</a>
        <a>下一页 ›</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { searchItems } from '@/api/item';
import ProductCard from '@/components/ProductCard.vue';

const route = useRoute();
const router = useRouter();
const items = ref([]);
const total = ref(0);
const query = ref('');
const sortKey = ref('default');
const fuzzy = ref(true);

const related = ['降噪耳机', '蓝牙耳机 入耳式', '无线耳机', '运动耳机', '头戴式耳机', '骨传导'];

const sortOptions = [
  { key: 'default', label: '综合排序' },
  { key: 'sold', label: '销量 ↓' },
  { key: 'priceAsc', label: '价格 ↑' },
  { key: 'priceDesc', label: '价格 ↓' },
  { key: 'new', label: '新品优先' },
  { key: 'rating', label: '好评优先' },
];

const sortedItems = computed(() => {
  const list = [...items.value];
  switch (sortKey.value) {
    case 'sold':
      list.sort((a, b) => (b.sold || 0) - (a.sold || 0));
      break;
    case 'priceAsc':
      list.sort((a, b) => (a.price || 0) - (b.price || 0));
      break;
    case 'priceDesc':
      list.sort((a, b) => (b.price || 0) - (a.price || 0));
      break;
    case 'new':
      list.sort((a, b) => (b.createTime || '').localeCompare(a.createTime || ''));
      break;
    case 'rating':
      list.sort((a, b) => (b.rating || 0) - (a.rating || 0));
      break;
    default:
      break;
  }
  return list;
});

function goSearch(keyword) {
  router.push({ path: '/search', query: { q: keyword } });
}

async function search() {
  const q = route.query.q || '';
  query.value = q;
  if (!q) {
    items.value = [];
    total.value = 0;
    return;
  }
  try {
    const data = await searchItems({ name: q });
    items.value = data?.list || [];
    total.value = data?.total || items.value.length;
  } catch (err) {
    /* ignore */
  }
}

onMounted(search);
watch(() => route.query.q, search);
</script>

<style scoped>
.srow {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 6px 0 16px;
  flex-wrap: wrap;
}
.srow h2 {
  font-size: 18px;
  font-weight: 700;
}
.srow h2 b {
  color: var(--brand);
}
.related {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 13px;
  color: var(--ink-3);
  flex-wrap: wrap;
}
.related a {
  padding: 3px 11px;
  border: 1px solid var(--line-2);
  border-radius: 999px;
  color: var(--ink-2);
  font-size: 12.5px;
  background: #fff;
  cursor: pointer;
}
.related a:hover {
  border-color: var(--brand);
  color: var(--brand);
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
.sortbar .mode {
  margin-left: auto;
  display: flex;
  gap: 6px;
  align-items: center;
  font-size: 12.5px;
  color: var(--ink-3);
}
.sortbar .mode .tg {
  display: inline-flex;
  border: 1px solid var(--line-2);
  border-radius: 7px;
  overflow: hidden;
}
.sortbar .mode .tg span {
  padding: 5px 10px;
  font-size: 12px;
  color: var(--ink-2);
  cursor: pointer;
}
.sortbar .mode .tg span.on {
  background: var(--brand-soft);
  color: var(--brand-700);
  font-weight: 700;
}

.empty-hint {
  background: var(--gold-soft);
  border: 1px solid #f3d9a0;
  border-radius: 10px;
  padding: 10px 16px;
  font-size: 12.5px;
  color: #8a5d00;
  margin-bottom: 14px;
}

@media (max-width: 1024px) {
  .g5 {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 768px) {
  .g5 {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
