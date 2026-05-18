<template>
  <el-card class="product-card" shadow="hover" @click="goDetail">
    <img :src="item.image || '/placeholder.png'" class="product-image" />
    <div class="product-info">
      <h3 class="product-name">{{ item.name }}</h3>
      <p class="product-price">&yen;{{ (item.price / 100).toFixed(2) }}</p>
      <p class="product-sold">已售 {{ item.sold }}</p>
    </div>
  </el-card>
</template>
<script setup>
import { useRouter } from 'vue-router'
const props = defineProps({ item: Object })
const router = useRouter()
function goDetail() { router.push(`/item/${props.item.id}`) }
</script>
<style scoped>
.product-card { cursor: pointer; margin-bottom: 16px; }
.product-image { width: 100%; height: 200px; object-fit: cover; }
.product-name { font-size: 14px; margin: 8px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price { color: #f56c6c; font-size: 18px; font-weight: bold; }
.product-sold { color: #999; font-size: 12px; }
</style>
