<template>
  <div>
    <h2>订单管理</h2>
    <el-table :data="orders" style="margin-top:16px">
      <el-table-column prop="id" label="订单号" width="180" />
      <el-table-column label="金额" width="120"><template #default="{row}">&yen;{{(row.totalFee/100).toFixed(2)}}</template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="{row}">{{ ['','待支付','已支付','已发货','已完成','已取消','退款中'][row.status]||'-' }}</template></el-table-column>
      <el-table-column label="时间" width="180"><template #default="{row}">{{ row.createTime?.slice(0,19) }}</template></el-table-column>
      <el-table-column label="操作">
        <template #default="{row}">
          <el-button size="small" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
          <el-button v-if="row.status===2" size="small" type="primary" @click="showShip(row)">发货</el-button>
          <el-button size="small" type="warning" @click="updateStatus(row,5)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="shipVisible" title="填写物流单号"><el-input v-model="trackingNumber" placeholder="物流单号" />
      <template #footer><el-button @click="shipVisible=false">取消</el-button><el-button type="primary" @click="doShip">确认发货</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { getOrders, shipOrder, updateOrderStatus } from '@/api/order'
import { ElMessage } from 'element-plus'
const orders = ref([]); const shipVisible = ref(false); const trackingNumber = ref(''); const currentOrder = ref(null)
async function fetch() { try { const r=await getOrders({page:1,size:50}); orders.value=r.list||[] } catch {} }
function showShip(row) { currentOrder.value=row; shipVisible.value=true }
async function doShip() { try { await shipOrder(currentOrder.value.id,trackingNumber.value); shipVisible.value=false; fetch(); ElMessage.success('已发货') } catch {} }
async function updateStatus(row,status) { try { await updateOrderStatus(row.id,status); row.status=status; ElMessage.success('已更新') } catch {} }
fetch()
</script>
