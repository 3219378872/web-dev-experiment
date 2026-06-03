<template>
  <div>
    <div class="adm-ph">
      <div>
        <h1>评价管理</h1>
        <p>共 {{ total }} 条评价</p>
      </div>
    </div>

    <div class="stat-row">
      <div class="stat" style="padding: 16px">
        <div class="lab">总评价数</div>
        <div class="val" style="font-size: 24px">{{ total }}</div>
      </div>
      <div class="stat" style="padding: 16px">
        <div class="lab">平均评分</div>
        <div class="val" style="font-size: 24px; color: var(--gold)">— ★</div>
      </div>
      <div class="stat" style="padding: 16px">
        <div class="lab">好评率</div>
        <div class="val" style="font-size: 24px; color: var(--success)">—</div>
      </div>
      <div class="stat" style="padding: 16px">
        <div class="lab">待处理</div>
        <div class="val" style="font-size: 24px; color: var(--danger)">—</div>
      </div>
    </div>

    <div class="acard" style="margin-bottom: 16px">
      <div class="tabs" style="padding: 0 16px">
        <button :class="{ active: tab === 'all' }" @click="tab = 'all'">全部</button>
        <button :class="{ active: tab === 'good' }" @click="tab = 'good'">好评</button>
        <button :class="{ active: tab === 'mid' }" @click="tab = 'mid'">中评</button>
        <button :class="{ active: tab === 'bad' }" @click="tab = 'bad'">差评</button>
      </div>
    </div>

    <div class="acard">
      <div class="ab" id="rev-rows">
        <div v-for="(r, i) in filteredReviews" :key="r.id" class="rev-row">
          <div class="av" :style="`background:${avatarColor(i)}`">
            {{ (r.userName || '匿')[0] }}
          </div>
          <div class="main">
            <div class="who">
              <b>{{ r.userName || '匿名用户' }}</b>
              <span class="stars"
                >{{ starStr(r.rating) }}<span class="off">{{ offStars(r.rating) }}</span></span
              >
              <span class="dim" style="margin-left: 6px; font-size: 12px">{{
                r.createTime?.slice(0, 16) || '—'
              }}</span>
            </div>
            <div class="txt">{{ r.content }}</div>
            <div v-if="r.images?.length" class="imgs">
              <div
                v-for="(img, idx) in r.images.slice(0, 4)"
                :key="idx"
                class="ph"
                :style="`background:${avatarColor(idx)}`"
              />
            </div>
            <div class="prod" style="margin-top: 8px">
              <div
                class="ph"
                style="width: 28px; height: 28px; border-radius: 5px; background: var(--line-2)"
              />
              <span>商品ID: {{ r.itemId || '—' }}</span>
            </div>
          </div>
          <div class="side">
            <span class="sdot green">已通过</span>
            <el-button class="btn-ghost" size="small">回复</el-button>
            <el-button
              size="small"
              style="background: #fff; border: 1px solid var(--line-2); color: var(--danger)"
              @click="del(r.id)"
              >删除</el-button
            >
          </div>
        </div>
      </div>
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { getReviews, deleteReview } from '@/api/item';
import { ElMessage } from 'element-plus';

const reviews = ref([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tab = ref('all');

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

const filteredReviews = computed(() => {
  if (tab.value === 'all') return reviews.value;
  if (tab.value === 'good') return reviews.value.filter((r) => r.rating >= 4);
  if (tab.value === 'mid') return reviews.value.filter((r) => r.rating === 3);
  if (tab.value === 'bad') return reviews.value.filter((r) => r.rating <= 2);
  return reviews.value;
});

function starStr(n) {
  return '★★★★★'.slice(0, Math.max(0, Math.min(5, n || 0)));
}
function offStars(n) {
  return '★★★★★'.slice(Math.max(0, Math.min(5, n || 0)));
}

async function fetch() {
  try {
    const r = await getReviews();
    reviews.value = r.list || r || [];
    total.value = reviews.value.length;
  } catch (err) {
    console.error(err);
  }
}

async function del(id) {
  try {
    await deleteReview(id);
    fetch();
    ElMessage.success('已删除');
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

.acard {
  background: #fff;
  border: 1px solid var(--admin-line);
  border-radius: 14px;
}
.acard .ab {
  padding: 0 20px;
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

.rev-row {
  display: flex;
  gap: 14px;
  padding: 18px 0;
  border-bottom: 1px solid var(--admin-line);
}
.rev-row:last-child {
  border-bottom: 0;
}
.rev-row .av {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
}
.rev-row .main {
  flex: 1;
}
.rev-row .who {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}
.rev-row .who b {
  font-weight: 700;
}
.rev-row .txt {
  font-size: 13.5px;
  margin: 8px 0;
  line-height: 1.6;
}
.rev-row .prod {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--ink-3);
  background: var(--bg);
  padding: 6px 10px;
  border-radius: 8px;
  width: fit-content;
}
.rev-row .prod .ph {
  width: 28px;
  height: 28px;
  border-radius: 5px;
}
.rev-row .imgs {
  display: flex;
  gap: 6px;
  margin-top: 8px;
}
.rev-row .imgs .ph {
  width: 54px;
  height: 54px;
  border-radius: 7px;
}
.rev-row .side {
  width: 130px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

.stars {
  display: inline-flex;
  gap: 1px;
  color: var(--gold);
  font-size: 13px;
  letter-spacing: 1px;
}
.stars .off {
  color: var(--line-2);
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
