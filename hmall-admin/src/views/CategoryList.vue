<template><div><h2>分类管理</h2><el-button type="primary" @click="showDialog(null)" style="margin:16px 0">新增分类</el-button><el-table :data="categories"><el-table-column prop="id" label="ID" width="80"/><el-table-column prop="name" label="名称"/><el-table-column prop="sortOrder" label="排序"/><el-table-column label="操作" width="200"><template #default="{row}"><el-button size="small" @click="showDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="del(row.id)">删除</el-button></template></el-table-column></el-table>
    <el-dialog v-model="dialogVisible" :title="editing? '编辑分类':'新增分类'"><el-form :model="form" label-width="80px"><el-form-item label="名称"><el-input v-model="form.name"/></el-form-item><el-form-item label="排序"><el-input-number v-model="form.sortOrder"/></el-form-item><template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-form></el-dialog></div></template>
<script setup>
import { ref, reactive } from 'vue'
import { getCategories, saveCategory, updateCategory, deleteCategory } from '@/api/item'
import { ElMessage, ElMessageBox } from 'element-plus'
const categories = ref([]); const dialogVisible = ref(false); const editing = ref(null)
const form = reactive({ name:'', sortOrder:0, status:1 })
async function fetch() { try { categories.value = await getCategories() } catch {} }
function showDialog(row) { if(row) { editing.value = row; Object.assign(form, row) } else { editing.value = null; form.name=''; form.sortOrder=0 } dialogVisible.value = true }
async function save() {
  try { if(editing.value) await updateCategory(editing.value.id, form); else await saveCategory(form); dialogVisible.value=false; fetch(); ElMessage.success('已保存') } catch {}
}
async function del(id) { try { await ElMessageBox.confirm('确认删除?'); await deleteCategory(id); fetch(); ElMessage.success('已删除') } catch {} }
fetch()
</script>
