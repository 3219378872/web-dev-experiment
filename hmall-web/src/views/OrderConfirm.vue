<template>
  <div class="order-confirm">
    <h2>确认订单</h2>
    <el-card class="section-card"><template #header>选择收货地址</template>
      <div v-if="addresses.length">
        <el-radio-group v-model="selectedAddress">
          <div v-for="addr in addresses" :key="addr.id" style="margin:8px 0">
            <el-radio :label="addr.id">
              {{ addr.receiverName }} {{ addr.phone }} {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
              <el-tag v-if="addr.isDefault" size="small">默认</el-tag>
            </el-radio>
          </div>
        </el-radio-group>
      </div>
      <el-empty v-else description="暂无收货地址，请先添加" />
    </el-card>

    <el-card class="section-card"><template #header>商品清单</template>
      <el-table :data="cartStore.selectedItems" v-if="cartStore.selectedItems.length">
        <el-table-column label="商品">
          <template #default="{ row }"><div style="display:flex;align-items:center;gap:8px"><img :src="row.image" style="width:40px;height:40px;object-fit:cover" />{{ row.name }}</div></template>
        </el-table-column>
        <el-table-column label="单价"><template #default="{ row }">&yen;{{ (row.price/100).toFixed(2) }}</template></el-table-column>
        <el-table-column label="数量"><template #default="{ row }">x{{ row.num }}</template></el-table-column>
        <el-table-column label="小计"><template #default="{ row }">&yen;{{ (row.price*row.num/100).toFixed(2) }}</template></el-table-column>
      </el-table>
    </el-card>

    <div class="submit-bar">
      <span>合计: &yen;{{ (cartStore.totalAmount / 100).toFixed(2) }}</span>
      <el-button type="primary" size="large" @click="submitOrder" :disabled="!selectedAddress">提交订单</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import { getAddresses } from '@/api/address'
import { createOrder } from '@/api/order'

const router = useRouter()
const cartStore = useCartStore()
const addresses = ref([])
const selectedAddress = ref(null)

onMounted(async () => {
  try { addresses.value = await getAddresses(); if (addresses.value.length) selectedAddress.value = addresses.value.find(a => a.isDefault)?.id || addresses.value[0].id } catch {}
  try { await cartStore.fetchCart() } catch {}
})

async function submitOrder() {
  const details = cartStore.selectedItems.map(i => ({ itemId: i.itemId || i.id, num: i.num }))
  try {
    const orderId = await createOrder({ paymentType: 2, details })
    ElMessage.success('下单成功')
    router.push('/orders')
  } catch {}
}
</script>

<style scoped>
.section-card { margin-bottom: 16px; }
.submit-bar { display: flex; justify-content: flex-end; align-items: center; gap: 20px; padding: 16px; background: #fff; border-radius: 8px; font-size: 20px; font-weight: bold; }
</style>
