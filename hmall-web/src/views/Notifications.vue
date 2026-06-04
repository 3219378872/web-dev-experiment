<template>
  <div class="wrap">
    <div class="crumb">
      <router-link to="/">首页</router-link><span class="s">/</span
      ><b style="color: var(--ink)">系统公告</b>
    </div>

    <div class="ann-layout">
      <aside class="ann-cats">
        <a
          v-for="c in categories"
          :key="c.key"
          :class="{ on: activeCat === c.key }"
          @click="activeCat = c.key"
        >
          {{ c.label }} <span class="n">{{ c.count }}</span>
        </a>
      </aside>

      <main class="panel">
        <div class="panel-head">
          <h3>📢 系统公告</h3>
          <span class="dim" style="font-size: 13px">及时了解平台动态</span>
        </div>
        <div>
          <div v-for="n in filteredNotifications" :key="n.id" class="ann">
            <div class="date">
              <div class="d">{{ dayOf(n.publishTime) }}</div>
              <div class="m">{{ monthOf(n.publishTime) }}</div>
            </div>
            <div class="ct">
              <div class="tt">
                {{ n.title }}
                <span v-if="n.tag" class="tag" :class="n.tagClass">{{ n.tag }}</span>
              </div>
              <div class="ds">{{ n.content }}</div>
              <div class="meta">
                <span
                  >{{ n.category || '系统通知'
                  }}<template v-if="n.readCount"> · {{ n.readCount }}阅读</template></span
                >
                <span style="cursor: pointer">查看详情 ›</span>
              </div>
            </div>
          </div>
          <div
            v-if="!filteredNotifications.length"
            class="dim"
            style="text-align: center; padding: 40px 0; font-size: 13px"
          >
            暂无公告
          </div>
        </div>
        <div v-if="filteredNotifications.length > pageSize" class="pager">
          <span>‹</span>
          <a class="cur">1</a>
          <a>›</a>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getNotifications } from '@/api/common';

const notifications = ref([]);
const activeCat = ref('all');
const pageSize = ref(10);

const categories = ref([
  { key: 'all', label: '全部公告', count: 0 },
  { key: 'promo', label: '活动促销', count: 0 },
  { key: 'system', label: '系统通知', count: 0 },
  { key: 'rule', label: '规则更新', count: 0 },
  { key: 'security', label: '安全公告', count: 0 },
  { key: 'maintain', label: '维护通知', count: 0 },
]);

const filteredNotifications = computed(() => {
  if (activeCat.value === 'all') return notifications.value;
  return notifications.value.filter((n) => n.categoryKey === activeCat.value);
});

function dayOf(time) {
  if (!time) return '--';
  const d = new Date(time);
  return String(d.getDate()).padStart(2, '0');
}

function monthOf(time) {
  if (!time) return '--';
  const d = new Date(time);
  return String(d.getMonth() + 1).padStart(2, '0') + '月';
}

/**
 * 将后端 Notification（id/title/content/publishTime）映射为展示结构。
 * 后端未提供分类/标签/阅读量，统一用中性默认，不杜撰运营数据；列表为空即空态。
 */
function enrich(list) {
  return (list || []).map((n) => ({
    id: n.id,
    title: n.title,
    content: n.content,
    publishTime: n.publishTime || n.createTime || '',
    tag: '',
    tagClass: '',
    category: '系统通知',
    categoryKey: 'system',
    readCount: '',
  }));
}

async function load() {
  try {
    const data = await getNotifications();
    notifications.value = enrich(data);
    // 更新分类计数
    categories.value.forEach((c) => {
      if (c.key === 'all') {
        c.count = notifications.value.length;
      } else {
        c.count = notifications.value.filter((n) => n.categoryKey === c.key).length;
      }
    });
  } catch (err) {
    console.error(err);
    notifications.value = enrich([]);
  }
}

onMounted(load);
</script>

<style scoped>
.ann-layout {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 18px;
  margin: 14px 0 30px;
  align-items: start;
}
.ann-cats {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  position: sticky;
  top: 88px;
}
.ann-cats a {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  font-size: 13.5px;
  color: var(--ink-2);
  border-bottom: 1px solid var(--bg);
  cursor: pointer;
}
.ann-cats a:last-child {
  border-bottom: 0;
}
.ann-cats a.on {
  background: var(--brand-soft);
  color: var(--brand-700);
  font-weight: 700;
  border-right: 3px solid var(--brand);
}
.ann-cats a:hover {
  background: var(--brand-softer);
  color: var(--brand);
}
.ann-cats a .n {
  font-size: 11px;
  background: var(--bg-2);
  color: var(--ink-3);
  padding: 1px 7px;
  border-radius: 999px;
}
.ann {
  display: flex;
  gap: 16px;
  padding: 18px 20px;
  border-bottom: 1px solid var(--line);
}
.ann:last-child {
  border-bottom: 0;
}
.ann .date {
  flex-shrink: 0;
  text-align: center;
  width: 60px;
}
.ann .date .d {
  font-size: 24px;
  font-weight: 900;
  color: var(--brand);
  line-height: 1;
}
.ann .date .m {
  font-size: 11px;
  color: var(--ink-3);
  margin-top: 4px;
}
.ann .ct {
  flex: 1;
}
.ann .ct .tt {
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}
.ann .ct .ds {
  font-size: 13px;
  color: var(--ink-2);
  margin-top: 7px;
  line-height: 1.6;
}
.ann .ct .meta {
  font-size: 11.5px;
  color: var(--ink-3);
  margin-top: 9px;
  display: flex;
  gap: 14px;
}
.ann:hover .tt {
  color: var(--brand);
}
@media (max-width: 768px) {
  .ann-layout {
    grid-template-columns: 1fr;
  }
  .ann-cats {
    position: static;
    display: flex;
    flex-wrap: wrap;
  }
  .ann-cats a {
    border-bottom: 0;
    border-right: 1px solid var(--bg);
  }
}
</style>
