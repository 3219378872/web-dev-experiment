<template>
  <div class="category-page">
    <h2>商品分类</h2>
    <el-row :gutter="16" v-if="categories.length">
      <el-col :span="6" v-for="cat in categories" :key="cat.id">
        <el-card shadow="hover" class="cat-card" @click="$router.push(`/search?q=${cat.name}`)">
          <p style="text-align:center;font-size:16px;padding:20px 0">{{ cat.name }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getCategories } from '@/api/item'
const categories = ref([])
onMounted(async () => { try { categories.value = await getCategories() } catch {} })
</script>
