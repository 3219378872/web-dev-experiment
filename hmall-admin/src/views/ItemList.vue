<template>
  <div>
    <h2>商品管理</h2>
    <div style="margin:16px 0;display:flex;gap:12px">
      <el-input v-model="searchName" placeholder="搜索商品名称" style="width:200px" @keyup.enter="fetch" />
      <el-select v-model="searchStatus" placeholder="状态筛选" style="width:120px" @change="fetch" clearable>
        <el-option label="上架" :value="1" /><el-option label="下架" :value="2" />
      </el-select>
      <el-button type="primary" @click="$router.push('/items/add')">新增商品</el-button>
    </div>
    <el-table :data="items">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图片" width="80"><template #default="{row}"><img :src="row.image" style="width:50px;height:50px;object-fit:cover" /></template></el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column label="价格" width="120"><template #default="{row}">&yen;{{(row.price/100).toFixed(2)}}</template></el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':'info'">{{row.status===1?'上架':'下架'}}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{row}">
          <el-button size="small" @click="$router.push(`/items/${row.id}/edit`)">编辑</el-button>
          <el-button size="small" :type="row.status===1?'warning':'success'" @click="toggleStatus(row)">{{row.status===1?'下架':'上架'}}</el-button>
          <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination layout="prev,pager,next" :total="total" :page-size="size" @current-change="p=>fetch(p)" style="margin-top:16px" />
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { getItems, updateItemStatus, deleteItem } from '@/api/item'
import { ElMessage, ElMessageBox } from 'element-plus'
const items = ref([]); const total = ref(0); const size = ref(10)
const searchName = ref(''); const searchStatus = ref(null)
async function fetch(page=1) {
  try { const r = await getItems({page,size:size.value,name:searchName.value,status:searchStatus.value}); items.value=r.list||[]; total.value=r.total||0 } catch {}
}
async function toggleStatus(row) {
  const newStatus = row.status===1?2:1
  try { await updateItemStatus(row.id,newStatus); row.status=newStatus; ElMessage.success('已更新') } catch {}
}
async function del(id) {
  try { await ElMessageBox.confirm('确认删除?','提示',{type:'warning'}); await deleteItem(id); fetch(); ElMessage.success('已删除') } catch {}
}
fetch()
</script>
