<template><div><h2>评价管理</h2><el-table :data="reviews" style="margin-top:16px"><el-table-column prop="id" label="ID" width="80"/><el-table-column prop="userId" label="用户ID" width="80"/><el-table-column prop="content" label="内容"/><el-table-column label="评分" width="80"><template #default="{row}">{{row.rating}}星</template></el-table-column><el-table-column label="时间" width="180"><template #default="{row}">{{row.createTime?.slice(0,19)}}</template></el-table-column><el-table-column label="操作" width="100"><template #default="{row}"><el-button size="small" type="danger" @click="del(row.id)">删除</el-button></template></el-table-column></el-table></div></template>
<script setup>
import { ref } from 'vue'
import { getReviews, deleteReview } from '@/api/item'
import { ElMessage } from 'element-plus'
const reviews = ref([])
async function fetch() { try { const r = await getReviews(); reviews.value = r.list || r || [] } catch {} }
async function del(id) { try { await deleteReview(id); fetch(); ElMessage.success('已删除') } catch {} }
fetch()
</script>
