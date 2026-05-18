<template>
  <el-header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">hmall 电商平台</router-link>
      <el-input v-model="keyword" placeholder="搜索商品" class="search-input" @keyup.enter="search">
        <template #append><el-button @click="search"><el-icon><Search /></el-icon></el-button></template>
      </el-input>
      <div class="header-actions">
        <template v-if="userStore.isLoggedIn">
          <router-link to="/cart">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0">
              <el-button text><el-icon><ShoppingCart /></el-icon>购物车</el-button>
            </el-badge>
          </router-link>
          <router-link to="/orders"><el-button text>我的订单</el-button></router-link>
          <router-link to="/profile"><el-button text>个人中心</el-button></router-link>
          <el-button text @click="userStore.logout()">退出</el-button>
        </template>
        <template v-else>
          <router-link to="/login"><el-button type="primary">登录</el-button></router-link>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const keyword = ref('')
function search() {
  if (keyword.value.trim()) router.push({ path: '/search', query: { q: keyword.value.trim() } })
}
</script>

<style scoped>
.app-header { background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.1); position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1280px; margin: 0 auto; display: flex; align-items: center; gap: 20px; height: 60px; }
.logo { font-size: 20px; font-weight: bold; color: #409eff; white-space: nowrap; }
.search-input { width: 360px; }
.header-actions { display: flex; gap: 8px; align-items: center; margin-left: auto; }
</style>
