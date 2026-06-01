<template>
  <div>
    <h2>我的收藏</h2>
    <el-row :gutter="16"
      ><el-col v-for="f in favorites" :key="f.id" :xs="12" :sm="8" :md="6"
        ><ProductCard :item="f" /></el-col
    ></el-row>
    <el-empty v-if="!favorites.length" description="暂无收藏" />
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import { getFavorites } from '@/api/common';
import ProductCard from '@/components/ProductCard.vue';
const favorites = ref([]);
onMounted(async () => {
  try {
    favorites.value = await getFavorites();
  } catch (err) {
    console.error(err);
  }
});
</script>
