<template>
  <div class="wrap">
    <div class="crumb">
      <router-link to="/">首页</router-link><span class="s">/</span
      ><b style="color: var(--ink)">意见反馈</b>
    </div>

    <div class="acct-layout">
      <AccountSidebar active="feedback" />

      <main>
        <div class="fb-grid">
          <div class="panel">
            <div class="panel-head">
              <h3>✍ 意见反馈</h3>
              <span class="dim" style="font-size: 13px">您的建议是我们前进的动力</span>
            </div>
            <div class="panel-pad">
              <div class="field" style="margin-bottom: 18px">
                <label>反馈类型</label>
                <div class="fb-type">
                  <span
                    v-for="t in types"
                    :key="t.value"
                    class="t"
                    :class="{ on: type === t.value }"
                    @click="type = t.value"
                  >
                    <span class="ic">{{ t.icon }}</span
                    >{{ t.label }}
                  </span>
                </div>
              </div>
              <div class="field" style="margin-bottom: 18px">
                <label>问题描述</label>
                <textarea
                  v-model="content"
                  class="input"
                  rows="5"
                  placeholder="请详细描述您遇到的问题或建议，我们会认真处理（5-500字）"
                ></textarea>
                <div class="dim" style="font-size: 11.5px; text-align: right; margin-top: 4px">
                  {{ content.length }} / 500
                </div>
              </div>
              <div class="field" style="margin-bottom: 18px">
                <label>上传截图（选填，最多 4 张）</label>
                <div class="up-zone" @click="triggerUpload">
                  <div class="ic">📷</div>
                  <div style="margin-top: 8px">
                    点击或拖拽上传图片<br /><span style="font-size: 11.5px"
                      >支持 JPG / PNG，单张不超过 5MB</span
                    >
                  </div>
                </div>
                <input
                  ref="fileInput"
                  type="file"
                  accept="image/*"
                  multiple
                  style="display: none"
                  @change="onFileChange"
                />
                <div v-if="imageUrls.length" class="uploaded-imgs">
                  <div v-for="(url, idx) in imageUrls" :key="idx" class="uploaded-img">
                    <img :src="url" alt="截图" />
                    <span class="remove" @click="removeImage(idx)">×</span>
                  </div>
                </div>
              </div>
              <div class="field" style="margin-bottom: 18px">
                <label>联系方式（选填）</label>
                <input
                  v-model="contact"
                  class="input"
                  placeholder="留下手机号或邮箱，方便我们与您联系"
                />
              </div>
              <button class="btn btn-hot btn-lg" @click="submit">提交反馈</button>
            </div>
          </div>

          <aside>
            <div class="panel">
              <div class="panel-head"><h3 style="font-size: 15px">我的反馈记录</h3></div>
              <div class="panel-pad">
                <div v-for="(h, idx) in history" :key="idx" class="hist">
                  <span class="st"
                    ><span class="tag" :class="h.tagClass">{{ h.status }}</span></span
                  >
                  <div class="ct">
                    <div class="q">{{ h.question }}</div>
                    <div v-if="h.reply" class="r">{{ h.reply }}</div>
                    <div class="time">{{ h.time }}</div>
                  </div>
                </div>
                <div
                  v-if="!history.length"
                  class="dim"
                  style="text-align: center; padding: 20px 0; font-size: 13px"
                >
                  暂无反馈记录
                </div>
              </div>
            </div>
          </aside>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { submitFeedback, getMyFeedbacks, uploadImage } from '@/api/common';
import { ElMessage } from 'element-plus';
import AccountSidebar from '@/components/AccountSidebar.vue';

const types = [
  { value: 'bug', icon: '🐞', label: '功能异常' },
  { value: 'suggest', icon: '💡', label: '体验建议' },
  { value: 'product', icon: '📦', label: '商品问题' },
  { value: 'logistics', icon: '🚚', label: '物流配送' },
  { value: 'other', icon: '💬', label: '其他' },
];

const type = ref('bug');
const content = ref('');
const contact = ref('');
const imageUrls = ref([]);
const uploading = ref(false);
const fileInput = ref(null);

const history = ref([]);

function mapFeedback(f) {
  const replied = f.status === 1;
  return {
    status: replied ? '已回复' : '待处理',
    tagClass: replied ? 'tag-success' : 'tag-warn',
    question: f.content,
    reply: f.reply || '',
    time: `${(f.createTime || '').slice(0, 10)} 提交${replied ? ' · 客服已回复' : ''}`,
  };
}

async function loadHistory() {
  try {
    const data = await getMyFeedbacks({ page: 1, size: 20 });
    history.value = (data?.list || []).map(mapFeedback);
  } catch (err) {
    history.value = [];
  }
}

onMounted(loadHistory);

function triggerUpload() {
  fileInput.value?.click();
}

async function onFileChange(e) {
  const files = e.target.files;
  if (!files?.length) return;
  if (imageUrls.value.length + files.length > 4) {
    ElMessage.warning('最多上传 4 张图片');
    return;
  }
  uploading.value = true;
  try {
    for (const file of files) {
      if (file.size > 5 * 1024 * 1024) {
        ElMessage.warning(`图片 ${file.name} 超过 5MB，已跳过`);
        continue;
      }
      const url = await uploadImage(file);
      imageUrls.value.push(url);
    }
  } catch (err) {
    ElMessage.error('图片上传失败，请重试');
  } finally {
    uploading.value = false;
    // 重置 input 以便重复选择同一文件
    if (fileInput.value) fileInput.value.value = '';
  }
}

function removeImage(idx) {
  imageUrls.value.splice(idx, 1);
}

async function submit() {
  if (!content.value.trim()) {
    ElMessage.warning('请填写问题描述');
    return;
  }
  try {
    await submitFeedback({
      content: content.value,
      type: type.value,
      contact: contact.value,
    });
    content.value = '';
    contact.value = '';
    imageUrls.value = [];
    ElMessage.success('反馈已提交');
    loadHistory();
  } catch (err) {
    console.error(err);
  }
}
</script>

<style scoped>
.fb-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 18px;
  align-items: start;
}
.fb-type {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.fb-type .t {
  border: 1.5px solid var(--line-2);
  border-radius: 10px;
  padding: 14px 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13.5px;
  background: #fff;
}
.fb-type .t.on {
  border-color: var(--brand);
  background: var(--brand-softer);
  color: var(--brand-700);
  font-weight: 700;
}
.fb-type .t .ic {
  font-size: 18px;
}
.up-zone {
  border: 2px dashed var(--line-2);
  border-radius: 10px;
  padding: 24px;
  text-align: center;
  color: var(--ink-3);
  cursor: pointer;
}
.up-zone:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.up-zone .ic {
  font-size: 32px;
}
.uploaded-imgs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 10px;
}
.uploaded-img {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}
.uploaded-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.uploaded-img .remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 14px;
  cursor: pointer;
  line-height: 1;
}
.uploaded-img .remove:hover {
  background: rgba(0, 0, 0, 0.8);
}
.hist {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--bg);
}
.hist:last-child {
  border-bottom: 0;
}
.hist .st {
  flex-shrink: 0;
}
.hist .ct {
  flex: 1;
}
.hist .ct .q {
  font-size: 13.5px;
  color: var(--ink);
}
.hist .ct .r {
  font-size: 12.5px;
  color: var(--ink-2);
  background: var(--bg);
  border-radius: 8px;
  padding: 9px 12px;
  margin-top: 8px;
  border-left: 3px solid var(--brand);
}
.hist .ct .time {
  font-size: 11.5px;
  color: var(--ink-3);
  margin-top: 6px;
}
@media (max-width: 1024px) {
  .fb-grid {
    grid-template-columns: 1fr;
  }
}
</style>
