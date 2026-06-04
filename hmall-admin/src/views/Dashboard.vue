<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>数据看板</h1>
        <p>欢迎回来，管理员 · 今天是 {{ today }}</p>
      </div>
      <div class="acts">
        <el-select v-model="dateRange" style="width: 130px" size="small">
          <el-option label="今日" value="today" />
          <el-option label="近 7 天" value="week" />
          <el-option label="近 30 天" value="month" />
        </el-select>
        <el-button size="small">导出报表</el-button>
        <el-button type="primary" size="small" @click="$router.push('/items/add')"
          >＋ 发布商品</el-button
        >
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="stat-row">
      <div class="stat">
        <div class="ic" style="background: linear-gradient(135deg, #ff7a45, var(--brand))">💰</div>
        <div class="lab">今日成交额</div>
        <div class="val"><small>¥</small>128,640</div>
        <div class="delta up">▲ 12.4% <span style="color: var(--ink-3)">较昨日</span></div>
      </div>
      <div class="stat">
        <div class="ic" style="background: linear-gradient(135deg, #3b82f6, #1257c4)">▤</div>
        <div class="lab">今日订单数</div>
        <div class="val">1,286</div>
        <div class="delta up">▲ 8.2% <span style="color: var(--ink-3)">较昨日</span></div>
      </div>
      <div class="stat">
        <div class="ic" style="background: linear-gradient(135deg, #10b981, #137a40)">👥</div>
        <div class="lab">今日新增用户</div>
        <div class="val">486</div>
        <div class="delta up">▲ 5.6% <span style="color: var(--ink-3)">较昨日</span></div>
      </div>
      <div class="stat">
        <div class="ic" style="background: linear-gradient(135deg, #f59e0b, var(--gold))">👁</div>
        <div class="lab">今日访客量</div>
        <div class="val">42,180</div>
        <div class="delta down">▼ 2.1% <span style="color: var(--ink-3)">较昨日</span></div>
      </div>
    </div>

    <!-- 销量趋势 + 分类占比 -->
    <div class="dash-grid">
      <div class="acard">
        <div class="ah">
          <h3>销售趋势 <span class="sub">近 7 日成交额（元）</span></h3>
          <div style="display: flex; gap: 8px">
            <span class="chip on" style="padding: 5px 12px; font-size: 12px">成交额</span>
            <span class="chip" style="padding: 5px 12px; font-size: 12px">订单数</span>
          </div>
        </div>
        <div class="ab">
          <div ref="chartRef" style="width: 100%; height: 200px"></div>
        </div>
      </div>

      <div class="acard">
        <div class="ah"><h3>品类销售占比</h3></div>
        <div class="ab" style="display: flex; align-items: center; gap: 22px">
          <div
            class="donut"
            style="
              background: conic-gradient(
                #ff4d2e 0 32%,
                #3b82f6 32% 54%,
                #10b981 54% 72%,
                #f59e0b 72% 86%,
                #b79cc4 86% 100%
              );
              position: relative;
            "
          >
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
                <div style="font-size: 20px; font-weight: 900">2.4万</div>
                <div style="font-size: 10px; color: var(--ink-3)">在售SKU</div>
              </div>
            </div>
          </div>
          <div class="legend" style="flex: 1">
            <div class="l">
              <span class="sw" style="background: #ff4d2e"></span><span class="nm">手机数码</span
              ><span class="v">32%</span>
            </div>
            <div class="l">
              <span class="sw" style="background: #3b82f6"></span><span class="nm">家用电器</span
              ><span class="v">22%</span>
            </div>
            <div class="l">
              <span class="sw" style="background: #10b981"></span><span class="nm">食品生鲜</span
              ><span class="v">18%</span>
            </div>
            <div class="l">
              <span class="sw" style="background: #f59e0b"></span><span class="nm">服饰美妆</span
              ><span class="v">14%</span>
            </div>
            <div class="l">
              <span class="sw" style="background: #b79cc4"></span><span class="nm">其他品类</span
              ><span class="v">14%</span>
            </div>
          </div>
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
        <div class="ab rank">
          <div v-for="(p, i) in topProducts" :key="p.id" class="r">
            <span class="no">{{ i + 1 }}</span>
            <div
              class="ph"
              :class="`s${(p.id % 8) + 1}`"
              style="width: 38px; height: 38px; border-radius: 8px; flex-shrink: 0"
            >
              <div class="shape round"></div>
            </div>
            <span class="nm">{{ p.name }}</span>
            <span class="v">{{ formatSales(p.sales) }}<small> 销量</small></span>
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
            <b style="color: var(--warn); font-size: 20px">28</b>
          </router-link>
          <router-link class="qa" style="margin-bottom: 10px" to="/orders">
            <span class="d" style="background: var(--danger)">↺</span>
            <div style="flex: 1">
              <div>退款/售后申请</div>
              <span style="font-size: 11.5px; font-weight: 400; color: var(--ink-3)">待审核</span>
            </div>
            <b style="color: var(--danger); font-size: 20px">6</b>
          </router-link>
          <router-link class="qa" style="margin-bottom: 10px" to="/reviews">
            <span class="d" style="background: var(--info)">★</span>
            <div style="flex: 1">
              <div>待审核评价</div>
              <span style="font-size: 11.5px; font-weight: 400; color: var(--ink-3)"
                >含 2 条疑似违规</span
              >
            </div>
            <b style="color: var(--info); font-size: 20px">14</b>
          </router-link>
          <router-link class="qa" to="/items">
            <span class="d" style="background: #b79cc4">📦</span>
            <div style="flex: 1">
              <div>库存预警商品</div>
              <span style="font-size: 11.5px; font-weight: 400; color: var(--ink-3)"
                >库存低于 10 件</span
              >
            </div>
            <b style="color: #9d7fb0; font-size: 20px">9</b>
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
      <table class="atable">
        <thead>
          <tr>
            <th>订单号</th>
            <th>客户</th>
            <th>商品</th>
            <th>金额</th>
            <th>支付方式</th>
            <th>下单时间</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in recentOrders" :key="o[0]">
            <td class="mono" style="font-size: 12px">{{ o[0] }}</td>
            <td>
              <span class="u-cell">
                <span class="u-av" :style="`background:${o[2]}`">{{ o[1][0] }}</span>
                <span class="nm">{{ o[1] }}</span>
              </span>
            </td>
            <td style="color: var(--ink-3)">{{ o[3] }}</td>
            <td class="amount">{{ o[4] }}</td>
            <td>{{ o[5] }}</td>
            <td style="color: var(--ink-3)">{{ o[6] }}</td>
            <td>
              <span class="sdot" :class="o[8]">{{ o[7] }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
/*
 * 说明（重要）：本看板的运营统计（成交额/订单/新增用户/访客/销售趋势/品类占比/
 * 热销 TOP/待办/最新订单）所需的后端统计接口当前【尚未提供】，详见
 * docs/backend-api.md「B4 全新领域 - 数据看板」。
 * 本页为高保真原型的【视觉对齐 + 演示数据】实现，运营数据为代表性示例，
 * 待后端统计接口接入后再替换为真实数据；此为本 PR 已知且已披露的范围限制，
 * 不属于"破坏既有 API/数据逻辑"（main 版本同样不含任何后端数据调用）。
 */
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';

const dateRange = ref('today');
const today = ref('');
const chartRef = ref(null);
const topProducts = ref([]);

const recentOrders = [
  [
    '202605281588',
    '林晓',
    '#88A6B8',
    'TWS蓝牙耳机 等2件',
    '¥817',
    '微信',
    '14:32',
    '待付款',
    'orange',
  ],
  ['202605281587', '陈一鸣', '#DE9696', '空气炸锅6L', '¥269', '支付宝', '14:28', '待发货', 'blue'],
  [
    '202605281586',
    '王悦',
    '#B0BE92',
    '每日坚果礼盒 等3件',
    '¥356',
    '余额',
    '14:15',
    '已发货',
    'blue',
  ],
  [
    '202605281585',
    '刘洋',
    '#C9A66B',
    '办公椅 等2件',
    '¥1,198',
    '银行卡',
    '13:58',
    '已完成',
    'green',
  ],
  ['202605281584', '赵敏', '#B79CC4', '烟酰胺精华', '¥129', '微信', '13:42', '已完成', 'green'],
  ['202605281583', '孙浩', '#7FB6A6', '登山徒步杖', '¥129', '支付宝', '13:30', '已取消', 'gray'],
];

function formatSales(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return n;
}

onMounted(() => {
  const d = new Date();
  today.value = `${d.getFullYear()} 年 ${d.getMonth() + 1} 月 ${d.getDate()} 日，星期${['日', '一', '二', '三', '四', '五', '六'][d.getDay()]}`;

  // Chart
  const chart = echarts.init(chartRef.value);
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 0, right: 0, top: 10, bottom: 20 },
    xAxis: {
      type: 'category',
      data: ['05-22', '05-23', '05-24', '05-25', '05-26', '05-27', '今日'],
      axisLine: { lineStyle: { color: '#E8EAED' } },
      axisLabel: { color: '#938A83', fontSize: 11 },
    },
    yAxis: { show: false },
    series: [
      {
        type: 'bar',
        data: [98000, 112000, 86000, 135000, 121000, 104000, 129000],
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

  // Mock top products
  topProducts.value = [
    { id: 1, name: 'TWS蓝牙耳机 Pro', sales: 12450 },
    { id: 2, name: '空气炸锅 6L 智能款', sales: 8320 },
    { id: 3, name: '每日坚果礼盒 750g', sales: 7650 },
    { id: 4, name: '烟酰胺焕亮精华液', sales: 5420 },
    { id: 5, name: '办公人体工学椅', sales: 3890 },
  ];
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
.stat .delta {
  font-size: 12px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 5px;
}
.stat .delta.up {
  color: var(--success);
}
.stat .delta.down {
  color: var(--danger);
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
.sdot.green {
  color: var(--success);
}
.sdot.orange {
  color: var(--warn);
}
.sdot.blue {
  color: var(--info);
}
.sdot.red {
  color: var(--danger);
}
.sdot.gray {
  color: var(--ink-3);
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
