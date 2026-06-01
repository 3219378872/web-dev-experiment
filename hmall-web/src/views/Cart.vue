<template>
  <div class="cart-page">
    <h2>购物车</h2>
    <el-table
      v-if="cartStore.items.length"
      :data="cartStore.items"
      @selection-change="onSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column label="商品" width="400">
        <template #default="{ row }">
          <div style="display: flex; align-items: center; gap: 12px">
            <img
              :src="row.image || '/placeholder.png'"
              style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px"
              :alt="row.name"
            />
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="120"
        ><template #default="{ row }"
          >&yen;{{ (row.price / 100).toFixed(2) }}</template
        ></el-table-column
      >
      <el-table-column label="数量" width="160">
        <template #default="{ row }"
          ><el-input-number
            v-model="row.num"
            :min="1"
            size="small"
            @change="cartStore.updateItem(row.id, { num: row.num })"
        /></template>
      </el-table-column>
      <el-table-column label="小计" width="120"
        ><template #default="{ row }"
          >&yen;{{ ((row.price * row.num) / 100).toFixed(2) }}</template
        ></el-table-column
      >
      <el-table-column label="操作" width="100">
        <template #default="{ row }"
          ><el-button type="danger" text @click="cartStore.removeItem(row.id)"
            >删除</el-button
          ></template
        >
      </el-table-column>
    </el-table>
    <el-empty v-else description="购物车是空的" />
    <div v-if="cartStore.items.length" class="cart-bottom">
      <span
        >已选 {{ cartStore.totalCount }} 件，合计: &yen;{{
          (cartStore.totalAmount / 100).toFixed(2)
        }}</span
      >
      <el-button
        type="primary"
        size="large"
        :disabled="!cartStore.totalCount"
        @click="$router.push('/order/confirm')"
        >结算</el-button
      >
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useCartStore } from '@/stores/cart';
const cartStore = useCartStore();

function onSelectionChange(rows) {
  cartStore.selectedIds = rows.map((r) => r.id);
}
onMounted(async () => {
  try {
    await cartStore.fetchCart();
  } catch (err) {
    console.error(err);
  }
});
</script>

<style scoped>
.cart-bottom {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
}
</style>
