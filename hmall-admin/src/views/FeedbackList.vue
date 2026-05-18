<template><div><h2>反馈管理</h2><el-table :data="feedbacks" style="margin-top:16px"><el-table-column prop="userId" label="用户ID" width="80"/><el-table-column prop="content" label="内容"/><el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="row.status===0?'warning':row.status===1?'success':'info'">{{['待处理','已回复','已关闭'][row.status]||'-'}}</el-tag></template></el-table-column><el-table-column label="操作" width="120"><template #default="{row}"><el-button size="small" type="primary" @click="showReply(row)" v-if="row.status===0">回复</el-button></template></el-table-column></el-table>
    <el-dialog v-model="replyVisible" title="回复反馈"><el-input v-model="replyContent" type="textarea" :rows="3"/><template #footer><el-button type="primary" @click="doReply">发送回复</el-button></template></el-dialog></div></template>
<script setup>
import { ref } from 'vue'
import { getFeedbacks, replyFeedback } from '@/api/system'
import { ElMessage } from 'element-plus'
const feedbacks = ref([]); const replyVisible = ref(false); const replyContent = ref(''); const currentRow = ref(null)
async function fetch() { try { const r=await getFeedbacks(); feedbacks.value=r.list||[] } catch {} }
function showReply(row) { currentRow.value=row; replyContent.value=''; replyVisible.value=true }
async function doReply() { try { await replyFeedback(currentRow.value.id,{reply:replyContent.value}); replyVisible.value=false; fetch(); ElMessage.success('已回复') } catch {} }
fetch()
</script>
