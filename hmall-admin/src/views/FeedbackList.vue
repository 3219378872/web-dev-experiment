<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>用户反馈管理</h1>
        <p>共 {{ total }} 条反馈</p>
      </div>
    </div>

    <div class="fb-layout">
      <div class="acard">
        <div class="ah">
          <h3>反馈列表</h3>
          <div class="tabs" style="border: 0">
            <button
              :class="{ active: tab === 'all' }"
              style="padding: 6px 12px; font-size: 12.5px"
              @click="tab = 'all'"
            >
              全部
            </button>
            <button
              :class="{ active: tab === 'pending' }"
              style="padding: 6px 12px; font-size: 12.5px"
              @click="tab = 'pending'"
            >
              待处理
            </button>
            <button
              :class="{ active: tab === 'replied' }"
              style="padding: 6px 12px; font-size: 12.5px"
              @click="tab = 'replied'"
            >
              处理中
            </button>
            <button
              :class="{ active: tab === 'closed' }"
              style="padding: 6px 12px; font-size: 12.5px"
              @click="tab = 'closed'"
            >
              已处理
            </button>
          </div>
        </div>
        <div id="fb-list">
          <div
            v-for="(f, i) in filteredFeedbacks"
            :key="f.id"
            class="fb-item"
            :class="{ on: selected?.id === f.id }"
            @click="select(f)"
          >
            <div class="top">
              <span class="u-av" :style="`background:${avatarColor(i)}`">{{
                (f.userName || '匿')[0]
              }}</span>
              <b>{{ f.userName || '匿名用户' }}</b>
              <span class="tag tag-ghost" style="font-size: 11px">{{ f.type || '反馈' }}</span>
              <span class="sdot" :class="statusDot(f.status)" style="margin-left: auto">{{
                statusText(f.status)
              }}</span>
            </div>
            <div class="q">{{ f.content }}</div>
            <div class="bt">
              <span>{{ f.createTime?.slice(0, 16) || '—' }}</span>
            </div>
          </div>
        </div>
        <div class="adm-pager">
          <span>共 {{ total }} 条</span>
          <div class="pgs">
            <a :class="{ on: page === 1 }" @click="page = 1">1</a>
            <a v-if="total > pageSize">2</a>
            <a v-if="total > pageSize * 2">3</a>
            <a v-if="total > pageSize">›</a>
          </div>
        </div>
      </div>

      <aside class="acard">
        <div class="ah">
          <h3>反馈详情</h3>
          <span v-if="selected" class="tag tag-warn">{{ statusText(selected.status) }}</span>
        </div>
        <div v-if="selected" class="ab">
          <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 14px">
            <span class="u-av" :style="`background:${avatarColor(0)}`">{{
              (selected.userName || '匿')[0]
            }}</span>
            <div>
              <div style="font-weight: 700; font-size: 14px">
                {{ selected.userName || '匿名用户' }}
              </div>
              <div class="dim" style="font-size: 12px">
                {{ selected.createTime?.slice(0, 16) || '—' }}
              </div>
            </div>
          </div>
          <div style="display: flex; gap: 8px; margin-bottom: 12px">
            <span class="tag tag-new">{{ selected.type || '反馈' }}</span>
            <span class="dim" style="font-size: 12px; align-self: center">{{
              selected.createTime?.slice(0, 16) || '—'
            }}</span>
          </div>
          <div class="detail-msg">{{ selected.content }}</div>
          <div style="display: flex; gap: 8px; margin-top: 12px">
            <div
              class="ph s4"
              style="width: 64px; height: 64px; border-radius: 8px"
              data-label=""
            ></div>
            <div
              class="ph s7"
              style="width: 64px; height: 64px; border-radius: 8px"
              data-label=""
            ></div>
          </div>

          <div class="reply-box">
            <div class="field">
              <label>处理状态</label>
              <div style="display: flex; gap: 8px">
                <span
                  v-for="s in statusOptions"
                  :key="s.value"
                  class="chip"
                  :class="{ on: selected.status === s.value }"
                  @click="selected.status = s.value"
                  >{{ s.label }}</span
                >
              </div>
            </div>
            <div class="field" style="margin-top: 14px">
              <label>回复用户</label>
              <textarea
                v-model="replyContent"
                class="input"
                rows="4"
                placeholder="输入回复内容，将通过站内信通知用户…"
              ></textarea>
            </div>
            <div style="display: flex; gap: 10px; margin-top: 14px">
              <button class="btn btn-primary btn-block" @click="doReply">提交回复</button>
              <button class="btn btn-ghost">转技术</button>
            </div>
          </div>
        </div>
        <div v-else class="ab" style="color: var(--ink-3); text-align: center; padding: 40px 20px">
          请选择一条反馈查看详情
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { getFeedbacks, replyFeedback } from '@/api/system';
import { ElMessage } from 'element-plus';

const feedbacks = ref([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tab = ref('all');
const selected = ref(null);
const replyContent = ref('');

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

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '处理中', value: 1 },
  { label: '已解决', value: 2 },
  { label: '已关闭', value: 3 },
];

function statusText(s) {
  return ['待处理', '处理中', '已解决', '已关闭'][s] || '-';
}
function statusDot(s) {
  return ['warn', 'blue', 'green', 'gray'][s] || 'gray';
}

const filteredFeedbacks = computed(() => {
  if (tab.value === 'all') return feedbacks.value;
  if (tab.value === 'pending') return feedbacks.value.filter((f) => f.status === 0);
  if (tab.value === 'replied') return feedbacks.value.filter((f) => f.status === 2);
  if (tab.value === 'closed') return feedbacks.value.filter((f) => f.status === 3);
  return feedbacks.value;
});

function select(row) {
  selected.value = row;
  replyContent.value = row.reply || '';
}

async function fetch() {
  try {
    const r = await getFeedbacks();
    feedbacks.value = r.list || [];
    total.value = r.total || feedbacks.value.length;
    if (feedbacks.value.length && !selected.value) {
      selected.value = feedbacks.value[0];
      replyContent.value = selected.value.reply || '';
    }
  } catch (err) {
    console.error(err);
  }
}

async function doReply() {
  if (!selected.value) return;
  try {
    await replyFeedback(selected.value.id, {
      reply: replyContent.value,
      status: selected.value.status,
    });
    ElMessage.success('已回复');
    fetch();
  } catch (err) {
    console.error(err);
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

.fb-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
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

.tabs {
  display: flex;
  gap: 2px;
  border-bottom: 1px solid var(--admin-line);
}
.tabs button {
  padding: 13px 20px;
  font-size: 14px;
  font-weight: 500;
  color: var(--ink-2);
  border: 0;
  background: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  cursor: pointer;
}
.tabs button.active {
  color: var(--brand);
  font-weight: 700;
  border-bottom-color: var(--brand);
}
.tabs button:hover {
  color: var(--brand);
}

.fb-item {
  padding: 16px 18px;
  border-bottom: 1px solid var(--admin-line);
  cursor: pointer;
}
.fb-item:hover {
  background: #fbfbfc;
}
.fb-item.on {
  background: var(--brand-softer);
  border-left: 3px solid var(--brand);
}
.fb-item .top {
  display: flex;
  align-items: center;
  gap: 10px;
}
.fb-item .top .u-av {
  width: 30px;
  height: 30px;
  font-size: 12px;
}
.fb-item .top b {
  font-size: 13px;
}
.fb-item .q {
  font-size: 13px;
  margin-top: 8px;
  color: var(--ink-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.fb-item .bt {
  font-size: 11.5px;
  color: var(--ink-3);
  margin-top: 8px;
  display: flex;
  gap: 10px;
}

.detail-msg {
  background: var(--bg);
  border-radius: 10px;
  padding: 14px;
  font-size: 13.5px;
  line-height: 1.7;
}
.reply-box {
  margin-top: 16px;
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

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 11.5px;
  font-weight: 700;
  line-height: 1.6;
}
.tag-new {
  background: var(--info-soft);
  color: #1257c4;
}
.tag-warn {
  background: var(--warn-soft);
  color: #9a5e00;
}
.tag-ghost {
  background: var(--bg-2);
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
.sdot.warn {
  color: var(--warn);
}
.sdot.blue {
  color: var(--info);
}
.sdot.gray {
  color: var(--ink-3);
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

.dim {
  color: var(--ink-3);
}
</style>
