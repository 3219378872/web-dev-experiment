<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>订单详情</h1>
        <p>订单号 {{ order?.id || '-' }} · 下单于 {{ (order?.createTime || '').slice(0, 16) }}</p>
      </div>
      <div class="acts">
        <el-button size="small" @click="$router.push('/orders')">← 返回列表</el-button>
        <el-button size="small">打印小票</el-button>
        <el-button size="small">导出</el-button>
      </div>
    </div>

    <div v-if="order" class="od-state">
      <div>
        <h2>{{ stateTitle }}</h2>
        <p>{{ stateHint }}</p>
      </div>
      <div style="display: flex; gap: 10px">
        <el-button v-if="order.status === 2" type="primary" size="small" @click="showShip"
          >立即发货</el-button
        >
        <el-button size="small">修改地址</el-button>
        <el-button size="small">退款</el-button>
      </div>
    </div>

    <div class="od-grid">
      <div>
        <div class="acard" style="margin-bottom: 16px">
          <div class="ah">
            <h3>商品信息</h3>
            <span class="dim" style="font-size: 12px">好集食品旗舰店</span>
          </div>
          <table class="atable">
            <thead>
              <tr>
                <th>商品</th>
                <th>规格</th>
                <th>单价</th>
                <th>数量</th>
                <th>小计</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(g, idx) in goodsList" :key="idx">
                <td>
                  <span class="prod-cell">
                    <img v-if="g.image" :src="g.image" :alt="g.name" class="prod-img" />
                    <div v-else class="ph" :class="g.shade" :data-label="g.category">
                      <span class="glyph">{{ g.glyph }}</span>
                    </div>
                    <span class="nm">{{ g.name }}</span>
                  </span>
                </td>
                <td class="dim">{{ g.spec || '默认规格' }}</td>
                <td>¥{{ (g.price / 100).toFixed(2) }}</td>
                <td>×{{ g.num }}</td>
                <td class="amount">¥{{ ((g.price * g.num) / 100).toFixed(2) }}</td>
              </tr>
            </tbody>
          </table>
          <div class="od-total">
            <div class="row">
              <span class="dim">商品总额</span
              ><span>¥{{ (order?.totalFee / 100).toFixed(2) }}</span>
            </div>
            <div class="row"><span class="dim">运费</span><span>¥0.00</span></div>
            <div class="row">
              <span class="dim">优惠减免</span><span style="color: var(--price)">−¥0.00</span>
            </div>
            <div class="row total">
              <span>实付款</span>
              <span class="amount">¥{{ (order?.totalFee / 100).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="acard">
          <div class="ah">
            <h3>物流跟踪</h3>
            <span class="dim" style="font-size: 12px">顺丰速运 · SF1366028866018</span>
          </div>
          <div class="ab tl">
            <div class="ti on">
              <span class="dot" />
              <div>
                <div class="tx"><b>商品已在 杭州华东仓 完成打包，等待揽收</b></div>
                <div class="tm">2026-05-28 15:02 · 操作员 仓管03</div>
              </div>
            </div>
            <div class="ti">
              <span class="dot" />
              <div>
                <div class="tx">买家已付款</div>
                <div class="tm">2026-05-28 14:16</div>
              </div>
            </div>
            <div class="ti">
              <span class="dot" />
              <div>
                <div class="tx">买家提交订单</div>
                <div class="tm">2026-05-28 14:15</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <aside>
        <div class="acard" style="margin-bottom: 16px">
          <div class="ah"><h3>买家信息</h3></div>
          <div class="ab">
            <div class="info-row">
              <span class="k">买家</span>
              <span class="u-cell">
                <span
                  class="u-av"
                  style="width: 26px; height: 26px; font-size: 11px; background: #b0be92"
                  >{{ (order?.userName || '?')[0] }}</span
                >
                {{ order?.userName || '-' }}
              </span>
            </div>
            <div class="info-row">
              <span class="k">会员等级</span>
              <span class="tag tag-ghost">普通会员</span>
            </div>
            <div class="info-row">
              <span class="k">联系电话</span>
              {{ maskPhone(order?.userPhone) }}
            </div>
          </div>
        </div>

        <div class="acard" style="margin-bottom: 16px">
          <div class="ah"><h3>收货信息</h3></div>
          <div class="ab">
            <div class="info-row">
              <span class="k">收货人</span>{{ order?.receiverName || '-' }}
            </div>
            <div class="info-row">
              <span class="k">电话</span>{{ maskPhone(order?.receiverPhone) }}
            </div>
            <div class="info-row">
              <span class="k">地址</span><span>{{ order?.receiverAddress || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="acard">
          <div class="ah"><h3>订单信息</h3></div>
          <div class="ab">
            <div class="info-row">
              <span class="k">订单号</span
              ><span class="mono" style="font-size: 12px">{{ order?.id }}</span>
            </div>
            <div class="info-row"><span class="k">支付方式</span>{{ order?.payType || '-' }}</div>
            <div class="info-row">
              <span class="k">支付时间</span>{{ (order?.payTime || '').slice(0, 16) || '-' }}
            </div>
            <div class="info-row"><span class="k">配送方式</span>普通快递（免邮）</div>
            <div class="info-row"><span class="k">买家备注</span><span class="dim">无</span></div>
          </div>
        </div>
      </aside>
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
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getOrderById, shipOrder } from '@/api/order';
import { ElMessage } from 'element-plus';

const route = useRoute();
const order = ref(null);
const shipVisible = ref(false);
const trackingNumber = ref('');

const statusTitleMap = {
  1: '等待买家付款',
  2: '买家已付款，待发货',
  3: '已发货，等待买家收货',
  4: '订单已完成',
  5: '订单已关闭',
  6: '退款处理中',
};

const statusHintMap = {
  1: '买家尚未付款，订单将在 30 分钟后自动关闭',
  2: '请在 24 小时内安排发货，超时将影响店铺评分',
  3: '物流运输中，预计 2-3 天送达',
  4: '买家已确认收货，交易完成',
  5: '订单已取消或超时关闭',
  6: '买家申请退款，请尽快处理',
};

const stateTitle = computed(() => statusTitleMap[order.value?.status] || '-');
const stateHint = computed(() => statusHintMap[order.value?.status] || '-');

const goodsList = computed(() => {
  const o = order.value;
  if (!o) return [];
  if (o.details?.length) {
    return o.details.map((d, i) => ({
      name: d.name || '-',
      spec: d.spec || '默认规格',
      price: d.price || 0,
      num: d.num || 1,
      image: d.image || '',
      shade: d.shade || `s${(i % 8) + 1}`,
      glyph: d.glyph || '▦',
      category: d.category || '',
    }));
  }
  return [
    { name: o.goodsText || '-', spec: '默认规格', price: o.totalFee || 0, num: 1, image: '' },
  ];
});

function maskPhone(phone) {
  if (!phone) return '-';
  const s = String(phone);
  if (s.length === 11) return s.slice(0, 3) + '****' + s.slice(7);
  return s;
}

function showShip() {
  shipVisible.value = true;
}

async function doShip() {
  try {
    await shipOrder(order.value.id, trackingNumber.value);
    shipVisible.value = false;
    order.value.status = 3;
    ElMessage.success('已发货');
  } catch (err) {
    console.error(err);
  }
}

onMounted(async () => {
  try {
    order.value = await getOrderById(route.params.id);
  } catch (err) {
    console.error(err);
  }
});
</script>

<style scoped>
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
.od-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
  align-items: start;
}

/* 商品行缩略图色块（与原型 admin.css .prod-cell + 共享 .ph 一致） */
.atable .prod-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.atable .prod-cell .ph {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  flex-shrink: 0;
  position: relative;
  display: grid;
  place-items: center;
  overflow: hidden;
}
.atable .prod-cell .prod-img {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.atable .prod-cell .nm {
  font-weight: 500;
  line-height: 1.4;
  max-width: 280px;
}
.atable .prod-cell .ph::after {
  content: attr(data-label);
  position: absolute;
  bottom: 6px;
  left: 6px;
  font-size: 9px;
  color: rgba(255, 255, 255, 0.78);
  letter-spacing: 0.3px;
}
.atable .prod-cell .ph .glyph {
  font-size: 22px;
  opacity: 0.55;
  filter: saturate(0) brightness(0) invert(1);
}
.atable .prod-cell .ph.s1 {
  background: linear-gradient(135deg, #ec9b86, #e07c5e);
}
.atable .prod-cell .ph.s2 {
  background: linear-gradient(135deg, #edc079, #e0a24d);
}
.atable .prod-cell .ph.s3 {
  background: linear-gradient(135deg, #b0be92, #8fa06b);
}
.atable .prod-cell .ph.s4 {
  background: linear-gradient(135deg, #92adbd, #6c8da1);
}
.atable .prod-cell .ph.s5 {
  background: linear-gradient(135deg, #bfa6cc, #9d7fb0);
}
.atable .prod-cell .ph.s6 {
  background: linear-gradient(135deg, #de9696, #c97070);
}
.atable .prod-cell .ph.s7 {
  background: linear-gradient(135deg, #8cc0b0, #65a18f);
}
.atable .prod-cell .ph.s8 {
  background: linear-gradient(135deg, #d2b176, #b88f4a);
}
.od-state {
  background: linear-gradient(120deg, #ff7a45, var(--brand));
  color: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.od-state h2 {
  font-size: 20px;
  font-weight: 900;
}
.od-state p {
  opacity: 0.9;
  font-size: 13px;
  margin-top: 4px;
}

.od-total {
  padding: 16px 20px;
  border-top: 1px solid var(--admin-line);
  display: flex;
  flex-direction: column;
  gap: 7px;
  align-items: flex-end;
  font-size: 13px;
}
.od-total .row {
  display: flex;
  gap: 50px;
}
.od-total .row span:first-child {
  width: 90px;
  text-align: right;
}
.od-total .row span:last-child {
  width: 90px;
  text-align: right;
}
.od-total .row.total {
  font-size: 16px;
  margin-top: 4px;
}
.od-total .row.total .amount {
  font-size: 20px;
  color: var(--price);
}

.info-row {
  display: flex;
  padding: 9px 0;
  font-size: 13px;
}
.info-row .k {
  width: 90px;
  color: var(--ink-3);
  flex-shrink: 0;
}

.tl {
  padding-left: 6px;
}
.tl .ti {
  display: grid;
  grid-template-columns: 14px 1fr;
  gap: 14px;
  padding-bottom: 18px;
  position: relative;
}
.tl .ti:last-child {
  padding-bottom: 0;
}
.tl .ti .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--line-2);
  margin-top: 4px;
  z-index: 2;
}
.tl .ti.on .dot {
  background: var(--brand);
  box-shadow: 0 0 0 3px var(--brand-soft);
}
.tl .ti::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 13px;
  bottom: -4px;
  width: 2px;
  background: var(--line);
}
.tl .ti:last-child::before {
  display: none;
}
.tl .ti .tx {
  font-size: 12.5px;
}
.tl .ti .tm {
  font-size: 11px;
  color: var(--ink-3);
  margin-top: 2px;
}

.tag-ghost {
  display: inline-flex;
  align-items: center;
  padding: 2px 9px;
  border-radius: 6px;
  font-size: 11.5px;
  font-weight: 600;
  background: var(--admin-bg);
  color: var(--ink-2);
}

.dim {
  color: var(--ink-3);
}

.mono {
  font-family: var(--mono);
}
</style>
