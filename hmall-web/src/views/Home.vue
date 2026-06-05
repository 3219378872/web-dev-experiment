<template>
  <div class="home">
    <!-- HERO -->
    <section class="hero">
      <div class="catmenu">
        <router-link
          v-for="c in categories"
          :key="c.name"
          class="it"
          :to="`/category?cat=${encodeURIComponent(c.name)}`"
        >
          <b>{{ c.name }}</b> <span class="sub">{{ c.sub }} ›</span>
        </router-link>
      </div>

      <div
        class="carousel"
        @mouseenter="bannerTimer && clearInterval(bannerTimer)"
        @mouseleave="startBannerTimer"
      >
        <template v-if="banners.length">
          <a
            v-for="(b, i) in banners"
            :key="b.id"
            :href="b.linkUrl || '#'"
            class="banner-slide"
            :class="{ active: bannerIdx === i }"
          >
            <img :src="b.imageUrl" :alt="b.title || '轮播图'" />
          </a>
          <div class="dots">
            <i
              v-for="(b, i) in banners"
              :key="b.id"
              :class="{ on: bannerIdx === i }"
              @click="bannerIdx = i"
            ></i>
          </div>
        </template>
        <template v-else>
          <span class="blob b1"></span><span class="blob b2"></span>
          <span class="eyebrow">好集 618 年中狂欢</span>
          <h1>万物好集<br />低价开抢</h1>
          <p>全场最高直降 60%，每满 300 减 50，叠加平台券更划算。</p>
          <div class="cta">
            <router-link class="btn btn-gold btn-lg" to="/flashsale">⚡ 进入会场</router-link>
            <router-link
              class="btn btn-lg"
              style="background: rgba(255, 255, 255, 0.18); color: #fff"
              to="/coupons"
              >领券中心</router-link
            >
          </div>
          <div class="price-burst">
            <div>
              <div style="font-size: 12px">立减</div>
              <div class="big">¥50</div>
              <div style="font-size: 11px">满300可用</div>
            </div>
          </div>
        </template>
      </div>

      <div class="side-stack">
        <div class="login-card">
          <div class="av">◍</div>
          <div style="font-weight: 700; font-size: 15px">
            Hi，{{
              userStore.isLoggedIn
                ? userStore.userInfo?.nickname || userStore.userInfo?.username || '用户'
                : '访客'
            }}
          </div>
          <div class="muted" style="font-size: 12px; margin: 3px 0 12px">
            {{ userStore.isLoggedIn ? '好集会员' : '登录享受更多权益' }}
          </div>
          <div style="display: flex; gap: 8px">
            <router-link class="btn btn-primary btn-sm btn-block" to="/orders"
              >我的订单</router-link
            >
            <router-link class="btn btn-ghost btn-sm btn-block" to="/coupons">我的券</router-link>
          </div>
        </div>
        <div class="coupon-card">
          <h4>🎟 新人专享券</h4>
          <div class="mini-coupon">
            <span class="v"><small>¥</small>20</span
            ><router-link class="btn btn-primary btn-sm" to="/coupons">领取</router-link>
          </div>
          <div class="mini-coupon">
            <span class="v"><small>¥</small>50</span
            ><router-link class="btn btn-primary btn-sm" to="/coupons">领取</router-link>
          </div>
          <div class="mini-coupon">
            <span class="v">9<small>折</small></span
            ><router-link class="btn btn-primary btn-sm" to="/coupons">领取</router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- 服务保障 -->
    <section class="services">
      <div class="svc">
        <span class="d">✓</span>
        <div><b>正品保障</b><span>假一赔十</span></div>
      </div>
      <div class="svc">
        <span class="d">⚡</span>
        <div><b>极速发货</b><span>当日达/次日达</span></div>
      </div>
      <div class="svc">
        <span class="d">↺</span>
        <div><b>七天退换</b><span>无理由退货</span></div>
      </div>
      <div class="svc">
        <span class="d">🛡</span>
        <div><b>安全支付</b><span>多重加密</span></div>
      </div>
    </section>

    <!-- 限时秒杀 -->
    <section style="margin-top: 30px" class="flash">
      <div class="flash-head">
        <div class="lg">⚡ 限时<span>秒杀</span></div>
        <div class="cd">
          <span style="font-size: 13px; opacity: 0.8">距结束</span><b>{{ countdown.h }}</b
          ><i>:</i><b>{{ countdown.m }}</b
          ><i>:</i><b>{{ countdown.s }}</b>
        </div>
        <router-link class="more" to="/flashsale">查看全部 ›</router-link>
      </div>
      <div class="flash-grid">
        <router-link
          v-for="(p, i) in flashItems"
          :key="p.id"
          class="flash-item"
          :to="`/item/${p.id}`"
        >
          <div class="ph" :class="p.shade || `s${(p.id % 8) + 1}`" :data-label="p.category">
            <img v-if="p.image" :src="p.image" :alt="p.name" />
            <span v-else class="glyph">{{ p.glyph }}</span>
          </div>
          <div class="nm">{{ p.name }}</div>
          <div class="pr">
            <small style="font-size: 12px">¥</small>{{ (p.price / 100).toFixed(0) }}
            <span v-if="p.originalPrice" class="price-old" style="font-size: 11px"
              >¥{{ (p.originalPrice / 100).toFixed(0) }}</span
            >
          </div>
          <div class="bar"><i :style="`width:${flashPct[i]}%`"></i></div>
          <div class="sold">已抢 {{ flashPct[i] }}%</div>
        </router-link>
      </div>
    </section>

    <!-- 金刚区 -->
    <div class="sec-head">
      <div class="t">
        <span class="bar"></span>
        <h2>热门分类</h2>
        <span class="sub">逛遍全场好物</span>
      </div>
    </div>
    <div class="kingkong">
      <router-link
        v-for="k in kingKong"
        :key="k[0]"
        class="kk"
        :to="`/category?cat=${encodeURIComponent(k[0])}`"
      >
        <span class="d" :style="`background:${k[2]}`">{{ k[1] }}</span
        ><span>{{ k[0] }}</span>
      </router-link>
    </div>

    <!-- 双广告 -->
    <div class="twocol" style="margin-top: 14px">
      <div class="ad-banner" style="background: linear-gradient(120deg, #88a6b8, #5d7f93)">
        <span class="b"></span>
        <h3>数码焕新季</h3>
        <p>耳机 / 投影 / 键盘 低至 5 折</p>
        <router-link
          class="btn btn-sm"
          style="background: #fff; color: var(--ink); margin-top: 14px; width: fit-content"
          to="/category"
          >立即选购 ›</router-link
        >
      </div>
      <div class="ad-banner" style="background: linear-gradient(120deg, #b0be92, #8aa05f)">
        <span class="b"></span>
        <h3>食品生鲜节</h3>
        <p>坚果 / 牛排 / 水果 产地直发</p>
        <router-link
          class="btn btn-sm"
          style="background: #fff; color: var(--ink); margin-top: 14px; width: fit-content"
          to="/category"
          >立即选购 ›</router-link
        >
      </div>
    </div>

    <!-- 热门商品 -->
    <div class="sec-head">
      <div class="t">
        <span class="bar"></span>
        <h2>热门好物</h2>
        <span class="sub">大家都在买</span>
      </div>
      <router-link class="more" to="/search">查看更多 ›</router-link>
    </div>
    <div class="grid g5">
      <ProductCard v-for="item in hotItems" :key="item.id" :item="item" />
    </div>

    <!-- 新品推荐 -->
    <div class="sec-head">
      <div class="t">
        <span class="bar" style="background: var(--info)"></span>
        <h2>新品推荐</h2>
        <span class="sub">抢先体验上新</span>
      </div>
      <router-link class="more" to="/search">查看更多 ›</router-link>
    </div>
    <div class="grid g5">
      <ProductCard v-for="item in newItems" :key="item.id" :item="item" />
    </div>

    <!-- 促销专区 -->
    <div class="sec-head">
      <div class="t">
        <span class="bar" style="background: var(--gold)"></span>
        <h2>促销专区</h2>
        <span class="sub">省钱就现在</span>
      </div>
      <router-link class="more" to="/search">查看更多 ›</router-link>
    </div>
    <div class="grid g5">
      <ProductCard v-for="item in promoItems" :key="item.id" :item="item" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { getItems, getCategories, getBanners, getActiveSeckills } from '@/api/item';
import { takeTagged, buildPromoItems } from '@/utils/homeSections';
import { mapSeckillItem } from '@/utils/seckill';
import ProductCard from '@/components/ProductCard.vue';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const hotItems = ref([]);
const newItems = ref([]);
const promoItems = ref([]);
const flashItems = ref([]);
const flashPct = ref([]);
const countdown = ref({ h: '02', m: '48', s: '13' });

const banners = ref([]);
const bannerIdx = ref(0);
let bannerTimer = null;
function startBannerTimer() {
  if (bannerTimer) clearInterval(bannerTimer);
  if (banners.value.length > 1) {
    bannerTimer = setInterval(() => {
      bannerIdx.value = (bannerIdx.value + 1) % banners.value.length;
    }, 4000);
  }
}

const categories = ref([]);
const kkIcons = ['▣', '▤', '◈', '✿', '◉', '▦', '❀', '◐', '▥', '◆'];
const kkColors = [
  '#FF6A45',
  '#88A6B8',
  '#B79CC4',
  '#DE9696',
  '#B0BE92',
  '#C9A66B',
  '#E9B775',
  '#7FB6A6',
  '#92ADBD',
  '#A7906B',
];
const kingKong = computed(() =>
  categories.value.map((c, i) => [
    c.name,
    kkIcons[i % kkIcons.length],
    kkColors[i % kkColors.length],
  ])
);

let timer = null;
function startCountdown() {
  let total = 2 * 3600 + 48 * 60 + 13;
  timer = setInterval(() => {
    total--;
    if (total <= 0) total = 2 * 3600 + 48 * 60 + 13;
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

onMounted(async () => {
  startCountdown();
  try {
    const cats = await getCategories();
    categories.value = (cats || []).map((c) => ({ name: c.name, sub: '' }));
  } catch (e) {
    /* ignore */
  }
  try {
    const bannerList = await getBanners();
    banners.value = (bannerList || []).filter((b) => b.status !== 0);
    startBannerTimer();
  } catch (e) {
    /* ignore */
  }
  try {
    const data = await getItems({ page: 1, size: 10, sortBy: 'sold', isAsc: false });
    hotItems.value = takeTagged(data?.list, '热卖', 5);
  } catch (e) {
    /* ignore */
  }
  try {
    const data = await getItems({ page: 1, size: 10, sortBy: 'create_time', isAsc: false });
    newItems.value = takeTagged(data?.list, '新品', 5);
  } catch (e) {
    /* ignore */
  }
  try {
    const data = await getItems({ page: 1, size: 20, sortBy: 'sold', isAsc: false });
    promoItems.value = buildPromoItems(data?.list, 5);
  } catch (e) {
    /* ignore */
  }
  await loadFlashItems();
});

/** 秒杀区接入 /seckill/active，与 FlashSale.vue 口径一致（issue #127） */
async function loadFlashItems() {
  try {
    const data = await getActiveSeckills({ page: 1, size: 6 });
    const list = data?.list || data || [];
    const items = (Array.isArray(list) ? list : []).slice(0, 6).map(mapSeckillItem);
    flashItems.value = items;
    flashPct.value = items.map((p) => p.percent);
  } catch (e) {
    flashItems.value = [];
    flashPct.value = [];
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer);
  if (bannerTimer) clearInterval(bannerTimer);
});
</script>

<style scoped>
.home {
  max-width: var(--maxw);
  margin: 0 auto;
  padding: 0 20px 40px;
}

/* ---- HERO ---- */
.hero {
  display: grid;
  grid-template-columns: 210px 1fr 240px;
  gap: 14px;
  margin: 14px 0;
}
.catmenu {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}
.catmenu .it {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  font-size: 13px;
  border-bottom: 1px solid var(--bg);
  cursor: pointer;
}
.catmenu .it:last-child {
  border-bottom: 0;
}
.catmenu .it:hover {
  background: var(--brand-softer);
  color: var(--brand);
}
.catmenu .it b {
  font-weight: 700;
}
.catmenu .it .sub {
  color: var(--ink-3);
  font-size: 11px;
  font-weight: 400;
}
.catmenu .it:hover .sub {
  color: var(--brand);
}

.carousel {
  position: relative;
  border-radius: var(--radius);
  overflow: hidden;
  color: #fff;
  background: linear-gradient(120deg, #ff7a45 0%, var(--brand) 45%, var(--brand-700) 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 46px;
  min-height: 340px;
}
.carousel .eyebrow {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 3px;
  opacity: 0.9;
}
.carousel h1 {
  font-size: 46px;
  font-weight: 900;
  letter-spacing: -1px;
  line-height: 1.1;
  margin: 14px 0;
  text-shadow: 0 2px 16px rgba(120, 30, 0, 0.3);
}
.carousel p {
  font-size: 16px;
  opacity: 0.92;
  max-width: 380px;
}
.carousel .cta {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
.carousel .blob {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.13);
}
.carousel .b1 {
  width: 300px;
  height: 300px;
  right: -80px;
  top: -90px;
}
.carousel .b2 {
  width: 180px;
  height: 180px;
  right: 120px;
  bottom: -70px;
}
.dots {
  position: absolute;
  bottom: 18px;
  left: 46px;
  display: flex;
  gap: 7px;
}
.dots i {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.5);
  display: block;
}
.dots i.on {
  width: 24px;
  background: #fff;
}
.carousel .price-burst {
  position: absolute;
  right: 50px;
  top: 50%;
  transform: translateY(-50%);
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: var(--gold);
  color: #7a3b00;
  display: grid;
  place-items: center;
  text-align: center;
  font-weight: 900;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}
.carousel .price-burst .big {
  font-size: 40px;
  line-height: 1;
}

.side-stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.login-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 18px;
  text-align: center;
}
.login-card .av {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: var(--brand-soft);
  color: var(--brand);
  display: grid;
  place-items: center;
  font-size: 24px;
  margin: 0 auto 10px;
}
.coupon-card {
  background: linear-gradient(135deg, #fff3dc, #ffe2c0);
  border: 1px solid #f3d9a0;
  border-radius: var(--radius);
  padding: 16px;
}
.coupon-card h4 {
  font-size: 14px;
  color: #8a5d00;
  margin-bottom: 10px;
}
.mini-coupon {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 7px;
}
.mini-coupon .v {
  color: var(--price);
  font-weight: 900;
  font-size: 17px;
}
.mini-coupon .v small {
  font-size: 11px;
}

.services {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin: 6px 0 0;
}
.svc {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 13px;
}
.svc .d {
  width: 42px;
  height: 42px;
  border-radius: 11px;
  background: var(--brand-softer);
  color: var(--brand);
  display: grid;
  place-items: center;
  font-size: 21px;
}
.svc b {
  font-size: 14px;
  display: block;
}
.svc span {
  font-size: 12px;
  color: var(--ink-3);
}

/* 秒杀 */
.flash {
  background: linear-gradient(100deg, #2a1410, #46201a);
  border-radius: var(--radius);
  overflow: hidden;
  color: #fff;
}
.flash-head {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 24px;
}
.flash-head .lg {
  font-size: 26px;
  font-weight: 900;
  letter-spacing: -0.5px;
}
.flash-head .lg span {
  color: var(--gold);
}
.flash-head .cd {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 6px;
}
.flash-head .cd b {
  background: #fff;
  color: var(--ink);
  width: 30px;
  height: 30px;
  border-radius: 7px;
  display: grid;
  place-items: center;
  font-size: 15px;
  font-weight: 900;
}
.flash-head .cd i {
  color: var(--gold);
  font-style: normal;
  font-weight: 900;
}
.flash-head .more {
  margin-left: auto;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}
.flash-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 0;
  background: #fff;
  border-radius: 0 0 var(--radius) var(--radius);
}
.flash-item {
  padding: 14px;
  border-right: 1px solid var(--line);
  text-align: center;
  text-decoration: none;
  color: inherit;
}
.flash-item:last-child {
  border-right: 0;
}
.flash-item .ph {
  aspect-ratio: 1;
  border-radius: 10px;
  margin-bottom: 10px;
  overflow: hidden;
}
.flash-item .ph img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.flash-item .nm {
  font-size: 12.5px;
  color: var(--ink);
  height: 36px;
  overflow: hidden;
  line-height: 1.4;
}
.flash-item .pr {
  color: var(--price);
  font-weight: 900;
  font-size: 18px;
  margin-top: 6px;
}
.flash-item .bar {
  height: 6px;
  border-radius: 999px;
  background: var(--brand-soft);
  margin-top: 8px;
  overflow: hidden;
}
.flash-item .bar i {
  display: block;
  height: 100%;
  background: var(--brand);
}
.flash-item .sold {
  font-size: 10.5px;
  color: var(--ink-3);
  margin-top: 4px;
}

/* 金刚区 */
.kingkong {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 8px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 20px 12px;
}
.kk {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  border-radius: 10px;
  text-decoration: none;
  color: inherit;
}
.kk:hover {
  background: var(--brand-softer);
}
.kk .d {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 22px;
  color: #fff;
}
.kk span {
  font-size: 12px;
  color: var(--ink-2);
}

.twocol {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.ad-banner {
  border-radius: var(--radius);
  min-height: 150px;
  padding: 26px 30px;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.ad-banner h3 {
  font-size: 24px;
  font-weight: 900;
}
.ad-banner p {
  opacity: 0.9;
  font-size: 14px;
  margin-top: 4px;
}
.ad-banner .b {
  position: absolute;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  right: -40px;
  bottom: -50px;
}

@media (max-width: 1024px) {
  .hero {
    grid-template-columns: 1fr;
  }
  .catmenu,
  .side-stack {
    display: none;
  }
  .flash-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .kingkong {
    grid-template-columns: repeat(5, 1fr);
  }
  .g5 {
    grid-template-columns: repeat(3, 1fr);
  }
  .twocol {
    grid-template-columns: 1fr;
  }
}

.carousel {
  position: relative;
  overflow: hidden;
}
.banner-slide {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.6s ease;
  pointer-events: none;
}
.banner-slide.active {
  opacity: 1;
  pointer-events: auto;
  position: relative;
}
.banner-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
</style>
