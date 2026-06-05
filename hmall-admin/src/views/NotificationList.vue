<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>公告管理</h1>
        <p>共 {{ total }} 条公告</p>
      </div>
    </div>

    <div class="ann-grid">
      <div class="acard">
        <div class="ah">
          <h3>公告列表</h3>
        </div>
        <div v-if="loading" style="padding: 40px; text-align: center; color: var(--ink-3)">
          加载中...
        </div>
        <div v-else-if="notifications.length === 0" style="padding: 40px">
          <el-empty description="暂无公告" />
        </div>
        <template v-else>
          <table class="atable">
            <thead>
              <tr>
                <th>标题</th>
                <th>状态</th>
                <th>发布时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in notifications" :key="row.id">
                <td>
                  <div style="font-weight: 600; max-width: 300px">
                    {{ row.title }}
                  </div>
                </td>
                <td>
                  <span class="sdot" :class="row.status === 1 ? 'green' : 'gray'">{{
                    row.status === 1 ? '已发布' : '草稿'
                  }}</span>
                </td>
                <td class="dim">
                  {{ row.publishTime?.slice(0, 16) || row.createTime?.slice(0, 16) || '—' }}
                </td>
                <td class="actions">
                  <span class="lk" @click="showDialog(row)">编辑</span>
                  <span class="lk" @click="toggleStatus(row)">{{
                    row.status === 1 ? '下线' : '发布'
                  }}</span>
                  <span class="lk danger" @click="del(row.id)">删除</span>
                </td>
              </tr>
            </tbody>
          </table>
        </template>
        <div class="adm-pager">
          <span>共 {{ total }} 条</span>
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

      <aside class="acard">
        <div class="ah"><h3>＋ 发布公告</h3></div>
        <div class="ab" style="display: flex; flex-direction: column; gap: 16px">
          <div class="field">
            <label>公告标题</label>
            <el-input v-model="form.title" placeholder="请输入公告标题" />
          </div>
          <div class="field">
            <label>公告内容</label>
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="5"
              placeholder="请输入公告正文…"
            />
          </div>
          <div style="display: flex; gap: 10px">
            <el-button type="primary" style="flex: 1" :loading="saving" @click="save"
              >立即发布</el-button
            >
            <el-button class="btn-ghost" @click="saveDraft">存草稿</el-button>
          </div>
        </div>
      </aside>
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑公告' : '发布公告'" width="520px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="内容"
          ><el-input v-model="editForm.content" type="textarea" :rows="4"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doEditSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import {
  getNotifications,
  saveNotification,
  updateNotification,
  deleteNotification,
} from '@/api/system';
import { ElMessage } from 'element-plus';

const notifications = ref([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const editing = ref(null);
const form = reactive({ title: '', content: '' });
const editForm = reactive({ title: '', content: '' });

async function fetch(p) {
  if (p) page.value = p;
  loading.value = true;
  try {
    const r = await getNotifications({ page: page.value, size: pageSize.value });
    notifications.value = r.list || [];
    total.value = r.total || 0;
  } catch (err) {
    console.error(err);
    ElMessage.error('加载公告失败');
  } finally {
    loading.value = false;
  }
}

function showDialog(row) {
  if (row) {
    editing.value = row;
    Object.assign(editForm, row);
  } else {
    editing.value = null;
    editForm.title = '';
    editForm.content = '';
  }
  dialogVisible.value = true;
}

async function doEditSave() {
  try {
    if (editing.value) await updateNotification(editing.value.id, editForm);
    else await saveNotification(editForm);
    dialogVisible.value = false;
    fetch();
    ElMessage.success('已保存');
  } catch (err) {
    console.error(err);
    ElMessage.error('保存失败');
  }
}

async function save() {
  if (!form.title) {
    ElMessage.warning('请输入公告标题');
    return;
  }
  saving.value = true;
  try {
    await saveNotification({ ...form, status: 1 });
    form.title = '';
    form.content = '';
    fetch();
    ElMessage.success('已发布');
  } catch (err) {
    console.error(err);
    ElMessage.error('发布失败');
  } finally {
    saving.value = false;
  }
}

async function saveDraft() {
  if (!form.title) {
    ElMessage.warning('请输入公告标题');
    return;
  }
  saving.value = true;
  try {
    await saveNotification({ ...form, status: 0 });
    form.title = '';
    form.content = '';
    fetch();
    ElMessage.success('已存草稿');
  } catch (err) {
    console.error(err);
    ElMessage.error('保存草稿失败');
  } finally {
    saving.value = false;
  }
}

async function toggleStatus(row) {
  try {
    await updateNotification(row.id, { ...row, status: row.status === 1 ? 0 : 1 });
    fetch();
    ElMessage.success('已更新');
  } catch (err) {
    console.error(err);
    ElMessage.error('操作失败');
  }
}

async function del(id) {
  try {
    await deleteNotification(id);
    fetch();
    ElMessage.success('已删除');
  } catch (err) {
    console.error(err);
    ElMessage.error('删除失败');
  }
}

fetch();
</script>

<style scoped>
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

.ann-grid {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 16px;
  align-items: start;
}

.acard {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 14px;
}
.acard .ah {
  padding: 16px 20px;
  border-bottom: 1px solid var(--admin-line);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.acard .ah h3 {
  font-size: 15px;
  font-weight: 700;
}
.acard .ab {
  padding: 20px;
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
.atable .lk.danger {
  color: var(--danger);
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

.field {
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.field label {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-2);
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
.sdot.gray {
  color: var(--ink-3);
}

.dim {
  color: var(--ink-3);
}
</style>
