<template>
  <div>
    <h2>{{ isEdit ? '编辑商品' : '新增商品' }}</h2>
    <el-card style="max-width:600px;margin-top:16px"><el-form :model="form" label-width="80px">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="价格(分)"><el-input-number v-model="form.price" :min="1" /></el-form-item>
      <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
      <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
      <el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item>
      <el-form-item label="规格"><el-input v-model="form.spec" /></el-form-item>
      <el-form-item label="图片URL"><el-input v-model="form.image" /></el-form-item>
      <el-form-item><el-button type="primary" @click="save">保存</el-button></el-form-item>
    </el-form></el-card>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getItems, saveItem, updateItem } from '@/api/item'
import { ElMessage } from 'element-plus'
const route = useRoute(); const router = useRouter()
const isEdit = ref(!!route.params.id)
const form = reactive({ name:'', price:100, stock:10, category:'', brand:'', spec:'', image:'' })
onMounted(async () => {
  if (isEdit.value) {
    try { const r = await getItems({page:1,size:1,name:''})
      const list = r.list||[]; const item = list.find(i=>i.id==route.params.id)
      if (item) Object.assign(form,item) } catch {}
  }
})
async function save() {
  try {
    if (isEdit.value) await updateItem(route.params.id, form)
    else await saveItem(form)
    ElMessage.success('已保存'); router.push('/items')
  } catch {}
}
</script>
