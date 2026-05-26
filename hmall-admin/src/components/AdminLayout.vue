<template>
  <el-container style="height:100vh">
    <el-aside width="220px" style="background:#304156">
      <div style="color:#fff;text-align:center;padding:16px;font-size:18px;font-weight:bold">hmall 管理后台</div>
      <el-menu :default-active="route.path" :default-openeds="['items-group', 'system-group']" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff">
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon>数据看板</el-menu-item>
        <el-menu-item index="/users"><el-icon><User /></el-icon>用户管理</el-menu-item>
        <el-sub-menu index="items-group">
          <template #title><el-icon><Goods /></el-icon>商品管理</template>
          <el-menu-item index="/items">商品列表</el-menu-item>
          <el-menu-item index="/items/add">新增商品</el-menu-item>
          <el-menu-item index="/categories">分类管理</el-menu-item>
          <el-menu-item index="/reviews">评价管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/orders"><el-icon><Tickets /></el-icon>订单管理</el-menu-item>
        <el-sub-menu index="system-group">
          <template #title><el-icon><Setting /></el-icon>系统管理</template>
          <el-menu-item index="/banners">轮播图管理</el-menu-item>
          <el-menu-item index="/notifications">公告管理</el-menu-item>
          <el-menu-item index="/feedbacks">反馈管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/profile"><el-icon><UserFilled /></el-icon>个人中心</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display:flex;justify-content:space-between;align-items:center;background:#fff;box-shadow:0 1px 4px rgba(0,0,0,.1)">
        <span>管理员，你好</span>
        <el-button text @click="logout">退出登录</el-button>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { useRoute, useRouter } from 'vue-router'
const route = useRoute(); const router = useRouter()
function logout() { localStorage.removeItem('adminToken'); router.push('/login') }
</script>
