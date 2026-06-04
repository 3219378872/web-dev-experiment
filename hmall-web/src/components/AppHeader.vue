<template>
  <div>
    <!-- topbar -->
    <div class="topbar">
      <div class="wrap">
        <div>
          <template v-if="userStore.isLoggedIn"
            >你好，<router-link to="/profile">{{
              userStore.userInfo?.username || '用户'
            }}</router-link
            ><span class="sep">|</span><router-link to="/orders">我的订单</router-link></template
          >
          <template v-else
            ><router-link to="/login">请登录</router-link><span class="sep">|</span
            ><router-link to="/login">免费注册</router-link></template
          >
        </div>
        <div>
          <router-link to="/orders">我的订单</router-link><span class="sep">|</span>
          <router-link to="/favorites">收藏夹</router-link><span class="sep">|</span>
          <router-link to="/coupons">优惠券</router-link><span class="sep">|</span>
          <router-link to="/notifications">公告</router-link><span class="sep">|</span>
          <router-link to="/service">客服</router-link><span class="sep">|</span>
          <a href="/admin/login">商家后台</a>
        </div>
      </div>
    </div>

    <!-- header -->
    <header class="header">
      <div class="wrap">
        <router-link to="/" class="logo">
          <span class="mark">集</span>
          <span class="name">好集<small>HAOJI MALL</small></span>
        </router-link>

        <div style="flex: 1; max-width: 560px">
          <div class="searchbar">
            <input v-model="keyword" placeholder="搜索 你想要的好物～" @keyup.enter="search" />
            <button @click="search">🔍 搜索</button>
          </div>
          <div class="hot-words">
            <a href="/search?q=蓝牙耳机">蓝牙耳机</a>
            <a href="/search?q=空气炸锅">空气炸锅</a>
            <a href="/search?q=通勤包">通勤包</a>
            <a href="/search?q=秒杀" style="color: var(--price)">秒杀</a>
          </div>
        </div>

        <div class="header-actions">
          <router-link to="/favorites" class="icon-btn">
            <span class="ic">♡</span>
            <span>收藏</span>
          </router-link>
          <router-link to="/orders" class="icon-btn">
            <span class="ic">▤</span>
            <span>订单</span>
          </router-link>
          <router-link to="/cart" class="icon-btn cart-btn">
            <span class="ic">🛒</span>
            <span>购物车</span>
            <span v-if="cartStore.totalCount > 0" class="badge">{{ cartStore.totalCount }}</span>
          </router-link>
          <router-link to="/profile" class="icon-btn">
            <span class="ic">◍</span>
            <span>我的</span>
          </router-link>
        </div>
      </div>
    </header>

    <!-- catnav -->
    <nav v-if="showCatnav" class="catnav">
      <div class="wrap">
        <router-link to="/category" class="all">▦ 全部商品分类</router-link>
        <router-link to="/search?q=手机数码">手机数码</router-link>
        <router-link to="/search?q=家用电器">家用电器</router-link>
        <router-link to="/search?q=服饰鞋包">服饰鞋包</router-link>
        <router-link to="/search?q=美妆个护">美妆个护</router-link>
        <router-link to="/search?q=食品生鲜">食品生鲜</router-link>
        <router-link to="/search?q=家居家装">家居家装</router-link>
        <router-link to="/search?q=母婴玩具">母婴玩具</router-link>
        <router-link to="/search?q=运动户外">运动户外</router-link>
        <span class="spacer"></span>
        <router-link to="/flashsale" class="promo">⚡ 限时秒杀</router-link>
        <router-link to="/coupons">领券中心</router-link>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useCartStore } from '@/stores/cart';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const cartStore = useCartStore();
const keyword = ref('');

/** 原型中部分页面不含 catnav，需对齐 */
const showCatnav = computed(() => {
  const noCatnav = ['/flashsale', '/order/confirm'];
  return !noCatnav.includes(route.path);
});

function search() {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { q: keyword.value.trim() } });
  }
}
</script>

<style scoped>
.topbar {
  background: var(--ink);
  color: #cfc7c1;
  font-size: 12.5px;
}
.topbar .wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 34px;
}
.topbar a {
  color: #cfc7c1;
  opacity: 0.85;
}
.topbar a:hover {
  opacity: 1;
  color: #fff;
}
.topbar .sep {
  margin: 0 10px;
  color: #5a534e;
}

.header {
  background: var(--surface);
  border-bottom: 1px solid var(--line);
  position: sticky;
  top: 0;
  z-index: 40;
}
.header .wrap {
  display: flex;
  align-items: center;
  gap: 28px;
  height: var(--header-h);
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.logo .mark {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--brand), var(--brand-700));
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 900;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(255, 77, 46, 0.35);
}
.logo .name {
  font-weight: 900;
  font-size: 21px;
  letter-spacing: -0.5px;
  color: var(--ink);
}
.logo .name small {
  display: block;
  font-size: 10px;
  font-weight: 500;
  color: var(--ink-3);
  letter-spacing: 3px;
}

.searchbar {
  flex: 1;
  max-width: 560px;
  display: flex;
  align-items: center;
  border: 2px solid var(--brand);
  border-radius: 999px;
  overflow: hidden;
  background: #fff;
}
.searchbar input {
  flex: 1;
  border: 0;
  outline: 0;
  padding: 11px 18px;
  font-size: 14px;
  background: transparent;
  font-family: inherit;
}
.searchbar button {
  border: 0;
  background: var(--brand);
  color: #fff;
  padding: 0 26px;
  align-self: stretch;
  font-weight: 700;
  font-size: 14px;
}
.searchbar button:hover {
  background: var(--brand-600);
}

.hot-words {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 6px;
}
.hot-words a:hover {
  color: var(--brand);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}
.icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 12px;
  border-radius: 10px;
  color: var(--ink-2);
  font-size: 11.5px;
  background: none;
  border: 0;
  cursor: pointer;
  text-decoration: none;
}
.icon-btn:hover {
  background: var(--brand-softer);
  color: var(--brand);
}
.icon-btn .ic {
  font-size: 19px;
  line-height: 1;
}
.cart-btn {
  position: relative;
}
.cart-btn .badge {
  position: absolute;
  top: 0;
  right: 6px;
  background: var(--brand);
  color: #fff;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  padding: 0 4px;
  font-weight: 700;
}

.catnav {
  background: var(--surface);
  border-bottom: 1px solid var(--line);
}
.catnav .wrap {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 46px;
  font-size: 13.5px;
  font-weight: 500;
}
.catnav .all {
  background: var(--brand);
  color: #fff;
  font-weight: 700;
  padding: 0 18px;
  height: 46px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 0;
}
.catnav a {
  padding: 0 14px;
  height: 46px;
  display: flex;
  align-items: center;
  color: var(--ink);
}
.catnav a:hover {
  color: var(--brand);
}
.catnav .spacer {
  flex: 1;
}
.catnav .promo {
  color: var(--price);
  font-weight: 700;
}
</style>
