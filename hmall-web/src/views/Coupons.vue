<template>
  <div class="wrap">
    <div class="crumb">
      <router-link to="/">首页</router-link><span class="s">/</span
      ><b style="color: var(--ink)">领券中心</b>
    </div>

    <div class="coupon-hero">
      <span class="b" style="width: 220px; height: 220px; right: -60px; top: -80px"></span>
      <span class="b" style="width: 120px; height: 120px; right: 200px; bottom: -60px"></span>
      <h1>🎟 领券中心</h1>
      <p>每日领券，叠加满减更省钱。新人专享 90 元礼包限时开抢！</p>
    </div>

    <div class="sec-head" style="margin-top: 6px">
      <div class="t">
        <span class="bar"></span>
        <h2>平台券 · 全场可用</h2>
      </div>
    </div>
    <div class="cgrid">
      <div v-for="c in platformCoupons" :key="c.id" :class="['cp', c.state === 'got' ? 'got' : '']">
        <div class="left">
          <div class="v">
            <template v-if="c.unit === '折'">{{ c.value }}<small>折</small></template>
            <template v-else><small>¥</small>{{ c.value }}</template>
          </div>
          <div class="cond">{{ c.condition }}</div>
        </div>
        <div class="right">
          <div v-if="c.state === 'use'" class="stamp">已领</div>
          <div class="nm">{{ c.name }}</div>
          <div class="scope">{{ c.scope }}</div>
          <div class="exp">有效期至 {{ c.expiry }}</div>
          <button v-if="c.state === 'got'" class="btn btn-ghost btn-sm" disabled>已抢光</button>
          <router-link v-else-if="c.state === 'use'" class="btn btn-outline btn-sm" to="/search"
            >去使用</router-link
          >
          <button v-else class="btn btn-hot btn-sm" @click="handleClaim(c.id)">立即领取</button>
        </div>
      </div>
    </div>

    <div class="sec-head">
      <div class="t">
        <span class="bar" style="background: var(--gold)"></span>
        <h2>品类券 · 指定分类</h2>
      </div>
    </div>
    <div class="cgrid">
      <div v-for="c in categoryCoupons" :key="c.id" :class="['cp', c.state === 'got' ? 'got' : '']">
        <div class="left">
          <div class="v">
            <template v-if="c.unit === '折'">{{ c.value }}<small>折</small></template>
            <template v-else><small>¥</small>{{ c.value }}</template>
          </div>
          <div class="cond">{{ c.condition }}</div>
        </div>
        <div class="right">
          <div v-if="c.state === 'use'" class="stamp">已领</div>
          <div class="nm">{{ c.name }}</div>
          <div class="scope">{{ c.scope }}</div>
          <div class="exp">有效期至 {{ c.expiry }}</div>
          <button v-if="c.state === 'got'" class="btn btn-ghost btn-sm" disabled>已抢光</button>
          <router-link v-else-if="c.state === 'use'" class="btn btn-outline btn-sm" to="/search"
            >去使用</router-link
          >
          <button v-else class="btn btn-hot btn-sm" @click="handleClaim(c.id)">立即领取</button>
        </div>
      </div>
    </div>

    <div class="sec-head">
      <div class="t">
        <span class="bar" style="background: var(--info)"></span>
        <h2>我的优惠券</h2>
      </div>
    </div>
    <div class="tabs mycp-tabs" style="border: 0">
      <button
        v-for="t in myTabs"
        :key="t.key"
        :class="{ active: myTab === t.key }"
        @click="myTab = t.key"
      >
        {{ t.label }} ({{ t.count }})
      </button>
    </div>
    <div class="cgrid">
      <div
        v-for="c in filteredMyCoupons"
        :key="c.id"
        :class="['cp', c.state === 'got' ? 'got' : '']"
      >
        <div class="left">
          <div class="v">
            <template v-if="c.unit === '折'">{{ c.value }}<small>折</small></template>
            <template v-else><small>¥</small>{{ c.value }}</template>
          </div>
          <div class="cond">{{ c.condition }}</div>
        </div>
        <div class="right">
          <div v-if="c.state === 'use'" class="stamp">已领</div>
          <div class="nm">{{ c.name }}</div>
          <div class="scope">{{ c.scope }}</div>
          <div class="exp">有效期至 {{ c.expiry }}</div>
          <button v-if="c.state === 'got'" class="btn btn-ghost btn-sm" disabled>已抢光</button>
          <router-link v-else-if="c.state === 'use'" class="btn btn-outline btn-sm" to="/search"
            >去使用</router-link
          >
          <button v-else class="btn btn-hot btn-sm" @click="handleClaim(c.id)">立即领取</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getCoupons, claimCoupon, getMyCoupons } from '@/api/order';
import { mapCoupon, mapMyCoupon } from '@/utils/couponStatus';

const platformCoupons = ref([]);
const categoryCoupons = ref([]);
const myCoupons = ref([]);
const myTab = ref('unused');

const stateByTab = { unused: 'use', used: 'used', expired: 'expired' };
const countByTab = (key) => myCoupons.value.filter((c) => c.state === stateByTab[key]).length;

const myTabs = computed(() => [
  { key: 'unused', label: '未使用', count: countByTab('unused') },
  { key: 'used', label: '已使用', count: countByTab('used') },
  { key: 'expired', label: '已过期', count: countByTab('expired') },
]);

const filteredMyCoupons = computed(() =>
  myCoupons.value.filter((c) => c.state === stateByTab[myTab.value])
);

/** 后端未单独建模"平台/品类"，依据券的适用范围/名称文案判定品类券 */
const CATEGORY_KEYWORDS = [
  '数码',
  '家电',
  '美妆',
  '服饰',
  '食品',
  '母婴',
  '运动',
  '家居',
  '品类',
  '指定',
];
function isCategoryCoupon(c) {
  const text = `${c.scope || ''}${c.name || ''}`;
  return CATEGORY_KEYWORDS.some((k) => text.includes(k));
}

async function handleClaim(id) {
  try {
    await claimCoupon(id);
    ElMessage.success('领取成功');
    // refresh lists
    await loadCoupons();
  } catch (err) {
    /* error handled by interceptor */
  }
}

async function loadCoupons() {
  try {
    const data = await getCoupons();
    const list = (data || []).map((c) => mapCoupon(c, ''));
    platformCoupons.value = list.filter((c) => !isCategoryCoupon(c));
    categoryCoupons.value = list.filter((c) => isCategoryCoupon(c));
  } catch (err) {
    // 接口失败时显示空态，不杜撰数据（错误已由拦截器提示）
    platformCoupons.value = [];
    categoryCoupons.value = [];
  }

  try {
    const data = await getMyCoupons();
    myCoupons.value = (data || []).map((c) => mapMyCoupon(c));
  } catch (err) {
    myCoupons.value = [];
  }
}

onMounted(loadCoupons);
</script>

<style scoped>
.coupon-hero {
  background: linear-gradient(120deg, #ff7a45, var(--brand) 55%, var(--brand-700));
  border-radius: var(--radius);
  color: #fff;
  padding: 30px 34px;
  margin: 14px 0;
  position: relative;
  overflow: hidden;
}
.coupon-hero .b {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}
.coupon-hero h1 {
  font-size: 30px;
  font-weight: 900;
}
.coupon-hero p {
  opacity: 0.92;
  margin-top: 6px;
}

.cgrid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.cp {
  display: flex;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--line);
  position: relative;
}
.cp .left {
  background: linear-gradient(135deg, #ff6a45, var(--brand-700));
  color: #fff;
  width: 130px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 18px 8px;
  position: relative;
}
.cp .left::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 0;
  bottom: 0;
  width: 12px;
  background: radial-gradient(circle at right, transparent 0, transparent 5px, #fff 5px);
  background-size: 12px 14px;
  background-repeat: repeat-y;
}
.cp .left .v {
  font-size: 34px;
  font-weight: 900;
  line-height: 1;
}
.cp .left .v small {
  font-size: 15px;
}
.cp .left .cond {
  font-size: 11px;
  opacity: 0.9;
  margin-top: 6px;
}
.cp .right {
  flex: 1;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
}
.cp .right .nm {
  font-size: 14px;
  font-weight: 700;
}
.cp .right .scope {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 4px;
}
.cp .right .exp {
  font-size: 11.5px;
  color: var(--ink-3);
  margin-top: auto;
}
.cp .right .btn {
  margin-top: 8px;
}
.cp.got .left {
  background: linear-gradient(135deg, #bbb, #999);
}
.cp .stamp {
  position: absolute;
  right: 14px;
  top: 14px;
  width: 48px;
  height: 48px;
  border: 2px solid var(--success);
  color: var(--success);
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 900;
  transform: rotate(-15deg);
  opacity: 0.85;
}

.mycp-tabs {
  margin: 10px 0 16px;
}

@media (max-width: 1024px) {
  .cgrid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 640px) {
  .cgrid {
    grid-template-columns: 1fr;
  }
}
</style>
