<template>
  <router-link :to="`/item/${item.id}`" class="pcard">
    <div class="ph" :class="`s${(item.id % 8) + 1}`">
      <div class="shape round"></div>
    </div>
    <div class="body">
      <div class="title">
        <span v-if="item.tag" class="tag" :class="tagClass">{{ item.tag }}</span>
        {{ item.name }}
      </div>
      <div class="row">
        <span class="price"><small class="cur">¥</small>{{ (item.price / 100).toFixed(0) }}</span>
        <span v-if="item.originalPrice" class="price-old"
          >¥{{ (item.originalPrice / 100).toFixed(0) }}</span
        >
      </div>
      <div class="meta">
        <span>{{ item.sold ? `已售 ${formatSold(item.sold)}` : '新品上市' }}</span>
        <span v-if="item.rating" class="stars">{{ renderStars(item.rating) }}</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
const props = defineProps({
  item: { type: Object, default: () => ({}) },
});

const tagClassMap = {
  热卖: 'tag-price',
  促销: 'tag-price',
  秒杀: 'tag-price',
  新品: 'tag-new',
  回购: 'tag-brand',
};

const tagClass = tagClassMap[props.item.tag] || 'tag-brand';

function formatSold(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万';
  return n;
}

function renderStars(rating) {
  const full = Math.floor(rating || 0);
  let s = '';
  for (let i = 0; i < full; i++) s += '★';
  for (let i = full; i < 5; i++) s += '<span class="off">★</span>';
  return s;
}
</script>
