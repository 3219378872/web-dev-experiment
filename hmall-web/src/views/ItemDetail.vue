<template>
  <div v-if="item" class="item-detail">
    <div class="wrap">
      <!-- Breadcrumb -->
      <div class="crumb">
        <router-link to="/">首页</router-link>
        <span class="s">/</span>
        <router-link to="/category">{{ item.categoryName || '商品分类' }}</router-link>
        <span class="s">/</span>
        <b style="color: var(--ink)">{{ item.name }}</b>
      </div>

      <!-- 主信息 -->
      <section class="pd">
        <div class="gallery">
          <div class="main">
            <img :src="item.image || '/placeholder.png'" :alt="item.name" />
          </div>
          <div class="thumbs">
            <div
              v-for="n in 5"
              :key="n"
              class="ph"
              :class="[`s${((item.id + n) % 8) + 1}`, { on: activeThumb === n }]"
              @click="activeThumb = n"
            >
              <div class="shape round"></div>
            </div>
          </div>
          <div class="acts">
            <a id="fav-btn" @click="toggleFavorite">
              <span :style="isFavorited ? 'color:var(--brand)' : ''">{{
                isFavorited ? '♥ 已收藏' : '♡ 收藏商品'
              }}</span>
            </a>
            <a>⤴ 分享</a>
            <a>⚖ 对比</a>
          </div>
        </div>

        <div class="pinfo">
          <h1>
            <span v-if="item.tag" class="promo-tag">{{ item.tag }}</span>
            {{ item.name }}
          </h1>
          <div class="subtitle">🔥 好集精选，品质保障，全场正品</div>
          <div class="rate-row">
            <span
              ><b>{{ item.rating || '4.9' }}</b> 综合评分</span
            >
            <span
              ><b>{{ formatCount(item.commentCount || 0) }}</b> 累计评价</span
            >
            <span
              ><b>{{ formatCount(item.sold || 0) }}</b> 件已售</span
            >
            <span><b>98%</b> 好评率</span>
          </div>

          <div class="price-box">
            <div class="coupon">
              <span class="c">满300减50</span>
              <span class="c">9折券</span>
            </div>
            <div class="lab">好集价</div>
            <div>
              <span class="now"><span class="cur">¥</span>{{ formatPrice(item.price) }}</span>
              <span v-if="item.originalPrice" class="old"
                >¥{{ formatPrice(item.originalPrice) }}</span
              >
            </div>
          </div>

          <div class="spec">
            <div class="line">
              <span class="k">颜色</span>
              <div class="opts">
                <span class="opt on"
                  ><span class="sw" style="background: #221b17"></span>曜石黑</span
                >
                <span class="opt"
                  ><span class="sw" style="background: #f2f0ec; border: 1px solid #ddd"></span
                  >云母白</span
                >
                <span class="opt"><span class="sw" style="background: #88a6b8"></span>雾霾蓝</span>
              </div>
            </div>
            <div class="line">
              <span class="k">版本</span>
              <div class="opts">
                <span class="opt on">标准版</span>
                <span class="opt">降噪增强版 +¥80</span>
                <span class="opt">游戏专业版 +¥150</span>
              </div>
            </div>
            <div class="line">
              <span class="k">数量</span>
              <div style="display: flex; align-items: center; gap: 14px">
                <div class="qty">
                  <button @click="quantity > 1 ? quantity-- : null">−</button>
                  <input v-model.number="quantity" readonly />
                  <button @click="quantity < item.stock ? quantity++ : null">+</button>
                </div>
                <span class="dim" style="font-size: 12.5px"
                  >库存 <b style="color: var(--ink-2)">{{ item.stock }}</b> 件 · 上海仓 现货</span
                >
              </div>
            </div>
          </div>

          <div class="buy-row">
            <a class="btn btn-hot btn-lg" @click="addCart">🛒 加入购物车</a>
            <router-link class="btn btn-gold btn-lg" to="/cart">⚡ 立即购买</router-link>
          </div>
          <div class="svc-row">
            <span>好集自营</span><span>当日发货</span><span>七天无理由</span><span>假一赔十</span
            ><span>全国联保</span>
          </div>
        </div>
      </section>

      <!-- 店铺 -->
      <div class="shopbar">
        <div class="logo2">集</div>
        <div class="meta">
          <b>好集数码官方旗舰店</b>
          <div class="st">
            <span>商品 1240</span><span>关注 86万</span><span>评分 4.9 / 5.0</span>
          </div>
        </div>
        <div class="acts">
          <router-link class="btn btn-outline btn-sm" to="/category">进入店铺</router-link>
          <router-link class="btn btn-ghost btn-sm" to="/service">联系客服</router-link>
        </div>
      </div>

      <!-- 详情区 -->
      <div class="dlayout">
        <div>
          <div class="dtabs tabs">
            <a :class="{ active: activeTab === 'detail' }" @click="activeTab = 'detail'"
              >商品详情</a
            >
            <a :class="{ active: activeTab === 'spec' }" @click="activeTab = 'spec'">规格参数</a>
            <a :class="{ active: activeTab === 'reviews' }" @click="activeTab = 'reviews'"
              >用户评价 ({{ reviews.length }})</a
            >
            <a :class="{ active: activeTab === 'service' }" @click="activeTab = 'service'"
              >售后保障</a
            >
          </div>
          <div
            class="panel"
            style="border-radius: 0 0 var(--radius) var(--radius); border-top: 0; padding: 24px"
          >
            <!-- 商品详情 -->
            <div v-if="activeTab === 'detail'">
              <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 14px">商品介绍</h3>
              <p class="muted" style="font-size: 13.5px; line-height: 1.8; margin: 10px 0">
                {{ item.description || '本商品为好集精选好物，品质保障，全场正品。' }}
              </p>
              <div class="detail-imgs">
                <div class="ph s4" data-label="detail"><span class="glyph">▣</span></div>
                <div class="ph s7" data-label="detail"><span class="glyph">◐</span></div>
                <div class="ph s1" data-label="detail" style="margin-top: 12px">
                  <span class="glyph">✦</span>
                </div>
              </div>
            </div>

            <!-- 规格参数 -->
            <div v-if="activeTab === 'spec'">
              <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 14px">规格参数</h3>
              <table class="ptable">
                <tr>
                  <td class="k">品牌</td>
                  <td>好集严选 HAOJI</td>
                  <td class="k">型号</td>
                  <td>{{ item.name?.slice(0, 12) || 'HJ-Pro' }}</td>
                </tr>
                <tr>
                  <td class="k">分类</td>
                  <td>{{ item.categoryName || '数码' }}</td>
                  <td class="k">价格</td>
                  <td>¥{{ formatPrice(item.price) }}</td>
                </tr>
                <tr>
                  <td class="k">库存</td>
                  <td>{{ item.stock }} 件</td>
                  <td class="k">销量</td>
                  <td>{{ item.sold || 0 }} 件</td>
                </tr>
                <tr>
                  <td class="k">评分</td>
                  <td>{{ item.rating || '4.9' }} 分</td>
                  <td class="k">评价</td>
                  <td>{{ item.commentCount || 0 }} 条</td>
                </tr>
              </table>
            </div>

            <!-- 用户评价 -->
            <div v-if="activeTab === 'reviews'">
              <h3 style="font-size: 16px; font-weight: 700; margin: 0 0 14px">用户评价</h3>
              <div class="rev-summary">
                <div class="rev-score">
                  <div class="big">{{ item.rating || '4.9' }}</div>
                  <div
                    class="stars"
                    style="font-size: 16px; justify-content: center; margin-top: 6px"
                  >
                    ★★★★★
                  </div>
                  <div class="muted" style="font-size: 12px; margin-top: 6px">
                    98% 好评 · 共 {{ formatCount(reviews.length) }} 条
                  </div>
                </div>
                <div class="rev-bars">
                  <div class="rb">
                    <span>5星</span>
                    <div class="track"><i style="width: 92%"></i></div>
                    <span>92%</span>
                  </div>
                  <div class="rb">
                    <span>4星</span>
                    <div class="track"><i style="width: 6%"></i></div>
                    <span>6%</span>
                  </div>
                  <div class="rb">
                    <span>3星</span>
                    <div class="track"><i style="width: 1.5%"></i></div>
                    <span>1.5%</span>
                  </div>
                  <div class="rb">
                    <span>2星</span>
                    <div class="track"><i style="width: 0.4%"></i></div>
                    <span>0.4%</span>
                  </div>
                  <div class="rb">
                    <span>1星</span>
                    <div class="track"><i style="width: 0.1%"></i></div>
                    <span>0.1%</span>
                  </div>
                </div>
              </div>
              <div class="rev-filters">
                <span class="chip on">全部 {{ reviews.length }}</span>
                <span class="chip">有图</span>
                <span class="chip">好评</span>
                <span class="chip">中评</span>
                <span class="chip">差评</span>
              </div>

              <div v-if="reviews.length" id="rev-list">
                <div v-for="r in reviews" :key="r.id" class="review">
                  <div class="av" :style="`background:${avatarColor(r.userId || r.id)}`">
                    {{ (r.nickname || r.userName || '用')[0] }}
                  </div>
                  <div style="flex: 1">
                    <div class="who">
                      <b>{{ r.nickname || r.userName || '匿名用户' }}</b>
                      <span class="stars">{{ renderStars(r.rating) }}</span>
                      <span class="dim" style="margin-left: auto; font-size: 12px">{{
                        r.createTime?.slice(0, 10)
                      }}</span>
                    </div>
                    <div class="body">{{ r.content }}</div>
                  </div>
                </div>
              </div>
              <div v-else style="text-align: center; padding: 40px 0; color: var(--ink-3)">
                暂无评价
              </div>

              <div style="text-align: center; margin-top: 18px">
                <a class="btn btn-ghost" href="#">查看全部 {{ reviews.length }} 条评价 ›</a>
              </div>

              <!-- 写评价 -->
              <div
                v-if="userStore.isLoggedIn"
                class="panel"
                style="
                  background: var(--bg);
                  border: 1px dashed var(--line-2);
                  padding: 18px;
                  margin-top: 22px;
                "
              >
                <h4 style="font-size: 14px; font-weight: 700; margin-bottom: 12px">✍ 发表评价</h4>
                <div
                  style="
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    margin-bottom: 12px;
                    font-size: 13px;
                  "
                >
                  <span class="muted">评分</span>
                  <StarRating v-model="reviewRating" />
                  <span class="muted">非常满意</span>
                </div>
                <textarea
                  v-model="reviewContent"
                  class="input"
                  rows="3"
                  placeholder="说说这件商品怎么样，分享给想买的小伙伴～"
                ></textarea>
                <div
                  style="
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-top: 14px;
                  "
                >
                  <label
                    style="
                      display: flex;
                      gap: 8px;
                      align-items: center;
                      font-size: 13px;
                      color: var(--ink-2);
                    "
                  >
                    <span class="checkbox on">✓</span>匿名评价
                  </label>
                  <button class="btn btn-primary" @click="submitReview">提交评价</button>
                </div>
              </div>
            </div>

            <!-- 售后保障 -->
            <div v-if="activeTab === 'service'">
              <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 14px">售后保障</h3>
              <div
                style="
                  display: flex;
                  flex-direction: column;
                  gap: 14px;
                  font-size: 13.5px;
                  color: var(--ink-2);
                  line-height: 1.8;
                "
              >
                <p>
                  <b style="color: var(--ink)">七天无理由退换货：</b
                  >自签收之日起7天内，商品未使用、包装完好，可申请无理由退货。
                </p>
                <p>
                  <b style="color: var(--ink)">正品保障：</b>好集承诺所售商品均为正品，假一赔十。
                </p>
                <p><b style="color: var(--ink)">全国联保：</b>凭订单可享受品牌官方全国联保服务。</p>
                <p>
                  <b style="color: var(--ink)">运费险：</b>退货时平台承担退货运费，无需额外付费。
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- 右栏推荐 -->
        <aside>
          <div class="rail-box">
            <h4>🔥 看了又看</h4>
            <router-link
              v-for="p in relatedItems"
              :key="p.id"
              class="rail-prod"
              :to="`/item/${p.id}`"
            >
              <div
                class="ph"
                :class="`s${(p.id % 8) + 1}`"
                style="width: 60px; height: 60px; border-radius: 8px; flex-shrink: 0"
              >
                <div class="shape round"></div>
              </div>
              <div>
                <div class="nm">{{ p.name }}</div>
                <div class="pr"><small class="cur">¥</small>{{ formatPrice(p.price) }}</div>
              </div>
            </router-link>
          </div>
          <div
            class="rail-box"
            style="background: linear-gradient(135deg, #fff3dc, #ffe2c0); border-color: #f3d9a0"
          >
            <h4 style="color: #8a5d00">🎟 本店优惠券</h4>
            <div
              class="mini-coupon"
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
                background: #fff;
                border-radius: 8px;
                padding: 9px 12px;
                margin-bottom: 8px;
              "
            >
              <span
                ><b style="color: var(--price); font-size: 17px">¥20</b>
                <small class="dim">满199</small></span
              >
              <button class="btn btn-primary btn-sm">领取</button>
            </div>
            <div
              class="mini-coupon"
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
                background: #fff;
                border-radius: 8px;
                padding: 9px 12px;
              "
            >
              <span
                ><b style="color: var(--price); font-size: 17px">¥50</b>
                <small class="dim">满399</small></span
              >
              <button class="btn btn-primary btn-sm">领取</button>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useCartStore } from '@/stores/cart';
import { ElMessage } from 'element-plus';
import { getItemById, getReviews, submitReview as submitReviewApi, getItems } from '@/api/item';
import { checkFavorite, addFavorite, removeFavorite } from '@/api/common';
import StarRating from '@/components/StarRating.vue';

const route = useRoute();
const userStore = useUserStore();
const cartStore = useCartStore();
const item = ref(null);
const quantity = ref(1);
const isFavorited = ref(false);
const reviews = ref([]);
const reviewRating = ref(5);
const reviewContent = ref('');
const activeThumb = ref(1);
const activeTab = ref('detail');
const relatedItems = ref([]);

const avatarColors = ['#88A6B8', '#DE9696', '#B0BE92', '#E9B775', '#B79CC4', '#7FB6A6'];

function avatarColor(seed) {
  const n = (seed || 0) % avatarColors.length;
  return avatarColors[n];
}

function formatPrice(price) {
  if (!price) return '0';
  return (price / 100).toFixed(0);
}

function formatCount(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return String(n);
}

function renderStars(rating) {
  const full = Math.floor(rating || 0);
  let s = '';
  for (let i = 0; i < full; i++) s += '★';
  for (let i = full; i < 5; i++) s += '<span class="off">★</span>';
  return s;
}

onMounted(async () => {
  try {
    item.value = await getItemById(route.params.id);
  } catch (err) {
    /* ignore */
  }
  try {
    reviews.value = await getReviews(route.params.id);
  } catch (err) {
    /* ignore */
  }
  try {
    isFavorited.value = await checkFavorite(route.params.id);
  } catch (err) {
    /* ignore */
  }
  try {
    const data = await getItems({ page: 1, size: 4 });
    relatedItems.value = (data?.list || [])
      .filter((x) => String(x.id) !== String(route.params.id))
      .slice(0, 4);
  } catch (err) {
    /* ignore */
  }
});

async function addCart() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }
  try {
    await cartStore.addItem({ itemId: item.value.id, num: quantity.value });
    ElMessage.success('已添加到购物车');
  } catch (err) {
    /* ignore */
  }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录');
    return;
  }
  try {
    if (isFavorited.value) {
      await removeFavorite(item.value.id);
      isFavorited.value = false;
    } else {
      await addFavorite(item.value.id);
      isFavorited.value = true;
    }
  } catch (err) {
    /* ignore */
  }
}

async function submitReview() {
  try {
    await submitReviewApi(item.value.id, {
      rating: reviewRating.value,
      content: reviewContent.value,
    });
    ElMessage.success('评价已提交');
    reviewContent.value = '';
    reviews.value = await getReviews(route.params.id);
  } catch (err) {
    /* ignore */
  }
}
</script>

<style scoped>
.pd {
  display: grid;
  grid-template-columns: 440px 1fr;
  gap: 30px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 24px;
  margin: 8px 0 20px;
}
.gallery .main {
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-2);
}
.gallery .main img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.thumbs {
  display: flex;
  gap: 9px;
  margin-top: 12px;
}
.thumbs .ph {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
}
.thumbs .ph.on {
  border-color: var(--brand);
}
.gallery .acts {
  display: flex;
  gap: 18px;
  margin-top: 16px;
  justify-content: center;
  font-size: 13px;
  color: var(--ink-2);
}
.gallery .acts a {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.gallery .acts a:hover {
  color: var(--brand);
}

.pinfo h1 {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.4;
  letter-spacing: -0.3px;
}
.pinfo .promo-tag {
  display: inline-block;
  background: var(--brand);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 1px 7px;
  border-radius: 4px;
  margin-right: 6px;
  vertical-align: middle;
}
.pinfo .subtitle {
  color: var(--price);
  font-size: 13.5px;
  margin-top: 8px;
  font-weight: 500;
}
.pinfo .rate-row {
  display: flex;
  gap: 20px;
  margin-top: 14px;
  font-size: 12.5px;
  color: var(--ink-3);
}
.pinfo .rate-row b {
  color: var(--ink);
  font-size: 15px;
}

.price-box {
  background: linear-gradient(100deg, #fff1eb, #ffe6dc);
  border-radius: 12px;
  padding: 18px 20px;
  margin-top: 16px;
  position: relative;
  overflow: hidden;
}
.price-box .lab {
  font-size: 12px;
  color: var(--price);
}
.price-box .now {
  color: var(--price);
  font-weight: 900;
  font-size: 36px;
  letter-spacing: -1px;
}
.price-box .now .cur {
  font-size: 18px;
}
.price-box .old {
  color: var(--ink-3);
  text-decoration: line-through;
  margin-left: 10px;
  font-size: 14px;
}
.price-box .coupon {
  position: absolute;
  right: 18px;
  top: 16px;
  display: flex;
  gap: 8px;
}
.price-box .coupon .c {
  background: #fff;
  border: 1px dashed var(--brand);
  color: var(--price);
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 6px;
}

.spec {
  margin-top: 18px;
}
.spec .line {
  display: grid;
  grid-template-columns: 64px 1fr;
  gap: 14px;
  padding: 11px 0;
  align-items: center;
}
.spec .line .k {
  color: var(--ink-3);
  font-size: 13px;
}
.spec .opts {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
}
.spec .opt {
  border: 1px solid var(--line-2);
  border-radius: 8px;
  padding: 7px 14px;
  font-size: 13px;
  cursor: pointer;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 7px;
}
.spec .opt.on {
  border-color: var(--brand);
  background: var(--brand-softer);
  color: var(--brand-700);
  font-weight: 700;
}
.spec .opt .sw {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.buy-row {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}
.buy-row .btn {
  flex: 1;
}
.svc-row {
  display: flex;
  gap: 20px;
  margin-top: 16px;
  font-size: 12.5px;
  color: var(--ink-2);
  flex-wrap: wrap;
}
.svc-row span {
  display: flex;
  align-items: center;
  gap: 6px;
}
.svc-row span::before {
  content: '✓';
  color: var(--success);
  font-weight: 900;
}

/* shop card */
.shopbar {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 16px 20px;
  margin-bottom: 20px;
}
.shopbar .logo2 {
  width: 46px;
  height: 46px;
  border-radius: 11px;
  background: var(--brand-soft);
  color: var(--brand);
  display: grid;
  place-items: center;
  font-size: 22px;
  font-weight: 900;
}
.shopbar .meta b {
  font-size: 15px;
}
.shopbar .meta .st {
  font-size: 12px;
  color: var(--ink-3);
  display: flex;
  gap: 12px;
  margin-top: 3px;
}
.shopbar .acts {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

/* detail tabs */
.dlayout {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
}
.dtabs {
  position: sticky;
  top: 118px;
  z-index: 20;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius) var(--radius) 0 0;
  border-bottom: 0;
}
.ptable {
  width: 100%;
  border-collapse: collapse;
}
.ptable td {
  border: 1px solid var(--line);
  padding: 11px 16px;
  font-size: 13px;
}
.ptable td.k {
  background: var(--bg);
  color: var(--ink-3);
  width: 130px;
  font-weight: 500;
}
.detail-imgs .ph {
  width: 100%;
  aspect-ratio: 16/7;
  border-radius: 10px;
  margin-bottom: 12px;
}
.detail-imgs .ph .glyph {
  font-size: 60px;
}

/* reviews */
.rev-summary {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 24px;
  padding: 20px 4px;
  align-items: center;
}
.rev-score {
  text-align: center;
}
.rev-score .big {
  font-size: 46px;
  font-weight: 900;
  color: var(--gold);
  line-height: 1;
}
.rev-bars {
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.rev-bars .rb {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--ink-3);
}
.rev-bars .rb .track {
  flex: 1;
  height: 7px;
  background: var(--bg-2);
  border-radius: 999px;
  overflow: hidden;
}
.rev-bars .rb .track i {
  display: block;
  height: 100%;
  background: var(--gold);
}
.rev-filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin: 6px 0 16px;
}
.review {
  display: flex;
  gap: 14px;
  padding: 18px 0;
  border-top: 1px solid var(--line);
}
.review .av {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
}
.review .who {
  display: flex;
  align-items: center;
  gap: 10px;
}
.review .who b {
  font-size: 13.5px;
}
.review .body {
  font-size: 13.5px;
  color: var(--ink);
  margin: 7px 0;
  line-height: 1.6;
}
.review .spec-buy {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 8px;
}

/* right rail */
.rail-box {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 14px;
  margin-bottom: 14px;
}
.rail-box h4 {
  font-size: 13px;
  font-weight: 700;
  margin-bottom: 12px;
}
.rail-prod {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  text-decoration: none;
  color: inherit;
}
.rail-prod .nm {
  font-size: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.rail-prod .pr {
  color: var(--price);
  font-weight: 900;
  font-size: 14px;
  margin-top: 4px;
}

@media (max-width: 1024px) {
  .pd {
    grid-template-columns: 1fr;
  }
  .dlayout {
    grid-template-columns: 1fr;
  }
  .gallery .main {
    max-width: 400px;
    margin: 0 auto;
  }
}
</style>
