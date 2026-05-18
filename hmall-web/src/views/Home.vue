<template>
  <div class="home">
    <el-carousel height="360px" class="banner">
      <el-carousel-item v-for="b in banners" :key="b">
        <div class="banner-placeholder">{{ b }}</div>
      </el-carousel-item>
    </el-carousel>
    <div class="section">
      <h2 class="section-title">热门商品</h2>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" v-for="item in hotItems" :key="item.id">
          <ProductCard :item="item" />
        </el-col>
      </el-row>
    </div>
    <div class="section" v-if="newItems.length">
      <h2 class="section-title">新品推荐</h2>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" v-for="item in newItems" :key="item.id">
          <ProductCard :item="item" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getItems, getCategories } from '@/api/item'
import ProductCard from '@/components/ProductCard.vue'

const banners = ref(['新品上市', '限时特惠', '品质生活'])
const hotItems = ref([])
const newItems = ref([])

onMounted(async () => {
  try {
    const data = await getItems({ page: 1, size: 8, sortBy: 'sold' })
    hotItems.value = data?.list || []
  } catch (e) { /* ignore */ }
  try {
    const data = await getItems({ page: 1, size: 8, sortBy: 'create_time' })
    newItems.value = data?.list || []
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.banner { margin-bottom: 24px; border-radius: 8px; overflow: hidden; }
.banner-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 32px; color: #fff; }
.banner .el-carousel__item:nth-child(1) .banner-placeholder { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.banner .el-carousel__item:nth-child(2) .banner-placeholder { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.banner .el-carousel__item:nth-child(3) .banner-placeholder { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.section { margin-top: 32px; }
.section-title { font-size: 22px; font-weight: bold; margin-bottom: 16px; padding-left: 8px; border-left: 4px solid #409eff; }
</style>
