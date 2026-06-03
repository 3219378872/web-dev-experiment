<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>分类管理</h1>
        <p>共 {{ level1Count }} 个一级分类 · {{ level2Count }} 个二级分类 · 拖拽 ⠿ 可调整排序</p>
      </div>
    </div>

    <div class="cat-grid">
      <div class="acard">
        <div class="ah">
          <h3>商品分类树</h3>
          <el-button size="small">全部展开</el-button>
        </div>
        <div class="ab ctree">
          <div v-for="cat in tree" :key="cat.id" class="lv1">
            <div class="row">
              <span class="handle">⠿</span>
              <span class="d" :style="`background:${cat.color}`">{{ cat.icon }}</span>
              {{ cat.name }}
              <span class="cnt"
                >{{ cat.count }} 件商品 · {{ cat.children?.length || 0 }} 个子类</span
              >
              <span class="acts">
                <a @click="addChild(cat)">＋ 子类</a>
                <a @click="editCat(cat)">编辑</a>
                <a @click="del(cat.id)">删除</a>
              </span>
            </div>
            <div class="lv2">
              <span v-for="sub in cat.children" :key="sub.id" class="c">
                {{ sub.name }}
                <span class="cnt">{{ sub.count }}</span>
                <span class="e" @click="editCat(sub)">✎</span>
              </span>
            </div>
          </div>
        </div>
      </div>

      <aside class="acard">
        <div class="ah">
          <h3>＋ {{ editing ? '编辑分类' : '新增分类' }}</h3>
        </div>
        <div class="ab" style="display: flex; flex-direction: column; gap: 16px">
          <div class="field">
            <label>上级分类</label>
            <el-select
              v-model="form.parentId"
              placeholder="作为一级分类"
              clearable
              style="width: 100%"
            >
              <el-option label="作为一级分类" :value="null" />
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </div>
          <div class="field">
            <label>分类名称</label>
            <el-input v-model="form.name" placeholder="如：智能穿戴" />
          </div>
          <div class="field">
            <label>分类图标</label>
            <div style="display: flex; gap: 8px; flex-wrap: wrap">
              <span
                v-for="ic in icons"
                :key="ic"
                :class="['chip', form.icon === ic ? 'on' : '']"
                @click="form.icon = ic"
                >{{ ic }}</span
              >
            </div>
          </div>
          <div class="field">
            <label>排序权重</label>
            <el-input-number
              v-model="form.sortOrder"
              :min="0"
              style="width: 100%"
              placeholder="数字越大越靠前"
            />
          </div>
          <div class="field">
            <label>分类图片</label>
            <div class="upload-grid">
              <div class="add">
                <div style="text-align: center">＋<small>上传</small></div>
              </div>
            </div>
          </div>
          <div
            style="
              display: flex;
              justify-content: space-between;
              align-items: center;
              font-size: 13px;
            "
          >
            <span>是否显示</span>
            <span
              :class="['switch', form.status === 1 ? 'on' : '']"
              @click="form.status = form.status === 1 ? 0 : 1"
            />
          </div>
          <el-button type="primary" class="btn-block" @click="save">保存分类</el-button>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { getCategories, saveCategory, updateCategory, deleteCategory } from '@/api/item';
import { ElMessage, ElMessageBox } from 'element-plus';

const categories = ref([]);
const editing = ref(null);
const form = reactive({ name: '', sortOrder: 0, status: 1, parentId: null, icon: '▣' });

const icons = ['▣', '▤', '◈', '✿', '◉', '▦'];

const tree = computed(() => {
  const map = {};
  categories.value.forEach((c) => {
    map[c.id] = {
      ...c,
      children: [],
      color: randomColor(c.name),
      count: Math.floor(Math.random() * 400) + 50,
      icon: c.icon || '▣',
    };
  });
  const roots = [];
  categories.value.forEach((c) => {
    const node = map[c.id];
    if (c.parentId && map[c.parentId]) {
      map[c.parentId].children.push(node);
    } else {
      roots.push(node);
    }
  });
  return roots;
});

const level1Count = computed(() => tree.value.length);
const level2Count = computed(() =>
  tree.value.reduce((sum, c) => sum + (c.children?.length || 0), 0)
);

function randomColor(name) {
  const palette = [
    '#FF6A45',
    '#88A6B8',
    '#B79CC4',
    '#DE9696',
    '#B0BE92',
    '#C9A66B',
    '#E9B775',
    '#7FB6A6',
  ];
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return palette[Math.abs(hash) % palette.length];
}

async function fetch() {
  try {
    categories.value = await getCategories();
  } catch (err) {
    console.error(err);
  }
}

function addChild(parent) {
  editing.value = null;
  form.name = '';
  form.sortOrder = 0;
  form.status = 1;
  form.parentId = parent.id;
  form.icon = '▣';
}

function editCat(row) {
  editing.value = row;
  Object.assign(form, row);
}

async function save() {
  try {
    if (editing.value) await updateCategory(editing.value.id, form);
    else await saveCategory(form);
    editing.value = null;
    form.name = '';
    form.sortOrder = 0;
    form.status = 1;
    form.parentId = null;
    form.icon = '▣';
    fetch();
    ElMessage.success('已保存');
  } catch (err) {
    console.error(err);
  }
}

async function del(id) {
  try {
    await ElMessageBox.confirm('确认删除?', '提示');
    await deleteCategory(id);
    fetch();
    ElMessage.success('已删除');
  } catch (err) {
    console.error(err);
  }
}

fetch();
</script>

<style scoped>
.cat-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 16px;
  align-items: start;
}

.ctree .lv1 {
  border: 1px solid var(--admin-line);
  border-radius: 10px;
  margin-bottom: 10px;
  overflow: hidden;
}
.ctree .lv1 > .row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  font-weight: 700;
}
.ctree .lv1 > .row .d {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 16px;
}
.ctree .lv2 {
  padding: 4px 16px 12px 62px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background: #fbfbfc;
  border-top: 1px solid var(--admin-line);
}
.ctree .lv2 .c {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 12.5px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.ctree .lv2 .c .cnt {
  color: var(--ink-3);
  font-size: 11px;
}
.ctree .lv2 .c .e {
  color: var(--info);
  cursor: pointer;
  font-size: 11px;
}
.ctree .lv1 .row .acts {
  margin-left: auto;
  display: flex;
  gap: 14px;
  font-size: 12.5px;
  font-weight: 400;
}
.ctree .lv1 .row .acts a {
  color: var(--ink-3);
  cursor: pointer;
}
.ctree .lv1 .row .acts a:hover {
  color: var(--brand);
}
.ctree .lv1 .row .handle {
  color: var(--line-2);
  cursor: grab;
  font-size: 16px;
}
.ctree .lv1 .row .cnt {
  color: var(--ink-3);
  font-weight: 400;
  font-size: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field label {
  font-size: 12.5px;
  color: var(--ink-2);
  font-weight: 500;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border: 1px solid var(--admin-line);
  border-radius: 999px;
  font-size: 13px;
  background: #fff;
  color: var(--ink-2);
  cursor: pointer;
}
.chip:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.chip.on {
  background: var(--brand);
  border-color: var(--brand);
  color: #fff;
  font-weight: 700;
}

.upload-grid {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.upload-grid .add {
  width: 88px;
  height: 88px;
  border: 2px dashed var(--line-2);
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: var(--ink-3);
  font-size: 24px;
  cursor: pointer;
}
.upload-grid .add:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.upload-grid .add small {
  font-size: 10px;
  display: block;
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

.btn-block {
  width: 100%;
}
</style>
