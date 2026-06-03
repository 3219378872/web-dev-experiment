<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>轮播图管理</h1>
        <p>首页轮播位 · 共 {{ banners.length }} 张</p>
      </div>
      <div class="acts">
        <el-button type="primary" size="small" @click="dialogVisible = true"
          >＋ 新增轮播图</el-button
        >
      </div>
    </div>

    <div class="ban-grid">
      <div v-for="b in banners" :key="b.id" class="ban">
        <div
          class="preview"
          :style="`background:${b.gradient || 'linear-gradient(120deg,#FF7A45,#D62E10)'}`"
        >
          <span class="order">{{ b.sortOrder || 1 }}</span>
          <h3>{{ b.title }}</h3>
          <p>{{ b.subtitle || '轮播图' }}</p>
        </div>
        <div class="meta">
          <div class="info">
            <div>
              跳转链接：<span class="lk">{{ b.linkUrl || '/' }}</span>
            </div>
            <div class="dim" style="margin-top: 4px">
              {{ b.timeRange || '长期有效' }} · <span class="sdot green">展示中</span>
            </div>
          </div>
          <div class="acts" style="flex-direction: column; align-items: flex-end; gap: 8px">
            <el-switch v-model="b.enabled" active-color="#1FA85A" />
            <span>
              <span class="lk" @click="editBanner(b)">编辑</span>
              <span class="lk" style="margin-left: 10px" @click="del(b.id)">删除</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑轮播图' : '新增轮播图'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-form-item label="链接"><el-input v-model="form.linkUrl" /></el-form-item>
        <el-form-item label="排序"
          ><el-input-number v-model="form.sortOrder" :min="0"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';

const banners = ref([
  {
    id: 1,
    title: '618 年中狂欢',
    subtitle: '全场最高直降 60%',
    gradient: 'linear-gradient(120deg,#FF7A45,#D62E10)',
    linkUrl: '/flashsale',
    timeRange: '2026-06-01 ~ 2026-06-20',
    sortOrder: 1,
    enabled: true,
  },
  {
    id: 2,
    title: '数码焕新季',
    subtitle: '耳机投影键盘低至5折',
    gradient: 'linear-gradient(120deg,#88A6B8,#5d7f93)',
    linkUrl: '/category?c=digital',
    timeRange: '长期有效',
    sortOrder: 2,
    enabled: true,
  },
  {
    id: 3,
    title: '食品生鲜节',
    subtitle: '坚果牛排水果产地直发',
    gradient: 'linear-gradient(120deg,#B0BE92,#8aa05f)',
    linkUrl: '/category?c=food',
    timeRange: '2026-05-20 ~ 2026-06-10',
    sortOrder: 3,
    enabled: true,
  },
  {
    id: 4,
    title: '新人专享礼包',
    subtitle: '注册立得90元券',
    gradient: 'linear-gradient(120deg,#B79CC4,#9d7fb0)',
    linkUrl: '/coupons',
    timeRange: '长期有效',
    sortOrder: 4,
    enabled: true,
  },
  {
    id: 5,
    title: '母婴狂欢周',
    subtitle: '奶粉纸尿裤满减',
    gradient: 'linear-gradient(120deg,#E9B775,#c9912f)',
    linkUrl: '/category?c=baby',
    timeRange: '2026-06-05 ~ 2026-06-12',
    sortOrder: 5,
    enabled: false,
  },
]);
const dialogVisible = ref(false);
const editing = ref(null);
const form = reactive({
  title: '',
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  subtitle: '',
  gradient: '',
});

function save() {
  if (editing.value) {
    const idx = banners.value.findIndex((b) => b.id === editing.value.id);
    if (idx !== -1) {
      banners.value[idx] = { ...banners.value[idx], ...form };
    }
  } else {
    banners.value.push({
      ...form,
      id: Date.now(),
      gradient: form.gradient || 'linear-gradient(120deg,#FF7A45,#D62E10)',
      timeRange: '长期有效',
      enabled: true,
    });
  }
  dialogVisible.value = false;
  ElMessage.success('已保存');
}

function editBanner(b) {
  editing.value = b;
  Object.assign(form, b);
  dialogVisible.value = true;
}

function del(id) {
  banners.value = banners.value.filter((b) => b.id !== id);
  ElMessage.success('已删除');
}
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

.dim {
  color: var(--ink-3);
}
</style>
