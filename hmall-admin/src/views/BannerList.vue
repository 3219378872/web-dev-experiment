<template><div><h2>轮播图管理</h2><el-button type="primary" @click="dialogVisible=true" style="margin:16px 0">新增轮播图</el-button><el-table :data="banners"><el-table-column label="预览" width="150"><template #default="{row}"><img :src="row.imageUrl || '/placeholder.png'" alt="轮播图" style="width:120px;height:60px;object-fit:cover"/></template></el-table-column><el-table-column prop="title" label="标题"/><el-table-column prop="sortOrder" label="排序"/><el-table-column label="操作" width="150"><template #default="{row}"><el-button size="small" type="danger" @click="del(row.id)">删除</el-button></template></el-table-column></el-table>
    <el-dialog v-model="dialogVisible" title="新增轮播图"><el-form :model="form" label-width="80px"><el-form-item label="标题"><el-input v-model="form.title"/></el-form-item><el-form-item label="图片URL"><el-input v-model="form.imageUrl"/></el-form-item><el-form-item label="链接"><el-input v-model="form.linkUrl"/></el-form-item><el-form-item label="排序"><el-input-number v-model="form.sortOrder"/></el-form-item><template #footer><el-button type="primary" @click="save">保存</el-button></template></el-form></el-dialog></div></template>
<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
const banners = ref([]); const dialogVisible = ref(false)
const form = reactive({ title:'', imageUrl:'', linkUrl:'', sortOrder:0 })
function save() { banners.value.push({...form,id:Date.now()}); dialogVisible.value=false; ElMessage.success('已保存') }
function del(id) { banners.value = banners.value.filter(b=>b.id!==id); ElMessage.success('已删除') }
</script>
