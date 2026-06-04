<template>
  <div id="app">
    <AppHeader v-if="!isAuthPage" />
    <main :class="isAuthPage || isFullWidth ? '' : 'main-content'"><router-view /></main>
    <AppFooter v-if="!isAuthPage" />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';

const route = useRoute();
const isAuthPage = computed(() => {
  const authPaths = ['/login', '/register', '/forgot'];
  return authPaths.includes(route.path);
});

/** 原型中部分页面使用通栏布局（无 max-width 限制） */
const isFullWidth = computed(() => {
  const fullWidthPaths = ['/flashsale'];
  return fullWidthPaths.includes(route.path);
});
</script>
