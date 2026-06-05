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
          <a class="btn btn-ghost btn-sm" @click="expandAll">全部展开</a>
        </div>
        <div class="ab ctree">
          <div v-for="cat in tree" :key="cat.id" class="lv1">
            <div class="row">
              <span class="handle">⠿</span>
              <span class="d" :style="`background:${cat.color || '#999'}`">{{
                cat.icon || '▣'
              }}</span>
              {{ cat.name }}
              <span class="cnt">{{ cat.children?.length || 0 }} 个子类</span>
              <span class="acts">
                <a @click="addChild(cat)">＋ 子类</a>
                <a @click="editCat(cat)">编辑</a>
                <a @click="del(cat.id)">删除</a>
              </span>
            </div>
            <div class="lv2">
              <span v-for="sub in cat.children" :key="sub.id" class="c">
                {{ sub.name }}
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
            <select v-model="form.parentId" class="select">
              <option :value="null">作为一级分类</option>
              <option v-for="c in categories.filter((x) => !x.parentId)" :key="c.id" :value="c.id">
                {{ c.name }}
              </option>
            </select>
          </div>
          <div class="field">
            <label>分类名称</label>
            <input v-model="form.name" class="input" placeholder="如：智能穿戴" />
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
            <input
              v-model="form.sortOrder"
              class="input"
              type="number"
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
          <button class="btn btn-primary btn-block" @click="save">保存分类</button>
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
    map[c.id] = { ...c, children: [] };
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

const level1Count = computed(() => categories.value.filter((c) => !c.parentId).length);
const level2Count = computed(() => categories.value.filter((c) => c.parentId).length);

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

function expandAll() {
  // 全部展开功能占位，后续可配合 el-tree 或手风琴组件实现
  ElMessage.info('分类已展开');
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
.cat-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
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

.select {
  width: 100%;
  border: 1px solid var(--line-2);
  border-radius: var(--radius-sm);
  padding: 8px 12px;
  font-size: 13px;
  font-family: inherit;
  background: #fff;
  color: var(--ink);
  outline: 0;
  transition: 0.14s;
}
.select:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px var(--brand-softer);
}

.input {
  width: 100%;
  border: 1px solid var(--line-2);
  border-radius: var(--radius-sm);
  padding: 8px 12px;
  font-size: 13px;
  font-family: inherit;
  background: #fff;
  color: var(--ink);
  outline: 0;
  transition: 0.14s;
}
.input:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px var(--brand-softer);
}
.input::placeholder {
  color: var(--ink-3);
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

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: 0.14s;
  font-family: inherit;
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
.btn-ghost {
  background: #fff;
  border-color: var(--line-2);
  color: var(--ink);
}
.btn-ghost:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.btn-sm {
  padding: 7px 13px;
  font-size: 12.5px;
}
.btn-block {
  display: flex;
  width: 100%;
  padding: 11px 13px;
}
</style>
