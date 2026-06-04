<template>
  <router-link :to="`/item/${item.id}`" class="pcard">
    <div class="ph" :class="shadeClass" :data-label="label">
      <span class="glyph">{{ glyph }}</span>
    </div>
    <div class="body">
      <div class="tags">
        <span v-if="item.tag" class="tag" :class="tagClass">{{ item.tag }}</span>
        <span v-if="saving > 0" class="tag tag-line">省¥{{ saving }}</span>
      </div>
      <div class="title">{{ item.name }}</div>
      <div class="row">
        <span class="price" style="font-size: 19px"
          ><span class="cur">¥</span>{{ yuan(item.price) }}</span
        >
        <span v-if="oldYuan" class="price-old">¥{{ oldYuan }}</span>
      </div>
      <div class="meta">
        <span>{{ item.sold ? `已售 ${formatSold(item.sold)}` : '新品上市' }}</span>
        <span v-if="shopLabel" class="dim">{{ shopLabel }}</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  item: { type: Object, default: () => ({}) },
});

const tagClassMap = {
  热卖: 'tag-price',
  秒杀: 'tag-price',
  新品: 'tag-new',
  促销: 'tag-gold',
  回购: 'tag-success',
};

const glyphMap = {
  数码: '▣',
  家电: '▤',
  服饰: '◈',
  美妆: '✿',
  食品: '◉',
  家居: '▦',
  母婴: '❀',
  运动: '◐',
};

const tagClass = computed(() => tagClassMap[props.item.tag] || 'tag-ghost');

const shadeClass = computed(
  () => props.item.shade || props.item.styleClass || `s${(props.item.id % 8) + 1}`
);

const label = computed(() => props.item.category || props.item.categoryName || '');

const glyph = computed(() => {
  if (props.item.glyph) return props.item.glyph;
  const cat = props.item.group || props.item.category || '';
  for (const key in glyphMap) {
    if (cat.includes(key)) return glyphMap[key];
  }
  return '▦';
});

const saving = computed(() => {
  const o = props.item.originalPrice;
  const p = props.item.price;
  return o && o > p ? Math.round((o - p) / 100) : 0;
});

const oldYuan = computed(() =>
  props.item.originalPrice ? Math.round(props.item.originalPrice / 100) : 0
);

const shopLabel = computed(() => (props.item.shop ? props.item.shop.slice(0, 6) : ''));

function yuan(price) {
  return Math.round((price || 0) / 100);
}

function formatSold(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return n;
}
</script>
