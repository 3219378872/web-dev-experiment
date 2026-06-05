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
              >共 <b>{{ total }}</b> 件商品</span
            >
          </div>

          <div class="grid g4">
            <ProductCard v-for="item in items" :key="item.id" :item="item" />
          </div>

          <div
            v-if="!items.length"
            style="text-align: center; padding: 60px 0; color: var(--ink-3)"
          >
            该分类下暂无商品
          </div>

          <div v-if="totalPages > 1" class="pager">
            <span :class="{ disabled: page <= 1 }" @click="prevPage">‹ 上一页</span>
            <a v-for="n in pageRange" :key="n" :class="{ cur: n === page }" @click="goPage(n)">{{
              n
            }}</a>
            <span :class="{ disabled: page >= totalPages }" @click="nextPage">下一页 ›</span>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getCategories, searchItems } from '@/api/item';
import ProductCard from '@/components/ProductCard.vue';

const route = useRoute();

const categories = ref([]);
const items = ref([]);
const activeCatId = ref(null);
const sortKey = ref('default');
const page = ref(1);
const size = ref(20);
const total = ref(0);

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));
const pageRange = computed(() => {
  const pages = [];
  const start = Math.max(1, page.value - 2);
  const end = Math.min(totalPages.value, start + 4);
  for (let i = start; i <= end; i++) pages.push(i);
  return pages;
});

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

function selectCategory(id) {
  activeCatId.value = activeCatId.value === id ? null : id;
  page.value = 1;
  loadItems();
}

async function loadItems() {
  const activeCatName =
    activeCatId.value != null
      ? categories.value.find((c) => c.id === activeCatId.value)?.name
      : null;

  const params = {
    pageNo: page.value,
    pageSize: size.value,
  };

  if (activeCatName) {
    params.category = activeCatName;
  }

  // 排序映射：前端 key → 后端字段名
  const sortMap = {
    sold: 'sold',
    priceAsc: 'price',
    priceDesc: 'price',
    new: 'create_time',
  };
  if (sortKey.value !== 'default' && sortMap[sortKey.value]) {
    params.sortBy = sortMap[sortKey.value];
    params.isAsc = sortKey.value === 'priceAsc';
  }

  try {
    const data = await searchItems(params);
    items.value = data?.list || [];
    total.value = data?.total || items.value.length;
  } catch (err) {
    items.value = [];
    total.value = 0;
  }
}

function goPage(n) {
  if (n < 1 || n > totalPages.value || n === page.value) return;
  page.value = n;
  loadItems();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}
function prevPage() {
  if (page.value > 1) goPage(page.value - 1);
}
function nextPage() {
  if (page.value < totalPages.value) goPage(page.value + 1);
}

watch(sortKey, () => {
  page.value = 1;
  loadItems();
});

onMounted(async () => {
  try {
    categories.value = await getCategories();
  } catch (err) {
    /* ignore */
  }
  // 从 URL query 参数读取分类
  const catFromQuery = route.query.cat;
  if (catFromQuery) {
    const found = categories.value.find((c) => c.name === catFromQuery);
    if (found) {
      activeCatId.value = found.id;
    }
  }
  await loadItems();
});

// 监听 URL cat 参数变化
watch(
  () => route.query.cat,
  (newCat) => {
    if (newCat && categories.value.length) {
      const found = categories.value.find((c) => c.name === newCat);
      if (found) {
        activeCatId.value = found.id;
        page.value = 1;
        loadItems();
      }
    }
  }
);
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
