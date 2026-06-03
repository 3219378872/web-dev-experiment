<template>
  <div class="wrap" style="padding: 20px 0">
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
                <span>{{ n.category || '系统通知' }} · {{ n.readCount || 0 }}阅读</span>
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

function enrich(list) {
  // 如果后端返回的数据缺少展示字段，用 mock 数据补充
  const mock = [
    {
      id: 1,
      title: '好集 618 年中狂欢节预热开启',
      tag: '置顶',
      tagClass: 'tag-price',
      content:
        '全场最高直降 60%，每满 300 减 50，叠加平台券更划算。6 月 1 日 0 点正式开抢，更有整点秒杀与抽奖福利等你来！',
      category: '活动促销',
      categoryKey: 'promo',
      readCount: '12.3万',
      publishTime: '2026-05-28T10:00:00',
    },
    {
      id: 2,
      title: '新增「当日达」配送服务上线',
      tag: '新',
      tagClass: 'tag-new',
      content: '即日起，上海、北京、杭州等 12 城核心区域支持当日达配送，下单后最快 4 小时送达。',
      category: '系统通知',
      categoryKey: 'system',
      readCount: '4.8万',
      publishTime: '2026-05-25T10:00:00',
    },
    {
      id: 3,
      title: '《好集会员积分规则》更新公告',
      tag: '',
      tagClass: '',
      content:
        '为提升会员权益，自 6 月 1 日起积分有效期延长至 24 个月，购物积分比例提升至 1:1，敬请关注。',
      category: '规则更新',
      categoryKey: 'rule',
      readCount: '2.1万',
      publishTime: '2026-05-20T10:00:00',
    },
    {
      id: 4,
      title: '关于谨防钓鱼诈骗的安全提示',
      tag: '重要',
      tagClass: 'tag-warn',
      content:
        '好集平台工作人员不会以任何理由要求您提供密码、验证码或进行转账。请通过官方渠道核实信息，谨防诈骗。',
      category: '安全公告',
      categoryKey: 'security',
      readCount: '6.5万',
      publishTime: '2026-05-18T10:00:00',
    },
    {
      id: 5,
      title: '系统例行维护通知',
      tag: '',
      tagClass: '',
      content:
        '为提供更稳定的服务，平台将于 5 月 16 日 02:00-04:00 进行系统升级维护，期间部分功能可能受影响，敬请谅解。',
      category: '维护通知',
      categoryKey: 'maintain',
      readCount: '1.2万',
      publishTime: '2026-05-15T10:00:00',
    },
    {
      id: 6,
      title: '七天无理由退换货升级为「极速退款」',
      tag: '新',
      tagClass: 'tag-new',
      content: '符合条件的退货申请，确认后退款即时到账，无需等待商品退回，购物更安心。',
      category: '规则更新',
      categoryKey: 'rule',
      readCount: '3.4万',
      publishTime: '2026-05-10T10:00:00',
    },
  ];
  if (!list || !list.length) return mock;
  return list.map((n, i) => ({
    ...mock[i % mock.length],
    ...n,
    publishTime: n.publishTime || mock[i % mock.length].publishTime,
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
