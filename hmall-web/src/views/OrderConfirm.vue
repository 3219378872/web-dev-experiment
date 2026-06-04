<template>
  <div class="wrap">
    <div
      style="display: flex; align-items: center; justify-content: space-between; padding: 18px 0"
    >
      <div class="logo">
        <span class="mark">集</span>
        <span class="name">好集<small>HAOJI MALL</small></span>
      </div>
      <div class="steps">
        <div class="st done"><span class="n">✓</span>购物车</div>
        <div class="line done"></div>
        <div class="st on"><span class="n">2</span>确认订单</div>
        <div class="line"></div>
        <div class="st"><span class="n">3</span>付款</div>
        <div class="line"></div>
        <div class="st"><span class="n">4</span>完成</div>
      </div>
    </div>

    <div class="co-layout">
      <div>
        <!-- 地址 -->
        <div class="block">
          <div class="bh">
            <h3>收货地址</h3>
            <router-link class="more" to="/addresses">管理收货地址 ›</router-link>
          </div>
          <div class="bd">
            <div v-if="addresses.length" class="addr-list">
              <div
                v-for="addr in addresses"
                :key="addr.id"
                class="addr"
                :class="{ on: selectedAddress === addr.id }"
                @click="selectedAddress = addr.id"
              >
                <span v-if="selectedAddress === addr.id" class="ck">✓</span>
                <div class="who">
                  {{ addr.receiverName }}
                  <span class="tel">{{ maskPhone(addr.phone) }}</span>
                  <span v-if="addr.isDefault" class="tag tag-brand def">默认</span>
                </div>
                <div class="det">
                  {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
                </div>
              </div>
              <router-link class="addr add" to="/addresses">
                <div>
                  <div style="font-size: 26px">＋</div>
                  新增收货地址
                </div>
              </router-link>
            </div>
            <el-empty v-else description="暂无收货地址，请先添加" />
          </div>
        </div>

        <!-- 商品清单 -->
        <div class="block">
          <div class="bh">
            <h3>商品清单</h3>
            <span class="more">好集自营</span>
          </div>
          <div class="bd">
            <div v-if="cartStore.selectedItems.length">
              <div v-for="item in cartStore.selectedItems" :key="item.id" class="co-item">
                <img class="ph-img" :src="item.image || '/placeholder.png'" :alt="item.name" />
                <div class="nm">
                  {{ item.name }}
                  <div class="spec">规格：{{ item.spec || '默认' }} · 现货</div>
                </div>
                <div class="p">¥{{ (item.price / 100).toFixed(2) }}</div>
                <div class="q">×{{ item.num }}</div>
                <div class="st">¥{{ ((item.price * item.num) / 100).toFixed(2) }}</div>
              </div>
            </div>
            <el-empty v-else description="未选择商品" />
          </div>
        </div>

        <!-- 配送 + 发票 + 备注 -->
        <div class="block">
          <div class="bh"><h3>配送与发票</h3></div>
          <div class="bd" style="padding-top: 6px">
            <div class="opt-row">
              <span>配送方式</span>
              <span style="display: flex; gap: 10px">
                <span class="chip on">普通快递 免运费</span>
                <span class="chip">当日达 +¥15</span>
              </span>
            </div>
            <div class="opt-row">
              <span>配送时间</span>
              <select>
                <option>工作日、双休日与节假日均可送货</option>
                <option>仅工作日送货</option>
                <option>仅双休日、节假日送货</option>
              </select>
            </div>
            <div class="opt-row">
              <span>发票信息</span>
              <select>
                <option>电子普通发票 — 个人</option>
                <option>电子普通发票 — 企业</option>
                <option>增值税专用发票</option>
              </select>
            </div>
            <div class="opt-row">
              <span>订单备注</span>
              <input
                v-model="remark"
                placeholder="选填，给商家留言（如指定送货时间）"
                style="width: 300px"
              />
            </div>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="block">
          <div class="bh"><h3>支付方式</h3></div>
          <div class="bd">
            <div class="pay-opts">
              <div
                v-for="p in payOptions"
                :key="p.value"
                class="pay"
                :class="{ on: payType === p.value }"
                @click="payType = p.value"
              >
                <span class="ic" :style="{ background: p.color }">{{ p.icon }}</span>
                {{ p.label }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 价格汇总 -->
      <aside>
        <div class="block summary">
          <div class="bh"><h3>订单汇总</h3></div>
          <div class="bd">
            <div class="sum-row">
              <span>商品总额（{{ cartStore.totalCount }}件）</span>
              <span>¥{{ (cartStore.totalAmount / 100).toFixed(2) }}</span>
            </div>
            <div class="sum-row"><span>运费</span><span>¥0.00</span></div>
            <div class="sum-row"><span>满减优惠</span><span class="sum-discount">−¥0.00</span></div>
            <div class="sum-row"><span>优惠券</span><span class="sum-discount">−¥0.00</span></div>
            <div class="sum-row">
              <span>会员积分抵扣</span><span class="sum-discount">−¥0.00</span>
            </div>
            <div class="sum-row big">
              <span style="align-self: flex-end; color: var(--ink)">应付总额</span>
              <span class="v">
                <span style="font-size: 17px">¥</span>{{ (cartStore.totalAmount / 100).toFixed(2) }}
              </span>
            </div>
          </div>
        </div>
        <div class="submit-addr">
          寄送至：{{ selectedAddrText }}<br />
          收货人：{{ selectedAddrPerson }}
        </div>
        <button
          class="btn btn-hot btn-lg btn-block"
          style="padding: 16px"
          :disabled="!selectedAddress || !cartStore.selectedItems.length"
          @click="submitOrder"
        >
          提交订单
        </button>
        <p class="dim" style="font-size: 11.5px; text-align: center; margin-top: 10px">
          点击提交即表示同意《好集购物协议》
        </p>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import { ElMessage } from 'element-plus';
import { getAddresses } from '@/api/address';
import { createOrder } from '@/api/order';

const router = useRouter();
const cartStore = useCartStore();
const addresses = ref([]);
const selectedAddress = ref(null);
const remark = ref('');
const payType = ref(1);

// 后端 Order.paymentType：1 支付宝 / 2 微信 / 3 余额
const payOptions = [
  { value: 1, label: '支付宝', icon: '支', color: '#1677FF' },
  { value: 2, label: '微信支付', icon: '微', color: '#09BB07' },
  { value: 3, label: '余额支付', icon: '余', color: '#FF9D00' },
];

const selectedAddr = computed(() => addresses.value.find((a) => a.id === selectedAddress.value));

const selectedAddrText = computed(() => {
  const a = selectedAddr.value;
  if (!a) return '-';
  return `${a.province}${a.city}${a.district}${a.detail}`;
});

const selectedAddrPerson = computed(() => {
  const a = selectedAddr.value;
  if (!a) return '-';
  return `${a.receiverName} ${maskPhone(a.phone)}`;
});

function maskPhone(phone) {
  if (!phone) return '';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
}

onMounted(async () => {
  try {
    addresses.value = await getAddresses();
    if (addresses.value.length) {
      selectedAddress.value = addresses.value.find((a) => a.isDefault)?.id || addresses.value[0].id;
    }
  } catch (err) {
    console.error(err);
  }
  try {
    await cartStore.fetchCart();
  } catch (err) {
    console.error(err);
  }
});

async function submitOrder() {
  const details = cartStore.selectedItems.map((i) => ({
    itemId: i.itemId || i.id,
    num: i.num,
  }));
  try {
    await createOrder({ addressId: selectedAddress.value, paymentType: payType.value, details });
    ElMessage.success('下单成功');
    router.push('/orders');
  } catch (err) {
    console.error(err);
  }
}
</script>

<style scoped>
.co-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  margin: 14px 0 30px;
  align-items: start;
}

.block {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  margin-bottom: 16px;
  overflow: hidden;
}

.block .bh {
  padding: 15px 22px;
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: center;
  gap: 10px;
}

.block .bh h3 {
  font-size: 15px;
  font-weight: 700;
}

.block .bh h3::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: var(--brand);
  margin-right: 9px;
  vertical-align: -2px;
}

.block .bh .more {
  margin-left: auto;
  font-size: 12.5px;
  color: var(--ink-3);
}

.block .bh .more:hover {
  color: var(--brand);
}

.block .bd {
  padding: 18px 22px;
}

.addr-list {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.addr {
  border: 1.5px solid var(--line-2);
  border-radius: 10px;
  padding: 14px 16px;
  width: 300px;
  cursor: pointer;
  position: relative;
}

.addr.on {
  border-color: var(--brand);
  background: var(--brand-softer);
}

.addr.on::after {
  content: '';
  position: absolute;
  right: 0;
  bottom: 0;
  width: 0;
  height: 0;
  border: 16px solid transparent;
  border-right-color: var(--brand);
  border-bottom-color: var(--brand);
}

.addr.on .ck {
  position: absolute;
  right: 3px;
  bottom: 1px;
  color: #fff;
  font-size: 11px;
  z-index: 2;
}

.addr .who {
  font-weight: 700;
  font-size: 14px;
}

.addr .who .tel {
  color: var(--ink-2);
  font-weight: 400;
  margin-left: 10px;
}

.addr .who .def {
  margin-left: 8px;
}

.addr .det {
  font-size: 12.5px;
  color: var(--ink-2);
  margin-top: 7px;
  line-height: 1.5;
}

.addr.add {
  display: grid;
  place-items: center;
  color: var(--ink-3);
  border-style: dashed;
  text-align: center;
}

.co-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid var(--bg);
}

.co-item:last-child {
  border-bottom: 0;
}

.co-item .ph-img {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  flex-shrink: 0;
  object-fit: cover;
  background: var(--bg-2);
}

.co-item .nm {
  font-size: 13.5px;
  flex: 1;
  line-height: 1.4;
}

.co-item .spec {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 4px;
}

.co-item .p {
  width: 90px;
  text-align: right;
  color: var(--ink-2);
}

.co-item .q {
  width: 60px;
  text-align: center;
  color: var(--ink-3);
}

.co-item .st {
  width: 100px;
  text-align: right;
  color: var(--price);
  font-weight: 700;
}

.pay-opts {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.pay {
  border: 1.5px solid var(--line-2);
  border-radius: 10px;
  padding: 14px 22px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.pay.on {
  border-color: var(--brand);
  background: var(--brand-softer);
  color: var(--brand-700);
}

.pay .ic {
  width: 28px;
  height: 28px;
  border-radius: 7px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 15px;
}

.opt-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 0;
  border-bottom: 1px solid var(--bg);
  font-size: 13.5px;
}

.opt-row:last-child {
  border-bottom: 0;
}

.opt-row select,
.opt-row input {
  border: 1px solid var(--line-2);
  border-radius: 7px;
  padding: 7px 12px;
  font-size: 13px;
  outline: 0;
  font-family: inherit;
}

.summary {
  position: sticky;
  top: 100px;
}

.sum-row {
  display: flex;
  justify-content: space-between;
  padding: 9px 0;
  font-size: 13.5px;
  color: var(--ink-2);
}

.sum-row.big {
  border-top: 1px dashed var(--line-2);
  margin-top: 6px;
  padding-top: 14px;
}

.sum-row.big .v {
  color: var(--price);
  font-weight: 900;
  font-size: 26px;
  letter-spacing: -1px;
}

.sum-discount {
  color: var(--price);
}

.submit-addr {
  font-size: 12px;
  color: var(--ink-3);
  text-align: right;
  margin: 8px 0 14px;
  line-height: 1.6;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo .mark {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--brand);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 20px;
  font-weight: 900;
}

.logo .name {
  font-size: 20px;
  font-weight: 900;
  color: var(--ink);
}

.logo .name small {
  display: block;
  font-size: 10px;
  font-weight: 700;
  color: var(--ink-3);
  letter-spacing: 1px;
  margin-top: -2px;
}

.steps {
  display: flex;
  align-items: center;
  gap: 0;
}

.steps .st {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--ink-3);
}

.steps .st .n {
  width: 26px;
  height: 26px;
  border-radius: 999px;
  background: var(--bg-2);
  color: var(--ink-3);
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 700;
}

.steps .st.done .n,
.steps .st.on .n {
  background: var(--brand);
  color: #fff;
}

.steps .st.on {
  color: var(--ink);
  font-weight: 700;
}

.steps .line {
  width: 60px;
  height: 2px;
  background: var(--line-2);
  margin: 0 16px;
}

.steps .line.done {
  background: var(--brand);
}
</style>
