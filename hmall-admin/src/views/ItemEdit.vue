<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>{{ isEdit ? '编辑商品' : '新增商品' }}</h1>
        <p>SKU: HJ{{ skuSuffix }} · 最后更新 {{ lastUpdate }}</p>
      </div>
      <div class="acts">
        <el-button size="small" @click="$router.push('/items')">← 返回列表</el-button>
        <el-button size="small">预览</el-button>
        <el-button type="primary" size="small" @click="save">保存并上架</el-button>
      </div>
    </div>

    <div class="edit-grid">
      <div>
        <div class="acard" style="margin-bottom: 16px">
          <div class="ah"><h3>基本信息</h3></div>
          <div class="ab">
            <div class="form-grid">
              <div class="field full">
                <label>商品名称</label>
                <el-input v-model="form.name" placeholder="输入商品名称" />
              </div>
              <div class="field">
                <label>商品分类</label>
                <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
                  <el-option
                    label="手机数码 / 影音娱乐 / 蓝牙耳机"
                    value="手机数码 / 影音娱乐 / 蓝牙耳机"
                  />
                  <el-option label="家用电器" value="家用电器" />
                  <el-option label="食品生鲜" value="食品生鲜" />
                </el-select>
              </div>
              <div class="field">
                <label>品牌</label>
                <el-select v-model="form.brand" placeholder="选择品牌" style="width: 100%">
                  <el-option label="好集严选 HAOJI" value="好集严选 HAOJI" />
                </el-select>
              </div>
              <div class="field full">
                <label>商品卖点（副标题）</label>
                <el-input v-model="form.subTitle" placeholder="输入副标题" />
              </div>
            </div>
          </div>
        </div>

        <div class="acard" style="margin-bottom: 16px">
          <div class="ah"><h3>价格与库存</h3></div>
          <div class="ab">
            <div class="form-grid">
              <div class="field">
                <label>销售价（¥）</label>
                <el-input v-model="form.price" />
              </div>
              <div class="field">
                <label>市场价（¥）</label>
                <el-input v-model="form.marketPrice" />
              </div>
              <div class="field">
                <label>总库存</label>
                <el-input v-model="form.stock" />
              </div>
              <div class="field">
                <label>库存预警值</label>
                <el-input v-model="form.stockAlert" />
              </div>
            </div>
          </div>
        </div>

        <div class="acard" style="margin-bottom: 16px">
          <div class="ah">
            <h3>规格设置 <span class="sub">SKU 多规格</span></h3>
            <el-button size="small">＋ 添加规格</el-button>
          </div>
          <div class="ab">
            <div style="margin-bottom: 14px">
              <div class="dim" style="font-size: 12px; margin-bottom: 8px">颜色</div>
              <div style="display: flex; gap: 8px">
                <span class="chip on">曜石黑</span>
                <span class="chip on">云母白</span>
                <span class="chip on">雾霾蓝</span>
                <span class="chip">＋</span>
              </div>
            </div>
            <div>
              <div class="dim" style="font-size: 12px; margin-bottom: 8px">版本</div>
              <div style="display: flex; gap: 8px">
                <span class="chip on">标准版</span>
                <span class="chip on">降噪增强版</span>
                <span class="chip on">游戏专业版</span>
                <span class="chip">＋</span>
              </div>
            </div>
          </div>
        </div>

        <div class="acard">
          <div class="ah"><h3>商品详情</h3></div>
          <div class="ab">
            <div style="border: 1px solid var(--line); border-radius: 8px; overflow: hidden">
              <div
                style="
                  background: var(--bg);
                  padding: 8px 12px;
                  display: flex;
                  gap: 14px;
                  font-size: 14px;
                  color: var(--ink-2);
                  border-bottom: 1px solid var(--line);
                "
              >
                <b>B</b><i>I</i><span>U</span><span>🖼</span><span>🔗</span><span>≡</span
                ><span>表格</span>
              </div>
              <el-input
                v-model="form.detail"
                type="textarea"
                :rows="6"
                style="border: 0; border-radius: 0"
                placeholder="输入商品详情"
              />
            </div>
          </div>
        </div>
      </div>

      <aside class="stick">
        <div class="acard" style="margin-bottom: 16px">
          <div class="ah"><h3>商品图片</h3></div>
          <div class="ab">
            <div class="dim" style="font-size: 12px; margin-bottom: 10px">主图（首张为封面）</div>
            <div class="upload-grid">
              <div
                v-for="(img, idx) in imageList"
                :key="idx"
                class="ph"
                :class="getPlaceholderClass(img, idx)"
                :style="getImageStyle(img)"
              >
                <span class="x" @click="removeImage(idx)">✕</span>
              </div>
              <div class="add" @click="addImage">
                <div style="text-align: center">＋<small>上传</small></div>
              </div>
            </div>
            <p class="dim" style="font-size: 11px; margin-top: 10px">
              建议 800×800，支持 JPG/PNG，最多 8 张
            </p>
          </div>
        </div>

        <div class="acard">
          <div class="ah"><h3>发布设置</h3></div>
          <div class="ab" style="display: flex; flex-direction: column; gap: 14px">
            <div
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-size: 13px;
              "
            >
              <span>立即上架</span>
              <span
                :class="['switch', form.status === 1 ? 'on' : '']"
                @click="form.status = form.status === 1 ? 2 : 1"
              />
            </div>
            <div
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-size: 13px;
              "
            >
              <span>加入秒杀</span>
              <span class="switch" />
            </div>
            <div
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-size: 13px;
              "
            >
              <span>推荐到首页</span>
              <span class="switch on" />
            </div>
            <div
              style="
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-size: 13px;
              "
            >
              <span>支持七天无理由</span>
              <span class="switch on" />
            </div>
            <hr class="divider" />
            <div class="field">
              <label>运费模板</label>
              <el-select
                v-model="form.shippingTemplate"
                placeholder="选择运费模板"
                style="width: 100%"
              >
                <el-option label="全国包邮" value="全国包邮" />
                <el-option label="满99包邮" value="满99包邮" />
              </el-select>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getItemById, saveItem, updateItem } from '@/api/item';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const isEdit = ref(!!route.params.id);

const form = reactive({
  name: '',
  price: 100,
  stock: 10,
  category: '',
  brand: '',
  spec: '',
  image: '',
  subTitle: '',
  marketPrice: 150,
  stockAlert: 20,
  detail: '',
  status: 1,
  shippingTemplate: '全国包邮',
});

const imageList = ref([]);
const lastUpdate = ref('-');

const skuSuffix = computed(() => {
  const id = route.params.id || 0;
  return (100000 + Number(id)).toString();
});

onMounted(async () => {
  if (isEdit.value) {
    try {
      const item = await getItemById(route.params.id);
      if (item && item.id) {
        Object.assign(form, item);
        // 价格以「分」存储，编辑表单按「元」展示（与原型 299.00 / 499.00 一致）
        if (typeof item.price === 'number') form.price = (item.price / 100).toFixed(2);
        const mp = item.marketPrice ?? item.originalPrice;
        if (typeof mp === 'number') form.marketPrice = (mp / 100).toFixed(2);
        if (item.images && item.images.length) {
          imageList.value = item.images.filter((img) => img);
        } else if (item.image) {
          imageList.value = [item.image];
        }
        // 商品无图时留空（不再填充无效图片路径）
        if (imageList.value.length === 0) {
          imageList.value = [];
        }
        lastUpdate.value = item.updateTime?.slice(0, 16) || '-';
      }
    } catch (err) {
      console.error(err);
    }
  }
});

const placeholderClasses = ['s4', 's7', 's1', 's2', 's3', 's5', 's6', 's8'];

function getPlaceholderClass(img, idx) {
  if (!img || img.includes('placeholder')) {
    return placeholderClasses[idx % placeholderClasses.length];
  }
  return '';
}

function getImageStyle(img) {
  if (img && !img.includes('placeholder')) {
    return `background:url(${img}) center/cover`;
  }
  return '';
}

function addImage() {
  if (imageList.value.length >= 8) {
    ElMessage.warning('最多上传 8 张图片');
    return;
  }
  imageList.value.push('/placeholder.png');
}

function removeImage(idx) {
  imageList.value.splice(idx, 1);
}

async function save() {
  try {
    // 表单以「元」展示/编辑，后端 ItemDTO.price 为整数「分」，提交前换算
    const priceYuan = parseFloat(form.price);
    const payload = {
      ...form,
      image: imageList.value[0] || '',
      price: Number.isFinite(priceYuan) ? Math.round(priceYuan * 100) : 0,
    };
    if (isEdit.value) await updateItem(route.params.id, payload);
    else await saveItem(payload);
    ElMessage.success('已保存');
    router.push('/items');
  } catch (err) {
    console.error(err);
  }
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
.acard .ah h3 .sub {
  font-size: 12px;
  color: var(--ink-3);
  font-weight: 400;
  margin-left: 8px;
}
.acard .ab {
  padding: 20px;
}
.edit-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 16px;
  align-items: start;
}
.stick {
  position: sticky;
  top: 76px;
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}
.form-grid .full {
  grid-column: 1 / -1;
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
.upload-grid .ph {
  width: 88px;
  height: 88px;
  border-radius: 10px;
  position: relative;
  background-size: cover;
  background-position: center;
}
/* Placeholder gradients matching prototype */
.upload-grid .ph.s1 {
  background: linear-gradient(135deg, #ec9b86, #e07c5e);
}
.upload-grid .ph.s2 {
  background: linear-gradient(135deg, #edc079, #e0a24d);
}
.upload-grid .ph.s3 {
  background: linear-gradient(135deg, #b0be92, #8fa06b);
}
.upload-grid .ph.s4 {
  background: linear-gradient(135deg, #92adbd, #6c8da1);
}
.upload-grid .ph.s5 {
  background: linear-gradient(135deg, #bfa6cc, #9d7fb0);
}
.upload-grid .ph.s6 {
  background: linear-gradient(135deg, #de9696, #c97070);
}
.upload-grid .ph.s7 {
  background: linear-gradient(135deg, #8cc0b0, #65a18f);
}
.upload-grid .ph.s8 {
  background: linear-gradient(135deg, #d2b176, #b88f4a);
}
.upload-grid .ph .x {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--danger);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 11px;
  cursor: pointer;
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

.divider {
  border: 0;
  border-top: 1px solid var(--admin-line);
  margin: 4px 0;
}

.dim {
  color: var(--ink-3);
}

/* Align Element Plus inputs with prototype native inputs */
:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  padding: 11px 13px !important;
  border-radius: 8px !important;
}
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--line-2) inset !important;
}
:deep(.el-input__inner) {
  font-size: 14px !important;
}
:deep(.el-button) {
  border-radius: 8px !important;
  font-weight: 700 !important;
}
:deep(.el-button--primary) {
  background: var(--brand) !important;
  border-color: var(--brand) !important;
  box-shadow: 0 4px 12px rgba(255, 77, 46, 0.28) !important;
}
:deep(.el-button--small) {
  padding: 7px 13px !important;
  font-size: 12.5px !important;
}
</style>
