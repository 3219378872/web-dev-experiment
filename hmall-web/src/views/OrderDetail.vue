<template>
  <div class="wrap">
    <div class="crumb">
      <router-link to="/">首页</router-link><span class="s">/</span>
      <router-link to="/orders">我的订单</router-link><span class="s">/</span>
      <b style="color: var(--ink)">订单详情</b>
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

      <main v-if="order">
        <!-- 状态条 -->
        <div class="od-status">
          <div>
            <h2>{{ statusTitle }}</h2>
            <p>{{ statusDesc }}</p>
          </div>
          <div class="acts">
            <button v-if="order.status === 1" class="btn solid" @click="handlePay">立即付款</button>
            <button v-if="order.status === 3" class="btn solid" @click="handleConfirm">
              确认收货
            </button>
            <button v-if="order.status === 1" class="btn" @click="handleCancel">取消订单</button>
            <button v-if="canRequestRefund(order.status)" class="btn" @click="handleRefund">
              申请售后
            </button>
          </div>
        </div>

        <!-- 物流时间轴 -->
        <div v-if="order.status === 3 || order.status === 4" class="track">
          <h3>物流跟踪</h3>
          <div v-if="logistics.length" class="tl">
            <div v-for="(log, idx) in logistics" :key="idx" class="ti" :class="{ on: idx === 0 }">
              <span class="dot"></span>
              <div class="txt">
                <b v-if="idx === 0">{{ log.text }}</b>
                <span v-else>{{ log.text }}</span>
                <div class="time">{{ log.time }}</div>
              </div>
            </div>
          </div>
          <div v-else class="muted" style="margin-top: 12px; font-size: 13px">暂无物流信息</div>
        </div>

        <!-- 收货信息 + 订单信息 -->
        <div class="od-block">
          <div class="od-info">
            <div class="ic">
              <div style="font-weight: 700; margin-bottom: 6px">收货信息</div>
              <div><span class="k">收货人：</span>{{ order.receiverName || '-' }}</div>
              <div>
                <span class="k">联系电话：</span
                >{{ maskPhone(order.receiverPhone || order.receiverMobile) }}
              </div>
              <div><span class="k">收货地址：</span>{{ fullAddress }}</div>
            </div>
            <div class="ic">
              <div style="font-weight: 700; margin-bottom: 6px">订单信息</div>
              <div>
                <span class="k">订单编号：</span>{{ order.id }}
                <a
                  class="dim"
                  style="color: var(--brand); font-size: 12px; cursor: pointer"
                  @click="copyId"
                  >复制</a
                >
              </div>
              <div><span class="k">下单时间：</span>{{ order.createTime?.slice(0, 19) }}</div>
              <div v-if="order.payTime">
                <span class="k">付款时间：</span>{{ order.payTime?.slice(0, 19) }}
              </div>
              <div><span class="k">支付方式：</span>{{ payTypeText }}</div>
              <div><span class="k">配送方式：</span>普通快递（免邮）</div>
            </div>
          </div>
        </div>

        <!-- 商品清单 -->
        <div class="od-block">
          <div class="bh">
            商品清单
            <span class="dim" style="font-weight: 400; font-size: 12.5px">好集自营</span>
          </div>
          <div>
            <div v-for="(item, idx) in orderItems" :key="idx" class="gi">
              <img class="ph-img" :src="item.image || '/placeholder.png'" :alt="item.name" />
              <div class="nm">
                {{ item.name }}
                <div class="spec">规格：{{ item.spec || '默认' }}</div>
              </div>
              <div class="p">¥{{ (item.price / 100).toFixed(2) }}</div>
              <div class="q">×{{ item.num }}</div>
              <div class="st">¥{{ ((item.price * item.num) / 100).toFixed(2) }}</div>
            </div>
          </div>
          <div class="amt-rows">
            <div class="r">
              <span>商品总额</span><span class="v">¥{{ (order.totalFee / 100).toFixed(2) }}</span>
            </div>
            <div class="r"><span>运费</span><span class="v">¥0.00</span></div>
            <div class="r">
              <span>满减优惠</span>
              <span class="v" style="color: var(--price)">−¥0.00</span>
            </div>
            <div class="r big">
              <span style="align-self: center; color: var(--ink)">实付款</span>
              <span class="v">¥{{ (order.totalFee / 100).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </main>

      <el-empty v-else description="加载中..." style="margin-top: 40px" />
    </div>
  </div>

  <!-- 支付弹窗 -->
  <el-dialog
    v-model="payDialogVisible"
    title="余额支付"
    width="380px"
    :close-on-click-modal="false"
  >
    <div class="pay-dialog-body">
      <div class="pay-amount">
        应付金额：<span class="price"
          >¥{{ order ? (order.totalFee / 100).toFixed(2) : '0.00' }}</span
        >
      </div>
      <div class="pay-channel">支付方式：余额支付</div>
      <el-input
        v-model="payPassword"
        type="password"
        placeholder="请输入支付密码"
        style="margin-top: 16px"
        show-password
        @keyup.enter="confirmPay"
      />
    </div>
    <template #footer>
      <el-button @click="payDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="payLoading" @click="confirmPay">确认支付</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import {
  getOrderById,
  cancelOrder,
  confirmOrder,
  refundOrder,
  createPayOrder,
  payOrderByBalance,
  getLogistics,
} from '@/api/order';
import { canRequestRefund } from '@/utils/orderStatus';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const order = ref(null);
const logistics = ref([]);

// 支付弹窗状态
const payDialogVisible = ref(false);
const payPassword = ref('');
const payLoading = ref(false);

const userName = computed(() => userStore.userInfo?.name || '用户');
const userInitial = computed(() => userName.value.charAt(0));

const statusTitle = computed(() => {
  const map = {
    1: '等待付款',
    2: '买家已付款，等待发货',
    3: '卖家已发货，待收货',
    4: '交易完成',
    5: '交易关闭',
    6: '退款处理中',
  };
  return map[order.value?.status] || '订单详情';
});

const statusDesc = computed(() => {
  const map = {
    1: '请在规定时间内完成支付，超时订单将自动关闭',
    2: '商家正在备货中，请耐心等待发货',
    3: '商品正在配送途中，收到商品请及时确认收货～',
    4: '感谢您的购买，欢迎再次光临',
    5: '订单已关闭，如有疑问请联系客服',
    6: '售后申请已提交，请等待商家审核',
  };
  return map[order.value?.status] || '';
});

const payTypeText = computed(() => {
  // 后端 OrderVO.paymentType：1 支付宝 / 2 微信 / 3 扣减余额
  const map = { 1: '支付宝', 2: '微信支付', 3: '余额支付' };
  return map[order.value?.paymentType] || '在线支付';
});

const fullAddress = computed(() => {
  const o = order.value;
  if (!o) return '-';
  return [o.province, o.city, o.district, o.detail].filter(Boolean).join(' ');
});

const orderItems = computed(() => {
  if (order.value?.details?.length) {
    return order.value.details.map((d) => ({
      name: d.name || '商品',
      price: d.price || 0,
      num: d.num || 1,
      image: d.image || '',
      spec: d.spec || '',
    }));
  }
  return [{ name: '商品', price: order.value?.totalFee || 0, num: 1, image: '', spec: '' }];
});

async function loadLogistics() {
  if (!order.value || (order.value.status !== 3 && order.value.status !== 4)) {
    logistics.value = [];
    return;
  }
  try {
    const traces = await getLogistics(order.value.id);
    logistics.value = (traces || []).map((t) => ({
      text: t.description || t.node || '',
      time: t.traceTime?.slice(0, 16) || '',
    }));
  } catch (err) {
    logistics.value = [];
  }
}

function maskPhone(phone) {
  if (!phone) return '';
  return String(phone).replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
}

async function fetchOrder() {
  try {
    const data = await getOrderById(route.params.id);
    if (data && data.id) {
      order.value = data;
      await loadLogistics();
    } else {
      ElMessage.error('订单不存在');
      router.push('/orders');
    }
  } catch (err) {
    console.error(err);
  }
}

function handlePay() {
  if (!order.value || order.value.status !== 1) return;
  payPassword.value = '';
  payDialogVisible.value = true;
}

async function confirmPay() {
  if (!payPassword.value.trim()) {
    ElMessage.warning('请输入支付密码');
    return;
  }
  payLoading.value = true;
  try {
    // 第一步：创建支付单
    const payOrderId = await createPayOrder({
      bizOrderNo: order.value.id,
      payChannelCode: 'balance',
      payType: 5,
      orderInfo: `订单${order.value.id}`,
    });
    // 第二步：执行余额支付
    await payOrderByBalance(payOrderId, payPassword.value);
    payDialogVisible.value = false;
    ElMessage.success('支付成功');
    await fetchOrder();
  } catch (err) {
    ElMessage.error('支付失败，请检查支付密码或余额是否充足');
    console.error(err);
  } finally {
    payLoading.value = false;
  }
}

async function handleCancel() {
  try {
    await cancelOrder(order.value.id);
    ElMessage.success('订单已取消');
    fetchOrder();
  } catch (err) {
    console.error(err);
  }
}

async function handleConfirm() {
  try {
    await confirmOrder(order.value.id);
    ElMessage.success('确认收货成功');
    fetchOrder();
  } catch (err) {
    console.error(err);
  }
}

async function handleRefund() {
  if (!order.value) return;
  try {
    await refundOrder(order.value.id);
    ElMessage.success('售后申请已提交');
    await fetchOrder();
  } catch (err) {
    console.error(err);
  }
}

function copyId() {
  if (!order.value?.id) return;
  navigator.clipboard?.writeText(String(order.value.id));
  ElMessage.success('订单号已复制');
}

onMounted(async () => {
  await fetchOrder();
  // 若携带 ?pay=1 且订单为待付款状态，自动弹出支付弹窗
  if (route.query.pay === '1' && order.value?.status === 1) {
    handlePay();
  }
});
</script>

<style scoped>
.od-status {
  background: linear-gradient(120deg, #ff7a45, var(--brand));
  border-radius: var(--radius);
  color: #fff;
  padding: 24px 28px;
  margin: 14px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.od-status h2 {
  font-size: 22px;
  font-weight: 900;
}

.od-status p {
  opacity: 0.92;
  font-size: 13.5px;
  margin-top: 5px;
}

.od-status .acts {
  display: flex;
  gap: 10px;
}

.od-status .acts .btn {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.35);
}

.od-status .acts .btn.solid {
  background: #fff;
  color: var(--brand);
  border: 0;
}

.track {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 24px 28px;
  margin-bottom: 16px;
}

.track h3 {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 6px;
}

.track h3::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: var(--brand);
  margin-right: 9px;
  vertical-align: -2px;
}

.tl {
  margin-top: 18px;
  padding-left: 8px;
}

.tl .ti {
  display: grid;
  grid-template-columns: 18px 1fr;
  gap: 16px;
  padding-bottom: 22px;
  position: relative;
}

.tl .ti:last-child {
  padding-bottom: 0;
}

.tl .ti .dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--line-2);
  margin-top: 4px;
  z-index: 2;
}

.tl .ti.on .dot {
  background: var(--brand);
  box-shadow: 0 0 0 4px var(--brand-softer);
}

.tl .ti::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 14px;
  bottom: -4px;
  width: 2px;
  background: var(--line);
}

.tl .ti:last-child::before {
  display: none;
}

.tl .ti .txt {
  font-size: 13.5px;
}

.tl .ti.on .txt b {
  color: var(--brand);
}

.tl .ti .txt b {
  font-weight: 700;
}

.tl .ti .time {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 3px;
}

.od-block {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  margin-bottom: 16px;
  overflow: hidden;
}

.od-block .bh {
  padding: 14px 22px;
  border-bottom: 1px solid var(--line);
  font-weight: 700;
  font-size: 15px;
}

.od-block .bh::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: var(--brand);
  margin-right: 9px;
  vertical-align: -2px;
}

.od-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
}

.od-info .ic {
  padding: 18px 22px;
  font-size: 13.5px;
  line-height: 1.9;
  border-right: 1px solid var(--line);
}

.od-info .ic:last-child {
  border-right: 0;
}

.od-info .ic .k {
  color: var(--ink-3);
  display: inline-block;
  width: 74px;
}

.gi {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 22px;
  border-bottom: 1px solid var(--bg);
}

.gi:last-child {
  border-bottom: 0;
}

.gi .ph-img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  flex-shrink: 0;
  object-fit: cover;
  background: var(--bg-2);
}

.gi .nm {
  font-size: 13.5px;
  flex: 1;
  line-height: 1.45;
}

.gi .spec {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 5px;
}

.gi .p {
  width: 90px;
  text-align: right;
  color: var(--ink-2);
}

.gi .q {
  width: 50px;
  text-align: center;
  color: var(--ink-3);
}

.gi .st {
  width: 90px;
  text-align: right;
  color: var(--price);
  font-weight: 700;
}

.amt-rows {
  padding: 14px 22px;
}

.amt-rows .r {
  display: flex;
  justify-content: flex-end;
  gap: 60px;
  padding: 6px 0;
  font-size: 13.5px;
  color: var(--ink-2);
}

.amt-rows .r .v {
  width: 110px;
  text-align: right;
}

.amt-rows .r.big {
  border-top: 1px solid var(--line);
  margin-top: 6px;
  padding-top: 14px;
}

.amt-rows .r.big .v {
  color: var(--price);
  font-weight: 900;
  font-size: 22px;
}

.pay-dialog-body {
  padding: 8px 0;
}

.pay-amount {
  font-size: 15px;
  color: var(--ink-2);
  margin-bottom: 8px;
}

.pay-amount .price {
  color: var(--price);
  font-size: 22px;
  font-weight: 900;
}

.pay-channel {
  font-size: 13.5px;
  color: var(--ink-3);
}
</style>
