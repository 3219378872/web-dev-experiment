<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>数据看板</h1>
        <p>欢迎回来，管理员 · 今天是 {{ today }}</p>
      </div>
      <div class="acts">
        <el-select v-model="dateRange" style="width: 130px" size="small" @change="loadTrend">
          <el-option label="近 7 天" value="7" />
          <el-option label="近 30 天" value="30" />
        </el-select>
        <el-button size="small" @click="exportReport">导出报表</el-button>
        <el-button type="primary" size="small" @click="$router.push('/items/add')"
          >＋ 发布商品</el-button
        >
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" style="text-align: center; padding: 60px 0; color: var(--ink-3)">
      加载中...
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" style="text-align: center; padding: 60px 0; color: var(--danger)">
      <p>{{ error }}</p>
      <el-button style="margin-top: 12px" @click="loadAll">重新加载</el-button>
    </div>

    <!-- 正常内容 -->
    <template v-else>
      <!-- 核心指标 -->
      <div class="stat-row">
        <div class="stat">
          <div class="ic" style="background: linear-gradient(135deg, #ff7a45, var(--brand))">
            💰
          </div>
          <div class="lab">累计成交额</div>
          <div class="val"><small>¥</small>{{ formatAmount(summary.totalAmount) }}</div>
        </div>
        <div class="stat">
          <div class="ic" style="background: linear-gradient(135deg, #3b82f6, #1257c4)">▤</div>
          <div class="lab">总订单数</div>
          <div class="val">{{ (summary.totalOrders || 0).toLocaleString() }}</div>
        </div>
        <div class="stat">
          <div class="ic" style="background: linear-gradient(135deg, #10b981, #137a40)">👥</div>
          <div class="lab">新增用户(近30天)</div>
          <div class="val">{{ (summary.newUsers || 0).toLocaleString() }}</div>
        </div>
        <div class="stat">
          <div class="ic" style="background: linear-gradient(135deg, #f59e0b, var(--gold))">👁</div>
          <div class="lab">访客量</div>
          <div class="val">{{ (summary.visitors || 0).toLocaleString() }}</div>
        </div>
      </div>

      <!-- 销量趋势 + 分类占比 -->
      <div class="dash-grid">
        <div class="acard">
          <div class="ah">
            <h3>
              销售趋势 <span class="sub">近 {{ dateRange }} 日成交额（元）</span>
            </h3>
          </div>
          <div class="ab">
            <div ref="chartRef" style="width: 100%; height: 200px"></div>
          </div>
        </div>

        <div class="acard">
          <div class="ah"><h3>品类销售占比</h3></div>
          <div class="ab" style="display: flex; align-items: center; gap: 22px">
            <div
              v-if="categoryShare.length === 0"
              style="color: var(--ink-3); font-size: 13px; padding: 20px"
            >
              暂无数据
            </div>
            <template v-else>
              <div class="donut" :style="{ background: categoryConic }">
                <div
                  style="
                    position: absolute;
                    inset: 30px;
                    background: #fff;
                    border-radius: 50%;
                    display: grid;
                    place-items: center;
                    text-align: center;
                  "
                >
                  <div>
                    <div style="font-size: 20px; font-weight: 900">{{ inStockSku }}</div>
                    <div style="font-size: 10px; color: var(--ink-3)">在售SKU</div>
                  </div>
                </div>
              </div>
              <div class="legend" style="flex: 1">
                <div v-for="(c, i) in categoryShare" :key="c.category" class="l">
                  <span
                    class="sw"
                    :style="{ background: categoryColors[i % categoryColors.length] }"
                  ></span>
                  <span class="nm">{{ c.category }}</span>
                  <span class="v">{{ (c.percentage || 0).toFixed(0) }}%</span>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- 热销榜 + 待办 -->
      <div class="dash-grid">
        <div class="acard">
          <div class="ah">
            <h3>热销商品 TOP 5</h3>
            <router-link class="lk" to="/items" style="font-size: 12.5px; color: var(--info)"
              >查看全部 ›</router-link
            >
          </div>
          <div v-if="topItems.length === 0" class="ab" style="color: var(--ink-3); font-size: 13px">
            暂无数据
          </div>
          <div v-else class="ab rank">
            <div v-for="(p, i) in topItems" :key="p.itemId || i" class="r">
              <span class="no">{{ i + 1 }}</span>
              <div
                class="ph"
                :class="`s${((p.itemId || i) % 8) + 1}`"
                style="width: 38px; height: 38px; border-radius: 8px; flex-shrink: 0"
              >
                <div class="shape round"></div>
              </div>
              <span class="nm">{{ p.name }}</span>
              <span class="v">{{ formatSales(p.sold) }}<small> 销量</small></span>
            </div>
          </div>
        </div>
        <div class="acard">
          <div class="ah"><h3>待办事项</h3></div>
          <div class="ab" style="display: flex; flex-direction: column; gap: 0">
            <router-link class="qa" style="margin-bottom: 10px" to="/orders">
              <span class="d" style="background: var(--warn)">▤</span>
              <div style="flex: 1">
                <div>待发货订单</div>
                <span style="font-size: 11.5px; font-weight: 400; color: var(--ink-3)"
                  >需尽快处理</span
                >
              </div>
              <b style="color: var(--warn); font-size: 20px">{{ todo.pendingShipment || 0 }}</b>
            </router-link>
            <router-link class="qa" style="margin-bottom: 10px" to="/orders">
              <span class="d" style="background: var(--danger)">↺</span>
              <div style="flex: 1">
                <div>退款/售后申请</div>
                <span style="font-size: 11.5px; font-weight: 400; color: var(--ink-3)">待审核</span>
              </div>
              <b style="color: var(--danger); font-size: 20px">{{ todo.pendingRefund || 0 }}</b>
            </router-link>
            <router-link class="qa" style="margin-bottom: 10px" to="/orders">
              <span class="d" style="background: var(--info)">★</span>
              <div style="flex: 1">
                <div>待付款订单</div>
                <span style="font-size: 11.5px; font-weight: 400; color: var(--ink-3)">待处理</span>
              </div>
              <b style="color: var(--info); font-size: 20px">{{ todo.pendingPayment || 0 }}</b>
            </router-link>
          </div>
        </div>
      </div>

      <!-- 最新订单 -->
      <div class="acard" style="margin-top: 16px">
        <div class="ah">
          <h3>最新订单</h3>
          <router-link class="lk" to="/orders" style="font-size: 12.5px; color: var(--info)"
            >查看全部 ›</router-link
          >
        </div>
        <div
          v-if="latestOrders.length === 0"
          class="ab"
          style="color: var(--ink-3); font-size: 13px"
        >
          暂无最新订单
        </div>
        <table v-else class="atable">
          <thead>
            <tr>
              <th>订单号</th>
              <th>金额</th>
              <th>支付方式</th>
              <th>下单时间</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in latestOrders" :key="o.id">
              <td class="mono" style="font-size: 12px">{{ o.id }}</td>
              <td class="amount">&yen;{{ (o.totalFee / 100).toFixed(2) }}</td>
              <td>{{ payLabel(o.paymentType) }}</td>
              <td style="color: var(--ink-3)">{{ o.createTime?.slice(0, 16) || '—' }}</td>
              <td>
                <span class="sdot" :class="statusClass(o.status)">{{ statusText(o.status) }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import * as echarts from 'echarts';
import {
  getSummary,
  getTrend,
  getCategoryShare,
  getTopItems,
  getTodo,
  getLatestOrders,
} from '@/api/dashboard';
import { ElMessage } from 'element-plus';

const dateRange = ref('7');
const today = ref('');
const chartRef = ref(null);
const loading = ref(true);
const error = ref('');

const summary = ref({});
const trend = ref([]);
const categoryShare = ref([]);
const topItems = ref([]);
const todo = ref({});
const latestOrders = ref([]);

const categoryColors = [
  '#ff4d2e',
  '#3b82f6',
  '#10b981',
  '#f59e0b',
  '#b79cc4',
  '#88a6b8',
  '#de9696',
  '#b0be92',
];

const categoryConic = computed(() => {
  if (!categoryShare.value.length) return 'none';
  let cone = 'conic-gradient(';
  let start = 0;
  categoryShare.value.forEach((c, i) => {
    const pct = c.percentage || 0;
    const end = start + pct;
    const color = categoryColors[i % categoryColors.length];
    if (i === 0) {
      cone += `${color} 0 ${end}%`;
    } else {
      cone += `, ${color} ${start}% ${end}%`;
    }
    start = end;
  });
  cone += ')';
  return cone;
});

const inStockSku = computed(() => {
  // 从品类占比推估总SKU数，无准确数据时展示合计品类数
  if (!categoryShare.value.length) return '0';
  return categoryShare.value.length + '+';
});

function formatAmount(val) {
  if (!val) return '0';
  return (val / 100).toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function formatSales(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return (n || 0).toLocaleString();
}

const payLabel = (t) => ({ 1: '支付宝', 2: '微信', 3: '余额' })[t] || '-';

const statusMap = {
  1: { text: '待付款', cls: 'orange' },
  2: { text: '待发货', cls: 'orange' },
  3: { text: '已发货', cls: 'blue' },
  4: { text: '已完成', cls: 'green' },
  5: { text: '已关闭', cls: 'gray' },
  6: { text: '退款中', cls: 'red' },
};

function statusText(s) {
  return statusMap[s]?.text || '-';
}
function statusClass(s) {
  return statusMap[s]?.cls || 'gray';
}

function initChart() {
  if (!chartRef.value) return;
  const chart = echarts.init(chartRef.value);
  const labels = trend.value.map((t) => t.date?.slice(5) || '');
  const amounts = trend.value.map((t) => (t.amount || 0) / 100);
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 0, right: 0, top: 10, bottom: 20 },
    xAxis: {
      type: 'category',
      data: labels.length ? labels : ['—'],
      axisLine: { lineStyle: { color: '#E8EAED' } },
      axisLabel: { color: '#938A83', fontSize: 11 },
    },
    yAxis: { show: false },
    series: [
      {
        type: 'bar',
        data: amounts.length ? amounts : [0],
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#FF7A45' },
            { offset: 1, color: '#FF4D2E' },
          ]),
        },
        barWidth: '60%',
      },
    ],
  });
}

async function loadSummary() {
  try {
    summary.value = await getSummary();
  } catch (err) {
    console.error('加载概览失败', err);
    summary.value = {};
  }
}

async function loadTrend() {
  try {
    trend.value = await getTrend(parseInt(dateRange.value));
    initChart();
  } catch (err) {
    console.error('加载趋势失败', err);
    trend.value = [];
  }
}

async function loadCategoryShare() {
  try {
    categoryShare.value = await getCategoryShare();
  } catch (err) {
    console.error('加载品类占比失败', err);
    categoryShare.value = [];
  }
}

async function loadTopItems() {
  try {
    topItems.value = await getTopItems();
  } catch (err) {
    console.error('加载热销商品失败', err);
    topItems.value = [];
  }
}

async function loadTodo() {
  try {
    todo.value = await getTodo();
  } catch (err) {
    console.error('加载待办失败', err);
    todo.value = {};
  }
}

async function loadLatestOrders() {
  try {
    latestOrders.value = await getLatestOrders();
  } catch (err) {
    console.error('加载最新订单失败', err);
    latestOrders.value = [];
  }
}

async function loadAll() {
  loading.value = true;
  error.value = '';
  try {
    await Promise.all([
      loadSummary(),
      loadTrend(),
      loadCategoryShare(),
      loadTopItems(),
      loadTodo(),
      loadLatestOrders(),
    ]);
  } catch (err) {
    error.value = '数据加载失败，请稍后重试';
    console.error(err);
  } finally {
    loading.value = false;
  }
}

function exportReport() {
  ElMessage.info('导出功能开发中');
}

onMounted(() => {
  const d = new Date();
  today.value = `${d.getFullYear()} 年 ${d.getMonth() + 1} 月 ${d.getDate()} 日，星期${['日', '一', '二', '三', '四', '五', '六'][d.getDay()]}`;
  loadAll();
});
</script>

<style scoped>
.dash-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 14px;
  padding: 20px;
  position: relative;
  overflow: hidden;
}
.stat .ic {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 21px;
  color: #fff;
  margin-bottom: 14px;
}
.stat .lab {
  font-size: 13px;
  color: var(--ink-3);
}
.stat .val {
  font-size: 28px;
  font-weight: 900;
  letter-spacing: -1px;
  margin-top: 4px;
}
.stat .val small {
  font-size: 14px;
  color: var(--ink-3);
  font-weight: 500;
}

.acard {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 14px;
}
.acard .ah {
  padding: 16px 20px;
  border-bottom: 1px solid var(--admin-line);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.acard .ah h3 {
  font-size: 15px;
  font-weight: 700;
}
.acard .ah h3 .sub {
  font-size: 12px;
  color: var(--ink-3);
  font-weight: 400;
  margin-left: 8px;
}
.acard .ab {
  padding: 20px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border: 1px solid var(--admin-line);
  border-radius: 999px;
  font-size: 13px;
  background: #fff;
  color: var(--ink-2);
  cursor: pointer;
}
.chip:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.chip.on {
  background: var(--brand);
  border-color: var(--brand);
  color: #fff;
  font-weight: 700;
}

.donut {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
}
.legend {
  display: flex;
  flex-direction: column;
  gap: 11px;
}
.legend .l {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 13px;
}
.legend .l .sw {
  width: 12px;
  height: 12px;
  border-radius: 4px;
}
.legend .l .v {
  margin-left: auto;
  font-weight: 700;
  color: var(--ink);
}
.legend .l .nm {
  color: var(--ink-2);
}

.rank .r {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 0;
  border-bottom: 1px solid var(--bg);
}
.rank .r:last-child {
  border-bottom: 0;
}
.rank .r .no {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: var(--bg-2);
  color: var(--ink-3);
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 900;
  flex-shrink: 0;
}
.rank .r:nth-child(1) .no {
  background: var(--brand);
  color: #fff;
}
.rank .r:nth-child(2) .no {
  background: #ff8a6b;
  color: #fff;
}
.rank .r:nth-child(3) .no {
  background: var(--gold);
  color: #fff;
}
.rank .r .nm {
  flex: 1;
  font-size: 13px;
  line-height: 1.4;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank .r .v {
  font-weight: 700;
  color: var(--ink);
}
.rank .r .v small {
  color: var(--ink-3);
  font-weight: 400;
  font-size: 11px;
}

.quick-acts {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.qa {
  border: 1px solid var(--admin-line);
  border-radius: 10px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 11px;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  color: inherit;
  transition: 0.14s;
}
.qa:hover {
  border-color: var(--brand);
  background: var(--brand-softer);
}
.qa .d {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 18px;
  flex-shrink: 0;
}

.atable {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.atable thead th {
  background: var(--admin-bg);
  color: var(--ink-3);
  font-weight: 700;
  text-align: left;
  padding: 12px 16px;
  font-size: 12px;
  white-space: nowrap;
  border-bottom: 1px solid var(--admin-line);
}
.atable tbody td {
  padding: 13px 16px;
  border-bottom: 1px solid var(--admin-line);
  vertical-align: middle;
}
.atable tbody tr:hover {
  background: #fbfbfc;
}
.atable tbody tr:last-child td {
  border-bottom: 0;
}

.u-av {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
}
.u-cell {
  display: flex;
  align-items: center;
  gap: 11px;
}
.u-cell .nm {
  font-weight: 600;
}

.sdot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
}
.sdot::before {
  content: '';
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}
.sdot.orange {
  color: var(--warn);
}
.sdot.blue {
  color: var(--info);
}
.sdot.green {
  color: var(--success);
}
.sdot.gray {
  color: var(--ink-3);
}
.sdot.red {
  color: var(--danger);
}

.amount {
  font-weight: 700;
  color: var(--price);
  font-variant-numeric: tabular-nums;
}

.lk {
  color: var(--info);
  cursor: pointer;
  font-size: 12.5px;
}
.lk:hover {
  text-decoration: underline;
}

.adm-ph {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20px;
}
.adm-ph h1 {
  font-size: 22px;
  font-weight: 900;
  letter-spacing: -0.5px;
}
.adm-ph p {
  color: var(--ink-3);
  font-size: 13px;
  margin-top: 4px;
}
.adm-ph .acts {
  display: flex;
  gap: 10px;
}
</style>
