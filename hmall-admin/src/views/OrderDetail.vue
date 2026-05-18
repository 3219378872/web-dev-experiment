<template><div><h2>订单详情</h2><el-card v-if="order" style="margin-top:16px"><p><strong>订单号:</strong> {{ order.id }}</p><p><strong>金额:</strong> &yen;{{ (order.totalFee/100).toFixed(2) }}</p><p><strong>状态:</strong> {{ ['','待支付','已支付','已发货','已完成','已取消','退款中'][order.status]||'-' }}</p><p><strong>创建时间:</strong> {{ order.createTime?.slice(0,19) }}</p><p><strong>支付时间:</strong> {{ order.payTime?.slice(0,19) || '-' }}</p><p><strong>发货时间:</strong> {{ order.consignTime?.slice(0,19) || '-' }}</p></el-card><el-empty v-else description="订单不存在"/></div></template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrders } from '@/api/order'
const route = useRoute(); const order = ref(null)
onMounted(async () => { try { const r=await getOrders({orderId:route.params.id}); order.value=r.list?.[0]||null } catch {} })
</script>
