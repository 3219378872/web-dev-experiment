<template>
  <div class="search-page">
    <h2>搜索结果: "{{ $route.query.q }}"</h2>
    <el-row :gutter="16"><el-col :xs="12" :sm="8" :md="6" v-for="item in items" :key="item.id"><ProductCard :item="item" /></el-col></el-row>
    <el-empty v-if="!items.length" description="暂无结果" />
  </div>
</template>
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { searchItems } from '@/api/item'
import ProductCard from '@/components/ProductCard.vue'
const route = useRoute()
const items = ref([])
async function search() {
  try { const data = await searchItems({ name: route.query.q }); items.value = data?.list || [] } catch {}
}
onMounted(search)
watch(() => route.query.q, search)
</script>
