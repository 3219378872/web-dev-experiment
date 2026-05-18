<template>
  <div><h2>系统公告</h2>
    <el-timeline v-if="notifications.length">
      <el-timeline-item v-for="n in notifications" :key="n.id" :timestamp="n.publishTime?.slice(0,19)" placement="top">
        <el-card><h4>{{ n.title }}</h4><p>{{ n.content }}</p></el-card>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="暂无公告" />
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getNotifications } from '@/api/common'
const notifications = ref([])
onMounted(async () => { try { notifications.value = await getNotifications() } catch {} })
</script>
