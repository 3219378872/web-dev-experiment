<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>轮播图管理</h1>
        <p>首页轮播位 · 共 {{ total }} 张 · 拖拽调整顺序</p>
      </div>
      <div class="acts">
        <a class="btn btn-primary" href="#" @click.prevent="openDialog()">＋ 新增轮播图</a>
      </div>
    </div>

    <div class="ban-grid">
      <div v-for="b in banners" :key="b.id" class="ban">
        <div
          class="preview"
          :style="`background:${b.gradient || 'linear-gradient(120deg,#FF7A45,#D62E10)'}`"
        >
          <span class="order">{{ b.sort || 1 }}</span>
          <h3>{{ b.title }}</h3>
          <p>{{ b.subtitle || '轮播图' }}</p>
        </div>
        <div class="meta">
          <div class="info">
            <div>
              跳转链接：<span class="lk">{{ b.linkUrl || '/' }}</span>
            </div>
            <div class="dim" style="margin-top: 4px">
              {{ b.timeRange || '长期有效' }} ·
              <span class="sdot" :class="b.status === 1 ? 'green' : 'gray'">{{
                b.status === 1 ? '展示中' : '已下线'
              }}</span>
            </div>
          </div>
          <div class="acts" style="flex-direction: column; align-items: flex-end; gap: 8px">
            <span class="switch" :class="{ on: b.status === 1 }" @click="toggleStatus(b)"></span>
            <span>
              <a class="lk" href="#" @click.prevent="editBanner(b)">编辑</a>
              <a class="lk" href="#" @click.prevent="del(b.id)">删除</a>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total > pageSize" class="adm-pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="(p) => fetch(p)"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑轮播图' : '新增轮播图'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="form.linkUrl" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" placeholder="选择类型" style="width: 100%">
            <el-option label="轮播图" value="carousel" />
            <el-option label="广告位" value="ad" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { getBanners, saveBanner, updateBanner, deleteBanner, updateBannerStatus } from '@/api/item';
import { ElMessage, ElMessageBox } from 'element-plus';

const banners = ref([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const dialogVisible = ref(false);
const editing = ref(null);
const form = reactive({
  title: '',
  imageUrl: '',
  linkUrl: '',
  sort: 0,
  type: 'carousel',
  subtitle: '',
  gradient: '',
});

async function fetch(p = 1) {
  page.value = p;
  try {
    const r = await getBanners({ page: p, size: pageSize.value, type: 'carousel' });
    banners.value = (r.list || []).map((b) => ({
      ...b,
      subtitle: b.subtitle || '轮播图',
      gradient: b.gradient || 'linear-gradient(120deg,#FF7A45,#D62E10)',
      timeRange: b.timeRange || '长期有效',
      enabled: b.status === 1,
    }));
    total.value = r.total || 0;
  } catch (err) {
    console.error(err);
  }
}

function openDialog() {
  editing.value = null;
  form.title = '';
  form.imageUrl = '';
  form.linkUrl = '';
  form.sort = 0;
  form.type = 'carousel';
  form.subtitle = '';
  form.gradient = '';
  dialogVisible.value = true;
}

async function save() {
  try {
    const payload = {
      ...form,
      status: editing.value ? editing.value.status : 1,
    };
    if (editing.value) {
      await updateBanner(editing.value.id, payload);
    } else {
      await saveBanner(payload);
    }
    dialogVisible.value = false;
    fetch(page.value);
    ElMessage.success('已保存');
  } catch (err) {
    console.error(err);
  }
}

function editBanner(b) {
  editing.value = b;
  Object.assign(form, b);
  dialogVisible.value = true;
}

async function toggleStatus(b) {
  const newStatus = b.status === 1 ? 0 : 1;
  try {
    await updateBannerStatus(b.id, newStatus);
    b.status = newStatus;
    b.enabled = newStatus === 1;
    ElMessage.success('已更新');
  } catch (err) {
    console.error(err);
  }
}

async function del(id) {
  try {
    await ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' });
    await deleteBanner(id);
    fetch(page.value);
    ElMessage.success('已删除');
  } catch (err) {
    console.error(err);
  }
}

onMounted(() => fetch());
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
.adm-ph .acts {
  display: flex;
  gap: 10px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 700;
  background: #fff;
  color: var(--ink);
  transition: 0.14s;
  line-height: 1;
}
.btn:hover {
  transform: translateY(-1px);
}
.btn-primary {
  background: var(--brand);
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 77, 46, 0.28);
}
.btn-primary:hover {
  background: var(--brand-600);
}

.ban-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.ban {
  border: 1px solid var(--admin-line);
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}
.ban .preview {
  height: 150px;
  position: relative;
  color: #fff;
  padding: 22px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.ban .preview .order {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  width: 26px;
  height: 26px;
  border-radius: 7px;
  display: grid;
  place-items: center;
  font-weight: 900;
  font-size: 12px;
}
.ban .preview h3 {
  font-size: 22px;
  font-weight: 900;
}
.ban .preview p {
  opacity: 0.9;
  font-size: 13px;
  margin-top: 4px;
}
.ban .meta {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.ban .meta .info {
  flex: 1;
  font-size: 12.5px;
}
.ban .meta .info .lk {
  color: var(--info);
  font-family: var(--mono);
  font-size: 11px;
}
.ban .meta .acts {
  display: flex;
  gap: 12px;
  font-size: 12.5px;
}
.ban .meta .acts .lk {
  color: var(--ink-3);
  cursor: pointer;
}
.ban .meta .acts .lk:hover {
  color: var(--brand);
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

.adm-pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
