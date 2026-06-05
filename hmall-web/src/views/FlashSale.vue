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
                <span v-if="p.originalPrice" class="old"
                  >¥{{ Math.round(p.originalPrice / 100) }}</span
                >
              </div>
              <div class="bar">
                <i :style="{ width: p.percent + '%' }"></i>
                <span>已售 {{ p.percent }}%</span>
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
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getActiveSeckills } from '@/api/item';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

const countdown = ref({ h: '00', m: '00', s: '00' });
const page = ref(1);
const size = ref(10);
const total = ref(0);
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));
const activeSessionIndex = ref(0);

const timeSlots = ref([]);
const flashItems = ref([]);

/** 占位图标映射（后端商品未配真实图片时使用） */
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

/**
 * 从秒杀活动列表构建时间段和商品
 * 后端 SeckillVO 字段：itemId, itemName, seckillPrice, originalPrice, startTime, endTime, stock, sold, stockPercent, status
 */
function buildFromSeckillData(list) {
  const now = Date.now();
  const activeItems = [];
  const sessions = [];
  let activeEndTime = null;

  // 按 startTime 分组为时间段
  const grouped = {};
  for (const item of list) {
    if (!item.startTime) continue;
    const key = item.startTime.slice(0, 13); // 按小时分组
    if (!grouped[key]) grouped[key] = [];
    grouped[key].push(item);
  }

  const sortedKeys = Object.keys(grouped).sort();
  let activeFound = false;

  sortedKeys.forEach((key, idx) => {
    const first = grouped[key][0];
    const start = new Date(first.startTime).getTime();
    const end = new Date(first.endTime || first.startTime).getTime();
    const isActive = now >= start && now < end;
    if (isActive) activeFound = true;

    sessions.push({
      time: first.startTime.slice(11, 16),
      status: isActive ? '疯抢中' : now < start ? '即将开始' : '已结束',
      active: isActive,
      onClick: () => {
        activeSessionIndex.value = idx;
        buildFromSeckillData(list);
      },
    });

    if (isActive && !activeEndTime) {
      activeEndTime = first.endTime;
    }

    // 收集该时间段的商品
    for (const item of grouped[key]) {
      activeItems.push(parseSeckillItem(item));
    }
  });

  // 如果没有活跃场次，默认选中第一个
  if (!activeFound && sessions.length) {
    sessions[0].active = false;
  }

  timeSlots.value = sessions;
  flashItems.value = activeItems;
  total.value = activeItems.length;

  // 更新倒计时基于活跃场次的 endTime
  if (activeEndTime) {
    updateCountdownFromEndTime(activeEndTime);
  }
}

function parseSeckillItem(x) {
  const sold = x.sold || 0;
  const stock = x.stock || 1;
  const percent =
    x.stockPercent != null
      ? x.stockPercent
      : x.percent != null
        ? x.percent
        : Math.min(100, Math.round((sold / stock) * 100));
  return {
    id: x.itemId || x.id,
    name: x.itemName || x.name || '秒杀商品',
    seckillPrice: x.seckillPrice || 0,
    originalPrice: x.originalPrice || x.price || 0,
    stock,
    sold,
    percent,
    status: x.status,
    phStyle: `s${((x.itemId || x.id || 0) % 8) + 1}`,
    phLabel: x.category || '好物',
    phGlyph: glyphMap[x.category] || '◆',
  };
}

async function loadFlash() {
  try {
    const data = await getActiveSeckills({ page: page.value, size: size.value });
    const list = data?.list || data || [];
    if (Array.isArray(list)) {
      buildFromSeckillData(list);
    } else {
      flashItems.value = [];
    }
  } catch (err) {
    flashItems.value = [];
    timeSlots.value = [
      { time: '12:00', status: '疯抢中', active: true },
      { time: '14:00', status: '即将开始', active: false },
      { time: '16:00', status: '即将开始', active: false },
      { time: '18:00', status: '敬请期待', active: false },
      { time: '20:00', status: '敬请期待', active: false },
      { time: '22:00', status: '敬请期待', active: false },
    ];
  }
}

watch(page, loadFlash);

let timer = null;

function updateCountdownFromEndTime(endTime) {
  if (timer) clearInterval(timer);
  const end = new Date(endTime).getTime();
  timer = setInterval(() => {
    const remaining = Math.max(0, Math.floor((end - Date.now()) / 1000));
    if (remaining <= 0) {
      countdown.value = { h: '00', m: '00', s: '00' };
      return;
    }
    const h = Math.floor(remaining / 3600);
    const m = Math.floor((remaining % 3600) / 60);
    const s = remaining % 60;
    countdown.value = {
      h: String(h).padStart(2, '0'),
      m: String(m).padStart(2, '0'),
      s: String(s).padStart(2, '0'),
    };
  }, 1000);
}

/** 回退倒计时：当无活跃场次时使用固定时长 */
function startFallbackCountdown() {
  if (timer) clearInterval(timer);
  let remaining = 1 * 3600 + 48 * 60 + 13;
  timer = setInterval(() => {
    remaining--;
    if (remaining <= 0) remaining = 2 * 3600;
    const h = Math.floor(remaining / 3600);
    const m = Math.floor((remaining % 3600) / 60);
    const s = remaining % 60;
    countdown.value = {
      h: String(h).padStart(2, '0'),
      m: String(m).padStart(2, '0'),
      s: String(s).padStart(2, '0'),
    };
  }, 1000);
}

function priceYuan(p) {
  return Math.round((p.seckillPrice || 0) / 100);
}

function handleBuy(p) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }
  // 后端无秒杀下单接口，跳转商品详情页
  router.push(`/item/${p.id}`);
}

onMounted(() => {
  loadFlash();
  startFallbackCountdown();
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
