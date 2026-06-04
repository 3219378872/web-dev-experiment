<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>用户管理</h1>
        <p>共 {{ total }} 名注册用户</p>
      </div>
      <div class="acts">
        <el-button class="btn-ghost" size="small">导出用户</el-button>
        <el-button type="primary" size="small">＋ 新增用户</el-button>
      </div>
    </div>

    <div class="stat-row">
      <div class="stat" style="padding: 16px">
        <div class="lab">总用户数</div>
        <div class="val" style="font-size: 24px">{{ total }}</div>
      </div>
      <div class="stat" style="padding: 16px">
        <div class="lab">活跃用户(30天)</div>
        <div class="val" style="font-size: 24px">—</div>
      </div>
      <div class="stat" style="padding: 16px">
        <div class="lab">付费会员</div>
        <div class="val" style="font-size: 24px">—</div>
      </div>
      <div class="stat" style="padding: 16px">
        <div class="lab">已禁用</div>
        <div class="val" style="font-size: 24px; color: var(--danger)">{{ frozenCount }}</div>
      </div>
    </div>

    <div class="adm-filter">
      <div class="field-inline">
        关键词
        <el-input
          v-model="filter.keyword"
          class="input"
          placeholder="用户名 / 手机 / 邮箱"
          size="small"
          style="width: 180px"
        />
      </div>
      <div class="field-inline">
        状态
        <el-select
          v-model="filter.status"
          class="select"
          placeholder="全部"
          size="small"
          style="width: 120px"
        >
          <el-option label="全部" value="" />
          <el-option label="正常" value="NORMAL" />
          <el-option label="已禁用" value="FROZEN" />
        </el-select>
      </div>
      <div class="grow" />
      <el-button type="primary" size="small" @click="fetch">查询</el-button>
      <el-button class="btn-ghost" size="small" @click="resetFilter">重置</el-button>
    </div>

    <div class="acard">
      <table class="atable">
        <thead>
          <tr>
            <th>用户</th>
            <th>手机号</th>
            <th>邮箱</th>
            <th>注册时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, i) in users" :key="row.id">
            <td>
              <span class="u-cell">
                <span class="u-av" :style="`background:${avatarColor(i)}`">{{
                  row.username ? row.username[0] : '?'
                }}</span>
                <span>
                  <div class="nm">{{ row.username }}</div>
                  <div class="sub">ID: {{ row.id }}</div>
                </span>
              </span>
            </td>
            <td>{{ row.phone || '—' }}</td>
            <td>{{ row.email || '—' }}</td>
            <td class="dim">{{ row.createTime?.slice(0, 10) || '—' }}</td>
            <td>
              <span class="sdot" :class="row.status === 'NORMAL' ? 'green' : 'red'">{{
                row.status === 'NORMAL' ? '正常' : '已禁用'
              }}</span>
            </td>
            <td class="actions">
              <span class="lk" @click="$message.info('详情')">详情</span>
              <span class="lk" @click="toggle(row)">{{
                row.status === 'FROZEN' ? '启用' : '禁用'
              }}</span>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="adm-pager">
        <span>共 {{ total }} 条 · 每页 {{ pageSize }} 条</span>
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          background
          layout="prev, pager, next"
          @current-change="fetch"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { getUsers, toggleUserStatus } from '@/api/user';
import { ElMessage } from 'element-plus';

const users = ref([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const filter = reactive({ keyword: '', status: '' });

const avatarColors = [
  '#88A6B8',
  '#DE9696',
  '#B0BE92',
  '#C9A66B',
  '#B79CC4',
  '#7FB6A6',
  '#E9B775',
  '#92ADBD',
  '#A7906B',
  '#9d7fb0',
];
function avatarColor(i) {
  return avatarColors[i % avatarColors.length];
}

const frozenCount = computed(() => users.value.filter((u) => u.status === 'FROZEN').length);

async function fetch() {
  try {
    const r = await getUsers({ page: page.value, pageSize: pageSize.value, ...filter });
    users.value = r.list || [];
    total.value = r.total || 0;
  } catch (err) {
    console.error(err);
  }
}

function resetFilter() {
  filter.keyword = '';
  filter.status = '';
  page.value = 1;
  fetch();
}

async function toggle(row) {
  try {
    await toggleUserStatus(row.id);
    row.status = row.status === 'FROZEN' ? 'NORMAL' : 'FROZEN';
    ElMessage.success('已更新');
  } catch (err) {
    console.error(err);
  }
}

fetch();
</script>

<style scoped>
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 14px;
  padding: 20px;
  position: relative;
  overflow: hidden;
}
.stat .lab {
  font-size: 13px;
  color: var(--ink-3);
}
.stat .val {
  font-size: 28px;
  font-weight: 900;
  letter-spacing: -1px;
  margin-top: 4px;
}

.adm-filter {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 14px;
  padding: 16px 20px;
  margin-bottom: 16px;
  display: flex;
  gap: 14px;
  align-items: center;
  flex-wrap: wrap;
}
.adm-filter .field-inline {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-2);
}
.adm-filter .grow {
  flex: 1;
}

.acard {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 14px;
}

.atable {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.atable thead th {
  background: var(--admin-bg);
  color: var(--ink-3);
  font-weight: 700;
  text-align: left;
  padding: 12px 16px;
  font-size: 12px;
  white-space: nowrap;
  border-bottom: 1px solid var(--admin-line);
}
.atable tbody td {
  padding: 13px 16px;
  border-bottom: 1px solid var(--admin-line);
  vertical-align: middle;
}
.atable tbody tr:hover {
  background: #fbfbfc;
}
.atable tbody tr:last-child td {
  border-bottom: 0;
}
.atable .actions {
  display: flex;
  gap: 10px;
}
.atable .lk {
  color: var(--info);
  cursor: pointer;
  font-size: 12.5px;
}
.atable .lk:hover {
  text-decoration: underline;
}

.adm-pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-top: 1px solid var(--admin-line);
  font-size: 13px;
  color: var(--ink-3);
}

.u-av {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  flex-shrink: 0;
}
.u-cell {
  display: flex;
  align-items: center;
  gap: 11px;
}
.u-cell .nm {
  font-weight: 600;
}
.u-cell .sub {
  font-size: 11.5px;
  color: var(--ink-3);
}

.sdot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
}
.sdot::before {
  content: '';
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}
.sdot.green {
  color: var(--success);
}
.sdot.red {
  color: var(--danger);
}

.adm-ph {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20px;
}
.adm-ph h1 {
  font-size: 22px;
  font-weight: 900;
  letter-spacing: -0.5px;
}
.adm-ph p {
  color: var(--ink-3);
  font-size: 13px;
  margin-top: 4px;
}
.adm-ph .acts {
  display: flex;
  gap: 10px;
}

.dim {
  color: var(--ink-3);
}
</style>
