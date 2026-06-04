<template>
  <div class="adm">
    <aside class="adm-side">
      <div class="adm-brand">
        <span class="mark">集</span>
        <span class="nm">好集后台<small>ADMIN PANEL</small></span>
      </div>
      <nav class="adm-nav">
        <div class="grp-t">总览</div>
        <router-link to="/dashboard" :class="{ on: route.path === '/dashboard' }">
          <span class="i">📊</span>数据看板
        </router-link>

        <div class="grp-t">运营</div>
        <router-link
          to="/items"
          :class="{ on: route.path === '/items' || route.path.startsWith('/items/') }"
        >
          <span class="i">📦</span>商品管理
        </router-link>
        <router-link to="/categories" :class="{ on: route.path === '/categories' }">
          <span class="i">🗂</span>分类管理
        </router-link>
        <router-link
          to="/orders"
          :class="{ on: route.path === '/orders' || route.path.startsWith('/order') }"
        >
          <span class="i">▤</span>订单管理
          <span v-if="orderBadge" class="badge">{{ orderBadge }}</span>
        </router-link>
        <router-link to="/reviews" :class="{ on: route.path === '/reviews' }">
          <span class="i">★</span>评价管理
        </router-link>
        <router-link to="/users" :class="{ on: route.path === '/users' }">
          <span class="i">👥</span>用户管理
        </router-link>

        <div class="grp-t">内容</div>
        <router-link to="/banners" :class="{ on: route.path === '/banners' }">
          <span class="i">🖼</span>轮播管理
        </router-link>
        <router-link to="/notifications" :class="{ on: route.path === '/notifications' }">
          <span class="i">📢</span>公告管理
        </router-link>
        <router-link to="/feedbacks" :class="{ on: route.path === '/feedbacks' }">
          <span class="i">💬</span>反馈管理
          <span v-if="feedbackBadge" class="badge">{{ feedbackBadge }}</span>
        </router-link>

        <div class="grp-t">系统</div>
        <router-link to="/profile" :class="{ on: route.path === '/profile' }">
          <span class="i">⚙</span>个人中心
        </router-link>
      </nav>
      <div class="foot">
        <div>好集 HAOJI ADMIN v2.6</div>
        <div style="margin-top: 4px">© 2026 高保真原型</div>
      </div>
    </aside>
    <div class="adm-main">
      <header class="adm-top">
        <div class="crumb">
          好集后台 <span style="margin: 0 6px; color: var(--line-2)">/</span>
          <b>{{ pageTitle }}</b>
        </div>
        <div class="search">🔍 搜索订单、商品、用户…</div>
        <div class="tools">
          <a class="tbtn" href="/feedbacks" title="消息"
            ><span>✉</span><span v-if="notifyDot" class="dot"></span
          ></a>
          <a class="tbtn" href="/notifications" title="通知"
            ><span>🔔</span><span v-if="notifyDot" class="dot"></span
          ></a>
          <a class="tbtn" href="/" title="前台" target="_blank"><span>🏪</span></a>
          <div class="me">
            <span class="av">{{ (adminName || '管')[0] }}</span>
            <div class="nm">{{ adminName }}<small>管理员</small></div>
          </div>
          <a class="tbtn" title="退出登录" style="cursor: pointer" @click="logout"
            ><span>↪</span></a
          >
        </div>
      </header>
      <div class="adm-body"><router-view /></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getOrders } from '@/api/order';
import { getFeedbacks } from '@/api/system';

const route = useRoute();
const router = useRouter();

const adminInfo = JSON.parse(localStorage.getItem('adminInfo') || 'null');
const adminName = computed(() => adminInfo?.username || '管理员');

// 侧边/顶栏徽标取自真实接口：待发货订单数、待处理反馈数
const orderBadge = ref(0);
const feedbackBadge = ref(0);
const notifyDot = computed(() => feedbackBadge.value > 0);

async function loadBadges() {
  try {
    const r = await getOrders({ page: 1, size: 1, status: 2 });
    orderBadge.value = r?.total || 0;
  } catch (err) {
    /* 忽略，徽标保持 0 */
  }
  try {
    const r = await getFeedbacks({ page: 1, size: 100 });
    feedbackBadge.value = (r?.list || []).filter((f) => f.status === 0).length;
  } catch (err) {
    /* 忽略 */
  }
}

onMounted(loadBadges);

const pageTitle = computed(() => {
  const map = {
    '/dashboard': '数据看板',
    '/users': '用户管理',
    '/categories': '分类管理',
    '/items': '商品管理',
    '/items/add': '新增商品',
    '/reviews': '评价管理',
    '/orders': '订单管理',
    '/banners': '轮播管理',
    '/notifications': '公告管理',
    '/feedbacks': '反馈管理',
    '/profile': '个人中心',
  };
  if (route.path.startsWith('/items/') && route.path !== '/items/add') return '编辑商品';
  if (route.path.startsWith('/orders/') && route.path !== '/orders') return '订单详情';
  return map[route.path] || '页面';
});

function logout() {
  localStorage.removeItem('adminToken');
  localStorage.removeItem('adminInfo');
  router.push('/login');
}
</script>

<style scoped>
.adm {
  display: grid;
  grid-template-columns: 226px 1fr;
  min-height: 100vh;
}
.adm-side {
  background: var(--admin-sidebar);
  color: #aeb4be;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}
.adm-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.adm-brand .mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--brand), var(--brand-700));
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 900;
  font-size: 18px;
}
.adm-brand .nm {
  color: #fff;
  font-weight: 900;
  font-size: 16px;
  line-height: 1.1;
}
.adm-brand .nm small {
  display: block;
  font-size: 9px;
  color: #6b7280;
  letter-spacing: 1.5px;
  font-weight: 500;
}
.adm-nav {
  flex: 1;
  overflow: auto;
  padding: 10px 12px;
}
.adm-nav .grp-t {
  font-size: 10.5px;
  color: #5b6370;
  padding: 14px 10px 7px;
  letter-spacing: 1px;
  font-weight: 700;
}
.adm-nav a {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 10px 12px;
  border-radius: 9px;
  font-size: 13.5px;
  color: #aeb4be;
  margin-bottom: 2px;
  text-decoration: none;
}
.adm-nav a .i {
  width: 18px;
  text-align: center;
  font-size: 15px;
}
.adm-nav a .badge {
  margin-left: auto;
  background: var(--brand);
  color: #fff;
  font-size: 10px;
  padding: 1px 7px;
  border-radius: 999px;
  font-weight: 700;
}
.adm-nav a:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}
.adm-nav a.on {
  background: linear-gradient(135deg, var(--brand), var(--brand-700));
  color: #fff;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(255, 77, 46, 0.3);
}
.adm-side .foot {
  padding: 14px 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 11px;
  color: #5b6370;
}

.adm-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.adm-top {
  background: #fff;
  border-bottom: 1px solid var(--admin-line);
  height: 60px;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 30;
}
.adm-top .crumb {
  font-size: 13px;
  color: var(--ink-3);
  display: flex;
  align-items: center;
}
.adm-top .crumb b {
  color: var(--ink);
  font-weight: 700;
}
.adm-top .crumb a:hover {
  color: var(--brand);
}
.adm-top .search {
  margin-left: auto;
  background: var(--admin-bg);
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 13px;
  color: var(--ink-3);
  width: 240px;
  display: flex;
  gap: 8px;
  align-items: center;
}
.adm-top .tools {
  display: flex;
  align-items: center;
  gap: 8px;
}
.adm-top .tbtn {
  width: 38px;
  height: 38px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  color: var(--ink-2);
  font-size: 17px;
  position: relative;
  cursor: pointer;
}
.adm-top .tbtn:hover {
  background: var(--admin-bg);
}
.adm-top .tbtn .dot {
  position: absolute;
  top: 8px;
  right: 9px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--brand);
}
.adm-top .me {
  display: flex;
  align-items: center;
  gap: 9px;
  padding-left: 14px;
  border-left: 1px solid var(--admin-line);
}
.adm-top .me .av {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: linear-gradient(135deg, #ff7a45, var(--brand));
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 900;
  font-size: 14px;
}
.adm-top .me .nm {
  font-size: 13px;
  font-weight: 700;
}
.adm-top .me .nm small {
  display: block;
  font-size: 11px;
  color: var(--ink-3);
  font-weight: 400;
}
.adm-body {
  padding: 24px;
  flex: 1;
}
</style>
