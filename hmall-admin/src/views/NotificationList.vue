<template><div><h2>公告管理</h2><el-button type="primary" @click="showDialog(null)" style="margin:16px 0">发布公告</el-button><el-table :data="notifications"><el-table-column prop="title" label="标题"/><el-table-column prop="publishTime" label="发布时间" width="180"/><el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':'info'">{{row.status===1?'发布':'下架'}}</el-tag></template></el-table-column><el-table-column label="操作" width="200"><template #default="{row}"><el-button size="small" @click="showDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="del(row.id)">删除</el-button></template></el-table-column></el-table>
    <el-dialog v-model="dialogVisible" :title="editing?'编辑公告':'发布公告'"><el-form :model="form" label-width="80px"><el-form-item label="标题"><el-input v-model="form.title"/></el-form-item><el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4"/></el-form-item><template #footer><el-button type="primary" @click="save">保存</el-button></template></el-form></el-dialog></div></template>
<script setup>
import { ref, reactive } from 'vue'
import { getNotifications, saveNotification, updateNotification, deleteNotification } from '@/api/system'
import { ElMessage } from 'element-plus'
const notifications = ref([]); const dialogVisible = ref(false); const editing = ref(null)
const form = reactive({ title:'', content:'', publishTime:'' })
async function fetch() { try { const r = await getNotifications(); notifications.value = r.list||[] } catch {} }
function showDialog(row) {
  if(row) { editing.value=row; Object.assign(form,row) } else { editing.value=null; form.title=''; form.content=''; form.publishTime=new Date().toISOString() }
  dialogVisible.value=true
}
async function save() {
  try { if(editing.value) await updateNotification(editing.value.id,form); else await saveNotification(form); dialogVisible.value=false; fetch(); ElMessage.success('已保存') } catch {}
}
async function del(id) { try { await deleteNotification(id); fetch(); ElMessage.success('已删除') } catch {} }
fetch()
</script>
