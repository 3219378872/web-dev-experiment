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
        <a class="btn btn-ghost">导出订单</a>
        <a class="btn btn-ghost">批量发货</a>
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
        <input
          v-model="searchId"
          class="input"
          placeholder="输入订单号"
          style="width: 150px"
          @keyup.enter="fetch"
        />
      </div>
      <div class="grow" />
      <a class="btn btn-primary btn-sm" @click="fetch">查询</a>
      <a class="btn btn-ghost btn-sm" @click="resetFilter">重置</a>
    </div>

    <div class="acard">
      <table class="atable">
        <thead>
          <tr>
            <th style="width: 30px"><span class="checkbox"></span></th>
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
            <td><span class="checkbox"></span></td>
            <td class="mono" style="font-size: 11.5px">{{ row.id }}</td>
            <td>
              <span class="u-cell">
                <span class="u-av" :style="`background:${userColor(customerName(row))}`">{{
                  customerName(row)[0]
                }}</span>
                <span class="nm">{{ customerName(row) }}</span>
              </span>
            </td>
            <td class="dim">{{ row.goodsText || '-' }}</td>
            <td class="amount">&yen;{{ (row.totalFee / 100).toFixed(2) }}</td>
            <td>{{ payLabel(row.paymentType) }}</td>
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
        <div v-if="totalPages > 1" class="pgs">
          <a :class="{ disabled: page === 1 }" @click="prevPage">‹</a>
          <a v-for="p in pageRange" :key="p" :class="{ on: page === p }" @click="fetch(p)">{{
            p
          }}</a>
          <a :class="{ disabled: page >= totalPages }" @click="nextPage">›</a>
        </div>
      </div>
    </div>

    <el-dialog v-model="shipVisible" title="填写物流单号">
      <input v-model="trackingNumber" class="input" placeholder="物流单号" />
      <template #footer>
        <button class="btn btn-ghost" @click="shipVisible = false">取消</button>
        <button class="btn btn-primary" @click="doShip">确认发货</button>
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

function statusText(s) {
  return statusMap[s]?.text || '-';
}
function statusClass(s) {
  return statusMap[s]?.cls || 'gray';
}
// OrderVO.paymentType：1 支付宝 / 2 微信 / 3 余额
const payLabel = (t) => ({ 1: '支付宝', 2: '微信', 3: '余额' })[t] || '-';
function customerName(o) {
  return o.userName || (o.userId != null ? `用户${o.userId}` : '-');
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
  activeTab.value = 'all';
  fetch(1);
}

async function fetch(p = 1) {
  page.value = p;
  try {
    const params = { page: p, size: size.value };
    if (searchId.value) params.orderId = searchId.value;
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

const totalPages = computed(() => Math.ceil(total.value / size.value) || 1);
const pageRange = computed(() => {
  const pages = [];
  for (let i = 1; i <= totalPages.value; i++) pages.push(i);
  return pages;
});
function prevPage() {
  if (page.value > 1) fetch(page.value - 1);
}
function nextPage() {
  if (page.value < totalPages.value) fetch(page.value + 1);
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
  gap: 2px;
  border-bottom: 1px solid var(--admin-line);
}
.tabs button {
  background: transparent;
  border: 0;
  padding: 13px 20px;
  font-size: 14px;
  color: var(--ink-2);
  cursor: pointer;
  position: relative;
  font-family: inherit;
  font-weight: 500;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}
.tabs button.active {
  color: var(--brand);
  font-weight: 700;
  border-bottom-color: var(--brand);
}
.tabs button.active::after {
  display: none;
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

/* align filter inputs/select with prototype sizing */
.adm-filter .input,
.adm-filter .select {
  height: 36px;
  padding: 8px 12px;
  font-size: 13px;
}

/* checkbox sizing to match prototype 18px */
.adm-filter .el-checkbox {
  --el-checkbox-size: 18px;
}

/* table cell vertical padding to match prototype 13px */
.atable tbody td {
  padding-top: 13px;
  padding-bottom: 13px;
}

/* status dot sizing */
.sdot::before {
  width: 7px;
  height: 7px;
}

/* action links sizing */
.atable .actions .lk {
  font-size: 12.5px;
}

/* pager button sizing */
.adm-pager .pgs {
  display: flex;
  gap: 6px;
}
.adm-pager .pgs a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  font-size: 12.5px;
  border: 1px solid var(--admin-line);
  border-radius: 7px;
  background: #fff;
  color: var(--ink-2);
  cursor: pointer;
}
.adm-pager .pgs a.on {
  background: var(--brand);
  border-color: var(--brand);
  color: #fff;
  font-weight: 700;
}

/* avatar sizing */
.u-av {
  width: 36px;
  height: 36px;
  font-size: 13px;
}

/* amount color */
.amount {
  font-weight: 700;
  color: var(--price);
  font-variant-numeric: tabular-nums;
}

/* dim color */
.dim {
  color: var(--ink-3);
}

/* user cell gap */
.u-cell {
  display: flex;
  align-items: center;
  gap: 11px;
}
.u-cell .nm {
  font-weight: 600;
}
</style>
