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
        <div class="val" style="font-size: 24px; color: var(--gold)">
          {{ avgRating > 0 ? avgRating.toFixed(1) : '—' }} ★
        </div>
      </div>
      <div class="stat" style="padding: 16px">
        <div class="lab">好评率</div>
        <div class="val" style="font-size: 24px; color: var(--success)">
          {{ goodRate > 0 ? (goodRate * 100).toFixed(0) + '%' : '—' }}
        </div>
      </div>
      <div class="stat" style="padding: 16px">
        <div class="lab">待处理</div>
        <div class="val" style="font-size: 24px; color: var(--danger)">{{ pendingCount }}</div>
      </div>
    </div>

    <div class="acard" style="margin-bottom: 16px">
      <div class="tabs" style="padding: 0 16px">
        <button :class="{ active: tab === 'all' }" @click="switchTab('all')">全部</button>
        <button :class="{ active: tab === 'good' }" @click="switchTab('good')">好评</button>
        <button :class="{ active: tab === 'mid' }" @click="switchTab('mid')">中评</button>
        <button :class="{ active: tab === 'bad' }" @click="switchTab('bad')">差评</button>
      </div>
    </div>

    <div class="acard">
      <div v-if="loading" style="padding: 40px 20px; text-align: center; color: var(--ink-3)">
        加载中...
      </div>
      <div v-else-if="reviews.length === 0" style="padding: 40px 20px">
        <el-empty description="暂无评价数据" />
      </div>
      <div v-else id="rev-rows" class="ab">
        <div v-for="(r, i) in reviews" :key="r.id" class="rev-row">
          <div class="av" :style="`background:${avatarColor(i)}`">
            {{ (r.username || r.userName || '匿')[0] }}
          </div>
          <div class="main">
            <div class="who">
              <b>{{ r.username || r.userName || '匿名用户' }}</b>
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
              <span>{{ r.itemName ? r.itemName : '商品ID: ' + (r.itemId || '—') }}</span>
            </div>
          </div>
          <div class="side">
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
import { ref, computed, watch } from 'vue';
import { getReviews, deleteReview } from '@/api/item';
import { reviewQueryParams } from '@/utils/adminOpsActions';
import { ElMessage } from 'element-plus';

const reviews = ref([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tab = ref('all');
const loading = ref(false);

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

const avgRating = computed(() => {
  if (!reviews.value.length) return 0;
  return reviews.value.reduce((s, r) => s + (r.rating || 0), 0) / reviews.value.length;
});

const goodRate = computed(() => {
  if (!reviews.value.length) return 0;
  return reviews.value.filter((r) => r.rating >= 4).length / reviews.value.length;
});

// 待处理评价计数
// ReviewVO 仅有 id/username/content/images/rating/createTime，不含 status/reply
// 当后端增加 status/reply 字段后应改为：
//   reviews.value.filter(r => r.status === 'pending' || r.reply === null).length
// 目前无待处理判定依据，暂返回 0
const pendingCount = ref(0);
watch(
  reviews,
  (val) => {
    if (val.length && ('status' in val[0] || 'reply' in val[0])) {
      pendingCount.value = val.filter((r) => r.status === 'pending' || r.reply == null).length;
    } else {
      pendingCount.value = 0;
    }
  },
  { immediate: true }
);

function starStr(n) {
  return '★★★★★'.slice(0, Math.max(0, Math.min(5, n || 0)));
}
function offStars(n) {
  return '★★★★★'.slice(Math.max(0, Math.min(5, n || 0)));
}

function switchTab(nextTab) {
  tab.value = nextTab;
  page.value = 1;
  fetch();
}

async function fetch(p) {
  if (p) page.value = p;
  loading.value = true;
  try {
    const r = await getReviews(
      reviewQueryParams({ page: page.value, pageSize: pageSize.value, tab: tab.value })
    );
    reviews.value = r.list || [];
    total.value = r.total || 0;
  } catch (err) {
    console.error(err);
    ElMessage.error('评价加载失败');
  } finally {
    loading.value = false;
  }
}

async function del(id) {
  try {
    await deleteReview(id);
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
