<template>
  <div class="wrap">
    <div class="crumb">
      <router-link to="/">首页</router-link><span class="s">/</span> 个人中心<span class="s"
        >/</span
      >
      <b style="color: var(--ink)">我的订单</b>
    </div>

    <div class="acct-layout">
      <aside class="acct-side">
        <div class="acct-user">
          <div class="av">{{ userInitial }}</div>
          <div>
            <b>{{ userName }}</b>
            <span class="lv">普通会员</span>
          </div>
        </div>
        <div class="acct-grp">
          <div class="gt">交易管理</div>
          <router-link to="/orders" class="on"> <span class="i">📦</span>我的订单 </router-link>
          <router-link to="/favorites"> <span class="i">♥</span>我的收藏 </router-link>
          <router-link to="/coupons"> <span class="i">🎟</span>优惠券 </router-link>
        </div>
        <div class="acct-grp">
          <div class="gt">账户管理</div>
          <router-link to="/profile"> <span class="i">👤</span>个人资料 </router-link>
          <router-link to="/addresses"> <span class="i">📍</span>收货地址 </router-link>
          <router-link to="/feedback"> <span class="i">✉</span>意见反馈 </router-link>
        </div>
      </aside>

      <main>
        <div
          class="tabs"
          style="
            background: #fff;
            border: 1px solid var(--line);
            border-radius: var(--radius) var(--radius) 0 0;
            border-bottom: 0;
            padding: 0 8px;
          "
        >
          <button
            v-for="tab in tabs"
            :key="tab.value"
            :class="{ active: activeStatus === tab.value }"
            @click="switchTab(tab.value)"
          >
            {{ tab.label }}
            <span v-if="tab.badge" :style="{ color: tab.badgeColor }">{{ tab.badge }}</span>
          </button>
        </div>

        <div
          class="panel"
          style="border-radius: 0; border-top: 0; border-bottom: 0; padding: 12px 16px"
        >
          <div class="order-search">
            <input v-model="searchKeyword" class="input" placeholder="🔍 搜索订单号 / 商品名称" />
            <select class="select" style="max-width: 140px">
              <option>近三个月订单</option>
              <option>今年内</option>
            </select>
            <button class="btn btn-ghost" @click="fetchOrders(1)">查询</button>
          </div>
        </div>

        <template v-if="orders.length">
          <div v-for="order in orders" :key="order.id" class="ord">
            <div class="oh">
              <span class="no">订单号：{{ order.id }}</span>
              <span>{{ order.createTime?.slice(0, 16) }}</span>
              <span class="shop">🏪 好集自营</span>
              <span class="status" :class="statusClass(order.status)">{{
                statusText(order.status)
              }}</span>
            </div>
            <div class="ob">
              <div class="goods">
                <div v-for="(item, idx) in orderItems(order)" :key="idx" class="gi">
                  <img class="ph-img" :src="item.image || '/placeholder.png'" :alt="item.name" />
                  <div style="flex: 1">
                    <div class="nm">{{ item.name }}</div>
                    <div class="spec">{{ item.spec || '默认规格' }}</div>
                  </div>
                  <div class="pq">
                    ¥{{ (item.price / 100).toFixed(2) }}
                    <div class="dim">×{{ item.num }}</div>
                  </div>
                </div>
              </div>
              <div class="side">
                <div class="amt">
                  实付款<b>¥{{ (order.totalFee / 100).toFixed(2) }}</b>
                </div>
                <template v-if="order.status === 1">
                  <router-link class="btn btn-hot btn-sm" :to="`/order/${order.id}?pay=1`"
                    >立即付款</router-link
                  >
                  <button class="btn btn-ghost btn-sm" @click="handleCancel(order.id)">
                    取消订单
                  </button>
                  <span class="dim" style="font-size: 11px">待支付</span>
                </template>
                <template v-else-if="order.status === 3">
                  <button class="btn btn-hot btn-sm" @click="handleConfirm(order.id)">
                    确认收货
                  </button>
                  <router-link class="btn btn-ghost btn-sm" :to="`/order/${order.id}`"
                    >查看物流</router-link
                  >
                  <router-link class="btn btn-ghost btn-sm" to="/service">联系卖家</router-link>
                </template>
                <template v-else-if="order.status === 4">
                  <router-link class="btn btn-gold btn-sm" to="/feedback">评价晒单</router-link>
                  <button class="btn btn-ghost btn-sm">再次购买</button>
                  <router-link class="btn btn-ghost btn-sm" :to="`/order/${order.id}`"
                    >订单详情</router-link
                  >
                </template>
                <template v-else-if="order.status === 5">
                  <div class="amt dim">
                    已取消<b style="color: var(--ink-3)"
                      >¥{{ (order.totalFee / 100).toFixed(2) }}</b
                    >
                  </div>
                  <button class="btn btn-ghost btn-sm">再次购买</button>
                  <button class="btn btn-ghost btn-sm">删除订单</button>
                </template>
                <template v-else>
                  <router-link class="btn btn-ghost btn-sm" :to="`/order/${order.id}`"
                    >订单详情</router-link
                  >
                </template>
              </div>
            </div>
          </div>

          <div v-if="totalPages > 1" class="pager">
            <span :class="{ disabled: page <= 1 }" @click="prevPage">‹</span>
            <a
              v-for="p in pageRange"
              :key="p"
              :class="{ cur: p === page }"
              @click="fetchOrders(p)"
              >{{ p }}</a
            >
            <span :class="{ disabled: page >= totalPages }" @click="nextPage">›</span>
          </div>
        </template>

        <el-empty v-else description="暂无订单" style="margin-top: 40px" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { getOrders, cancelOrder, confirmOrder } from '@/api/order';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();
const orders = ref([]);
const activeStatus = ref('');
const total = ref(0);
const size = ref(5);
const page = ref(1);
const searchKeyword = ref('');

const tabs = [
  { label: '全部订单', value: '' },
  { label: '待付款', value: '1', badge: '', badgeColor: 'var(--price)' },
  { label: '待发货', value: '2' },
  { label: '待收货', value: '3', badge: '', badgeColor: 'var(--info)' },
  { label: '已完成', value: '4' },
  { label: '已取消', value: '5' },
];

const userName = computed(() => userStore.userInfo?.name || '用户');
const userInitial = computed(() => userName.value.charAt(0));

function statusText(status) {
  const map = { 1: '待付款', 2: '待发货', 3: '卖家已发货', 4: '交易完成', 5: '交易关闭' };
  return map[status] || '-';
}

function statusClass(status) {
  const map = { 1: 'wait-pay', 2: 'wait-send', 3: 'wait-recv', 4: 'done', 5: 'closed' };
  return map[status] || '';
}

function orderItems(order) {
  if (order.details?.length) {
    return order.details.map((d) => ({
      name: d.name || '商品',
      price: d.price || 0,
      num: d.num || 1,
      image: d.image || '',
      spec: d.spec || '',
    }));
  }
  return [{ name: '商品', price: order.totalFee || 0, num: 1, image: '', spec: '' }];
}

const totalPages = computed(() => Math.ceil(total.value / size.value));

const pageRange = computed(() => {
  const pages = [];
  for (let i = 1; i <= totalPages.value; i++) pages.push(i);
  return pages;
});

function prevPage() {
  if (page.value > 1) fetchOrders(page.value - 1);
}

function nextPage() {
  if (page.value < totalPages.value) fetchOrders(page.value + 1);
}

function switchTab(status) {
  activeStatus.value = status;
  page.value = 1;
  fetchOrders(1);
}

async function fetchOrders(p = 1) {
  page.value = p;
  try {
    const data = await getOrders({
      page: p,
      size: size.value,
      status: activeStatus.value || undefined,
      keyword: searchKeyword.value || undefined,
    });
    orders.value = data?.list || [];
    total.value = data?.total || 0;
  } catch (err) {
    console.error(err);
  }
}

async function handleCancel(id) {
  try {
    await cancelOrder(id);
    ElMessage.success('订单已取消');
    fetchOrders(page.value);
  } catch (err) {
    console.error(err);
  }
}

async function handleConfirm(id) {
  try {
    await confirmOrder(id);
    ElMessage.success('确认收货成功');
    fetchOrders(page.value);
  } catch (err) {
    console.error(err);
  }
}

onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.order-search {
  display: flex;
  gap: 10px;
  align-items: center;
}

.ord {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  margin-bottom: 14px;
  overflow: hidden;
}

.ord .oh {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 13px 18px;
  background: var(--bg);
  border-bottom: 1px solid var(--line);
  font-size: 12.5px;
  color: var(--ink-3);
}

.ord .oh .no {
  color: var(--ink-2);
}

.ord .oh .shop {
  font-weight: 700;
  color: var(--ink);
  font-size: 13px;
}

.ord .oh .status {
  margin-left: auto;
  font-weight: 700;
  font-size: 13.5px;
}

.ord .ob {
  display: grid;
  grid-template-columns: 1fr 160px;
}

.ord .goods {
  padding: 6px 18px;
}

.ord .gi {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid var(--bg);
}

.ord .gi:last-child {
  border-bottom: 0;
}

.ord .gi .ph-img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  flex-shrink: 0;
  object-fit: cover;
  background: var(--bg-2);
}

.ord .gi .nm {
  font-size: 13.5px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ord .gi .spec {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 5px;
}

.ord .gi .pq {
  text-align: right;
  font-size: 13px;
  color: var(--ink-2);
  flex-shrink: 0;
}

.ord .side {
  border-left: 1px solid var(--line);
  padding: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 9px;
}

.ord .side .amt {
  font-size: 12.5px;
  color: var(--ink-3);
  text-align: center;
}

.ord .side .amt b {
  color: var(--price);
  font-size: 18px;
  display: block;
  margin-top: 2px;
}

.ord .side .btn {
  width: 118px;
}

.status.wait-pay {
  color: var(--price);
}

.status.wait-send {
  color: var(--warn);
}

.status.wait-recv {
  color: var(--info);
}

.status.done {
  color: var(--success);
}

.status.closed {
  color: var(--ink-3);
}

.pager span.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
