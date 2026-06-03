<template>
  <aside class="acct-side">
    <div class="acct-user">
      <div class="av">{{ avatarLetter }}</div>
      <div>
        <b>{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</b>
        <span class="lv">普通会员 · 积分 1280</span>
      </div>
    </div>
    <div class="acct-grp">
      <div class="gt">交易管理</div>
      <router-link to="/orders" :class="{ on: active === 'orders' }">
        <span class="i">📦</span>我的订单
      </router-link>
      <router-link to="/cart" :class="{ on: active === 'cart' }">
        <span class="i">🛒</span>购物车
      </router-link>
      <router-link to="/coupons" :class="{ on: active === 'coupons' }">
        <span class="i">🎟</span>我的优惠券
      </router-link>
    </div>
    <div class="acct-grp">
      <div class="gt">资产中心</div>
      <router-link to="/favorites" :class="{ on: active === 'favorites' }">
        <span class="i">♡</span>我的收藏
      </router-link>
      <router-link to="/addresses" :class="{ on: active === 'addresses' }">
        <span class="i">📍</span>收货地址
      </router-link>
    </div>
    <div class="acct-grp">
      <div class="gt">服务中心</div>
      <router-link to="/profile" :class="{ on: active === 'profile' }">
        <span class="i">◍</span>个人信息
      </router-link>
      <router-link to="/notifications" :class="{ on: active === 'notifications' }">
        <span class="i">🔔</span>系统公告
      </router-link>
      <router-link to="/feedback" :class="{ on: active === 'feedback' }">
        <span class="i">✍</span>意见反馈
      </router-link>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue';
import { useUserStore } from '@/stores/user';

defineProps({
  active: { type: String, default: '' },
});

const userStore = useUserStore();

const avatarLetter = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || '用户';
  return name.charAt(0);
});
</script>
