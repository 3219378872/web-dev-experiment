<template>
  <div class="category-page">
    <h2>商品分类</h2>
    <el-row v-if="categories.length" :gutter="16">
      <el-col v-for="cat in categories" :key="cat.id" :span="6">
        <el-card shadow="hover" class="cat-card" @click="$router.push(`/search?q=${cat.name}`)">
          <p style="text-align: center; font-size: 16px; padding: 20px 0">{{ cat.name }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import { getCategories } from '@/api/item';
const categories = ref([]);
onMounted(async () => {
  try {
    categories.value = await getCategories();
  } catch (err) {
    console.error(err);
  }
});
</script>
