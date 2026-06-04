<template>
  <aside class="acct-side">
    <div class="acct-user">
      <div class="av">{{ avatarLetter }}</div>
      <div>
        <b>{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</b>
        <span class="lv">好集会员</span>
      </div>
    </div>
    <div class="acct-grp">
      <div class="gt">交易管理</div>
      <router-link to="/orders" :class="{ on: active === 'orders' }">
        <span class="i">▤</span>我的订单
      </router-link>
      <router-link to="/order/1" :class="{ on: active === 'order-detail' }">
        <span class="i">📦</span>订单详情
      </router-link>
      <router-link to="/cart" :class="{ on: active === 'cart' }">
        <span class="i">🛒</span>购物车
      </router-link>
      <router-link to="/favorites" :class="{ on: active === 'favorites' }">
        <span class="i">♡</span>我的收藏
      </router-link>
    </div>
    <div class="acct-grp">
      <div class="gt">营销中心</div>
      <router-link to="/coupons" :class="{ on: active === 'coupons' }">
        <span class="i">🎟</span>优惠券
      </router-link>
      <router-link to="/flashsale" :class="{ on: active === 'flashsale' }">
        <span class="i">⚡</span>限时秒杀
      </router-link>
    </div>
    <div class="acct-grp">
      <div class="gt">账户设置</div>
      <router-link to="/profile" :class="{ on: active === 'profile' }">
        <span class="i">◐</span>个人信息
      </router-link>
      <router-link to="/addresses" :class="{ on: active === 'addresses' }">
        <span class="i">📍</span>收货地址
      </router-link>
      <router-link to="/feedback" :class="{ on: active === 'feedback' }">
        <span class="i">✍</span>意见反馈
      </router-link>
      <router-link to="/service" :class="{ on: active === 'service' }">
        <span class="i">💬</span>在线客服
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
