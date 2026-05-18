<template>
  <div class="item-detail" v-if="item">
    <el-row :gutter="40">
      <el-col :md="10"><img :src="item.image || '/placeholder.png'" class="detail-image" /></el-col>
      <el-col :md="14">
        <h1 class="item-name">{{ item.name }}</h1>
        <p class="item-price">&yen;{{ (item.price / 100).toFixed(2) }}</p>
        <p class="item-desc">库存: {{ item.stock }} | 已售: {{ item.sold }} | 评分: {{ item.commentCount }}条评价</p>
        <div class="item-actions">
          <el-input-number v-model="quantity" :min="1" :max="item.stock" />
          <el-button type="primary" size="large" @click="addCart">加入购物车</el-button>
          <el-button size="large" :type="isFavorited ? 'warning' : 'default'" @click="toggleFavorite">
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </div>
      </el-col>
    </el-row>
    <div class="review-section">
      <h3>商品评价 ({{ reviews.length }})</h3>
      <div v-if="reviews.length" class="review-list">
        <div v-for="r in reviews" :key="r.id" class="review-item">
          <p><StarRating :modelValue="r.rating" /><span class="review-time">{{ r.createTime?.slice(0,10) }}</span></p>
          <p>{{ r.content }}</p>
        </div>
      </div>
      <div v-else><p style="color:#999">暂无评价</p></div>
      <div class="review-form" v-if="userStore.isLoggedIn">
        <h4>写评价</h4>
        <StarRating v-model="reviewRating" />
        <el-input v-model="reviewContent" type="textarea" :rows="3" placeholder="分享你的使用体验" />
        <el-button type="primary" @click="submitReview" style="margin-top:8px">提交评价</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import { getItemById, getReviews, submitReview as submitReviewApi } from '@/api/item'
import { addToCart } from '@/api/cart'
import { checkFavorite, addFavorite, removeFavorite } from '@/api/common'
import StarRating from '@/components/StarRating.vue'

const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const item = ref(null)
const quantity = ref(1)
const isFavorited = ref(false)
const reviews = ref([])
const reviewRating = ref(5)
const reviewContent = ref('')

onMounted(async () => {
  try { item.value = await getItemById(route.params.id) } catch {}
  try { reviews.value = await getReviews(route.params.id) } catch {}
  try { isFavorited.value = await checkFavorite(route.params.id) } catch {}
})

async function addCart() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  try {
    await cartStore.addItem({ itemId: item.value.id, num: quantity.value })
    ElMessage.success('已添加到购物车')
  } catch {}
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  try {
    if (isFavorited.value) { await removeFavorite(item.value.id); isFavorited.value = false }
    else { await addFavorite(item.value.id); isFavorited.value = true }
  } catch {}
}

async function submitReview() {
  try {
    await submitReviewApi(item.value.id, { rating: reviewRating.value, content: reviewContent.value })
    ElMessage.success('评价已提交')
    reviewContent.value = ''
    reviews.value = await getReviews(route.params.id)
  } catch {}
}
</script>

<style scoped>
.detail-image { width: 100%; border-radius: 8px; }
.item-name { font-size: 24px; margin-bottom: 12px; }
.item-price { font-size: 28px; color: #f56c6c; font-weight: bold; margin-bottom: 12px; }
.item-desc { color: #666; margin-bottom: 20px; }
.item-actions { display: flex; gap: 12px; align-items: center; }
.review-section { margin-top: 40px; }
.review-item { padding: 12px 0; border-bottom: 1px solid #eee; }
.review-time { margin-left: 8px; color: #999; font-size: 12px; }
.review-form { margin-top: 20px; }
</style>
