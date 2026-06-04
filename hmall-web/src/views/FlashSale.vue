<template>
  <div>
    <div class="fs-hero">
      <div class="wrap" style="text-align: center">
        <div class="lg">⚡ 限时<span>秒杀</span> FLASH SALE</div>
        <div class="sub">整点开抢 · 限量特价 · 抢完即止</div>
        <div class="fs-timeline">
          <div
            v-for="slot in timeSlots"
            :key="slot.time"
            :class="['fs-slot', slot.active ? 'on' : '']"
            @click="slot.onClick ? slot.onClick() : null"
          >
            <div class="t">{{ slot.time }}</div>
            <div class="s">{{ slot.status }}</div>
          </div>
        </div>
        <div class="cd-bar">
          <span style="opacity: 0.8">本场距结束</span>
          <b>{{ countdown.h }}</b
          ><i>:</i><b>{{ countdown.m }}</b
          ><i>:</i><b>{{ countdown.s }}</b>
        </div>
      </div>
    </div>

    <div class="fs-body">
      <div class="wrap">
        <div class="grid g5">
          <router-link v-for="p in flashItems" :key="p.id" class="fs-card" :to="`/item/${p.id}`">
            <div class="ph" :class="p.phStyle" :data-label="p.phLabel">
              <span class="glyph">{{ p.phGlyph }}</span>
            </div>
            <div class="b">
              <div class="nm">{{ p.name }}</div>
              <div class="pr">
                <span class="now">¥{{ priceYuan(p) }}</span>
              </div>
            </div>
            <button class="btn btn-hot btn-block" @click.prevent="handleBuy(p)">立即抢购</button>
          </router-link>
        </div>
        <div class="pager">
          <a
            v-for="n in totalPages"
            :key="n"
            :class="{ cur: page === n }"
            @click.prevent="page = n"
            >{{ n }}</a
          >
          <a v-if="page < totalPages" @click.prevent="page++">›</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getItems } from '@/api/item';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';

const cartStore = useCartStore();
const userStore = useUserStore();

const countdown = ref({ h: '01', m: '48', s: '13' });
const page = ref(1);
const size = ref(10);
const total = ref(0);
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));

const timeSlots = [
  { time: '12:00', status: '疯抢中', active: true },
  { time: '14:00', status: '即将开始', active: false },
  { time: '16:00', status: '即将开始', active: false },
  { time: '18:00', status: '敬请期待', active: false },
  { time: '20:00', status: '敬请期待', active: false },
  { time: '22:00', status: '敬请期待', active: false },
];

/** 按品类映射占位图标（当前后端商品未配真实图片，沿用全站占位视觉） */
const glyphMap = {
  手机数码: '▣',
  家用电器: '▤',
  服饰鞋包: '◈',
  美妆个护: '✿',
  食品生鲜: '◉',
  家居家装: '▦',
  母婴玩具: '❀',
  运动户外: '◐',
};

const flashItems = ref([]);

function mapItem(x) {
  return {
    id: x.id,
    name: x.name,
    price: x.price,
    phStyle: `s${(x.id % 8) + 1}`,
    phLabel: x.category || '好物',
    phGlyph: glyphMap[x.category] || '◆',
  };
}

async function loadFlash() {
  try {
    const data = await getItems({ page: page.value, size: size.value, sortBy: 'sold' });
    flashItems.value = (data?.list || []).map(mapItem);
    total.value = data?.total || flashItems.value.length;
  } catch (err) {
    flashItems.value = [];
  }
}

watch(page, loadFlash);

let timer = null;
function startCountdown() {
  let total = 1 * 3600 + 48 * 60 + 13;
  timer = setInterval(() => {
    total--;
    if (total <= 0) total = 2 * 3600;
    const h = Math.floor(total / 3600);
    const m = Math.floor((total % 3600) / 60);
    const s = total % 60;
    countdown.value = {
      h: String(h).padStart(2, '0'),
      m: String(m).padStart(2, '0'),
      s: String(s).padStart(2, '0'),
    };
  }, 1000);
}

// 无秒杀后端，展示真实商品价格（与加入购物车的成交价一致）
function priceYuan(p) {
  return Math.round((p.price || 0) / 100);
}

async function handleBuy(p) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }
  try {
    await cartStore.addItem({ itemId: p.id, num: 1 });
    ElMessage.success('已加入购物车');
  } catch (err) {
    /* 错误已由响应拦截器统一提示 */
  }
}

onMounted(() => {
  loadFlash();
  startCountdown();
  document.body.style.background = '#2A1410';
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  document.body.style.background = '';
});
</script>

<style scoped>
.fs-hero {
  background: radial-gradient(120% 100% at 50% 0, #5a271c, #2a1410);
  color: #fff;
  padding: 30px 0 0;
}
.fs-hero .lg {
  font-size: 46px;
  font-weight: 900;
  letter-spacing: -1px;
}
.fs-hero .lg span {
  color: var(--gold);
}
.fs-hero .sub {
  opacity: 0.8;
  margin-top: 6px;
}
.fs-timeline {
  display: flex;
  gap: 0;
  justify-content: center;
  margin: 28px 0 0;
  background: rgba(0, 0, 0, 0.25);
  border-radius: 14px 14px 0 0;
  overflow: hidden;
}
.fs-slot {
  padding: 14px 30px;
  text-align: center;
  color: rgba(255, 255, 255, 0.65);
  border-bottom: 3px solid transparent;
  cursor: pointer;
}
.fs-slot.on {
  color: #fff;
  border-bottom-color: var(--gold);
  background: rgba(255, 176, 32, 0.08);
}
.fs-slot .t {
  font-size: 20px;
  font-weight: 900;
}
.fs-slot .s {
  font-size: 12px;
  margin-top: 2px;
}
.fs-slot.on .s {
  color: var(--gold);
  font-weight: 700;
}
.cd-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 18px;
  color: #fff;
}
.cd-bar b {
  background: var(--gold);
  color: #5a2c00;
  width: 38px;
  height: 38px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  font-size: 20px;
  font-weight: 900;
}
.cd-bar i {
  color: var(--gold);
  font-style: normal;
  font-weight: 900;
  font-size: 18px;
}
.fs-body {
  background: var(--bg);
  padding: 24px 0 50px;
}
.fs-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: 0.15s;
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
}
.fs-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-2);
}
.fs-card .ph {
  aspect-ratio: 1;
}
.fs-card .b {
  padding: 12px;
}
.fs-card .nm {
  font-size: 13px;
  line-height: 1.4;
  height: 36px;
  overflow: hidden;
  color: var(--ink);
}
.fs-card .pr {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 8px;
}
.fs-card .pr .now {
  color: var(--price);
  font-weight: 900;
  font-size: 20px;
}
.fs-card .pr .old {
  color: var(--ink-3);
  text-decoration: line-through;
  font-size: 12px;
}
.fs-card .bar {
  height: 18px;
  border-radius: 999px;
  background: var(--brand-soft);
  margin-top: 10px;
  position: relative;
  overflow: hidden;
}
.fs-card .bar i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #ff7a45, var(--brand));
}
.fs-card .bar span {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  font-size: 11px;
  color: #fff;
  font-weight: 700;
}
.fs-card .btn {
  margin: 10px 12px 12px;
}

@media (max-width: 1024px) {
  .g5 {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 640px) {
  .g5 {
    grid-template-columns: repeat(2, 1fr);
  }
  .fs-hero .lg {
    font-size: 28px;
  }
  .fs-slot {
    padding: 10px 14px;
  }
  .fs-slot .t {
    font-size: 16px;
  }
}
</style>
