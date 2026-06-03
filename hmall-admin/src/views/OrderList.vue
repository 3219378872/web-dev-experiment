<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>订单管理</h1>
        <p>
          今日新增 {{ todayCount }} 单 · 待发货 {{ pendingShipCount }} · 待处理售后
          {{ refundCount }}
        </p>
      </div>
      <div class="acts">
        <el-button size="small">导出订单</el-button>
        <el-button size="small">批量发货</el-button>
      </div>
    </div>

    <div class="acard" style="margin-bottom: 16px">
      <div class="tabs" style="padding: 0 16px">
        <button
          v-for="t in tabs"
          :key="t.value"
          :class="{ active: activeTab === t.value }"
          @click="switchTab(t.value)"
        >
          {{ t.label }}
          <span v-if="t.badge !== undefined" :style="t.badgeStyle">({{ t.badge }})</span>
        </button>
      </div>
    </div>

    <div class="adm-filter">
      <div class="field-inline">
        订单号
        <el-input
          v-model="searchId"
          placeholder="输入订单号"
          style="width: 180px"
          @keyup.enter="fetch"
        />
      </div>
      <div class="field-inline">
        客户
        <el-input
          v-model="searchUser"
          placeholder="用户名 / 手机"
          style="width: 160px"
          @keyup.enter="fetch"
        />
      </div>
      <div class="field-inline">
        支付方式
        <el-select
          v-model="searchPay"
          placeholder="全部"
          clearable
          style="width: 120px"
          @change="fetch"
        >
          <el-option label="全部" value="" />
          <el-option label="微信" value="微信" />
          <el-option label="支付宝" value="支付宝" />
          <el-option label="余额" value="余额" />
        </el-select>
      </div>
      <div class="field-inline">
        下单时间
        <el-input
          v-model="searchDate"
          placeholder="2026-05-01 ~ 2026-05-28"
          style="width: 200px"
          @keyup.enter="fetch"
        />
      </div>
      <div class="grow" />
      <el-button type="primary" size="small" @click="fetch">查询</el-button>
      <el-button size="small" @click="resetFilter">重置</el-button>
    </div>

    <div class="acard">
      <table class="atable">
        <thead>
          <tr>
            <th style="width: 30px"><el-checkbox v-model="allChecked" /></th>
            <th>订单号</th>
            <th>客户</th>
            <th>商品</th>
            <th>金额</th>
            <th>支付方式</th>
            <th>下单时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in orders" :key="row.id">
            <td><el-checkbox v-model="row.checked" /></td>
            <td class="mono" style="font-size: 11.5px">{{ row.id }}</td>
            <td>
              <span class="u-cell">
                <span class="u-av" :style="`background:${userColor(row.userName)}`">{{
                  (row.userName || '?')[0]
                }}</span>
                <span class="nm">{{ row.userName || '-' }}</span>
              </span>
            </td>
            <td class="dim">{{ row.goodsText || '-' }}</td>
            <td class="amount">&yen;{{ (row.totalFee / 100).toFixed(2) }}</td>
            <td>{{ row.payType || '-' }}</td>
            <td class="dim">{{ (row.createTime || '').slice(5, 16) }}</td>
            <td>
              <span class="sdot" :class="statusClass(row.status)">{{
                statusText(row.status)
              }}</span>
            </td>
            <td class="actions">
              <span class="lk" @click="$router.push(`/orders/${row.id}`)">详情</span>
              <span v-if="row.status === 2" class="lk" @click="showShip(row)">发货</span>
              <span v-if="row.status === 3" class="lk" @click="$router.push(`/orders/${row.id}`)"
                >物流</span
              >
              <span class="lk" @click="updateStatus(row, 5)">取消</span>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="adm-pager">
        <span>共 {{ total.toLocaleString() }} 条 · 每页 {{ size }} 条</span>
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="(p) => fetch(p)"
        />
      </div>
    </div>

    <el-dialog v-model="shipVisible" title="填写物流单号">
      <el-input v-model="trackingNumber" placeholder="物流单号" />
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" @click="doShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { getOrders, shipOrder, updateOrderStatus } from '@/api/order';
import { ElMessage } from 'element-plus';

const orders = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const searchId = ref('');
const searchUser = ref('');
const searchPay = ref('');
const searchDate = ref('');
const activeTab = ref('all');
const shipVisible = ref(false);
const trackingNumber = ref('');
const currentOrder = ref(null);

const statusMap = {
  1: { text: '待付款', cls: 'orange' },
  2: { text: '待发货', cls: 'orange' },
  3: { text: '已发货', cls: 'blue' },
  4: { text: '已完成', cls: 'green' },
  5: { text: '已关闭', cls: 'gray' },
  6: { text: '退款中', cls: 'red' },
};

const todayCount = computed(() => orders.value.length);
const pendingShipCount = computed(() => orders.value.filter((o) => o.status === 2).length);
const refundCount = computed(() => orders.value.filter((o) => o.status === 6).length);

const tabs = computed(() => [
  { label: '全部订单', value: 'all', badge: total.value },
  {
    label: '待付款',
    value: 'pendingPay',
    badge: orders.value.filter((o) => o.status === 1).length,
  },
  {
    label: '待发货',
    value: 'pendingShip',
    badge: pendingShipCount.value,
    badgeStyle: 'color:var(--warn)',
  },
  { label: '待收货', value: 'shipped', badge: orders.value.filter((o) => o.status === 3).length },
  { label: '已完成', value: 'done', badge: orders.value.filter((o) => o.status === 4).length },
  {
    label: '退款/售后',
    value: 'refund',
    badge: refundCount.value,
    badgeStyle: 'color:var(--danger)',
  },
  { label: '已关闭', value: 'closed', badge: orders.value.filter((o) => o.status === 5).length },
]);

const allChecked = computed({
  get: () => orders.value.length > 0 && orders.value.every((i) => i.checked),
  set: (v) =>
    orders.value.forEach((i) => {
      i.checked = v;
    }),
});

function statusText(s) {
  return statusMap[s]?.text || '-';
}
function statusClass(s) {
  return statusMap[s]?.cls || 'gray';
}
function userColor(name) {
  const palette = [
    '#88A6B8',
    '#DE9696',
    '#B0BE92',
    '#C9A66B',
    '#B79CC4',
    '#7FB6A6',
    '#92ADBD',
    '#A7906B',
    '#9d7fb0',
    '#E9B775',
  ];
  let hash = 0;
  for (let i = 0; i < (name || '').length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return palette[Math.abs(hash) % palette.length];
}

function switchTab(val) {
  activeTab.value = val;
  fetch(1);
}

function resetFilter() {
  searchId.value = '';
  searchUser.value = '';
  searchPay.value = '';
  searchDate.value = '';
  activeTab.value = 'all';
  fetch(1);
}

async function fetch(p = 1) {
  page.value = p;
  try {
    const params = { page: p, size: size.value };
    if (activeTab.value === 'pendingPay') params.status = 1;
    if (activeTab.value === 'pendingShip') params.status = 2;
    if (activeTab.value === 'shipped') params.status = 3;
    if (activeTab.value === 'done') params.status = 4;
    if (activeTab.value === 'closed') params.status = 5;
    if (activeTab.value === 'refund') params.status = 6;
    const r = await getOrders(params);
    const list = (r.list || []).map((o) => ({
      ...o,
      checked: false,
      goodsText: o.goodsText || '-',
    }));
    orders.value = list;
    total.value = r.total || 0;
  } catch (err) {
    console.error(err);
  }
}

function showShip(row) {
  currentOrder.value = row;
  shipVisible.value = true;
}

async function doShip() {
  try {
    await shipOrder(currentOrder.value.id, trackingNumber.value);
    shipVisible.value = false;
    fetch();
    ElMessage.success('已发货');
  } catch (err) {
    console.error(err);
  }
}

async function updateStatus(row, status) {
  try {
    await updateOrderStatus(row.id, status);
    row.status = status;
    ElMessage.success('已更新');
  } catch (err) {
    console.error(err);
  }
}

fetch();
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 0;
  border-bottom: 1px solid var(--admin-line);
}
.tabs button {
  background: transparent;
  border: 0;
  padding: 14px 18px;
  font-size: 13.5px;
  color: var(--ink-2);
  cursor: pointer;
  position: relative;
  font-family: inherit;
}
.tabs button.active {
  color: var(--brand);
  font-weight: 700;
}
.tabs button.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 12px;
  right: 12px;
  height: 2px;
  background: var(--brand);
  border-radius: 2px 2px 0 0;
}
.tabs button:hover {
  color: var(--brand);
}

.grow {
  flex: 1;
}

.mono {
  font-family: var(--mono);
}
</style>
