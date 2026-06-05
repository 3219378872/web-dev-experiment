<template>
  <div class="wrap">
    <div class="crumb">
      <router-link to="/">首页</router-link><span class="s">/</span>个人中心<span class="s">/</span
      ><b style="color: var(--ink)">我的收藏</b>
    </div>

    <div class="acct-layout">
      <AccountSidebar active="favorites" />

      <main>
        <div class="panel">
          <div class="panel-head">
            <h3>
              我的收藏
              <span class="dim" style="font-weight: 400; font-size: 13px"
                >共 {{ favorites.length }} 件</span
              >
            </h3>
            <div style="display: flex; gap: 10px; align-items: center">
              <span class="muted" style="font-size: 13px">管理</span>
            </div>
          </div>
          <div class="panel-pad">
            <div class="fav-bar">
              <div class="tabs">
                <button class="active">全部 ({{ favorites.length }})</button>
                <button>手机数码 (3)</button>
                <button>家用电器 (2)</button>
                <button>食品生鲜 (2)</button>
                <button>降价提醒 (1)</button>
              </div>
              <span class="chip">↓ 收藏时间</span>
            </div>
            <div v-if="favorites.length" class="grid g4">
              <div v-for="f in favorites" :key="f.id" class="pcard" style="position: relative">
                <span class="fav-x" title="取消收藏" @click.prevent="removeFav(f.itemId)">✕</span>
                <router-link
                  :to="`/item/${f.itemId}`"
                  style="color: inherit; text-decoration: none"
                >
                  <div
                    class="ph"
                    :class="f.styleClass || `s${(f.id % 8) + 1}`"
                    :data-label="f.category"
                  >
                    <img
                      v-if="f.itemImage"
                      :src="f.itemImage"
                      :alt="f.itemName"
                      style="width: 100%; height: 100%; object-fit: cover"
                    />
                    <span v-else class="glyph">{{ f.glyph }}</span>
                  </div>
                  <div class="body">
                    <div class="tags">
                      <span v-if="f.tag" class="tag" :class="tagClass(f.tag)">{{ f.tag }}</span>
                      <span
                        v-if="f.originalPrice && f.originalPrice > f.itemPrice"
                        class="tag tag-line"
                        >省¥{{ ((f.originalPrice - f.itemPrice) / 100).toFixed(0) }}</span
                      >
                    </div>
                    <div class="title">{{ f.itemName }}</div>
                    <div class="row">
                      <span class="price" style="font-size: 19px"
                        ><span class="cur">¥</span>{{ (f.itemPrice / 100).toFixed(0) }}</span
                      >
                      <span v-if="f.originalPrice" class="price-old"
                        >¥{{ (f.originalPrice / 100).toFixed(0) }}</span
                      >
                    </div>
                    <div class="meta">
                      <span>{{ f.itemSold ? `已售 ${formatSold(f.itemSold)}` : '新品上市' }}</span>
                      <span class="dim">{{ f.shop ? f.shop.slice(0, 6) : '' }}</span>
                    </div>
                    <button class="btn btn-outline btn-sm fav-add" @click.prevent="addToCart(f.id)">
                      加入购物车
                    </button>
                  </div>
                </router-link>
              </div>
            </div>
            <div v-else class="empty">
              <p>暂无收藏商品</p>
              <router-link class="btn btn-primary btn-sm" to="/" style="margin-top: 12px"
                >去逛逛</router-link
              >
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getFavorites, removeFavorite } from '@/api/common';
import { ElMessage } from 'element-plus';
import AccountSidebar from '@/components/AccountSidebar.vue';

const favorites = ref([]);

const tagClassMap = {
  热卖: 'tag-price',
  促销: 'tag-price',
  秒杀: 'tag-price',
  新品: 'tag-new',
  回购: 'tag-brand',
};

function tagClass(tag) {
  return tagClassMap[tag] || 'tag-brand';
}

function formatSold(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return n;
}

async function load() {
  try {
    favorites.value = await getFavorites();
  } catch (err) {
    console.error(err);
  }
}

async function removeFav(id) {
  try {
    await removeFavorite(id);
    favorites.value = favorites.value.filter((f) => f.id !== id);
    ElMessage.success('已取消收藏');
  } catch (err) {
    console.error(err);
  }
}

function addToCart() {
  ElMessage.info('加入购物车功能开发中');
}

onMounted(load);
</script>

<style scoped>
.fav-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.fav-bar .tabs {
  flex: 1;
  border: 0;
}
.pcard .fav-x {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  color: var(--brand);
  display: grid;
  place-items: center;
  font-size: 14px;
  z-index: 3;
  cursor: pointer;
}
.pcard .fav-add {
  margin-top: 8px;
}
.empty {
  background: #fff;
  border: 1px dashed var(--line-2);
  border-radius: var(--radius);
  text-align: center;
  padding: 60px;
  color: var(--ink-3);
}
@media (max-width: 1024px) {
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
