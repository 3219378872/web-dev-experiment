<template>
  <div class="wrap">
    <div class="crumb">
      <router-link to="/">首页</router-link><span class="s">/</span>
      <b style="color: var(--ink)">购物车</b>
    </div>

    <div class="sec-head" style="margin: 6px 0 16px">
      <div class="t">
        <span class="bar"></span>
        <h2>我的购物车</h2>
        <span class="sub">共 {{ cartStore.items.reduce((s, i) => s + i.num, 0) }} 件商品</span>
      </div>
    </div>

    <template v-if="cartStore.items.length">
      <div class="cart-head">
        <span style="display: flex; align-items: center; gap: 8px">
          <span class="checkbox" :class="{ on: isAllSelected }" @click="toggleAll">{{
            isAllSelected ? '✓' : ''
          }}</span>
        </span>
        <span>商品信息</span>
        <span>单价</span>
        <span style="text-align: center">数量</span>
        <span style="text-align: right">小计</span>
        <span style="text-align: center">操作</span>
      </div>

      <div v-for="group in groupedItems" :key="group.shopName" class="shop-group">
        <div class="sh">
          <span class="checkbox" :class="{ on: group.allSelected }" @click="toggleGroup(group)">
            {{ group.allSelected ? '✓' : '' }}
          </span>
          <span style="font-size: 16px">🏪</span>
          <b>{{ group.shopName }}</b>
          <span class="tag tag-brand">自营</span>
        </div>

        <div v-for="item in group.items" :key="item.id" class="citem">
          <span
            class="checkbox"
            :class="{ on: cartStore.selectedIds.includes(item.id) }"
            @click="cartStore.toggleSelect(item.id)"
            >{{ cartStore.selectedIds.includes(item.id) ? '✓' : '' }}</span
          >

          <div class="prod">
            <img class="ph-img" :src="item.image || '/placeholder.png'" :alt="item.name" />
            <div>
              <div class="nm">{{ item.name }}</div>
              <div v-if="item.spec" class="spec">{{ item.spec }}</div>
              <div v-else class="spec">默认规格</div>
            </div>
          </div>

          <div class="unit">
            <span class="dim" style="font-size: 12px">¥</span>{{ (item.price / 100).toFixed(0) }}
          </div>

          <div style="text-align: center">
            <div class="qty" style="justify-content: center">
              <button @click="changeQty(item, -1)">−</button>
              <input :value="item.num" readonly />
              <button @click="changeQty(item, 1)">+</button>
            </div>
          </div>

          <div class="sub" style="text-align: right">
            ¥{{ ((item.price * item.num) / 100).toFixed(0) }}
          </div>

          <div class="del" style="text-align: center">
            <a @click="cartStore.removeItem(item.id)">删除</a>
          </div>
        </div>
      </div>

      <div class="cart-foot">
        <div class="left">
          <span class="checkbox" :class="{ on: isAllSelected }" @click="toggleAll">
            {{ isAllSelected ? '✓' : '' }}
          </span>
          <span style="font-weight: 700; color: var(--ink)">全选</span>
          <a @click="removeSelected">删除选中</a>
          <a @click="clearCart">清空购物车</a>
        </div>
        <div class="right">
          <div class="total">
            <div class="l">
              已选 <b style="color: var(--brand)">{{ cartStore.totalCount }}</b> 件
            </div>
            <div>
              <span class="l">合计：</span>
              <span class="v"
                ><span style="font-size: 18px">¥</span
                >{{ (cartStore.totalAmount / 100).toFixed(2) }}</span
              >
            </div>
          </div>
          <a
            class="btn btn-hot btn-lg"
            style="padding: 16px 44px"
            :class="{ disabled: !cartStore.totalCount }"
            @click="cartStore.totalCount ? $router.push('/order/confirm') : null"
            >去结算</a
          >
        </div>
      </div>
    </template>

    <el-empty v-else description="购物车是空的" style="margin-top: 40px" />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useCartStore } from '@/stores/cart';

const cartStore = useCartStore();

const isAllSelected = computed(() => {
  if (!cartStore.items.length) return false;
  return cartStore.selectedIds.length === cartStore.items.length;
});

function toggleAll() {
  cartStore.toggleAll();
}

function changeQty(item, delta) {
  const newNum = item.num + delta;
  if (newNum < 1) return;
  cartStore.updateItem(item.id, { num: newNum });
}

async function removeSelected() {
  if (!cartStore.selectedIds.length) return;
  for (const id of [...cartStore.selectedIds]) {
    await cartStore.removeItem(id);
  }
}

async function clearCart() {
  for (const item of [...cartStore.items]) {
    await cartStore.removeItem(item.id);
  }
}

const groupedItems = computed(() => {
  const map = new Map();
  for (const item of cartStore.items) {
    const shop = item.shopName || '好集自营';
    if (!map.has(shop)) map.set(shop, []);
    map.get(shop).push(item);
  }
  return Array.from(map.entries()).map(([shopName, items]) => ({
    shopName,
    items,
    allSelected: items.every((i) => cartStore.selectedIds.includes(i.id)),
  }));
});

function toggleGroup(group) {
  const all = group.items.every((i) => cartStore.selectedIds.includes(i.id));
  for (const item of group.items) {
    const selected = cartStore.selectedIds.includes(item.id);
    if (all && selected) {
      cartStore.toggleSelect(item.id);
    } else if (!all && !selected) {
      cartStore.toggleSelect(item.id);
    }
  }
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
.cart-head {
  display: grid;
  grid-template-columns: 44px 1fr 130px 150px 120px 90px;
  gap: 14px;
  align-items: center;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 14px 18px;
  font-size: 13px;
  color: var(--ink-3);
  margin-bottom: 12px;
}

.shop-group {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  margin-bottom: 12px;
  overflow: hidden;
}

.shop-group .sh {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 13px 18px;
  border-bottom: 1px solid var(--line);
  font-size: 13px;
}

.shop-group .sh b {
  font-weight: 700;
}

.citem {
  display: grid;
  grid-template-columns: 44px 1fr 130px 150px 120px 90px;
  gap: 14px;
  align-items: center;
  padding: 18px;
  border-bottom: 1px solid var(--bg);
}

.citem:last-child {
  border-bottom: 0;
}

.citem .prod {
  display: flex;
  gap: 14px;
  align-items: center;
}

.citem .prod .ph-img {
  width: 84px;
  height: 84px;
  border-radius: 10px;
  flex-shrink: 0;
  object-fit: cover;
  background: var(--bg-2);
}

.citem .prod .nm {
  font-size: 13.5px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.citem .prod .spec {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 6px;
  background: var(--bg);
  padding: 3px 10px;
  border-radius: 6px;
  display: inline-flex;
  gap: 6px;
}

.citem .unit {
  font-weight: 700;
}

.citem .sub {
  color: var(--price);
  font-weight: 900;
  font-size: 17px;
}

.citem .del a {
  font-size: 12.5px;
  color: var(--ink-3);
  display: block;
  cursor: pointer;
}

.citem .del a:hover {
  color: var(--brand);
}

.cart-foot {
  position: sticky;
  bottom: 0;
  display: flex;
  align-items: center;
  gap: 18px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 16px 22px;
  margin-top: 14px;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.04);
  z-index: 10;
}

.cart-foot .left {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 13px;
  color: var(--ink-2);
}

.cart-foot .left a {
  cursor: pointer;
}

.cart-foot .left a:hover {
  color: var(--brand);
}

.cart-foot .right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 20px;
}

.cart-foot .total {
  text-align: right;
}

.cart-foot .total .l {
  font-size: 13px;
  color: var(--ink-2);
}

.cart-foot .total .v {
  color: var(--price);
  font-weight: 900;
  font-size: 28px;
  letter-spacing: -1px;
}

.btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}
</style>
