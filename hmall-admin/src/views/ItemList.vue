<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>商品管理</h1>
        <p>
          共 {{ stats.total.toLocaleString() }} 件商品 · 在售 {{ stats.onSale }} · 已下架
          {{ stats.offSale }}
        </p>
      </div>
      <div class="acts">
        <el-button size="small">批量导入</el-button>
        <el-button type="primary" size="small" @click="$router.push('/items/add')"
          >＋ 发布商品</el-button
        >
      </div>
    </div>

    <div class="acard" style="margin-bottom: 16px">
      <div class="tabs" style="padding: 0 16px">
        <button
          v-for="t in tabs"
          :key="t.value"
          :class="{ active: activeTab === t.value }"
          @click="switchTab(t.value)"
        >
          {{ t.label }}
          <span v-if="t.badge !== undefined" :style="t.badgeStyle">({{ t.badge }})</span>
        </button>
      </div>
    </div>

    <div class="adm-filter">
      <div class="field-inline">
        商品名
        <el-input
          v-model="searchName"
          placeholder="输入商品名称 / 编号"
          style="width: 200px"
          @keyup.enter="fetch"
        />
      </div>
      <div class="field-inline">
        分类
        <el-select
          v-model="searchCategory"
          placeholder="全部分类"
          clearable
          style="width: 160px"
          @change="fetch"
        >
          <el-option label="全部分类" value="" />
          <el-option v-for="cat in categoryOptions" :key="cat" :label="cat" :value="cat" />
        </el-select>
      </div>
      <div class="field-inline">
        价格
        <el-input v-model="minPrice" placeholder="¥最低" style="width: 90px" @keyup.enter="fetch" />
        <span>-</span>
        <el-input v-model="maxPrice" placeholder="¥最高" style="width: 90px" @keyup.enter="fetch" />
      </div>
      <div class="grow" />
      <el-button type="primary" size="small" @click="fetch">查询</el-button>
      <el-button size="small" @click="resetFilter">重置</el-button>
    </div>

    <div class="acard">
      <div class="ah">
        <h3>商品列表</h3>
        <div style="display: flex; gap: 8px">
          <el-button size="small" @click="batchOnSale">批量上架</el-button>
          <el-button size="small" @click="batchOffSale">批量下架</el-button>
          <el-button size="small" style="color: var(--danger)" @click="batchDelete"
            >批量删除</el-button
          >
        </div>
      </div>
      <table class="atable">
        <thead>
          <tr>
            <th style="width: 30px"><el-checkbox v-model="allChecked" /></th>
            <th>商品</th>
            <th>分类</th>
            <th>价格</th>
            <th>库存</th>
            <th>销量</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in items" :key="row.id">
            <td><el-checkbox v-model="row.checked" /></td>
            <td>
              <span class="prod-cell">
                <div
                  class="ph"
                  :style="`background:url(${row.image || '/placeholder.png'}) center/cover`"
                />
                <span>
                  <div class="nm">{{ row.name }}</div>
                  <div class="id">SKU: HJ{{ (100000 + (row.id || 0)).toString().slice(1) }}</div>
                </span>
              </span>
            </td>
            <td class="dim">{{ row.category || '-' }}</td>
            <td>
              <span class="amount">&yen;{{ (row.price / 100).toFixed(2) }}</span>
              <div class="dim" style="font-size: 11px; text-decoration: line-through">
                &yen;{{ (((row.price || 0) * 1.5) / 100).toFixed(2) }}
              </div>
            </td>
            <td>
              <span :style="row.stock < 20 ? 'color:var(--danger);font-weight:700' : ''">
                {{ row.stock }}
                <span v-if="row.stock < 20"> ⚠</span>
              </span>
            </td>
            <td>{{ formatSales(row.sold || 0) }}</td>
            <td>
              <span :class="['switch', row.status === 1 ? 'on' : '']" @click="toggleStatus(row)" />
              <div class="dim" style="font-size: 11px; margin-top: 3px">
                {{ row.status === 1 ? '在售' : '已下架' }}
              </div>
            </td>
            <td class="actions">
              <span class="lk" @click="$router.push(`/items/${row.id}/edit`)">编辑</span>
              <span class="lk" @click="toggleStatus(row)">{{
                row.status === 1 ? '下架' : '上架'
              }}</span>
              <span class="lk danger" @click="del(row.id)">删除</span>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="adm-pager">
        <span>共 {{ total.toLocaleString() }} 条 · 每页 {{ size }} 条</span>
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="(p) => fetch(p)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import {
  getItems,
  updateItemStatus,
  batchUpdateItemStatus,
  deleteItem,
  batchDeleteItems,
  getItemStats,
  getCategories,
} from '@/api/item';
import { ElMessage, ElMessageBox } from 'element-plus';

const items = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(12);
const searchName = ref('');
const searchCategory = ref('');
const minPrice = ref('');
const maxPrice = ref('');
const activeTab = ref('all');
const categoryOptions = ref([]);
const stats = ref({ total: 0, onSale: 0, offSale: 0, lowStock: 0 });

const tabs = computed(() => [
  { label: '全部', value: 'all', badge: stats.value.total },
  { label: '在售中', value: 'on', badge: stats.value.onSale },
  { label: '已下架', value: 'off', badge: stats.value.offSale },
  { label: '库存预警', value: 'low', badge: stats.value.lowStock },
  { label: '待审核', value: 'pending', badge: 0 },
]);

const allChecked = computed({
  get: () => items.value.length > 0 && items.value.every((i) => i.checked),
  set: (v) =>
    items.value.forEach((i) => {
      i.checked = v;
    }),
});

const selectedIds = computed(() => items.value.filter((i) => i.checked).map((i) => i.id));

function switchTab(val) {
  activeTab.value = val;
  if (val === 'on') searchCategory.value = '';
  if (val === 'off') searchCategory.value = '';
  fetch(1);
}

function resetFilter() {
  searchName.value = '';
  searchCategory.value = '';
  minPrice.value = '';
  maxPrice.value = '';
  activeTab.value = 'all';
  fetch(1);
}

function formatSales(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return n;
}

async function fetchStats() {
  try {
    const r = await getItemStats();
    if (r) {
      stats.value = {
        total: r.total || 0,
        onSale: r.onSale || 0,
        offSale: r.offSale || 0,
        lowStock: r.lowStock || 0,
      };
    }
  } catch (err) {
    console.error(err);
  }
}

async function fetchCategories() {
  try {
    const list = await getCategories();
    categoryOptions.value = (list || []).map((c) => c.name).filter(Boolean);
  } catch (err) {
    console.error(err);
  }
}

async function fetch(p = 1) {
  page.value = p;
  try {
    const params = { page: p, size: size.value, name: searchName.value };
    if (searchCategory.value) params.category = searchCategory.value;
    if (minPrice.value) params.minPrice = Math.round(parseFloat(minPrice.value) * 100);
    if (maxPrice.value) params.maxPrice = Math.round(parseFloat(maxPrice.value) * 100);
    if (activeTab.value === 'on') params.status = 1;
    if (activeTab.value === 'off') params.status = 2;
    const r = await getItems(params);
    const list = (r.list || []).map((i) => ({ ...i, checked: false }));
    items.value = list;
    total.value = r.total || 0;
  } catch (err) {
    console.error(err);
  }
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 2 : 1;
  try {
    await updateItemStatus(row.id, newStatus);
    row.status = newStatus;
    ElMessage.success('已更新');
    fetchStats();
  } catch (err) {
    console.error(err);
  }
}

async function del(id) {
  try {
    await ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' });
    await deleteItem(id);
    fetch(page.value);
    fetchStats();
    ElMessage.success('已删除');
  } catch (err) {
    console.error(err);
  }
}

async function batchOnSale() {
  const ids = selectedIds.value;
  if (ids.length === 0) {
    ElMessage.warning('请先选择商品');
    return;
  }
  try {
    await batchUpdateItemStatus(ids, 1);
    ElMessage.success('批量上架成功');
    fetch(page.value);
    fetchStats();
  } catch (err) {
    console.error(err);
  }
}

async function batchOffSale() {
  const ids = selectedIds.value;
  if (ids.length === 0) {
    ElMessage.warning('请先选择商品');
    return;
  }
  try {
    await batchUpdateItemStatus(ids, 2);
    ElMessage.success('批量下架成功');
    fetch(page.value);
    fetchStats();
  } catch (err) {
    console.error(err);
  }
}

async function batchDelete() {
  const ids = selectedIds.value;
  if (ids.length === 0) {
    ElMessage.warning('请先选择商品');
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${ids.length} 件商品?`, '提示', { type: 'warning' });
    await batchDeleteItems(ids);
    ElMessage.success('批量删除成功');
    fetch(page.value);
    fetchStats();
  } catch (err) {
    console.error(err);
  }
}

onMounted(() => {
  fetch();
  fetchStats();
  fetchCategories();
});
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 0;
  border-bottom: 1px solid var(--admin-line);
}
.tabs button {
  background: transparent;
  border: 0;
  padding: 14px 18px;
  font-size: 13.5px;
  color: var(--ink-2);
  cursor: pointer;
  position: relative;
  font-family: inherit;
}
.tabs button.active {
  color: var(--brand);
  font-weight: 700;
}
.tabs button.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 12px;
  right: 12px;
  height: 2px;
  background: var(--brand);
  border-radius: 2px 2px 0 0;
}
.tabs button:hover {
  color: var(--brand);
}

.switch {
  width: 40px;
  height: 22px;
  border-radius: 999px;
  background: var(--line-2);
  position: relative;
  cursor: pointer;
  transition: 0.18s;
  display: inline-block;
}
.switch::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  transition: 0.18s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}
.switch.on {
  background: var(--success);
}
.switch.on::after {
  left: 20px;
}

.dim {
  color: var(--ink-3);
}

.grow {
  flex: 1;
}
</style>
