<template>
  <div class="order-list">
    <h2>我的订单</h2>
    <el-tabs v-model="activeStatus" @tab-change="fetchOrders">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="1" />
      <el-tab-pane label="待发货" name="2" />
      <el-tab-pane label="待收货" name="3" />
      <el-tab-pane label="已完成" name="4" />
      <el-tab-pane label="已取消" name="5" />
    </el-tabs>
    <el-table :data="orders" v-if="orders.length">
      <el-table-column label="订单号" prop="id" width="180" />
      <el-table-column label="金额" width="120"><template #default="{ row }">&yen;{{ (row.totalFee/100).toFixed(2) }}</template></el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ ['','待支付','已支付','已发货','已完成','已取消','退款中'][row.status] || '-' }}</template>
      </el-table-column>
      <el-table-column label="时间" width="180"><template #default="{ row }">{{ row.createTime?.slice(0,19) }}</template></el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button v-if="row.status===1" type="primary" size="small" @click="cancelOrder(row.id)">取消</el-button>
          <el-button v-if="row.status===3" type="success" size="small" @click="confirmOrder(row.id)">确认收货</el-button>
          <el-button v-if="row.status===2||row.status===3" type="warning" size="small" @click="refundOrder(row.id)">退款</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else description="暂无订单" />
    <el-pagination v-if="total > size" layout="prev, pager, next" :total="total" :page-size="size" @current-change="page=>fetchOrders(page)" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getOrders, cancelOrder, confirmOrder, refundOrder } from '@/api/order'
import { ElMessage } from 'element-plus'

const orders = ref([])
const activeStatus = ref('')
const total = ref(0)
const size = ref(10)

async function fetchOrders(page = 1) {
  try {
    const data = await getOrders({ page, size: size.value, status: activeStatus.value || undefined })
    orders.value = data?.list || []; total.value = data?.total || 0
  } catch {}
}

async function cancel(id) { try { await cancelOrder(id); fetchOrders(); ElMessage.success('已取消') } catch {} }
async function confirm(id) { try { await confirmOrder(id); fetchOrders(); ElMessage.success('已确认收货') } catch {} }
async function refund(id) { try { await refundOrder(id); fetchOrders(); ElMessage.success('退款申请已提交') } catch {} }

fetchOrders()
</script>
